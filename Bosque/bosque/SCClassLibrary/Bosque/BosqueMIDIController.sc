/*
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 */
 
/**
 *	This class assumes the anatomy of a BCF2000 controller.
 *
 *	@version	0.11, 23-Jul-08
 */
BosqueMIDIController {
	classvar <>numFaders = 8;
	classvar <>period = 0.05; // update period (during transport performance) in seconds
	
	var bosque, doc, <in, <out;
	var follow, write;
	
//	var ccFaderMSB, ccFaderLSB;
	var faderMSBmem;
	var liveEnvs;
	var liveTracks;
	var liveEnvPainter;
	
	*new { arg bosque;
		^super.new.prInit( bosque );
	}
	
	prInit { arg argBosque;
		var routFollow;
	
		bosque		= argBosque ? Bosque.default;
		out			= bosque.midiOut;
		in			= bosque.midiIn;
		doc			= bosque.session;
		if( out.isNil || in.isNil, { ^this });
		
		follow		= false ! numFaders;
		write		= false ! numFaders;
		
		faderMSBmem	= 0 ! numFaders;
		liveEnvs		= nil ! numFaders;
		liveTracks	= nil ! numFaders;
		
		liveEnvPainter = bosque.timelineEditor.panel.liveEnvPainter;
			
		numFaders.do({ arg chan;
			// follow controller is 16 ... 16 + numFaders - 1
			CCResponder({ arg src, midiChan, num, value;
				var onOff = value != 0;
				follow[ chan ] = onOff;
				if( onOff, { this.prFollow });
			}, in, 0, chan + 16 );
			out.control( 0, chan + 16, 0 );
			
			// write controller is 8 ... 8 + numFaders - 1
			CCResponder({ arg src, midiChan, num, value;
				var onOff = value != 0;
				write[ chan ] = onOff;
				if( onOff, {
					// nada
				}, {
					this.prWriteStop( chan );
				});
			}, in, 0, chan + 8 );
			out.control( 0, chan + 8, 0 );

			// fader MSB is 0 ... 0 + numFaders - 1;
			CCResponder({ arg src, midiChan, num, value;
				faderMSBmem[ chan ] = value;
			}, in, 0, chan ); 

			// fader LSB is 32 ... 32 + numFaders - 1;
			CCResponder({ arg src, midiChan, num, value;
				if( write[ chan ] and: { doc.transport.isRunning }, {
					this.prWrite( chan, (value |Ê(faderMSBmem[ chan ] << 7)) / 0x3FFF );
				});
			}, in, 0, chan + 32 ); 
		});
		
		UpdateListener.newFor( doc.transport, { arg upd, trnsp, what, param;
			switch( what,
			\play, {
				routFollow = this.prStartRoutFollow;
			},
			\stop, {
				routFollow.stop; routFollow = nil;
				numFaders.do({ arg chan;
					if( write[ chan ], {
						this.prWriteStop( chan );
						out.control( 0, chan + 8, 0 );
					});
				});
			},
			\pause, {
				routFollow.stop; routFollow = nil;
			},
			\resume, {
				routFollow = this.prStartRoutFollow;
			});
		});
		
		UpdateListener.newFor( doc.timelineView.cursor, { arg upd, csr;
			this.prFollow;
		}, \changed );
	}
	
	prStartRoutFollow {
		^{
			inf.do({
				this.prFollow;
				period.wait;
			});
		}.fork;
	}
	
	prFollow {
		var frame, track, trackOff, level, full, lsb, msb;
		trackOff = block { arg break; doc.tracks.do({ arg t, i; if( t.trackID >= 0, { break.value( i )})}); nil };
//("trackOff = " ++trackOff).postln;
		if( trackOff.isNil, { ^this });
		follow.do({ arg yes, i;
			if( yes, {
				if( frame.isNil, { frame = doc.transport.currentFrame });
				track = doc.tracks.at( i + trackOff );
				if( track.notNil, {
					level = track.level( frame );
					if( level.notNil, {
//("level["++i++"] = " ++level).postln;
						full = (level * 0x3FFF).asInteger;
						lsb  = full & 0x7F;
						msb  = full >> 7;
						out.control( 0, i, msb );
						out.control( 0, i + 32, lsb );
					});
				});
			});
		});
	}
	
	prWrite { arg chan, level;
		var trackOff, env, segm, frame, hitIdx, hitStake1, newStake1, newStake2, span;
		
		frame = doc.transport.currentFrame;
//		[ chan, level ].postln;
		
		if( liveTracks[ chan ].isNil, {
			trackOff = block { arg break; doc.tracks.do({ arg t, i; if( t.trackID >= 0, { break.value( i )})}); nil };
			liveTracks[ chan ] = doc.tracks.at( chan + trackOff );
			if( liveTracks[ chan ].isNil, { ^this });
			span = Span( frame, doc.timeline.span.stop );
			segm = BosqueEnvSegmentStake( span.shift( frame.neg ), level, level );
			env = BosqueTrail.new;
			env.add( nil, segm );
			liveEnvs[ chan ] = BosqueEnvRegionStake( span, \Env, liveTracks[ chan ], env: env );
			liveEnvPainter.addTrack( liveTracks[ chan ], liveEnvs[ chan ]);
			liveTracks[ chan ].liveReplc = liveEnvs[ chan ];
		}, {
			frame	= frame - liveEnvs[ chan ].span.start;
			env		= liveEnvs[ chan ].env;
			hitIdx	= env.indexOf( frame );
			if( hitIdx >= -1, { ^this });
			hitStake1		= env.get( (hitIdx + 2).neg );
			Assertion({ hitStake1.span.containsPos( frame )});
			newStake1		= hitStake1.replaceStopWithLevel( frame, level );
			newStake2		= hitStake1.replaceStartAndLevels( frame, level, level );
			env.remove( this, hitStake1 );
			env.addAll( this, [ newStake1, newStake2 ]);
			span			= hitStake1.span; // .shift( pressedStake.span.start );
//			env.modified( this, span );
		});
	}
	
	prWriteStop { arg chan;
		var stake, newStake, segm, lastSegm;
		write[ chan ] = false;
		if( liveTracks[ chan ].notNil, {
			liveTracks[ chan ].liveReplc = nil;
			liveEnvPainter.removeTrack( liveTracks[ chan ]);
			liveTracks[ chan ] = nil;
//			liveEnvs[ chan ].dispose;
			stake = liveEnvs[ chan ];
			liveEnvs[ chan ] = nil;
			if( stake.env.numStakes > 1, {
				lastSegm = stake.env.get( stake.env.numStakes - 1 );
				stake.env.remove( nil, lastSegm );
//				stake.dirtyDirtyRegionStop( lastSegm.span.start + stake.span.start );
				newStake = stake.replaceStop( lastSegm.span.start + stake.span.start );
//				stake.dispose;
				bosque.timelineEditor.editAddEnvStake( newStake );
			});
		});
	}
}