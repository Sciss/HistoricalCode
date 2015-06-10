/**
 *	(C)opyright 2006-2007 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: TypeSafe, Span, GeneratorUnit, UnitAttr, RegionStake
 *
 *	ZZZ BUFFER CRASH FIX VERSION
 *	NOTE (BUG) :	don't add buffers to GeneratorUnit since buffers of other TapeUnits
 *				are freed as well with any unit disposed. This version frees the buffers
 *				itself, next version should go back to (fixed) GeneratorUnit
 *
 *	Changelog:
 *		- 11-Jun-06	subclass of GeneratorUnit; play defaults to position 0 now
 *					(use getCurrentPos to reproduce previous behaviour)
 *		- 14-Sep-06	fixed playToBundle and prUpdateBuffer omitting synth.run( true ) message
 *					in certain circumstances. fixed bug in prUpdateBuffer when looping.
 *					renamed prSetStartPos to setStartPos.
 *
 *	@todo	switching of loop not working as should (should use stop / server.sync / play manually now)
 *	@todo	CmdPeriod not yet working, checking for server re-launch not yet working
 *	@todo	handle NRT mode
 *	@todo	(FIXED?) stop with release time will stop the trig responder from updating the buffer any more
 *			; use a NodeProxy or similar for now
 *	@todo	add event listening, e.g. for detecting release-end
 *	@todo	could use re-use same nodeID, use OSCpathResponder
 *	@todo	caches should be in a per server list
 *	@todo	switching output bus becomes only effective after stop / play
 *	@todo	fails for files with >19 channels (bug in BufRd)
 *
 *
 *	TO DO : plays ahead 4 (pad) sample frames !
 *
 *	@version	0.22, 20-Jul-07
 *	@author	Hanns Holger Rutz
 */
TapeUnit2 { // : GeneratorUnit2
	classvar pad = 4;				// # sample frames padding for BufRd interpolation

	classvar mapPathsToSoundFiles;	// key = (String) path ; value = (SoundFile)
	classvar <allUnits;			// elem = instances of TapeUnit

	classvar	debugBuffer		= false;	// true to see buffers allocated + freed
	classvar	<debugProxy		= false;
	
	var currentSynth;
	var startPos = 0;
//	var startTime, defName = nil, path = nil, file = nil;

	var attributes;
	var cues;
	var cueIdx 				= nil;
	var cueAttrDirty			= true;
	
	var condSync;
	
	*initClass {
		mapPathsToSoundFiles	= Dictionary.new;
		allUnits				= IdentitySet.new;
	}

	*new { arg server;
		^super.new( server ).prInitTapeUnit;
	}
	
//	addCue { arg path, region;
//		var file, name;
//		file = this.cachePath( path );
//		if( file.notNil, {
//			name = path.asSymbol; // path.copyToEnd( path.lastIndexOf( $/ ));
//			region = region ?? { RegionStake( Span( 0, file.numFrames ), name ); };
//			cues.add([ path, region ]);
//			cueAttrDirty = true; // this.prUpdateCueAttr;
//		});
//	}
//
//	prSetCues { arg cueList;
//		cues			= List.newFrom( cueList );
//		cueAttrDirty	= true; // this.prUpdateCueAttr;
//	}
//	
//	setCueIndex { arg idx;
//		var cue;
//		
//		if( idx.notNil, {
//			cue = cues[ idx ];
//			if( cue.notNil, {
//				this.setPath( cue[ 0 ]);
//				this.setStartPos( cue[ 1 ].getSpan.start );
//				cueIdx = idx;
//			});
//		});
//	}
//	
//	getCueIndex {
//		^cueIdx;
//	}
//	
//	getNumCues {
//		^cues.size;
//	}
//	
//	setRatioPos { arg p;
//		if( file.notNil, {
//			this.setStartPos( (p * file.numFrames).asInteger );
//		});
//	}
//	
//	getRatioPos {
//		^if( file.notNil, {
//			this.getCurrentPos / file.numFrames;
//		}, 0 );
//	}
	
	prInitTapeUnit {
		cues				= List.new;

		attributes		= [
			UnitAttr( \speed,    ControlSpec( 0.125, 2.3511, \exp ), \normal, \getSpeed,    \setSpeed,    nil ),
			UnitAttr( \ratioPos, ControlSpec( 0, 1, \lin ),          \normal, \getRatioPos, \setRatioPos, nil ),
			nil;	// \cueIndex
		];
//		cueAttrDirty = true; // this.prUpdateCueAttr;

//		(" adding "++this).inform;
		allUnits.add( this );
		
		currentSynth = TapeUnitSynth.new;
	}
	
	getAttributes {
		if( cueAttrDirty, {
			attributes[ attributes.size - 1 ] = UnitAttr( \cueIndex, ControlSpec( 0, this.getNumCues - 1, \lin, 1 ), \normal, \getCueIndex, \setCueIndex, nil );
			cueAttrDirty		= false;
		});
		^attributes;
	}

	// XXX : should call class method prFlushDefs only once and not per instance ... but how?
	// ALSO : cache should be in a per server list!!!!
//	protServerStarted {
//		var result;
//
//// XXX		
//		result = super.protServerStarted;
//		this.class.prFlushDefs;
//		if( this.getPath.notNil, {
//			this.prCacheDef( this.getNumChannels );
//		});
//		^result;
//	}
	
//	cmdPeriod {
//		var result;
//		
//		result		= super.cmdPeriod;
//		trigResp.remove;
//		synth		= nil;
//		buf			= nil;
//		startPos		= this.getCurrentPos;
//		currentPosT	= nil;
//		
//		^result;
//	}

	startPos_ { arg pos;
		startPos = pos;
	}
	
	currentPos {
		^currentSynth.currentPos;
	}
	
	// getNumFrames
	numFrames {
		^if( currentSynth.notNil, { currentSynth.numFrames });
//		^if( file.notNil, {
//			file.numFrames;
//		}, nil );
	}

	// getSampleRate
	sampleRate {
		^if( currentSynth.notNil, { ^currentSynth.sampleRate });
	}

	/**
	 *	Activates or deactivates looping and adjusts the looping span.
	 *
	 *	@param	span		(Span) the span to loop, or nil to switch off looping.
	 *					Note that minimum loop length is 256 sample frames to
	 *					avoid OSC message overloads. Passing an empty span equals
	 *					setting the loop to nil (no looping).
	 *	@todo	changing the looping span while playing may result in strange position shift
	 *			; safer is to call stop / setLoop / play
	 */
	loop_ { arg span;
		if( currentSynth.notNil, { currentSynth.loop = span });
//		if( span.isNil or: { span.getLength >= 256 }, {
//			currentSynthloopSpan = span;
//		}, { if( span.isEmpty, {
//			loopSpan = nil;
//		})});
	}
	
	loopAll {
		this.loop = Span( 0, this.numFrames );
	}
	
	loop {
		^currentSynth.loop;
	}
	
//	play { arg position, atk = 0;
//		var bndl;
//
//		bndl = List.new;
//
//		if( condSync.test.value.not, {
//			if( thisThread.isKindOf( Routine ).not, {
//				fork { if( disposed.not, { this.play( position, atk )})};
//				^this;
//			});
//			condSync.wait;
//		});
//		this.playToBundle2( bndl, position, atk );
//		server.listSendBundle( nil, bndl );
//	}
//		
//	syncIfNecessary {
//		condSync.wait;
//	}

	dispose {
		var result;
		allUnits.remove( this );
		result = super.dispose;
		this.freeBuffer;
		^result;
	}
	
	*disposeAll {
		var all;
		
		all = List.newFrom( allUnits );
		all.do({ arg unit; unit.dispose; });
	}
	
	path_ { arg pathName;
		if( currentSynth.isPlaying, {
			currentSynth.die;
			currentSynth = TapeUnitSynth.newFrom( currentSynth );
		});
		currentSynth.file = this.cachePath( pathName );
	}
	
//	setPath { arg pathName;
//		var wasPlaying;
//	
//		wasPlaying = playing;
//		if( wasPlaying, { this.stop });
//		file	= this.cachePath( pathName );
//		if( file.notNil, {
//			path	= pathName;
//			if( buf.notNil, {
//				if( file.numChannels === buf.numChannels, {
//					buf.close;
//				}, {
//					this.freeBuffer;
//				});
//			});
//			numChannels	= file.numChannels;
//			defName		= "tapeUnit" ++ numChannels;
//			if( wasPlaying, { this.play( this.getCurrentPos )});
//		});
//	}
//	
//	getPath {
//		^path;
//	}
	
//	setSpeed { arg factor;
//		speed = factor;
//		if( playing, {
//			this.stop;
//			this.play( this.getCurrentPos );
//		});
//	}
//	
//	getSpeed {
//		^speed;
//	}

	*flushCache {
		mapPathsToSoundFiles.clear;
	}
	
	cachePath { arg pathName;
		var file;
		file	= mapPathsToSoundFiles[ pathName ];
		if( file.isNil, {
			try {
				file = SoundFile.openRead( pathName );
			};
			if( file.isNil, {
				TypeSafe.methodError( thisMethod, "Soundfile '" ++ pathName ++ "' couldn't be opened" );
				^nil;
			});
			file.close;
			mapPathsToSoundFiles.put( pathName, file );
		});
//		this.prCacheDef( file.numChannels );
		^file;
	}

	prBuildForProxy { arg proxy, channelOffset = 0, index;
		var def, func, numChannels; // , argNames;
		var defName;
		
		numChannels = currentSynth.file.numChannels;
		
		defName	= ("proxyTapeUnit" ++ numChannels).asSymbol;
//		argNames = [ \i_inBuf, \rate, \i_trigID, \i_bufRate ];
		func		= this.prPrepareForProxySynthDef( proxy, numChannels );
		def		= ProxySynthDef(
			defName,
			func,
			proxy.nodeMap.ratesFor( func.argNames ),
			nil,
			true,
			channelOffset,
			numChannels, // proxy.numChannels,
			\audio // proxy.rate
		);
		def.writeDefFile;
		^def;
	}

	prPrepareForProxySynthDef { arg proxy, numChannels;
		^{
			arg i_inBuf, speed = 1.0, i_trigID = 10, i_bufRate;

			var clockTrig, numFrames, halfPeriod, phasorRate;
			var phasorTrig, phasor, bufReader, interp, bufRate2;
//			var env, envGen;

//			env 			= Env.asr( 0.1, 1.0, 0.1, \sine ).asArray;
//			env[ 5 ]		= i_atk;
//			envGen 		= EnvGen.ar( env, gate, doneAction: 2 );

			numFrames		= BufFrames.kr( i_inBuf );  // must be k-rate!!
			bufRate2		= i_bufRate * speed;
			phasorRate 	= bufRate2 / SampleRate.ir;
			halfPeriod	= numFrames / (bufRate2 * 2);
			phasor		= Phasor.ar( 0, phasorRate, 0, numFrames - pad - pad ) + pad;

			// BufRd interpolation switches between 1 (none) and 4 (cubic)
			// depending on the rate being 1.0 or not
			interp		= (phasorRate - 1.0).sign.abs * 3 + 1;
//			bufReader 	= BufRd.ar( proxy.numChannels, i_inBuf, phasor, 0, interp );
			bufReader 	= BufRd.ar( numChannels, i_inBuf, phasor, 0, interp );
			phasorTrig	= Trig1.kr( A2K.kr( phasor ) - (numFrames / 2), 0.01 );
			clockTrig		= phasorTrig + TDelay.kr( phasorTrig, halfPeriod );

			SendTrig.kr( clockTrig, i_trigID, PulseCount.kr( clockTrig ));
			bufReader;
		};
	}

//	n_go { arg node;
//		if( node === synth, {
//			currentPosT = SystemClock.seconds;
//			currentPos = startPos;
//		});
//		^super.n_go( node );
//	}
//	
//	n_end { arg node;
//		if( node === synth, {
//			trigResp.remove;
//			synth		= nil;
//			startPos		= this.getCurrentPos;
//			currentPosT	= nil;
//			this.protSetPlaying( false );
//		});
//		^super.n_end( node );
//	}

//	protDuplicate { arg dup;
//		dup.setSpeed( this.getSpeed );
//		dup.setPath( this.getPath );
//		dup.setLoop( this.getLoop );
//		dup.prSetCues( cues );
//		dup.setCueIndex( this.getCueIndex );
//		dup.setStartPos( this.getCurrentPos );
//	}
	
	proxyControlClass { ^TapeUnitControl }
	
	loadToBundle { arg bundle, server;
	}
	
	playToBundle { arg bundle, args, proxy, startFrame;
		if( currentSynth.isPlaying, {
			currentSynth.die;
			currentSynth = TapeUnitSynth.newFrom( currentSynth );
		});

//		if( buf.notNil and: { buf.server != server }, {
//			this.stop;
//			this.freeBuffer;
//		});

		^currentSynth.playToBundle( bundle, args, proxy.asGroup, \addToTail, proxy.asBus, startFrame );

//		startPos = this.getCurrentPos;
//		synth	= Synth.basicNew( "proxy" ++ defName, server );
//
////		this.protAddNode( synth );
//		trigResp.add;
//		if( playing.not, { bundle.addMessage( this,\changed, [ \unitPlaying, true ])});
//		^synth;
	}

	stopToBundle { arg bundle, dt;
		^currentSynth.stopToBundle( bundle, dt );
	}

	freeToBundle { arg bundle, dt;
		currentSynth.die;
		currentSynth = TapeUnitSynth.newFrom( currentSynth );
	}
}

TapeUnitSynth {
	classvar pad = 4;				// # sample frames padding for BufRd interpolation

	var <>file, startTime, startPos = 0, <looping = false, <rate = 1.0, <server;
	var buf, <node;
	var bufSize, halfBufSize, halfBufSizeM, trigResp;
	var loopSpan = nil;
	
	var shouldDie = false;
	var <>verbose = false;

	var currentPos, currentPosT;
	
	var status = \none;
	
	*new {
		^super.new.prInit;
	}
	
	*newFrom { arg aUnitSynth;
		^this.new.prFrom( aUnitSynth );
	}
	
	prInit { }
	
	prFrom { arg aUnitSynth;
		file			= aUnitSynth.file;
		looping		= aUnitSynth.looping;
		rate			= aUnitSynth.rate;
		server		= aUnitSynth.server;
		loopSpan		= aUnitSynth.loop;
		verbose		= aUnitSynth.verbose;
		currentPos	= aUnitSynth.currentPos;
	}
	
	isPlaying { ^(node.notNil and: { node.isPlaying })}

	loop_ { arg span;
		if( span.isNil or: { span.getLength >= 256 }, {
			loopSpan = span;
		}, { if( span.isEmpty, {
			loopSpan = nil;
		})});
	}
	
	loop { ^loopSpan }

	// getNumFrames
	numFrames {
		^if( file.notNil, {
			file.numFrames;
		}, nil );
	}

	// getSampleRate
	sampleRate {
		^if( file.notNil, {
			file.sampleRate;
		}, nil );
	}

	playToBundle { arg bundle, args, target, addAction, bus, startFrame;
		var defName;

		target	= target.asTarget;
		server	= target.server;

if( TapeUnit2.debugProxy, { ("ARGS : "++args).postln });

		if( buf.isNil, {
			halfBufSizeM	= server.sampleRate.asInteger;
			halfBufSize	= halfBufSizeM + pad;
			bufSize		= halfBufSize << 1;
			buf			= Buffer.new( server, bufSize, file.numChannels );
			bundle.addPrepare( buf.allocMsg );
		});

		defName	= "proxyTapeUnit" ++ file.numChannels;
		node		= Synth.basicNew( defName, server );
		NodeWatcher.register( node, true );
		node.addDependant( this );
		trigResp	= OSCpathResponder( server.addr, [ '/tr', node.nodeID ], { arg time, resp, msg;
			var trigVal, bndl;

			if( node.isPlaying, {
				trigVal 	= msg[ 3 ].asInt + 1;
				if( verbose, {
					("TapeUnit: got /tr node=" ++ node.nodeID ++ " val=" ++ trigVal ++ " ; system " ++
						SystemClock.seconds).postln;
				});
				bndl = MixedBundle.new;
				this.prUpdateBuffer( trigVal, bndl );
				if( (bndl.preparationMessages.size > 0) or: { (bndl.messages.size > 0) }, {
					bndl.send( server, server.latency.max( 0.1 ));
				}, {
					if( verbose, {
						("TapeUnit: freeing node=" ++ node.nodeID).postln;
					});
					server.sendMsg( '/n_free', node.nodeID );
				});
			});
		});

		startPos = startFrame ?? { this.currentPos };

		bundle.add( node.newMsg( target, [ \i_inBuf, buf.bufnum, \i_bufRate, file.sampleRate, \rate, rate, \out,
			bus.notNil.if({ bus.index }, 0 )], \addToHead ));
		this.prUpdateBuffer( 0, bundle );
		this.prUpdateBuffer( 1, bundle ); // , synth.runMsg( true )

// NOTE: bug in OSCpathResponder : not allowed to call remove when add hadn't been called.
// therefore safer to add responder _now_
		trigResp.add;		
//		bundle.addFunction({ trigResp.add });
		
//		bundle.addMessage( this,\didSpawn );
//		status = \aboutToPlay;
		^node;
	}

	stopToBundle { arg bundle, dt;
		if( node.notNil, {
			if( dt > 0, {
				bundle.add( node.releaseMsg( dt ));
			}, {
				bundle.add( node.freeMsg );
			});
		});
	}
	
	update { arg obj, what;
		if( obj === node, {
			switch( what,
			\n_end, {
				if( shouldDie, {
					this.dispose;
				})
			},
			\n_go, {
				if( shouldDie, {
				
				});
			});
		});
	}
	
	die {
		shouldDie = true;
		if( node.isPlaying.not, {
			this.dispose;
		});
	}
		
	dispose {
		trigResp.remove;
		trigResp = nil;
		if( buf.notNil, {
			buf.server.listSendMsg( buf.closeMsg( buf.freeMsg ));
			buf = nil;
		});
		node.removeDependant( this );
		node.isPlaying = false;
	}

	currentPos {
		var pos;
		
		if( currentPosT.notNil && file.notNil, {
			pos = (currentPos + ((SystemClock.seconds - currentPosT) * server.sampleRate * rate).asInt)
				.clip( 0, file.numFrames );
			if( loopSpan.notNil and: { currentPos > loopSpan.start }, {
				pos = ((pos - loopSpan.start) % loopSpan.getLength) + loopSpan.start;
			});
		}, { pos = startPos });
		
		^pos;
	}
	
	// doesn't return boolean anymore, instead returns first message
	// (can be used as completionMessage e.g. in b_allocRead)
	// ; if bndl is nil, messages are sequenced using completionMsg mechanism
	prUpdateBuffer { arg trigVal, bndl, completionMsg;
		var msg, bufOff, frame, frameStop, frameMax, frameLen;
				
		bufOff		= trigVal.even.if( 0, halfBufSize );
		frame		= (trigVal * halfBufSizeM) + startPos + trigVal.even.if( 0, pad );
		currentPosT	= SystemClock.seconds;
		// NOT LOOPED
		if( loopSpan.isNil, {
			currentPos	= frame - halfBufSize;
			frameStop 	= frame + halfBufSize;
			frameMax  	= file.numFrames;
			case { frameStop <= frameMax }
			{
				msg = [ "/b_read", buf.bufnum, file.path, frame, halfBufSize, bufOff, 0, completionMsg ];
				if( bndl.notNil, { bndl.addPrepare( msg ); });
			}
			{ frame < frameMax }
			{
				msg 		= [ "/b_read", buf.bufnum, file.path, frame, frameMax - frame, bufOff, 0, completionMsg ];
				bufOff	= bufOff + frameMax - frame;
				if( bndl.notNil, {
					bndl.addPrepare( msg );
					bndl.addPrepare([ "/b_fill", buf.bufnum, bufOff, halfBufSize - frameMax + frame, 0.0 ]);
// otherwise assume, buffer has been zeroed before!
//				}, {
//					if( emptyFilePath.notNil, {
//						
//					});	// else omit the /b_fill (not possible in async chain)
				});
				if( verbose, {
					("TapeUnit: zeroing off = "++bufOff++
						"; len = "++(halfBufSize - frameMax + frame)).postln;
				});
			}
			{
				if( frame - halfBufSize < frameMax, {
					if( bndl.notNil, {
						msg = [ "/b_zero", buf.bufnum, completionMsg ];
						bndl.addPrepare( msg );
					}, {
						// assume buffer has been zeroed
						msg = completionMsg;
					});
					if( verbose, {
						("TapeUnit: zeroing complete buffer").postln;
					});
				}, {
					currentPosT	= nil;
					startPos		= 0;
					msg			= completionMsg;
				});
			};
		},
		// LOOPED
		{
			currentPos	= frame - halfBufSize;
			if( frame > loopSpan.start, { frame = ((frame - loopSpan.start) % loopSpan.getLength) + loopSpan.start; });
			if( currentPos > loopSpan.start, {
				currentPos = ((currentPos - loopSpan.start) % loopSpan.getLength) + loopSpan.start;
			});
			frameMax		= loopSpan.stop;
			frameLen		= halfBufSize;
			if( bndl.notNil, {
				while({ frameLen > 0 }, {
					if( frame + frameLen <= frameMax, {
						msg			= [ "/b_read", buf.bufnum, file.path, frame, frameLen, bufOff, 0, completionMsg ];
						bndl.addPrepare( msg );
						frameLen 		= 0;	
					}, {
						msg			= [ "/b_read", buf.bufnum, file.path, frame, frameMax - frame, bufOff, 0 ];
						bndl.addPrepare( msg );
						bufOff		= bufOff + frameMax - frame;
						frameLen		= frameLen - frameMax + frame;
						frame		= loopSpan.start;
					});
				});
			}, {
				msg = completionMsg;	// since the loop is executed in reverse order on the server, make sure the completionMsg is executed last
				while({ frameLen > 0 }, {
					if( frame + frameLen <= frameMax, {
						msg			= [ "/b_read", buf.bufnum, file.path, frame, frameLen, bufOff, 0, msg ];
						frameLen		= 0;	
					}, {
						msg			= [ "/b_read", buf.bufnum, file.path, frame, frameMax - frame, bufOff, 0, msg ];
						bufOff		= bufOff + frameMax - frame;
						frameLen		= frameLen - frameMax + frame;
						frame		= loopSpan.start;
					});
				});
			});
		});
		
//		if( bndl.notNil, { this.changed( \currentPos, currentPos );});
//		this.changed( \currentPos, currentPos );
		bndl.addMessage( this, \changed, [ \currentPos, currentPos ]);
		^msg;
	}
}