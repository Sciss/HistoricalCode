/*
 *	BosqueNetTimelineViewEditor
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
 *	@version	0.10, 20-Jul-08
 */
BosqueNetTimelineViewEditor {
	var view;
	
	*new { arg view;
		^super.new.prInit( view );
	}
	
	prInit { arg argView;
		view = argView;
	}
	
	value { arg mon, edit, coll;
		coll.do({ arg cmd;
			switch( cmd.first,
			\pos, { edit.addPerform( BosqueTimelineViewEdit.position( this, view, cmd[ 1 ]))},
			\scr, { edit.addPerform( BosqueTimelineViewEdit.scroll( this, view, Span( cmd[ 1 ], cmd[ 2 ])))},
			\sel, { edit.addPerform( BosqueTimelineViewEdit.select( this, view, Span( cmd[ 1 ], cmd[ 2 ])))},
			{ ("Illegal timeline edit command: " ++ cmd).error; ^false }
			);
		});
		^true;
	}
}
