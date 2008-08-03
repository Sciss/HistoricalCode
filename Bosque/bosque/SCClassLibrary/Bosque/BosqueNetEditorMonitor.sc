/*
 *	BosqueNetEditorMonitor
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
BosqueNetEditorMonitor {
	var resp;
	var edits;
	
	*new { arg swing, cmd, id, undoMgr, processor;
		^super.new.prInit( swing, cmd, id, undoMgr, processor );
	}
	
	prInit { arg swing, cmd, id, undoMgr, processor;
		edits = IdentityDictionary.new;
		resp = ScissOSCPathResponder( swing.addr, [ cmd, id ], { arg time, resp, msg;
			var id, coll, edit;
			id	= msg[ 2 ];
			switch( msg[ 3 ],
			\beg, {
				coll = List.new;
				edits[ id ] = coll;
				coll.add( msg[ 4 ]);
			},
			\end, {
				coll	= edits.removeAt( id );
				if( coll.notNil, {
					edit = JSyncCompoundEdit( coll.first.asString );
					if( processor.value( this, edit, coll.array.copyToEnd( 1 )), {
						undoMgr.addEdit( edit.performAndEnd );
					}, {
						edit.cancel;
					});
				}, {
					("BosqueNetEditorMonitor end: edit " ++ id ++ "not found").error;
				});
			}, {
				coll	= edits[ id ];
				if( coll.notNil, {
					coll.add( msg.copyToEnd( 3 ));
				}, {
					("BosqueNetEditorMonitor add: edit " ++ id ++ "not found").error;
				});
			});
			
		}).add;
	}
	
	dispose {
		resp.remove;
	}
}