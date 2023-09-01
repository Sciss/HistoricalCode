/*
 *	BosqueRegionStake
 *	(Bosque)
 *
 *	Copyright (c) 2007-2008 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation; either
 *	version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	This software is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public
 *	License (gpl.txt) along with this software; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 *
 *
 *	Changelog:
 */

/**
 *	ABSTRACT CLASS!
 *
 *	@author	Hanns Holger Rutz
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
		this.protDupArgs( args );
		^this.class.new( *args );
	}

	replaceTrackAndMove { arg newTrack, newStart;
		var args = this.storeArgs;
		args[ 0 ] = span.shift( newStart - span.start );
		args[ 2 ] = newTrack;
		this.protDupArgs( args );
		^this.class.new( *args );
	}

	replaceStart { arg newStart;
		var args = this.storeArgs;
		args[ 0 ] = Span( newStart, span.stop );
		args[ 4 ] = fadeIn.replaceFrames( 0 );
		this.protFixFade( args );
		this.protDupArgs( args );
		^this.class.new( *args );
	}
	
	replaceStop { arg newStop;
		var args = this.storeArgs;
		args[ 0 ] = Span( span.start, newStop );
		args[ 5 ] = fadeOut.replaceFrames( 0 );
		this.protFixFade( args );
		this.protDupArgs( args );
		^this.class.new( *args );
	}

	replaceColor { arg newColor;
		var args = this.storeArgs;
		args[ 3 ] = newColor;
		this.protDupArgs( args );
		^this.class.new( *args );
	}

	replaceFadeIn { arg newFadeIn;
		var args = this.storeArgs;
		args[ 4 ] = newFadeIn;
		this.protDupArgs( args );
		^this.class.new( *args );
	}

	replaceFadeOut { arg newFadeOut;
		var args = this.storeArgs;
		args[ 5 ] = newFadeOut;
		this.protDupArgs( args );
		^this.class.new( *args );
	}

	replaceGain { arg newGain;
		var args = this.storeArgs;
		args[ 6 ] = newGain;
		this.protDupArgs( args );
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