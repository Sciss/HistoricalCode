/**
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: GeneratorUnit, UpdateListener, TypeSafe
 *
 *	ZZZ BUFFER CRASH TEST
 *
 *	Changelog:
 *
 *	@version	0.18, 26-Aug-09
 *	@author	Hanns Holger Rutz
 *
 *	@todo	laufende aufnahme ermoeglichen, die "hinter" dem loop laeuft (bei rate == 1)
 *	@todo	cmdPeriod -> clear bufUses, clear recSynth
 *	@todo	moeglichkeit fuer PingProc, setRatioStart und setRatioDur aufzurufen, ohne
 *			dass die werte zu vorschnell korrigiert werden
 */
NuagesULoop : NuagesUGenAudioSynth {
	classvar debug 	= false;

	var buf			= nil;
	
	var <numFrames;
	var minFrames, maxFrames;
	var speed			= 1.0;
	
	var reuseBuf		= nil;
	var startFrame	= 0;
	var nowNumFrames;
	
//	var recDefName;
	var recSynth;
	var recBuf;
	var recDoneAction;
	var useRecBuf		= false;
	var recBufFrames	= 0;
	
	var wouldLikeStartFrame, wouldLikeNumFrames;
	var prefNumOutChannels = 2;
	
	classvar bufUses;
	
	*initClass {
		bufUses = Dictionary.new;	// Buffer -> use count
	}

	*new { arg server;
		^super.new( server ).prInitLoop;
	}
	
	// detector responder
	isRecorder { ^true }
	
	*debugPrintBufUses {
		bufUses.keysValuesDo({ arg key, value; ("buf = "++key++"; use = "++value).inform; });
	}
	
	prInitLoop {
	
// XXX
		this.protPrependAttr(
			UnitAttr( \speed, ControlSpec( 0.125, 2.3511, \exp ), \normal, \getSpeed, \setSpeed, nil ),
			UnitAttr( \ratioStart, ControlSpec( 0, 1, \lin ),     \normal, \getRatioStart, \setRatioStart, nil, false ),
			UnitAttr( \ratioDur, ControlSpec( 0, 1, \lin ),       \normal, \getRatioDur, \setRatioDur, nil, false ));

//		minFrames		= (server.sampleRate * 2).asInteger;
		minFrames		= (server.sampleRate * 4).asInteger;
	}

	setPreferredNumOutChannels { arg numCh;
		prefNumOutChannels = numCh;
	}
	
	setDuration { arg dur;
		^this.setNumFrames( (dur * server.sampleRate).asInteger );
	}
	
	setRatioStart { arg ratio;
		this.protMakeBundle({ arg bndl; this.setRatioStartToBundle( bndl, ratio )});
	}
	
	setRatioStartToBundle { arg bndl, ratio;
//("setRatioStart : "++ratio).postln;
		this.setStartFrameToBundle( bndl, ratio * maxFrames );
		this.tryChanged( \attrUpdate, \ratioStart );
	}

	setRatioDur { arg ratio;
		this.protMakeBundle({ arg bndl; this.setRatioDurToBundle( bndl, ratio )});
	}
	
	setRatioDurToBundle { arg bndl, ratio;
//("setRatioDur : "++ratio).postln;
		this.setNowNumFramesToBundle( bndl, ratio * (maxFrames - startFrame) ); // minus startFrame !!
		this.tryChanged( \attrUpdate, \ratioDur );
	}
	
	getRatioStart {
		^(startFrame / maxFrames;)
	}
	
	getRatioDur {
		^(nowNumFrames / (maxFrames - startFrame)); // minus startFrame !!
	}
	
	setStartFrame { arg frame;
		this.protMakeBundle({ arg bndl; this.setStartFrameToBundle( bndl, frame )});
	}
	
	setStartFrameToBundle { arg bndl, frame;
//		var updated = false;
		startFrame = frame;
		wouldLikeStartFrame = startFrame;
		startFrame = startFrame.clip( 0, maxFrames - minFrames );
		nowNumFrames = (wouldLikeNumFrames ?? nowNumFrames ?? 0).clip( minFrames, maxFrames - startFrame );
		if( synth.notNil, {
			bndl.add( synth.setMsg( \startFrame, startFrame, \numFrames, nowNumFrames ));
		});
//("startFrame : "++startFrame).postln;
//		if( updated, {
//			this.changed( \attrUpdate, \ratioStart );
//			this.changed( \attrUpdate, \ratioDur );
//		});
	}
	
	getStartFrame {
		^startFrame;
	}
	
	setNowNumFrames { arg frames;
		this.protMakeBundle({ arg bndl; this.setNowNumFramesToBundle( bndl, frames )});
	}
	
	setNowNumFramesToBundle { arg bndl, frames;
//		var updated = false;
		nowNumFrames = frames;
		wouldLikeNumFrames = nowNumFrames;
		nowNumFrames = nowNumFrames.clip( minFrames, maxFrames );
		startFrame = (wouldLikeStartFrame ?? startFrame ?? 0).clip( 0, maxFrames - nowNumFrames );
if( debug, { TypeSafe.methodInform( thisMethod, "nowNumFrames = "++nowNumFrames++"; startFrame = "++startFrame )});
		if( synth.notNil, {
if( debug, { TypeSafe.methodInform( thisMethod, "synth.set( \\startFrame, "++startFrame++", \\numFrames, "++nowNumFrames++" );" )});
			bndl.add( synth.setMsg( \startFrame, startFrame, \numFrames, nowNumFrames ));
		});
//("nowNumFrames : "++nowNumFrames).postln;
//		if( updated, {
//			this.changed( \attrUpdate, \ratioStart );
//			this.changed( \attrUpdate, \ratioDur );
//		});
	}
	
	getNowNumFrames {
		^nowNumFrames;
	}

	setNumFrames { arg frames;
		numFrames = max( minFrames, frames );
		maxFrames = numFrames;
if( debug, { TypeSafe.methodInform( thisMethod, "numFrames = "++numFrames ); });
//		if( numChannels.notNil, {
//			this.allocBuffer;
//		});
	}
	
//	setNumChannels { arg chan;
//		[ "Juchuuuuuu", chan ];
//		numChannels 	= chan;
////		this.protCacheDef( numChannels );
////		defName		= ("loopUnit" ++ numChannels).asSymbol;
////		recDefName	= ("loopUnit" ++ numChannels ++ "Rec").asSymbol;
////if( debug, { TypeSafe.methodInform( thisMethod, "numChannels = "++numChannels++"; defName = '"++defName++"'; recDefName = '"+recDefName+"'" ); });
//		if( numFrames.notNil, {
//			this.allocBuffer;	// XXX
//		});
//	}
	
//	numAudioInputs { ^1 }
//	numVisibleAudioInputs { ^0 }
	
	getNumFrames {
		^numFrames;
	}
	
//	/**
//	 *	This method can be called prior to play in order
//	 *	to have the buffer creation ready and decrease latency
//	 */
//	allocBuffer {
//		if( buf.isNil, {
//			if( numFrames.notNil && numChannels.notNil, {
//				buf	= Buffer.new( server, numFrames, numChannels );
//// ZZZ
////				this.protAddGlobalBuffer( buf );
//				buf.alloc;
//				bufUses.put( buf, 1 );
//if( debug, { TypeSafe.methodInform( thisMethod, "buf.isNil -> buf = "++buf ); });
//			}, {
//				TypeSafe.methodWarn( thisMethod, "# of frames and channel have not been specified" );
//			});
//		}, {
//if( debug, { TypeSafe.methodInform( thisMethod, "buf.notNil -> buf = "++buf ); });
//		});
//	}

	prSetBuffer { arg argBuf;
		buf			= argBuf;
//		numChannels	= buf.numChannels;
//		numFrames		= buf.numFrames;
		bufUses.put( buf, (bufUses[ buf ] ?? 0) + 1 );
//		this.protCacheDef( numFrames );
if( debug, { TypeSafe.methodInform( thisMethod, "buf = "++buf++"; bufUses = "++bufUses[ buf ] ); });
		this.setNumFrames( buf.numFrames );
//		this.setNumChannels( buf.numChannels );
	}
	
	prSetMaxFrames { arg frames;
		maxFrames = frames;
	}
	
	prFreeBufferToBundle { arg bndl;
		var count;
		
		count = bufUses.at( buf ) - 1;
		
		if( count == 0, {
//			this.protRemoveGlobalBuffer( buf );
			bufUses.removeAt( buf );
if( debug, { TypeSafe.methodInform( thisMethod, "buf = "++buf ); });
			bndl.add( buf.freeMsg );	// carefull with this? XXX
			buf = nil;
		}, {
			bufUses.put( buf, count );
			if( verbose, { ("LoopPlayer: don't free buffer "++buf.object.bufnum++" ; useCount "++count).postln; });
if( debug, { TypeSafe.methodInform( thisMethod, "buf = "++buf++"; count = "++count ); });
		});
	}

	trashRecording {
//		[ "trashRecording" ].postln;
//		mBuf.put( \valid, false );
//		mBuf.object.free;
//		this.prFreeBuffer( mBuf.object );
//		if( verbose, { ("LoopPlayer.trashRecording : buf "++mBuf.object.bufnum++"; useCount "++
//			mBuf.object.at( \useCount )).postln; });
if( debug, { TypeSafe.methodInform( thisMethod, "recBuf = "++recBuf ); });
		recBuf.free;
		recBuf = nil;
		useRecBuf = false;
	}
	
	useRecording {
//		[ "useRecording", recBuf.notNil ].postln;
		if( recBuf.notNil, {
			useRecBuf = true;
if( debug, { TypeSafe.methodInform( thisMethod, "recBuf = "++recBuf ); });
		}, {
if( debug, { TypeSafe.methodInform( thisMethod, "recBuf.isNil" ); });
		});
	}

	// arg bus, startFrame = 0, numFrames, target, addAction = \addToHead, doneAction;

	startRecording { arg proc, numFrames, doneAction;
		var inBus, bndl, startTime, stopTime, updSource, defName, numChannels, inputNumChans;

		TypeSafe.checkArgClasses( thisMethod, [ proc, numFrames, doneAction ],
		                                      [ NuagesProc, Integer, AbstractFunction ],
		                                      [ false, true, false ]);

		numFrames	= numFrames ?? { this.getNumFrames };
		
//		inBus = this.getInputBus;
//		if( inBus.isNil, {
//			TypeSafe.methodError( thisMethod, "No record source specified" );
//			^false;
//		});

//		if( inBus.numChannels > numChannels, {
//			TypeSafe.methodError( thisMethod, "Incompatible # of channels (want " ++ numChannels ++ " but got "++inBus.numChannels++")" );
//			^false;
//		});

		inBus = proc.getAudioOutputBus;
//this.setInputBus( inBus );	// XXX
		if( inBus.isNil, {
			TypeSafe.methodError( thisMethod, "Proc has no output bus" );
			^false;
		});
		
		// Es kann nur EINEN geben (EINE?)
		if( this.isRecording, {
			TypeSafe.methodWarn( thisMethod, "Already recording!" );
			^false;
		});

		bndl		= OSCBundle.new;
		numChannels = this.numOutChannels;
		recBuf	= Buffer.new( server, numFrames, numChannels );
		inputNumChans = min( inBus.numChannels, numChannels );
//[ "yo", inBus, inputNumChans ].postln;
		defName	= ("nuages-loop" ++ numChannels ++ "rec" ++ inputNumChans).asSymbol;
		recSynth	= Synth.basicNew( defName, server );
		this.protNewSynthToBundle( bndl, recSynth, { arg defName; SynthDef.new( defName, {
			arg aOutBuf, aInBus, startFrame = 0, numFrames;
			
			var rec, line, input, inputWrap;
			
			input	= In.ar( aInBus, inputNumChans ).asArray;
			inputWrap	= Array.fill( numChannels, { arg ch; input[ ch % inputNumChans ]});
			rec		= RecordBuf.ar( inputArray: inputWrap, bufnum: aOutBuf,
								  offset: startFrame, loop: 0 );
			line		= Line.kr( 0.0, 0.0, numFrames / BufSampleRate.kr( aOutBuf ), doneAction: 2 );
		})});
//		recSynth	= Synth.basicNew( recDefName, server );
		bndl.addPrepare( recBuf.allocMsg( recSynth.newMsg( proc.group,
			[ \aOutBuf, recBuf.bufnum, \aInBus, inBus.index, \startFrame, 0,
			  \numFrames, numFrames ], \addAfter );
		));

//recSynth.defName.postcs;

//		nw.register( recSynth );
		UpdateListener.newFor( recSynth, { arg upd, synth, what;
			case { what == \n_end }
			{
				upd.remove;
				updSource.remove;
				stopTime = SystemClock.seconds - server.latency;
				if( verbose, { ("LoopSamplerRec.n_end : " ++ synth.nodeID ++ "; recorded time " ++
						(stopTime - startTime)).postln; });
// useCounts remains at 1 to be consistent with prepareCrossFade !
//				recBuf.put( \useCount, 0 );
				recBufFrames = min( numFrames, (max( 1, stopTime - startTime ) * server.sampleRate).asInteger );
if( debug, { TypeSafe.methodInform( thisMethod, "n_end; stopTime = "++stopTime++"; recBufFrames = "++recBufFrames ); });
				recDoneAction.value( recBufFrames );
				recSynth = nil;
//				recBuf = nil;
				this.tryChanged( \unitRecording, false );
			}
			{ what == \n_go }
			{
				if( verbose, { ("NuagesLoopUnit->startRecording.n_go : " ++ synth.nodeID).postln; });
				startTime = SystemClock.seconds;
if( debug, { TypeSafe.methodInform( thisMethod, "n_go; startTime = "++startTime ); });
			};
		});

		if( verbose, { ("NuagesLoopUnit->startRecording : starting to write bus " ++ inBus.index ++
			" to buffer " ++ recBuf.bufnum).postln; });

if( debug, { TypeSafe.methodInform( thisMethod, "recBuf = "++recBuf++"; inBus = "++inBus++"; numFrames = "++numFrames++"; recSynth = "++recSynth ); });

		recDoneAction = doneAction;

		bndl.send( server );
//		server.listSendBundle( nil, bndl );
		
		useRecBuf = false;
		
		// stop recording when source is disposed
		updSource = UpdateListener.newFor( proc, { arg upd;
			upd.remove;
			recSynth.free;
		}, \disposed );
		
		this.tryChanged( \unitRecording, true );
		
		^true;
	}
	
	cancelRecording {
if( debug, { TypeSafe.methodInform( thisMethod, "" ); });
		recDoneAction = { this.trashRecording };
		^this.stopRecording;
	}

	stopRecording {
		if( recSynth.isNil.not, {
if( debug, { TypeSafe.methodInform( thisMethod, "recSynth = "++recSynth )});
			recSynth.free;
			^true;
		}, {
if( debug, { TypeSafe.methodInform( thisMethod, "recSynth.isNil" )});
			^false;
		});
	}

	isRecording {
		^recSynth.isNil.not;
	}

	setSpeed { arg factor;
		this.protMakeBundle({ arg bndl; this.setSpeedToBundle( bndl, factor )});
	}
	
	setSpeedToBundle { arg bndl, factor;
		if( factor != speed, {
			speed = factor;
			if( synth.notNil, {
				synth.set( \rate, speed );
			});
			this.tryChanged( \attrUpdate, \speed );
		});
	}
	
	getSpeed {
		^speed;
	}

//	cmdPeriod {
//		var result;
//		
//		result		= super.cmdPeriod;
////		synth		= nil;
//		recSynth		= nil;
//		buf			= nil;
//		^result;
//	}

	protPreferredNumInChannels { arg idx; ^if( buf.isNil, prefNumOutChannels, { buf.numChannels })}
	protPreferredNumOutChannels { arg idx; ^if( buf.isNil, prefNumOutChannels, { buf.numChannels })}
	
	protSetAttrToBundle { arg bndl, synth;
		if( buf.isNil, {
			buf	= Buffer.new( server, numFrames, this.numOutChannels );
			bufUses.put( buf, 1 );
			bndl.addPrepare( buf.allocMsg );
		});
		if( startFrame.isNil, {
			TypeSafe.methodWarn( thisMethod, "Unspecified start frame" );
			startFrame = 0;
		});
		if( nowNumFrames.isNil, {
			TypeSafe.methodWarn( thisMethod, "Unspecified number of frames" );
			nowNumFrames = maxFrames - startFrame;
		});
		if( nowNumFrames < minFrames, {
			TypeSafe.methodWarn( thisMethod, "numFrames too small. adjusting" );
			nowNumFrames = minFrames;
			startFrame = min( startFrame, maxFrames - nowNumFrames );
		});
		if( startFrame + nowNumFrames > maxFrames, {
			TypeSafe.methodWarn( thisMethod, "numFrames too big. adjusting" );
			startFrame	= max( 0, maxFrames - nowNumFrames );
			nowNumFrames	= min( nowNumFrames, maxFrames );
		});
		bndl.add( synth.setMsg( \aInBuf, buf.bufnum, \rate, speed, \startFrame, startFrame, \numFrames, nowNumFrames ));
	}

//	playToBundle { arg bndl;
//		var outBus, allocMsg, defName;
//
//		if( playing, {
//			this.stopToBundle( bndl );
//			synth = nil;
//		});
//
//		defName	= this.protCreateDefName( numChannels );
//		synth	= Synth.basicNew( defName, server );
//		this.protNewSynthToBundle( bndl, synth, { arg defName; this.protMakeDef( defName, numChannels )});
//
//		if( buf.isNil, {
//			buf	= Buffer.new( server, numFrames, numChannels );
//// ZZZ
////			this.protAddGlobalBuffer( buf );
////			allocMsg = buf.allocMsg( synth.runMsg( true ));
//			allocMsg = buf.allocMsg;
//			bufUses.put( buf, 1 );
//		});
//				
//		if( startFrame.isNil, {
//			TypeSafe.methodWarn( thisMethod, "Unspecified start frame" );
//			startFrame = 0;
//		});
//		if( nowNumFrames.isNil, {
//			TypeSafe.methodWarn( thisMethod, "Unspecified number of frames" );
//			nowNumFrames = maxFrames - startFrame;
//		});
//		if( nowNumFrames < minFrames, {
//			TypeSafe.methodWarn( thisMethod, "numFrames too small. adjusting" );
//			nowNumFrames = minFrames;
//			startFrame = min( startFrame, maxFrames - nowNumFrames );
//		});
//		if( startFrame + nowNumFrames > maxFrames, {
//			TypeSafe.methodWarn( thisMethod, "numFrames too big. adjusting" );
//			startFrame	= max( 0, maxFrames - nowNumFrames );
//			nowNumFrames	= min( nowNumFrames, maxFrames );
//		});
//		
//		outBus = this.getAudioOutputBus;
//		bndl.add( synth.newMsg( target, [ \aInBuf, buf.bufnum, \rate, speed, \out,
//			outBus.notNil.if({ outBus.index }, 0 ), \startFrame, startFrame, \numFrames, nowNumFrames ],
//				\addToHead ));
//		if( allocMsg.notNil, {
////			bndl.add( synth.runMsg( false ));
////			bndl.add( allocMsg );
//			bndl.addPrepare( allocMsg );
//		});
//
//		UpdateListener.newFor( synth, { arg upd, node;
//			upd.remove;
//			if( node === synth, {
//				synth = nil;
//				this.protSetPlaying( false );
//			});
//		}, \n_end );
//
//		this.protSetPlaying( true );
//
//if( debug, { TypeSafe.methodInform( thisMethod, "startFrame = "++startFrame++"; nowNumFrames = "++nowNumFrames++"; outBus = "++outBus++"; synth = "++synth++"; buf = "++buf++"; allocMsg = "++allocMsg ); });
//	}

	disposeToBundle { arg bndl ... rest;
		var result;
		result = super.disposeToBundle( bndl, *rest );
		if( recSynth.notNil, {
			nw.unregister( recSynth );
if( debug, { TypeSafe.methodInform( thisMethod, "recSynth = "++recSynth ); });
			bndl.add( recSynth.freeMsg );
			bndl.add( recBuf.freeMsg );  // carefull with this? XXX
		}, {
if( debug, { TypeSafe.methodInform( thisMethod, "recSynth.isNil" ); });
		});
		this.prFreeBufferToBundle( bndl );
		^result;
	}

	// ----------- protected instance methods -----------
	
//	protCacheDef { arg numChannels;
//		var defName, def;
//
////		defName = ("loopUnit" ++ numChannels).asSymbol;
////		if( defCache.contains( defName ).not, {
////			def = this.class.prMakeDef( defName, numChannels );
////			defCache.add( def );
////		});
////
//		(1..numChannels).do({ arg inputNumChans;
//			defName = ("nuages-loop" ++ numChannels ++ "rec" ++ inputNumChans ).asSymbol;
//			if( defCache.contains( defName ).not, {
//				def = SynthDef.new( defName, {
//					arg aOutBuf, aInBus, startFrame = 0, numFrames;
//					
//					var rec, line, input, inputWrap;
//					
//					input	= In.ar( bus: aInBus, numChannels: inputNumChans ).asArray;
//					inputWrap	= Array.fill( numChannels, { arg ch; input[ ch % inputNumChans ]});
//					rec		= RecordBuf.ar( inputArray: inputWrap, bufnum: aOutBuf,
//										  offset: startFrame, loop: 0 );
//					line		= Line.kr( 0.0, 0.0, numFrames / BufSampleRate.kr( aOutBuf ), doneAction: 2 );
//				});
//				defCache.add( def );
//			});
//		});
//		
//		^super.protCacheDef( numChannels );
//	}
	
	protMakeDef { arg defName, numInChannels, numChannels;
		^SynthDef.new( defName, { arg out, aInBuf, rate = 1.0, startFrame = 0, numFrames;
			var	lOffset, gate1, gate2, lLength, play1, play2, trig1, trig2, duration,
				dlyDur, env, amp1, amp2, output, gateTrig1, gateTrig2;
			
			trig1	= LocalIn.kr( 1 ); // LFPulse.kr( freq: LocalIn.kr( 1 ).max( 0.1 ));
			gateTrig1	= PulseDivider.kr( trig: trig1, div: 2, start: 1 );
			gateTrig2	= PulseDivider.kr( trig: trig1, div: 2, start: 0 );
			lOffset	= Latch.kr( in: startFrame, trig: trig1 );
			lLength	= Latch.kr( in: numFrames, trig: trig1 );
			duration	= lLength / (rate * SampleRate.ir) - 2;
			gate1	= Trig1.kr( in: gateTrig1, dur: duration );
			env		= Env.asr( 2, 1, 2, \lin );	// \sin
			
			play1	= PlayBuf.ar( numChannels: numChannels, bufnum: aInBuf, rate: rate, loop: 0,
								trigger: gateTrig1, startPos: lOffset );
			play2	= PlayBuf.ar( numChannels: numChannels, bufnum: aInBuf, rate: rate, loop: 0,
								trigger: gateTrig2, startPos: lOffset );
			amp1		= EnvGen.kr( env, gate1, 0.999 );  // 0.999 = bug fix !!!
			amp2		= 1.0 - amp1.squared;
			amp1		= 1.0 - amp1;
			amp1		= 1.0 - amp1.squared;
			output	= (play1 * amp1) + (play2 * amp2);

			Out.ar( out, output );
		
			LocalOut.kr( Impulse.kr( 1.0 / duration.max( 0.1 )));
		});
	}

//	n_end { arg node;
//		if( node === synth, {
//			synth		= nil;
//			this.protSetPlaying( false );
//		});
//		^super.n_end( node );
//	}

	protDuplicate { arg dup;
//		[ "useRecBuf", useRecBuf, "recBuf.notNil", recBuf.notNil ].postln;
	
		if( useRecBuf and: { recBuf.notNil }, {
if( debug, { TypeSafe.methodInform( thisMethod, "recBuf = "++recBuf++"; recBufFrames = "++recBufFrames++"; startFrame = "++this.getStartFrame  );});
			dup.prSetBuffer( recBuf );
			dup.prSetMaxFrames( recBufFrames );
			recBuf		= nil;
			useRecBuf		= false;
			dup.setNowNumFrames( recBufFrames );
//			dup.setInputBus( this.getInputBus ); // ???
		}, {
if( debug, { TypeSafe.methodInform( thisMethod, "buf = "++buf++"; nowNumFrames = "++this.getNowNumFrames++"; startFrame = "++this.getStartFrame );});
			if( buf.notNil, { dup.prSetBuffer( buf )});
			dup.prSetMaxFrames( maxFrames );
//			dup.setNowNumFrames( this.getNowNumFrames );
//			dup.setNumChannels( numChannels );
		});
//		dup.setStartFrame( this.getStartFrame );
		dup.setRatioStart( this.getRatioStart );
		dup.setRatioDur( this.getRatioDur );
		dup.setSpeed( this.getSpeed );
		^super.protDuplicate( dup );
	}
}