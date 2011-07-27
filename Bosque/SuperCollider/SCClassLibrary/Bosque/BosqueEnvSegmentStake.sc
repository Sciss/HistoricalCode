/*
 *	BosqueEnvSegmentStake
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
 *	@author	Hanns Holger Rutz
 *	@version	0.11, 25-Jul-08
 */
BosqueEnvSegmentStake : Stake {
	var <java;
	var <startLevel;
	var <stopLevel;
	var <shape;
	var <curve;

	classvar array; // for quick envAt calculations
	
	*initClass {
		array = [ /* 0=startLevel */ 0, 1, -99, -99, /* 4=stopLevel */ 0, /* 5=stopTime */ 1.0, /* 6=shapeNumber */ 0, /* 7=curveValue */ 0 ];
	}
	
	*new { arg span, startLevel, stopLevel, shape = 1, curve = 0.0;
		^super.new( span ).prInitFESS( startLevel, stopLevel, shape, curve );
	}

	prInitFESS { arg argStartLevel, argStopLevel, argShape, argCurve;
		startLevel	= argStartLevel;
		stopLevel		= argStopLevel;
		shape		= argShape;
		curve		= argCurve;
		java			= JavaObject( "de.sciss.timebased.bosque.EnvSegmentStake", Bosque.default.swing,
								span, startLevel, stopLevel, shape, curve );
	}
	
	level { arg frame;
//		("stake #" ++ idx ++ "; startLvl " ++ stake.startLevel ++ "; stopLvl " ++ stake.stopLevel).postln;
		
		array[ 0 ] = startLevel;
		array[ 4 ] = stopLevel;
//		array[ 5 ] = span.length;
		array[ 6 ] = shape;
		array[ 7 ] = curve;
//		^array.envAt( max( frame, span.stop ) - span.start );
		^array.envAt( ((frame - span.start) / span.length).clip( 0, 1 ));
	}
		
	duplicate {
		^this.class.new( *this.storeArgs );
	}

	shiftVirtual { arg delta;
		var args = this.storeArgs;
		args[ 0 ] = span.shift( delta );
		^this.class.new( *args );
	}

	replaceStart { arg newStart;
//		var args = this.storeArgs;
//		args[ 0 ] = Span( newStart, span.stop );
//		^this.class.new( *args );
		^this.replaceStartWithLevel( newStart, this.level( newStart ));
	}
	
	replaceStop { arg newStop;
//		var args = this.storeArgs;
//		args[ 0 ] = Span( span.start, newStop );
//		^this.class.new( *args );
		^this.replaceStopWithLevel( newStop, this.level( newStop ));
	}

	replaceStartLevel { arg newLevel;
		var args = this.storeArgs;
		args[ 1 ] = newLevel;
		^this.class.new( *args );
	}

	replaceStartWithLevel { arg newStart, newLevel;
		var args = this.storeArgs;
		args[ 0 ] = Span( newStart, span.stop );
		args[ 1 ] = newLevel;
		^this.class.new( *args );
	}

	replaceStartAndLevels { arg newStart, newStartLevel, newStopLevel;
		var args = this.storeArgs;
		args[ 0 ] = Span( newStart, span.stop );
		args[ 1 ] = newStartLevel;
		args[ 2 ] = newStopLevel;
		^this.class.new( *args );
	}

	replaceStopLevel { arg newLevel;
		var args = this.storeArgs;
		args[ 2 ] = newLevel;
		^this.class.new( *args );
	}
	
	replaceStopWithLevel { arg newStop, newLevel;
		var args = this.storeArgs;
		args[ 0 ] = Span( span.start, newStop );
		args[ 2 ] = newLevel;
		^this.class.new( *args );
	}

	replaceShape { arg newShape;
		var args = this.storeArgs;
		args[ 3 ] = newShape;
		^this.class.new( *args );
	}

	replaceCurve { arg newCurve;
		var args = this.storeArgs;
		args[ 4 ] = newCurve;
		^this.class.new( *args );
	}

	storeArgs { ^[ span, startLevel, stopLevel, shape, curve ]}
	
	asSwingArg {
		^java.asSwingArg;
	}
	
	dispose {
		java.dispose;
		java.destroy;
	}

	protRemoved {
		// XXX
	}
}