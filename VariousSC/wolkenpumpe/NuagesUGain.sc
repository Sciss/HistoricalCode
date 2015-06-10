/**
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: GeneratorUnit, TypeSafe
 *
 *	Changelog:
 *
 *	@version	0.13, 26-Aug-09
 *	@author	Hanns Holger Rutz
 */
NuagesUGain : NuagesUFilterSynth {
	var gainAttr;
	
	init {
		gainAttr = this.protMakeAttr( \gain, ControlSpec( -30, 30, 'linear', 0.0, 0, " dB"));
	}

	protMakeDef { arg defName, numInChannels, numChannels;
		^SynthDef( defName, { arg in;
			var inp, flt, gain;

			gain		= gainAttr.kr;
			inp		= this.protCreateIn( in, numInChannels, numChannels );
			flt		= inp * gain.dbamp;
			this.protCreateXOut( flt, numChannels );
		});
	}
}