/*
 *	(C)opyright 2008 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 */

/**
 *  @version	0.10, 06-Jul-08
 *  @author	Hanns Holger Rutz
 */
BosqueEnvSegmentStake : Stake {
	var <java;
	var <startLevel;
	var <stopLevel;
	var <shape;
	var <curve;

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
	
	duplicate {
		^this.class.new( *this.storeArgs );
	}

	shiftVirtual { arg delta;
		var args = this.storeArgs;
		args[ 0 ] = span.shift( delta );
		^this.class.new( *args );
	}

	replaceStart { arg newStart;
		var args = this.storeArgs;
		args[ 0 ] = Span( newStart, span.stop );
		^this.class.new( *args );
	}
	
	replaceStop { arg newStop;
		var args = this.storeArgs;
		args[ 0 ] = Span( span.start, newStop );
		^this.class.new( *args );
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

	replaceStopLevel { arg newLevel;
		var args = this.storeArgs;
		args[ 2 ] = newLevel;
		^this.class.new( *args );
	}
	
	replaceStopWithLevel {Êarg newStop, newLevel;
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