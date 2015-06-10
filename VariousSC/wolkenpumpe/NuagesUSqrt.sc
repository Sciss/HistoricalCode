/**
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: GeneratorUnit, TypeSafe
 *
 *	Changelog:
 *
 *	@version	0.13, 23-Jul-09
 *	@author	Hanns Holger Rutz
 */
NuagesUSqrt : NuagesUFilterSynth {
	protMakeDef { arg defName, numInChannels, numChannels;
		^SynthDef( defName, { arg in;
			var inp, flt;
			inp		= this.protCreateIn( in, numInChannels, numChannels );
			flt		= inp.abs.sqrt;
			flt		= LeakDC.ar( flt ) * Amplitude.kr( flt );

			this.protCreateXOut( flt, numChannels );
		});
	}
}