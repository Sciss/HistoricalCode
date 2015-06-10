/**
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: GeneratorUnit, UpdateListener, TypeSafe
 *
 *	Changelog:
 *
 *	@version	0.14, 26-Aug-09
 *	@author	Hanns Holger Rutz
 *
 *	@todo	laufende aufnahme ermoeglichen, die "hinter" dem loop laeuft (bei speed == 1)
 *	@todo	cmdPeriod -> clear bufUses, clear recSynth
 *	@todo	moeglichkeit fuer PingProc, setRatioStart und setRatioDur aufzurufen, ohne
 *			dass die werte zu vorschnell korrigiert werden
 */
NuagesUFragment : NuagesUFilterSynth {
	classvar debug 	= false;

	var recSynth		= nil;
	
	var <numFrames;
	var speedAttr, grainAttr, feedBackAttr;
		
	init {
		speedAttr    = this.protMakeAttr( \speed,    ControlSpec( 0.125, 2.3511, \exp, default: 1.0 ));
		grainAttr    = this.protMakeAttr( \grain,    ControlSpec( 0, 1, \lin, default: 0.5 ));
		feedBackAttr = this.protMakeAttr( \feedBack, ControlSpec( 0, 1, \lin, default: 0.0 ));
		this.setDuration( 4.0 );
	}

	setDuration { arg dur;
		^this.setNumFrames( (dur * server.sampleRate).asInteger );
	}
	
	setNumFrames { arg frames;
		numFrames = frames;
if( debug, { TypeSafe.methodInform( thisMethod, "numFrames = "++numFrames ); });
	}
	
//	getNumFrames {
//		^numFrames;
//	}
	
	protCreateBuffersToBundle { arg bndl, synth, numInChannels, numChannels;
		var buf, recDefName, recSynth, inBus;
		
		inBus = this.getAudioInputBus;
		
		buf	= Buffer.new( server, numFrames, numInChannels );
		bndl.addPrepare( buf.allocMsg );
		bndl.add( synth.setMsg( \aInBuf, buf.bufnum ));
		if( recSynth.notNil, { this.protRemoveNode( recSynth )}); // XXX
		recDefName	= ("nuages-fragment" ++ numInChannels ++ "rec").asSymbol;
		recSynth		= Synth.basicNew( recDefName, server );
		this.protNewSynthToBundle( bndl, recSynth, { arg recDefName; this.protMakeRecDef( recDefName, numInChannels )});
		
		bndl.add( recSynth.newMsg( target, [ \aInBuf, buf.bufnum, \in, inBus.index ], \addToHead ));

		^buf;
	}

	protMakeDef { arg defName, numInChannels, numChannels;
		^SynthDef.new( defName, { arg aInBuf;
			var play, speed;

			speed	= speedAttr.kr;
			play		= PlayBuf.ar( numInChannels, aInBuf, speed, loop: 1 );
			this.protCreateXOut( play, numChannels );
		});
	}
	
	protMakeRecDef { arg defName, numInChannels;
		^SynthDef.new( defName, { arg aInBuf, in;
			var env, recLevel, preLevel, gate, off, dur, trig, white, lFade, fadeIn, fadeOut, inp, play, run, gaga;
			var minDur, maxDur, fade, rec, pre, feedBack, grain;

			feedBack	= feedBackAttr.kr( lag: 0.1 );
			grain	= grainAttr.kr( lag: 0.1 );
// rec = 1; pre = 0; fade = 2; minDur = 0.5; maxDur = 0.5;
			maxDur	= LinExp.kr( grain, 0, 0.5, 0.01, 1.0 );
			minDur	= LinExp.kr( grain, 0.5, 1, 0.01, 1.0 );
			fade		= LinExp.kr( grain, 0, 1, 0.25, 4 );
			rec		= (1 - feedBack).sqrt;
			pre		= feedBack.sqrt;
		
			inp		= In.ar( in, numInChannels ); // crucial to be placed before the replaceout!!
			trig		= LocalIn.kr( 1 );
		//	white	= Latch.kr( WhiteNoise.kr( 0.5, 0.5 ), trig );
			white	= TRand.kr( 0, 1, trig );
			dur		= LinExp.kr( white, 0, 1, minDur, maxDur );
			off		= BufFrames.kr( aInBuf ) * white;
			off		= off - (off % 1.0);
//Poll.kr( Impulse.kr( 1 ), dur, "dur" );
//Poll.kr( Impulse.kr( 1 ), off, "off" );
//Poll.kr( trig, trig, "trig" );
			gate		= trig;
//gate = Impulse.kr( 1 );
//gate = Impulse.kr( 1 );
			lFade	= Latch.kr( fade, trig );
			fadeIn	= lFade * 0.05;
			fadeOut	= lFade * 0.15;
//				env 		= EnvGen.ar( Env.linen( fadeIn, dur, fadeOut, 1, \sin ), gate, doneAction: 0 );
			env 		= EnvGen.ar( Env.linen( fadeIn, dur, fadeOut, 0.999, \sin ), gate, doneAction: 0 );
//				env 		= EnvGen.ar( Env.linen( fadeIn, dur, fadeOut, 0.999, \lin ), gate, doneAction: 0 );
			recLevel = env.sqrt;
			preLevel = (0.999 - env).sqrt;
//recLevel = env;
//preLevel = (1 - env);
recLevel = recLevel * rec;
preLevel = preLevel * (1 - pre) + pre;
//Poll.kr( Impulse.kr( 4 ), preLevel, "pre" );
//preLevel = 0;
run = recLevel > 0;
			RecordBuf.ar( inp, aInBuf, off, recLevel, preLevel, run, 1 );
gaga = 0; // gaga = ControlRate.ir.reciprocal;
//				LocalOut.kr( Impulse.kr( 1.0 / (dur + fadeIn + fadeOut + gaga ).max( 0.01 )));
			LocalOut.kr( Impulse.kr( 1.0 / (dur + fadeIn + fadeOut ).max( 0.01 )));
		});
	}
}