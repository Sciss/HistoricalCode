/*
 *	(C)opyright 2007-2008 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 */
 
/**
 *	Note: this is used for the master volume and comes from the original forest project.
 *	Not to be confused with the new ForestEnvRegionStake!
 *
 *	@version	0.11, 06-Sep-07
 *	@author	Hanns Holger Rutz
 */
BosqueEnvStake : Stake {
	var <pos, <level;

	*new { arg pos, level;
		^super.new( Span( pos, pos )).prInitEnvStake( pos, level );
	}

	*newFrom { arg anotherStake;
		^this.new( anotherStake.pos, anotherStake.level );
	}
	
	prInitEnvStake { arg argPos, argLevel;
		pos		= argPos;
		level	= argLevel;
	}
	
	duplicate {
		^this.class.newFrom( this );
	}

	replaceStart { arg newStart;
		^this.class.new( newStart, level );
	}
	
	replaceStop { arg newStart;
		^this.class.new( newStart, level );
	}
	
	replaceLevel { arg newLevel;
		^this.class.new( pos, newLevel );
	}
	
	shiftVirtual { arg delta;
		^this.class.new( pos + delta, level );
	}
	
	asString {
		^this.asCompileString;
	}
	
	storeArgs { ^[ pos, level ]}
}