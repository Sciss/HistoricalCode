BosqueNetSessionCollectionEditor {
	var sc;
	
	*new { arg sc;
		^super.new.prInit( sc );
	}
	
	prInit { arg argSC;
		sc = argSC;
	}
	
	value { arg mon, edit, coll, objects;
		coll.do({ arg cmd;
//			("SessColl : " ++ cmd).postln;
			switch( cmd.first,
			\add, { objects = cmd.copyToEnd( 1 ).collect({ arg id; this.protGetObject( id )});
//				("add: " ++ objects).postln;
				edit.addPerform( BosqueEditAddSessionObjects( this, sc, objects ))},
			\rem, { objects = cmd.copyToEnd( 1 ).collect({ arg id; this.protGetObject( id )});
//				("rem: " ++ objects).postln;
				edit.addPerform( BosqueEditRemoveSessionObjects( this, sc, objects ))},
			{ ("Illegal timeline edit command: " ++ cmd).error; ^false }
			);
		});
		^true;
	}

	protGetObject { arg id; this.subclassResponsibility( thisMethod )}
}

BosqueNetTracksEditor : BosqueNetSessionCollectionEditor {
	var scAll;
	
	*new { arg sc, scAll;
		^super.new( sc ).prInitBNTE( scAll );
	}
	
	prInitBNTE { arg argSCAll;
		scAll = argSCAll;
	}
	
	protGetObject { arg id;
		^scAll.detect({ arg x; x.trackID == id });
	}
}
