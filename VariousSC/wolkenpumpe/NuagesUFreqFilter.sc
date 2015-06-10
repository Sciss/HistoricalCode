/**
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: GeneratorUnit, TypeSafe
 *
 *	Changelog:
 *
 *	@version	0.14, 26-Aug-09
 *	@author	Hanns Holger Rutz
 */
NuagesUFreqFilter : NuagesUFilterSynth {
	var freqAttr;
	
	init {
		freqAttr = this.protMakeAttr( \freq, ControlSpec( -1, 1, \lin, default: 0.54 ));
	}

	protMakeDef { arg defName, numInChannels, numChannels;
		^SynthDef( defName, { arg in;
			var inp, dry, dryMix, lowFreqN, highFreqN, lowFreq, lowMix, highFreq, highMix, lpf, hpf, flt, normFreq;

			inp			= this.protCreateIn( in, numInChannels, numChannels );

			normFreq		= freqAttr.kr;
			lowFreqN		= Lag.kr( normFreq.clip( -1.0, 0.0 ));
			highFreqN		= Lag.kr( normFreq.clip( 0.0, 1.0 ));
			
			lowFreq		= LinExp.kr( lowFreqN, -1, 0, 30, 20000 );
			highFreq		= LinExp.kr( highFreqN, 0, 1, 30, 20000 );

			lowMix		= (lowFreqN * -10.0).clip( 0, 1 );
			highMix		= (highFreqN * 10.0).clip( 0, 1 );
			dryMix		= 1 - (lowMix + highMix);
			
			lpf			= LPF.ar( inp, lowFreq ) * lowMix;
			hpf			= HPF.ar( inp, highFreq ) * highMix;
			dry			= inp * dryMix;
			
			flt			= dry + lpf + hpf;
			
			this.protCreateXOut( flt, numChannels );
		});
	}
}