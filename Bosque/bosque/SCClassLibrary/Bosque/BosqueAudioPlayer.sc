/**
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.20, 08-Sep-07
 */
BosqueAudioPlayer : Object {
	var <doc;
	var <forest;
	var task, taskVol;
	var <scsynth, <nw, <sampleRate;
	var <diskGroup;
	
	classvar <bufferLatency 	= 0.2;
	classvar <transportDelta 	= 0.1;
	
	var playStartPos;
//	var playStartTime;
	
	var debug		= false;
	var debugFunc = false;
	var debugVol	= false;
	
	// func region support
	var bndl; // funcStake
	
	var volBus;

	*new { arg doc;
		^super.new.prInit( doc );
	}
	
	prInit { arg argDoc;
		doc		= argDoc;
		forest	= doc.forest;
		forest.doWhenScSynthBooted({ this.prAudioInit });
		
//		task		= Routine({ this.prTaskBody });
//		taskVol	= Routine({ this.prTaskVolBody });
	}
	
	prAudioInit {
		scsynth		= forest.scsynth;
		nw			= NodeWatcher.newFrom( scsynth );
		sampleRate	= scsynth.sampleRate;
		diskGroup		= forest.diskGroup;
		volBus		= Bus.control( scsynth );
		volBus.set( 1.0 );
		UpdateListener.newFor( doc.transport, { arg upd, transport, what ... params;
			switch( what,
			\play, { this.prPlay( *params )},
			\stop, { this.prStop( *params )},
			\pause, { this.prPause( *params )},
			\resume, { this.prResume( *params )}
			);
		});
	}
	
	// ---------- func region support ----------
	makeFuncSynth { arg stake, defName, args, target, addAction = \addToHead;
		var synth, event = stake.event;
		synth = Synth.basicNew( defName /* ? \forestDur */, scsynth );
//		if( dur.notNil, {
//			args = args ++ [ \dur, dur ];
//		});
		bndl.add( synth.newMsg( target ?? { stake.group }, args, addAction ));
//		if( dur.notNil, {
			nw.register( synth );
//			UpdateListener.newFor( synth, { arg upd, obj, what;
//				if( what === \n_end, {
//					event.synths = event.synths.remove( synth );
//					event.changed( \playing, false );
//					if( debugFunc, {
//						[ stake, synth, "n_end" ].postln;
//					});
//				});
//			});
//		});
		event.synths = event.synths.add( synth );
		if( debugFunc, {
			[ stake, synth, "makeSynth" ].postln;
		});
		^synth;
	}

//	makeFuncSynthWithBufRead { arg stake, defName, args, target, addAction = \addToTail;
//		var synth, event = stake.event;
//		synth = Synth.basicNew( defName /* ? \forestDur */, scsynth );
//		bndl.add( synth.newMsg( target ?? { stake.group }, args, addAction ));
//		nw.register( synth );
//		UpdateListener.newFor( synth, { arg upd, obj, what;
//			if( what === \n_end, {
//				event.synths = event.synths.remove( synth );
//				event.changed( \playing, false );
//				if( debugFunc, {
//					[ stake, synth, "n_end" ].postln;
//				});
//			});
//		});
//		event.synths = event.synths.add( synth );
//		if( debugFunc, {
//			[ stake, synth, "makeSynth" ].postln;
//		});
//		^synth;
//	}

	addFuncUpd { arg stake, upd;
		var event = stake.event;
		event.upd = event.upd.add( upd );
	}

	makeFuncDur { arg stake, dur, group;
		var synth, event = stake.event;
		synth = Synth.basicNew( \forestDur, scsynth );
		bndl.add( synth.newMsg( group, [ \dur, dur ]));
		nw.register( synth );
		event.upd = event.upd.add( UpdateListener.newFor( synth, { arg upd, obj, what;
			if( what === \n_end, {
				event.synths.remove( synth );
				this.freeFuncSynths( stake );
//				event.changed( \playing, false );
//				if( debugFunc, {
//					[ stake, synth, "n_end" ].postln;
//				});
			});
		}));
		event.synths = event.synths.add( synth );
		if( debugFunc, {
			[ stake, synth, "makeDur" ].postln;
		});
		^synth;
	}
	
	freeFuncSynths { arg stake, bndl;
		var selfBndl = bndl.isNil, event = stake.event;
		if( event.isNil, { ^this });
		if( selfBndl, { bndl = OSCBundle.new });
		event.upd.do({ arg upd; upd.remove });
		event.upd = nil;
		event.rout.do({ arg r; r.stop });
		event.rout = nil;
		event.synths.do({ arg synth; bndl.add( synth.freeMsg )});
		event.synths = nil;
		if( selfBndl, {
			bndl.send( scsynth /*, bufferLatency + transportDelta */);
		});
		event.busses.do({ arg bus; bus.free });
		event.busses = nil;
		event.changed( \playing, false );
		if( debugFunc, {
			[ stake, "freeSynths" ].postln;
		});
	}

	funcPreviousBus { arg stake;
		var track, cfg;
		track = doc.tracks[ (doc.tracks.indexOf( stake.modTrack ) ? 0) - 1 ];
		^if( track.notNil, { track.busConfig.bus });
	}
	
	funcTrackingSense { arg tv, sense = 0.5;
		forest.chris.sendMsg( '/tv', tv );
		forest.chris.sendMsg( '/sense', sense );
	}
	
	makeFuncTrack { arg stake, type = \dancerSpeed, spec = \spec;
		var event = stake.event, fUpd, oUpd, fSpec, fSet;
		fSpec = event[ spec ] ? { arg in; in };
		fSet = { arg value;
			value = fSpec.value( value );
			event.synths.do({ arg n; if( n.isPlaying, { n.set( spec, value )})});
		};
		switch( type,
		\dancerSpeed, {
			oUpd	= forest.trackDancer;
			fUpd = { arg upd, obj, x, y, speed;
//				[ x, y, speed ].postln;
				fSet.value( speed );
			};
		},
		\dancerX, {
			oUpd	= forest.trackDancer;
			fUpd = { arg upd, obj, x, y, speed;
				fSet.value( x );
			};
		},
		\dancerY, {
			oUpd	= forest.trackDancer;
			fUpd = { arg upd, obj, x, y, speed;
				fSet.value( y );
			};
		}
		);
		if( oUpd.notNil, {
			event.upd = event.upd.add( UpdateListener.newFor( oUpd, fUpd ));
		}, {
			TypeSafe.methodWarn( thisMethod, "Unknown type '" ++ type ++ "'" );
		});
	}
	
	makeFuncField { arg stake, matrix = \matrix;
		var event = stake.event, fMatrix;
		fMatrix = event[ matrix ];
		event.upd = event.upd.add( UpdateListener.newFor( forest.trackField, { arg upd, obj ... values;
			// due to Object -> change, we'll get values == [ nil ] in some cases!!!
			if( values[0].notNil, { fMatrix.value( values )}, fMatrix );
		}));
	}
	
	makeFuncControlBus { arg stake, numChannels = 1, name = \cbus;
		var bus, event = stake.event;
		bus = Bus.control( scsynth, numChannels );
		event[ name ] = bus;
		event[ \busses ] = event[ \busses ].add( bus );
		if( debugFunc, {
			[ stake, bus, "makeFuncControlBus" ].postln;
		});
		^bus;
	}

	makeFuncTrig { arg stake, type = \dancerSpeed, threshUp = \thresh, threshDown = \thresh, trig = \trig;
		var event = stake.event, fUpd, oUpd, fSpec, oldVals, above, fThreshUp, fThreshDown, fTrig;
		fThreshUp		= event[ threshUp ];
		fThreshDown	= event[ threshDown ];
		fTrig		= event[ trig ];
		switch( type,
		\dancerSpeed, {
			oUpd	= forest.trackDancer;
//			oldVals = 0.0;
			above = false;
			fUpd = { arg upd, obj, x, y, speed;
//				[ x, y, speed ].postln;
				if( above, {
					above = fThreshDown.value( speed );
				}, {
					above = fThreshUp.value( speed );
					if( above, {
						fTrig.value;
					});
				});
			};
		},
		\dancerX, {
			oUpd	= forest.trackDancer;
//			oldVals = 0.0;
			above = false;
			fUpd = { arg upd, obj, x, y, speed;
				if( above, {
					above = fThreshDown.value( x );
				}, {
					above = fThreshUp.value( x );
					if( above, {
						fTrig.value;
					});
				});
			};
		},
		\dancerY, {
			oUpd	= forest.trackDancer;
//			oldVals = 0.0;
			above = false;
			fUpd = { arg upd, obj, x, y, speed;
				if( above, {
					above = fThreshDown.value( y );
				}, {
					above = fThreshUp.value( y );
					if( above, {
						fTrig.value;
					});
				});
			};
		},
		\fieldSpeed, {
			oldVals = false ! Bosque.numFieldsV ! Bosque.numFieldsH;
			above = false; // ! Bosque.numFieldsV ! Bosque.numFieldsH;
			oUpd	= forest.trackField;
			fUpd = { arg upd, obj ... fields; var x, y, speed;
//				fields.do({ arg field;
//					#x, y, speed = field;
//					if( above[ x ][ y ], {
//						above[ x ][ y ] = fThreshDown.value( x, y, speed );
//					}, {
//						above[ x ][ y ] = fThreshUp.value( x, y, speed );
//						if( above[ x ][ y ], {
//							fTrig.value;
//						});
//					});
//				});
				if( above, {
					above = false;
					fields.do({ arg field;
						#x, y, speed = field;
						oldVals[ x ][ y ] = fThreshDown.value( x, y, speed );
						if( oldVals[ x ][ y ], { above = true });
					});
				}, {
					fields.do({ arg field;
						#x, y, speed = field;
						oldVals[ x ][ y ] = fThreshUp.value( x, y, speed );
						if( oldVals[ x ][ y ], { above = true });
					});
					if( above, {
						fTrig.value;
					});
				});
			};
		},
		\trackSpeed, {
			above = false ! Bosque.numTracks;
			oUpd	= forest.trackTrack;
			fUpd = { arg upd, obj ... tracks; var x, y, speed;
				tracks.do({ arg track, idx;
					#x, y, speed = track;
					if( above[ idx ], {
						above[ idx ] = fThreshDown.value( speed ).not;
					}, {
						above[ idx ] = fThreshUp.value( speed );
						if( above[ idx ], {
							fTrig.value( idx );
						});
					});
				});
			};
		}
		);
		if( oUpd.notNil, {
			event.upd = event.upd.add( UpdateListener.newFor( oUpd, fUpd ));
		}, {
			TypeSafe.methodWarn( thisMethod, "Unknown type '" ++ type ++ "'" );
		});
	}
	
	makeFuncRout { arg stake, func;
		var event = stake.event;
		event.rout = event.rout.add({ var t1; inf.do({ t1 = thisThread.seconds; func.value; if( t1 == thisThread.seconds, { "makeFuncRout: deadlock loop!".warn; 0.2.wait })})}.fork );
		if( debugFunc, {
			[ stake, "makeFuncRout" ].postln;
		});
	}

	makeFuncBang { arg stake, trig = \trig;
		var event = stake.event;
		event.upd = event.upd.add( UpdateListener.newFor( forest.trackBang, event[ trig ]));
		if( debugFunc, {
			[ stake, "makeFuncBang" ].postln;
		});
	}

	prTaskBody {
		var start, deltaFrames, latencyFrames, span, stakes, bndlTime;
//var test;

//		playStartTime	= thisThread.seconds;
		latencyFrames	= (bufferLatency * sampleRate).asInteger;
		deltaFrames	= (transportDelta * sampleRate).asInteger;
		start		= playStartPos + latencyFrames;
		inf.do({
//			start	= ((thisThread.seconds - playStartTime) * scsynth.sampleRate).asInteger + playStartPos;
			span		= Span( start, start + deltaFrames );
			// XXX effizienter waere nur nach start zu suchen
			// bzw. einfach einen incremental index zu durchlaufen ?
//			stakes	= doc.trail.getRange( span );
//			stakes	= doc.trail.editGetRange( span, filter: { arg stake; /*[ stake.isPlaying.not, stake.track.muted.not, stake.track.busConfig.notNil ].postln;*/ stake.isPlaying.not and: { stake.track.muted.not and: { stake.track.busConfig.notNil }}});
			stakes	= doc.trail.editGetRange( span, filter: { arg stake; /*[ stake.isPlaying.not, stake.track.muted.not, stake.track.busConfig.notNil ].postln;*/ stake.isPlaying.not and: { stake.track.muted.not }});
			if( debug, {[ "task loop", span, stakes.size ].postln });
			stakes.do({ arg stake;
				bndl = OSCBundle.new;
//				funcStake = stake;
//				protect {
					stake.playToBundle( bndl, this, max( 0, start - stake.span.start ));
//				} {
//					funcStake = nil;
//				};
				bndlTime = max( 0, (stake.span.start - start) / sampleRate ) + bufferLatency;
				if( debug, {[ "send", bndl, bndlTime ].postln });
				bndl.send( scsynth, bndlTime );
			});
			transportDelta.wait;
			start	= start + deltaFrames;
		});
	}
	
	prTaskVolBody {
		var start, latencyFrames, volEnv, idx, startStake, stopStake, dur;

		if( debugVol, { "taskVolBody".postln });

		latencyFrames	= (bufferLatency * sampleRate).asInteger;
		start		= playStartPos + latencyFrames;
		volEnv		= doc.volEnv;
		start		= playStartPos + latencyFrames;
		idx			= volEnv.indexOfPos( start );
		if( idx < 0, {
			idx = (idx + 2).neg.max( 0 );
		});
		startStake	= volEnv.get( idx );
		stopStake		= volEnv.get( idx + 1 ) ? startStake;
//		if( startStake.notNil, {
//			dur		= this.prTaskVolSpawn( startStake, stopStake, start );
//		});
		while({ stopStake.notNil }, {
			dur			= this.prTaskVolSpawn( startStake, stopStake, start );
			dur.wait;
			start		= stopStake.pos + 1;
			idx			= volEnv.indexOfPos( start );
			if( idx < 0, {
				idx = (idx + 1).neg;
			});
			startStake = stopStake;
			stopStake	= volEnv.get( idx );
		});
	}
	
	prTaskVolSpawn { arg startStake, stopStake, start;
		var dur, startLevel, synth;
		startLevel	= startStake.level + ((stopStake.level - startStake.level) *
			((start - startStake.pos) / (stopStake.pos - startStake.pos).max(1)));
		dur			= (stopStake.pos - start).max(0) / sampleRate;
		synth		= Synth.basicNew( \forestXEnvWrite, scsynth, -1 );
		if( debugVol, { [ "taskVolSpawn", startStake, stopStake, start, startLevel, stopStake.level, dur ].postcs });
		scsynth.sendBundle( bufferLatency, synth.newMsg( diskGroup,
			[ \bus, volBus.index, \start, startLevel.dbamp, \end, stopStake.level.dbamp, \dur, dur ]));
		^dur;
	}

	prPlay { arg pos, rate;
		playStartPos	= pos;
		task.stop; taskVol.stop;
		task 	= Routine.run({ this.prTaskBody });
		taskVol	= Routine.run({ this.prTaskVolBody });
	}
	
	prStop {
		task.stop; taskVol.stop;
//		scsynth.sendBundle( bufferLatency + transportDelta, diskGroup.freeAllMsg );
		scsynth.sendBundle( bufferLatency + transportDelta, diskGroup.freeAllMsg, forest.preFilterGroup.freeAllMsg, forest.postFilterGroup.freeAllMsg );
	}
	
	prPause {
		task.stop; taskVol.stop;
	}
	
	prResume { arg pos;
		playStartPos = pos;
		task.stop; taskVol.stop;
		task 	= Routine.run({ this.prTaskBody });
		taskVol	= Routine.run({ this.prTaskVolBody });
	}
}
