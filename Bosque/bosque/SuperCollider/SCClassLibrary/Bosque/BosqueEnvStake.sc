/*
 *	BosqueEnvStake
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
 *	Note: this is used for the master volume and comes from the original forest project.
 *	Not to be confused with the new ForestEnvRegionStake!
 *
 *	@author	Hanns Holger Rutz
 *	@version	0.11, 06-Sep-07
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