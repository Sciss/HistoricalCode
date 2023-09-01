/*
 *	BosqueNetTrailEditor
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
 *	@version	0.10, 19-Jul-08
 */
BosqueNetTrailEditor {
	var trail;
	
	*new { arg trail;
		^super.new.prInit( trail );
	}
	
	prInit { arg argTrail;
		trail = argTrail;
	}
	
	value { arg mon, edit, coll;
		var lastCmd, collAddRemove, success = false;
		
//("BosqueNetTrailEditor.value : " ++ coll).postln;
		
		trail.editBegin( edit );
		block { arg break;
			coll.do({ arg cmd;
//("Now cmd is " ++ cmd).postln;
				if( cmd.first !== lastCmd, {
					if( this.prFlush( lastCmd, collAddRemove, edit ).not, break );
//("So i called flush with " ++ lastCmd ++ "; collAddRemove.size is " ++ collAddRemove.size).postln;
					lastCmd		= cmd.first;
					collAddRemove	= nil;
				});
				collAddRemove = collAddRemove.add( this.protCreateStake( cmd.copyToEnd( 1 )));
//("So i added a stake").postln;
			});
			success = this.prFlush( lastCmd, collAddRemove, edit );
//("Finally i called flush with " ++ lastCmd ++ "; collAddRemove.size is " ++ collAddRemove.size).postln;
		};
		trail.editEnd( edit );
		^success;
	}

	prFlush { arg cmd, collAddRemove, edit;
		if( cmd.isNil, { ^true });
		if( collAddRemove.size > 0, {
			switch( cmd,
			\add, { trail.editAddAll( this, collAddRemove, edit )},
			\rem, { trail.editRemoveAll( this, collAddRemove, edit )},
			{ ("Illegal trail edit command: " ++ cmd).error; ^false }
			);
		});
		^true;
	}
	
	protCreateStake { arg oscRepr; this.subclassResponsibility( thisMethod )}
}

BosqueNetMarkerTrailEditor : BosqueNetTrailEditor {
	protCreateStake { arg oscRepr;
		^BosqueMarkerStake( oscRepr[ 0 ], oscRepr[ 1 ]);
	}
}
