/*
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
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