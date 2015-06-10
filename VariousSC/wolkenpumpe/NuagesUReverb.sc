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
NuagesUReverb : NuagesUFilterSynth {
	var extentAttr, colorAttr;

	init {
		extentAttr = this.protMakeAttr( \extent, ControlSpec( 0, 1, \lin, default: 0.5 ));
		colorAttr  = this.protMakeAttr( \color, ControlSpec( 0, 1, \lin, default: 0.5 ));
	}
	
	// XXX this could make things nicer for numChannels > numInChannels
	protMakeDef { arg defName, numInChannels, numChannels;
		^SynthDef( defName, { arg in;
			var inp, verb, spread, flt, off, verbInp, i_roomSize, i_revTime, color, extent;

//			extent		= extentAttr.kr;
			extent		= extentAttr.ir;
			color		= colorAttr.kr( lag: 0.1 );
//			i_roomSize	= Latch.kr( extent.linexp( 0, 1, 1, 100 ), Impulse.kr( 0 ));
			i_roomSize	= extent.linexp( 0, 1, 1, 100 );
			i_revTime		= extent.linexp( 0, 1, 0.3, 20 );
			inp			= this.protCreateIn( in, numInChannels, numChannels );
			spread		= 15;
			
			flt			= Array( numInChannels );
			off			= 0;
//[ "-------R1", extent ].postln;
			while({ off < numInChannels }, {
				verbInp = if( (off + 1) == numInChannels, {
					inp[ off ];
				}, {
					Mix([ inp[ off ], inp[ off + 1 ]]);
				});
//"verb args are %, %, %, %, %, %, %, %, %, %\n".postf( i_roomSize, i_revTime, color, color, spread, 0, 1, 0.7, i_roomSize, 0.3 );
				verb	= GVerb.ar( verbInp,
					i_roomSize,
					i_revTime,
					color, 
					color, 
					spread, 
					0,
					1, 
					0.7,
					i_roomSize, 0.3 );
//"for % verbInp is % and verb is %\n".postf( off, verbInp, verb );
				flt.add( verb[ 0 ]);
				if( (off + 1) < numInChannels, { flt.add( verb[ 1 ])});
				off = off + 2;
			});
//[ "-------R2", flt, numChannels ].postln;
			
			this.protCreateXOut( flt, numChannels );
//"-------R3".postln;
		});
	}
}