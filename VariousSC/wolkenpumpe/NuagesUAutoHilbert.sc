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
NuagesUAutoHilbert : NuagesUFilterSynth {
	protMakeDef { arg defName, numInChannels, numChannels;
		^SynthDef( defName, { arg in;
			var inp, hlb, hlb2, flt;

			inp		= this.protCreateIn( in, numInChannels, numChannels );
			flt		= Array.fill( numChannels, { arg ch;
				hlb		= Hilbert.ar( DelayN.ar( inp[ ch ], 0.01, 0.01 ));
				hlb2		= Hilbert.ar( Normalizer.ar( inp[ ch ], dur: 0.02 ));
				(hlb[ 0 ] * hlb2[ 0 ]) - (hlb[ 1 ] * hlb2[ 1 ]);
			});

			this.protCreateXOut( flt, numChannels );
		});
	}
}