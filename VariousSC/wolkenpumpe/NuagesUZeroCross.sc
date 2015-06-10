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
NuagesUZero : NuagesUFilterSynth {
	var widthAttr, divAttr, lagAttr;
	
	init {
		widthAttr	= this.protMakeAttr( \width, ControlSpec( 0, 1, \lin, default: 0.5 ));
		divAttr	= this.protMakeAttr( \div,   ControlSpec( 1, 10, \lin, 1, default: 1 ));
		lagAttr	= this.protMakeAttr( \lag,   ControlSpec( 0.001, 0.1, \exp, default: 0.01 ));
	}

	protMakeDef { arg defName, numInChannels, numChannels;
		^SynthDef( defName, { arg in;
			var inp, flt, freq, pulse, amp, div, mix, width, lagTime;

//			amount	= amountAttr.kr( lag: 0.1 );
			inp		= this.protCreateIn( in, numInChannels, numChannels );
			freq		= ZeroCrossing.ar( inp ).max( 20 );
//			width	= 0.5; // Lag.kr( Demand.kr( dust, 0, Drand([ 0.125, 0.25, 0.5 ], inf )));
			width	= widthAttr.kr( lag: 0.1 ); // TIRand.kr( 2, 8, dust );
			amp		= width.sqrt;
			width	= width.reciprocal;
			div		= divAttr.kr( lag: 0.1 ); // Lag.kr( TIRand.kr( 1, 8, dust ), 1 );
			lagTime	= lagAttr.kr; // Lag.kr( TExpRand.kr( 0.01, 0.1, dust ), 1 );
//			amp		= Lag.kr( amp, 1 );
//			width	= Lag.kr( width, 1 );
			pulse	= Lag.ar( LFPulse.ar( freq / div, 0, width, amp ), lagTime );
			flt		= inp * pulse;
				
			this.protCreateXOut( flt, numChannels );
		});
	}
}