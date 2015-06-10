/**
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
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
 *	@version	0.26, 16-Apr-09
 *	@author	Hanns Holger Rutz
 */
NuagesUTape : NuagesUGenAudioSynth {
//	classvar defSet;				// elem = channel # for defs sent to the server
	classvar mapPathsToSoundFiles;	// key = (String) path ; value = (SoundFile)

	classvar pad = 4;				// # sample frames padding for BufRd interpolation
	
	var path = nil, file = nil, startTime, startPos = 0, looping = false, frozen = false, speed = 1.0;
	var bufSize, halfBufSize, halfBufSizeM;
	var currentPos, currentPosT;
	var loopSpan = nil;

	var cues;
	var cueIdx 				= nil;
	var channelMap			= nil;
	var readCmd				= "/b_read";
	
	*initClass {
//		defSet				= IdentitySet.new;
		mapPathsToSoundFiles	= Dictionary.new;
	}

	*new { arg server;
		^super.new( server ).prInitTape;
	}
	
	autoPlay { ^false }
	
	addCue { arg path, region;
		var file, name;
		file = this.cachePath( path );
		if( file.notNil, {
			name = path.asSymbol; // path.copyToEnd( path.lastIndexOf( $/ ));
			region = region ?? { RegionStake( Span( 0, file.numFrames ), name ); };
			cues   = cues.add([ path, region ]);
			this.prUpdateCueAttr;
		});
	}

	prSetCues { arg cueList;
		cues			= cueList.copy;
		this.prUpdateCueAttr;
	}
	
	setCueIndex { arg idx;
		this.protMakeBundle({ arg bndl; this.setCueIndexToBundle( bndl, idx )});
	}
	
	setCueIndexToBundle { arg bndl, idx;
		var cue, wasPlaying;
		
		if( idx.notNil && (idx != cueIdx), {
			cue = cues.notNil.if({ cues[ idx ] });
			if( cue.notNil, {
//				wasPlaying = playing;
//				if( wasPlaying, { this.stopToBundle( bndl )});
				this.setPathToBundle( bndl, cue[ 0 ]);
				this.setStartPos( cue[ 1 ].getSpan.start );
//				if( wasPlaying, { this.playToBundle( bndl )});
				cueIdx = idx;
				this.tryChanged( \attrUpdate, \cueIndex );
			});
		});
	}
	
	getCueIndex {
		^cueIdx;
	}
	
	getNumCues {
		^cues.size;
	}
	
	setRatioPos { arg p;
//		this.protMakeBundle({ arg bndl; this.setRatioPosToBundle( bndl, p )});
		if( file.notNil, {
			this.setStartPos( (p * file.numFrames).asInteger );
		});
	}
	
	setRatioPosToBundle { arg bndl, p;
		if( file.notNil, {
//			this.setStartPosToBundle( (p * file.numFrames).asInteger );
			this.setStartPos( (p * file.numFrames).asInteger );
		});
	}
	
	getRatioPos {
		^if( file.notNil, {
			this.getCurrentPos / file.numFrames;
		}, 0 );
	}
	
	prInitTape {
		bufSize			= (server.sampleRate.asInteger + pad) * 2;
		bufSize			= bufSize & 1.bitNot;	    // must be even
		halfBufSize		= bufSize >> 1;
		halfBufSizeM		= halfBufSize - pad;

		this.protPrependAttr(
			UnitAttr( \speed,    ControlSpec( 0.125, 2.3511, \exp ), \normal, \getSpeed,    \setSpeed,    nil ),
			UnitAttr( \ratioPos, ControlSpec( 0, 1, \lin ),          \normal, \getRatioPos, \setRatioPos, nil ),
			UnitAttr( \cueIndex, ControlSpec( 0, 0, \lin, 1 ), \normal, \getCueIndex, \setCueIndex, nil ));	}
	
	// doesn't return boolean anymore, instead returns first message
	// (can be used as completionMessage e.g. in b_allocRead)
	// ; if bndl is nil, messages are sequenced using completionMsg mechanism
	prUpdateBuffer { arg buf, trigVal, bndl, completionMsg;
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
				msg = [ readCmd, buf.bufnum, path, frame, halfBufSize, bufOff, 0 ] ++ channelMap ++ if( completionMsg.notNil, {[ completionMsg ]});
				if( bndl.notNil, { bndl.addPrepare( msg ); });
			}
			{ frame < frameMax }
			{
				msg 		= [ readCmd, buf.bufnum, path, frame, frameMax - frame, bufOff, 0, ] ++ channelMap ++ if( completionMsg.notNil, {[ completionMsg ]});
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
						msg			= [ readCmd, buf.bufnum, path, frame, frameLen, bufOff, 0 ] ++ channelMap ++ if( completionMsg.notNil, {[ completionMsg ]});
						bndl.addPrepare( msg );
						frameLen 		= 0;	
					}, {
						msg			= [ readCmd, buf.bufnum, path, frame, frameMax - frame, bufOff, 0 ] ++ channelMap;
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
						msg			= [ readCmd, buf.bufnum, path, frame, frameLen, bufOff, 0 ] ++ channelMap ++ if( msg.notNil, {[ msg ]});
						frameLen		= 0;	
					}, {
						msg			= [ readCmd, buf.bufnum, path, frame, frameMax - frame, bufOff, 0 ] ++ channelMap ++ if( msg.notNil, {[ msg ]});
						bufOff		= bufOff + frameMax - frame;
						frameLen		= frameLen - frameMax + frame;
						frame		= loopSpan.start;
					});
				});
			});
		});
		
//		if( bndl.notNil, { this.tryChanged( \currentPos, currentPos )});
		this.tryChanged( \currentPos, currentPos );
		this.tryChanged( \attrUpdate, \ratioPos );
		^msg;
	}

	prUpdateCueAttr {
		var idx;
		
		this.protReplaceAttr( UnitAttr( \cueIndex, ControlSpec( 0, this.getNumCues - 1, \lin, 1 ), \normal, \getCueIndex, \setCueIndex, nil ));
	}

//	// XXX : should call class method prFlushDefs only once and not per instance ... but how?
//	// ALSO : cache should be in a per server list!!!!
//	protServerStarted {
//		var result;
//		
//		result = super.protServerStarted;
////		this.class.prFlushDefs;
//		if( this.getPath.notNil, {
//			this.prCacheDef( numChannels );
//		});
//		^result;
//	}
//	
//	cmdPeriod {
//		var result;
//		
//		result		= super.cmdPeriod;
//		trigResp.remove;
//		buf			= nil;
//		startPos		= this.getCurrentPos;
//		currentPosT	= nil;
//		
//		^result;
//	}

	setStartPos { arg pos;
		startPos = pos;
	}
	
	getCurrentPos {
		var pos;
		
		if( currentPosT.notNil && file.notNil, {
			pos = (currentPos + ((SystemClock.seconds - currentPosT) * server.sampleRate * speed).asInt)
				.clip( 0, file.numFrames );
			if( loopSpan.notNil and: { currentPos > loopSpan.start }, {
				pos = ((pos - loopSpan.start) % loopSpan.getLength) + loopSpan.start;
			});
		}, { pos = startPos });
		
		^pos;
	}
	
	getNumFrames {
		^if( file.notNil, {
			file.numFrames;
		}, -1 );
	}

	getSampleRate {
		^if( file.notNil, {
			file.sampleRate;
		}, -1 );
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
	setLoop { arg span;
		if( span.isNil or: { span.getLength >= 256 }, {
			loopSpan = span;
		}, { if( span.isEmpty, {
			loopSpan = nil;
		})});
	}
	
	loopAll {
		this.setLoop( Span( 0, this.getNumFrames ));
	}
	
	getLoop {
		^loopSpan;
	}

	protSetAttrToBundle { arg bndl, synth;
		bndl.add( synth.setMsg( \rate, speed ));
	}

	protCreateBuffersToBundle { arg bndl, aSynth, numInChannels, numChannels;
		var buf, trigResp;
		
		buf = Buffer.new( server, bufSize, numChannels );
		bndl.addPrepare( buf.allocMsg( this.prUpdateBuffer( buf, 0, nil, this.prUpdateBuffer( buf, 1, nil ))));
		bndl.add( aSynth.setMsg( \i_inBuf, buf.bufnum, \i_bufRate, file.sampleRate ));

		trigResp	= OSCpathResponder( server.addr, [ '/tr', aSynth.nodeID ], { arg time, resp, msg;
			var trigVal, bndl, nodeID;

			nodeID	= msg[ 1 ];
			trigVal 	= msg[ 3 ].asInt + 1;
			if( verbose, {
				("TapeUnit: got /tr node=" ++nodeID ++ " val=" ++ trigVal ++ " ; system " ++
					SystemClock.seconds).postln;
			});
			bndl = OSCBundle.new;
			this.prUpdateBuffer( buf, trigVal, bndl );
			if( bndl.preparationMessages.size > 0, {
//					bndl.send( server, max( 0.1, server.latency ));
				server.listSendBundle( nil, bndl.preparationMessages );
			}, {
				if( verbose, {
					("TapeUnit: freeing node=" ++ nodeID).postln;
				});
				server.sendMsg( "/n_free", nodeID );
			});
		}).add;

		UpdateListener.newFor( aSynth, { arg upd, node, what;
			switch( what,
			\n_go, {
				if( node === synth, {
					currentPosT = SystemClock.seconds;
					currentPos = startPos;
				});
			},
			\n_end, {
				upd.remove;
				trigResp.remove;
				if( node === synth, {
					startPos		= this.getCurrentPos;
					currentPosT	= nil;
//					synth		= nil;
//					this.protSetPlaying( false );
				});
			});
		});
		
		^[ buf ];
	}
	
	playToBundle { arg ... args;
		if( file.isNil, {
			TypeSafe.methodWarn( thisMethod, "No path has been specified" );
			^this;
		});
		^super.playToBundle( *args );
	}
		
//	playToBundleLALALALA { arg bndl;
//		var bus, defName, buf, trigResp;
//
//		if( playing, {
//			this.stopToBundle( bndl );
//			synth = nil;
//		});
//
//		if( file.isNil, {
//			TypeSafe.methodWarn( thisMethod, "No path has been specified" );
//			^this;
//		});
//		
//		startPos = this.getCurrentPos;
//		defName	= this.protCreateDefName( numChannels );
//		synth	= Synth.basicNew( defName, server );
//		this.protNewSynthToBundle( bndl, synth, { arg defName; this.protMakeDef( defName, nil, numChannels )});
//		bus		= this.getOutputBus;
//		buf		= Buffer.new( server, bufSize, numChannels );
//		bndl.addPrepare( buf.allocMsg( this.prUpdateBuffer( buf, 0, nil, this.prUpdateBuffer( buf, 1, nil ))));
//		bndl.add( synth.newMsg( target, [ \i_inBuf, buf.bufnum, \i_bufRate, file.sampleRate, \rate, speed, \out,
//			bus.notNil.if({ bus.index }, 0 ), \i_atk, 0.0, \volume, volume ], \addToHead ));
//
//		trigResp	= OSCpathResponder( server.addr, [ '/tr', synth.nodeID ], { arg time, resp, msg;
//			var trigVal, bndl, nodeID;
//
//			nodeID	= msg[ 1 ];
//			trigVal 	= msg[ 3 ].asInt + 1;
//			if( verbose, {
//				("TapeUnit: got /tr node=" ++nodeID ++ " val=" ++ trigVal ++ " ; system " ++
//					SystemClock.seconds).postln;
//			});
//			bndl = OSCBundle.new;
//			this.prUpdateBuffer( buf, trigVal, bndl );
//			if( bndl.preparationMessages.size > 0, {
////					bndl.send( server, max( 0.1, server.latency ));
//				server.listSendBundle( nil, bndl.preparationMessages );
//			}, {
//				if( verbose, {
//					("TapeUnit: freeing node=" ++ nodeID).postln;
//				});
//				server.sendMsg( "/n_free", nodeID );
//			});
//		}).add;
//
//		UpdateListener.newFor( synth, { arg upd, node, what;
//			switch( what,
//			\n_go, {
//				if( node === synth, {
//					currentPosT = SystemClock.seconds;
//					currentPos = startPos;
//				});
//			},
//			\n_end, {
//				upd.remove;
//				buf.close; buf.free;
//				trigResp.remove;
//				if( node === synth, {
//					startPos		= this.getCurrentPos;
//					currentPosT	= nil;
//					synth		= nil;
//					this.protSetPlaying( false );
//				});
//			});
//		});
//
//		this.protSetPlaying( true );
//	}
	
	setPath { arg pathName;
		this.protMakeBundle({ arg bndl; this.setPathToBundle( bndl, pathName )});
	}
	
	setPathToBundle { arg bndl, pathName;
		var wasPlaying;
		
		if( pathName == path, { ^this });
	
		wasPlaying = playing;
		if( wasPlaying, { this.stopToBundle( bndl )});
		file	= this.cachePath( pathName );
		if( file.notNil, {
			path	= pathName;
			this.prAfterChannelMod( bndl, wasPlaying );
		});
	}
	
//	protCreateDefName { arg numChannels;
//		^("tapeUnit" ++ numChannels).asSymbol;
//	}
	
	getPath {
		^path;
	}
	
	setChannelMap { arg map;
		this.protMakeBundle({ arg bndl; this.setChannelMapToBundle( bndl, map )});
	}
	
	setChannelMapToBundle { arg bndl, map;
		var wasPlaying;
		
		if( map == channelMap, { ^this });
		
		wasPlaying	= playing;
		if( wasPlaying, { this.stopToBundle( bndl )});
		channelMap	= map.copy;
		readCmd		= if( channelMap.isNil, "/b_read", "/b_readChannel" );
		if( file.notNil, { this.prAfterChannelMod( bndl, wasPlaying )});
	}
	
	prAfterChannelMod { arg bndl, wasPlaying;
		if( channelMap.isNil, {
//			numChannels	= file.numChannels;
		}, {
			channelMap	= channelMap.select({ arg chan; chan < file.numChannels });
//			numChannels	= channelMap.size;
		});
//		if( buf.notNil, {
//			if( numChannels === buf.numChannels, {
//				buf.close;
//			}, {
//				this.freeBuffer;
//			});
//		});
//		defName = this.protCreateDefName( numChannels );
//		this.protCacheDef( numChannels );
		if( wasPlaying, { this.playToBundle( bndl )});
	}
	
	getChannelMap { ^channelMap.copy }
	
	setSpeed { arg factor;
		this.protMakeBundle({ arg bndl; this.setSpeedToBundle( bndl, factor )});
	}
	
	setSpeedToBundle { arg bndl, factor;
		if( speed != factor, {
			speed = factor;
			if( playing, {
				this.stopToBundle( bndl );
				this.playToBundle( bndl );
			});
//[ "setSpeed", factor ].postln;
			this.tryChanged( \attrUpdate, \speed );
		});
	}
	
	getSpeed {
		^speed;
	}

//	*flushCache {
//		mapPathsToSoundFiles.clear;
//		this.prFlushDefs;
//	}
	
//	*prFlushDefs {
//		defSet.clear;
//	}
	
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
		^file;
	}
	
	protPreferredNumOutChannels { arg idx;
//[ "aqi", idx, if( file.notNil, { file.numChannels })].postln;
		^if( file.notNil, { file.numChannels });
	}
	
	// XXX should deal with file.numChannels / channelMap
	protMakeDef { arg defName, numInChannels, numChannels;
		^SynthDef( defName, {
			arg out = 0, i_inBuf, rate = 1.0, i_trigID = 10, i_atk = 0, volume = 1, gate = 1, i_bufRate;

			var clockTrig, numFrames, halfPeriod, phasorRate;
			var phasorTrig, phasor, bufReader, interp;
			var env, envGen;

			env 			= Env.asr( 0.1, 1.0, 0.1, \sine ).asArray;
			env[ 5 ]		= i_atk;
			envGen 		= EnvGen.ar( env, gate, levelScale: 0.999, doneAction: 2 ); // 0.999 = bug fix !!!

//			numFrames		= BufFrames.ir( i_inBuf );
// FUCKING CRUCIAL TO BE KR!!
			numFrames		= BufFrames.kr( i_inBuf );
//			phasorRate 	= BufRateScale.kr( i_inBuf ) * rate;
			phasorRate 	= (i_bufRate / SampleRate.ir) * rate;
//			halfPeriod	= BufDur.kr( i_inBuf ) / (2 * rate);
			halfPeriod	= numFrames / (i_bufRate * 2 * rate);
			phasor		= Phasor.ar( 0, phasorRate, 0, numFrames - pad - pad ) + pad;

			// BufRd interpolation switches between 1 (none) and 4 (cubic)
			// depending on the rate being 1.0 or not
//			interp		= (rate - 1.0).sign.abs * 3 + 1;
			interp		= (phasorRate - 1.0).sign.abs * 3 + 1;
//Poll.kr( Impulse.kr( 0.5 ), phasorRate, "phasorRate" );
//Poll.kr( Impulse.kr( 0.5 ), interp, "interp" );
			bufReader 	= BufRd.ar( numChannels, i_inBuf, phasor, 0, interp );
			phasorTrig	= Trig1.kr( A2K.kr( phasor ) - (numFrames / 2), 0.01 );
			clockTrig		= phasorTrig + TDelay.kr( phasorTrig, halfPeriod );

			SendTrig.kr( clockTrig, i_trigID, PulseCount.kr( clockTrig ));
			Out.ar( out, bufReader * envGen * volume );
		}, [ nil, nil, nil, nil, nil, 0.01, nil ]);
	}
	
//	n_go { arg node;
//		if( node === synth, {
//			currentPosT = SystemClock.seconds;
//			currentPos = startPos;
//		});
//		^super.n_go( node );
//	}
	
//	n_end { arg node;
//		if( node === synth, {
//			trigResp.remove;
//			startPos		= this.getCurrentPos;
//			currentPosT	= nil;
//		});
//		^super.n_end( node );
//	}

	protDuplicate { arg dup;
		dup.setSpeed( this.getSpeed );
		dup.setChannelMap( this.getChannelMap );
		dup.setPath( this.getPath );
		dup.setLoop( this.getLoop );
		dup.prSetCues( cues );
		dup.setCueIndex( this.getCueIndex );
		dup.setStartPos( this.getCurrentPos );
		^super.protDuplicate( dup );
	}
}