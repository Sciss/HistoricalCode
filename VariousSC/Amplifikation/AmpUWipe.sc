/**
 *	AmpUWipe
 *	(Amplifikation)
 *
 *	(C)opyright 2009 Hanns Holger Rutz. All rights reserved.
 *
 *	Changelog:
 *		28-Aug-09  created
 *
 *	@version	0.10, 28-Aug-09
 *	@author	Hanns Holger Rutz
 */
AmpUWipe : NuagesUFilterSynth {
	var smoothAttr, wipeAttr, prefNumOutChannels;
		
	*displayName { ^\Wipe }
	
	init {
		smoothAttr = this.protMakeAttr( \smooth, ControlSpec( 0.125, 8, \exp, default: 1 ));
		wipeAttr   = this.protMakeAttr( \wipe, ControlSpec( -1, 1, \lin, default: 0 ));
	}
	
//	protPreferredNumInChannels { ^16 }
//	hasPreferredNumInChannels { ^true }
	shouldConnectOutputFirst { ^true }

	setPreferredNumOutChannels { arg numCh;
		prefNumOutChannels = numCh;
	}
	
	protPreferredNumOutChannels { arg idx; ^if( prefNumOutChannels.notNil, prefNumOutChannels, {Êsuper.protPreferredNumOutChannels( idx )})}

	protMakeDef { arg defName, numInChannels, numChannels;
[ "WIPE", numInChannels, numChannels ].postln;
		^SynthDef( defName, { arg in;
			var inp, chans, smooth, wipe, amps, diff1, diff2, flt;

			inp			= this.protCreateIn( in, numInChannels, extend: numChannels );
			chans		= (0..(numChannels - 1));
			smooth		= smoothAttr.kr( lag: 0.1 );
			wipe			= wipeAttr.kr( lag: 0.1 );
//			amps			= 1 - ((chans - (wipe * numChannels)).abs * smooth).min( 1 );
//			diff			= (chans - (wipe * numChannels)) * smooth;
//			amps			= (1 - diff.clip( 0, 1 )) * (1 - diff.neg.clip( 0, 1 ));
			diff1		= (numChannels - chans.reverse - (wipe * numChannels)) * smooth;
			diff2		= (numChannels - chans + (wipe * numChannels)) * smooth;
			amps			=  diff1.clip( 0, 1 ) * diff2.clip( 0, 1 );
			
			flt			= inp * amps;
			
			this.protCreateXOut( flt, numChannels );
		});
	}

	protDuplicate { arg dup;
		dup.setPreferredNumOutChannels( prefNumOutChannels );
		^super.protDuplicate( dup );
	}
}