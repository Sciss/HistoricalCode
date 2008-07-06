/**
 *	(C)opyright 2006 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: Stake, Span
 *
 *	SuperCollider implementation of the java class de.sciss.io.RegionStake
 *
 *  @version	0.13, 14-Aug-07
 *  @author	Hanns Holger Rutz
 */
RegionStake : Stake {
	var <name;

	*new { arg span, name;
		^super.new( span ).prInitRegionStake( name );
	}

	prInitRegionStake { arg argName;
		name	= argName;
	}
	
	*newFrom { arg anotherRegion;
		^this.new( *anotherRegion.storeArgs );
	}

	duplicate {
		^this.class.new( *this.storeArgs );
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

	shiftVirtual { arg delta;
		var args = this.storeArgs;
		args[ 0 ] = span.shift( delta );
		^this.class.new( *args );
	}
	
	rename { arg newName;
		var args = this.storeArgs;
		args[ 1 ] = newName;
		^this.class.new( *args );
	}

	asString {
		^this.asCompileString;
	}
	
//	asCompileString {
//		^(this.class.name ++ "( "++span.asCompileString++", "++name.asCompileString++" )");
//	}

	storeArgs { ^[ span, name ]}
}