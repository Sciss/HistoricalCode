/**
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: GeneratorUnit, TypeSafe
 *
 *	Changelog:
 *
 *	@version	0.14, 23-Jul-09
 *	@author	Hanns Holger Rutz
 */
NuagesURenoise : NuagesUFilterSynth {
	var colorAttr;
	
	init {
		colorAttr = this.protMakeAttr( \color, ControlSpec( 0, 1, \lin, default: 0.0 ));
	}
	
	// XXX could handle numChannels > numInChannels nicer
	protMakeDef { arg defName, numInChannels, numChannels;
		^SynthDef( defName, { arg in;
			var filt, freq2, inp, sig, freqs, step, amp, w1, w2, noise, color;
			
			color	= colorAttr.kr( lag: 0.1 );
			inp		= this.protCreateIn( in, numInChannels, numChannels );
			step		= 0.5; // 0.25;
			freqs	= Array.geom( 40, 32, pow( 2, step )).select({ arg f; f <= 16000 }); // .keep( 50.div( numChannels ));
//			[ "NumFreqs", freqs.size ].postln;
			sig		= 0;
			noise	= WhiteNoise.ar;
			freqs.do({ arg freq;
				filt = BPF.ar( inp, freq, step );
				freq2 = ZeroCrossing.ar( filt );
				w1 = Amplitude.kr( filt );
				w2 = w1 * color;
				w1 = w1 * (1 - color);
				sig = sig + BPF.ar( (noise * w1) + LFPulse.ar( freq2, mul: w2 ), freq, step );
			});
			amp = /* amp * */ step.reciprocal; // compensate for Q
			sig = sig * amp;

			this.protCreateXOut( sig, numChannels );
		});
	}
}