/**
 *	NuagesPitchUnit
 *	(Wolkenpumpe)
 *
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: GeneratorUnit, TypeSafe
 *
 *	Changelog:
 *
 *	@version	0.15, 23-Jul-09
 *	@author	Hanns Holger Rutz
 */
NuagesUPitch : NuagesUFilterSynth {
 	var pitchAttr, timeDispAttr, pitchDispAttr;
	
	init {
		pitchAttr     = this.protMakeAttr( \pitch,     ControlSpec( 0.125, 4, \exp, default: 1.0 ));
		timeDispAttr  = this.protMakeAttr( \timeDisp,  ControlSpec( 0.01, 1, \exp, default: 0.1 ));
		pitchDispAttr = this.protMakeAttr( \pitchDisp, ControlSpec( 0.01, 1, \exp, default: 0.1 ));
	}

	protMakeDef { arg defName, numInChannels, numChannels;
		^SynthDef( defName, { arg in;
			var ins, flt, grainSize = 0.5, pitch, timeDisp, pitchDisp;

			pitch	= pitchAttr.kr;
			timeDisp	= timeDispAttr.kr;
			pitchDisp	= pitchDispAttr.kr;
			ins		= this.protCreateIn( in, numInChannels, numChannels );
			flt		= PitchShift.ar(
				ins, 
				grainSize, 		
				pitch,				// nominal pitch rate = 1
				pitchDisp, 			// pitch dispersion
				timeDisp * grainSize	// time dispersion
			);
			
			this.protCreateXOut( flt, numChannels );
		});
	}
}