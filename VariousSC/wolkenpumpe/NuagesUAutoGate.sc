/**
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: GeneratorUnit, TypeSafe
 *
 *	Changelog:
 *
 *	@version	0.15, 26-Aug-09
 *	@author	Hanns Holger Rutz
 */
NuagesUAutoGate : NuagesUFilterSynth {
	var amountAttr;
	
	init {
		amountAttr = this.protMakeAttr( \amount, ControlSpec( 0, 1, \lin, default: 1.0 ));
	}

	protMakeDef { arg defName, numInChannels, numChannels;
		^SynthDef( defName, { arg in;
			var inp, cmp, flt, amount;

			amount	= amountAttr.kr( lag: 0.1 );
			inp		= this.protCreateIn( in, numInChannels, numChannels );
			flt		= Array.fill( numChannels, { arg ch;
				Compander.ar( inp[ ch ], inp[ ch ], Amplitude.kr( inp[ ch ] * (1 - amount) * 5 ), 20, 1, 0.01, 0.001 );
			});

			this.protCreateXOut( flt, numChannels );
		});
	}
}