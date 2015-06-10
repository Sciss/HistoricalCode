/**
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: GeneratorUnit, TypeSafe
 *
 *	Changelog:
 *
 *	@version	0.10, 26-Aug-09
 *	@author	Hanns Holger Rutz
 */
NuagesUHilbert : NuagesUFilterSynth {
	// ----------- public instance methods -----------

	numAudioInputs { ^2 }
	getAudioInputName { arg idx; ^if( idx == 1, \mod )}

	// ----------- protected instance methods -----------

	protMakeDef { arg defName, numInChannels, numChannels;
		var numInChan1, numInChan2;
		#numInChan1, numInChan2 = numInChannels;
		^SynthDef( defName, { arg in, in2;
			var inp1, inp2, flt, hlb, hlb2;
				
			inp1		= this.protCreateIn( in,  numInChan1, extend: numChannels );
			inp2		= this.protCreateIn( in2, numInChan2, extend: numChannels );

			flt		= Array.fill( numChannels, { arg ch;
				hlb		= Hilbert.ar( inp1[ ch ]);
				hlb2		= Hilbert.ar( Normalizer.ar( inp2[ ch ], dur: 0.02 ));
				(hlb[ 0 ] * hlb2[ 0 ]) - (hlb[ 1 ] * hlb2[ 1 ]);
			});

			this.protCreateXOut( flt, numChannels );
		});
	}
}