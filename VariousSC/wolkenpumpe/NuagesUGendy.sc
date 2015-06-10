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
NuagesUGendy : NuagesUFilterSynth {
	var amountAttr;
	
	init {
		amountAttr = this.protMakeAttr( \amount, ControlSpec( 0, 1, \lin, default: 1.0 ));
	}
	
	protMakeDef { arg defName, numInChannels, numChannels;
		^SynthDef( defName, { arg in;
			var ins, outs, flt, laggo, minFreq, scale, amount;
			
			amount	= amountAttr.kr( lag: 0.1 );
			ins		= this.protCreateIn( in, numInChannels, extend: numChannels );
			laggo	= amount; // Lag.kr( amount );
			minFreq	= laggo * 69 + 12;
			scale	= laggo * 13 + 0.146;
			outs		= Gendy1.ar( 2, 3, 1, 1,
						minfreq: minFreq, maxfreq: minFreq * 8,
						ampscale: scale, durscale: scale,
						initCPs: 7, mul: ins );
			flt		= Compander.ar( outs, outs, 0.7, 1, 0.1, 0.001, 0.02 );
			this.protCreateXOut( flt );
		});
	}
}