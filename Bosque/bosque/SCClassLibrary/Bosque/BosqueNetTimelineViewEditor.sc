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
