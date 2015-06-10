/**
 *	AmpZeroCross
 *	(Amplifikation)
 *
 *	(C)opyright 2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.11, 22-Aug-09
 *	@author	Hanns Holger Rutz
 */
AmpZeroCross : AmpProcess {
	classvar <>verbose = true;
	
	classvar <>minFadeTime = 10;
	classvar <>maxFadeTime = 60;
	
	// for the player
	var <procMinDur	= 60;
	var <procMaxDur	= 120;
	var <procMinPause	= 360;
	var <procMaxPause	= 540;
	
	*new {
		^super.new.prInit;
	}
	
	prInit {
	}
	
	prTaskBody { arg rmap;
		var bndl, synth, defName, def, dur;
		
		bndl			= this.newBundle;
		defName		= \ampZeroCross;
		synth		= Synth.basicNew( defName, amp.server );
		if( defWatcher.isOnline( defName ).not, {
			def = SynthDef( defName, { arg bus, dustFreq = 0.1, i_fadeIn = 1, gate = 1;
				var sig, freq, pulse, dust, amp, div, mix, width, lagTime;
				sig		= In.ar( bus, 16 );
				freq		= ZeroCrossing.ar( sig ).max( 20 );
				dust		= Dust.kr( dustFreq ) + Impulse.kr( 0 );
//				width	= 0.5; // Lag.kr( Demand.kr( dust, 0, Drand([ 0.125, 0.25, 0.5 ], inf )));
				width	= TIRand.kr( 2, 8, dust );
				amp		= width.sqrt;
				width	= width.reciprocal;
				div		= Lag.kr( TIRand.kr( 1, 8, dust ), 1 );
				lagTime	= Lag.kr( TExpRand.kr( 0.01, 0.1, dust ), 1 );
				amp		= Lag.kr( amp, 1 );
				width	= Lag.kr( width, 1 );
				pulse	= Lag.ar( LFPulse.ar( freq / div, 0, width, amp ), lagTime );
				mix		= EnvGen.kr( Env.asr( i_fadeIn ), gate, doneAction: 2 ) * (freq / 20).min( 1 );
				XOut.ar( bus, mix, sig * pulse );
			});
			defWatcher.sendToBundle( bndl, def, synth );
		});
		dur = exprand( minFadeTime, maxFadeTime );
		bndl.add( synth.newMsg( rmap.group, [ \bus, rmap.bus, \i_fadeIn, dur ]));
		bndl.send;

		while({Êrmap.keepRunning }, {
			1.1.wait;
		});
		
		// freeing nodes
		bndl = this.newBundle;		
		dur = if( rmap.stopQuickly, 1, { exprand( minFadeTime, maxFadeTime )});
		bndl.add( synth.releaseMsg( dur ));
		bndl.send;
		synth.waitForEnd;
	}
}