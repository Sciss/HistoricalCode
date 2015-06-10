/**
 *	AmpMedian
 *	(Amplifikation)
 *
 *	(C)opyright 2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.10, 22-Aug-09
 *	@author	Hanns Holger Rutz
 */
AmpMedian : AmpProcess {
	classvar <>verbose = true;
	classvar <>n = 6;
	
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
			def = SynthDef( defName, { arg bus, i_fadeIn = 1, gate = 1, mixFreq = 0.1, minMix = 0.5;
				var sig, freq, pulse, flt, mix;
				sig		= In.ar( bus, 16 );
				// (* 0.5 + 0.5) * (1 - minMix) + minMix
				mix		= LFNoise1.kr( mixFreq ).linlin( -1, 1, minMix, 1 );
				flt		= Median.ar( n, sig );
				mix		= mix * EnvGen.kr( Env.asr( i_fadeIn ), gate, doneAction: 2 ) * (freq / 20).min( 1 );
//				XOut.ar( bus, mix, flt );
				ReplaceOur.ar( bus, (sig * (1 - mix)) + (flt * mix) );
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