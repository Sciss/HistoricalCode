/**
 *	(C)opyright 2006 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	SuperCollider implementation of the java class de.sciss.io.MarkerStake
 *
 *	Class dependancies: Stake
 *
 *	@version	0.11, 04-Sep-07
 *	@author	Hanns Holger Rutz
 */
MarkerStake : Stake {
	var <pos, <name;

	*new { arg pos, name;
		^super.new( Span( pos, pos )).prInitMarkerStake( pos, name );
	}

	*newFrom { arg anotherMarker;
		^this.new( anotherMarker.pos, anotherMarker.name );
	}
	
	prInitMarkerStake { arg argPos, argName;
		pos	= argPos;
		name	= argName;
	}
	
	duplicate {
		^this.class.newFrom( this );
	}

	replaceStart { arg newStart;
		^this.class.new( newStart, name );
	}
	
	replaceStop { arg newStart;
		^this.class.new( newStart, name );
	}
	
	shiftVirtual { arg delta;
		^this.class.new( pos + delta, name );
	}
	
	asString {
		^this.asCompileString;
	}
	
//	asCompileString {
//		^(this.class.name ++ "( "++pos++", "++name.asCompileString++" )");
//	}

	storeArgs { ^[ pos, name ]}
}