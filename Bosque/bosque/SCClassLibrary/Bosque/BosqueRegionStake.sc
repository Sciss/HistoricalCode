/**
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 *
 *	ABSTRACT CLASS!
 *
 *	@version	0.16, 06-Jul-08
 */
BosqueRegionStake : RegionStake {
	var <java;	// subclasses fill this in
	var <track;
	var <colr;
	var <fadeIn;
	var <fadeOut;
	var <gain;
	
	*new { arg span, name, track, colr, fadeIn, fadeOut, gain = 1.0;
		^super.new( span, name ).prInitFRS( track, colr, fadeIn, fadeOut, gain );
	}
	
	prInitFRS { arg argTrack, argColr, argFadeIn, argFadeOut, argGain;
		track	= argTrack;
		colr		= argColr; // ?? { Color.green( 0.6 )};
		fadeIn	= argFadeIn ?? { BosqueFade.new };
		fadeOut	= argFadeOut ?? { BosqueFade.new };
		gain		= argGain;
	}

	protFixFade { arg args;
		var newLength = args[ 0 ].length, newFadeIn = args[ 4 ], newFadeOut = args[ 5 ];
		if( (newFadeIn.numFrames + newFadeOut.numFrames) > newLength, {
			args[ 4 ] = newFadeIn = newFadeIn.replaceFrames( max( 0, min( newFadeIn.numFrames, newLength - newFadeOut.numFrames )));
			args[ 5 ] = newFadeOut.replaceFrames( max( 0, min( newFadeOut.numFrames, newLength - newFadeIn.numFrames )));
		});
	}

	replaceTrack { arg newTrack;
		var args = this.storeArgs;
		args[ 2 ] = newTrack;
		^this.class.new( *args );
	}

	replaceStart { arg newStart;
		var args = this.storeArgs;
		args[ 0 ] = Span( newStart, span.stop );
		args[ 4 ] = fadeIn.replaceFrames( 0 );
		this.protFixFade( args );
		^this.class.new( *args );
	}
	
	replaceStop { arg newStop;
		var args = this.storeArgs;
		args[ 0 ] = Span( span.start, newStop );
		args[ 5 ] = fadeOut.replaceFrames( 0 );
		this.protFixFade( args );
		^this.class.new( *args );
	}

	replaceColor { arg newColor;
		var args = this.storeArgs;
		args[ 3 ] = newColor;
		^this.class.new( *args );
	}

	replaceFadeIn { arg newFadeIn;
		var args = this.storeArgs;
		args[ 4 ] = newFadeIn;
		^this.class.new( *args );
	}

	replaceFadeOut { arg newFadeOut;
		var args = this.storeArgs;
		args[ 5 ] = newFadeOut;
		^this.class.new( *args );
	}

	replaceGain { arg newGain;
		var args = this.storeArgs;
		args[ 6 ] = newGain;
		^this.class.new( *args );
	}

	storeArgs { ^super.storeArgs ++ [ track, colr, fadeIn, fadeOut, gain ]}
	
	asSwingArg {
		^java.asSwingArg;
	}
	
	dispose {
		java.dispose;
		java.destroy;
//		^super.dispose;
	}
}