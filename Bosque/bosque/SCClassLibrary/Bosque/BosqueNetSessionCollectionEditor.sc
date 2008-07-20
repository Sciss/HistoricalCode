/*
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 */
 
/**
 *	@author	Hanns Holger Rutz
 *	@version	0.11, 20-Jul-08
 */
BosqueNetSessionCollectionEditor {
	var sc, scAll;
	
	*new { arg sc, scAll;
		^super.new.prInit( sc, scAll );
	}
	
	prInit { arg argSC, argSCAll;
		sc = argSC;
		scAll = argSCAll;
	}
	
	value { arg mon, edit, coll, objects;
		coll.do({ arg cmd;
//			("SessColl : " ++ cmd).postln;
			switch( cmd.first,
			\add, { objects = cmd.copyToEnd( 1 ).collect({ arg idx; this.protGetObject( idx )});
//				("add: " ++ objects).postln;
				edit.addPerform( BosqueEditAddSessionObjects( this, sc, objects ))},
			\rem, { objects = cmd.copyToEnd( 1 ).collect({ arg idx; this.protGetObject( idx )});
//				("rem: " ++ objects).postln;
				edit.addPerform( BosqueEditRemoveSessionObjects( this, sc, objects ))},
			{ ("Illegal session collection edit command: " ++ cmd).error; ^false }
			);
		});
		^true;
	}

//	protGetObject { arg id; this.subclassResponsibility( thisMethod )}
	protGetObject { arg idx; ^scAll.at( idx )}
	
//	asSwingArg {
//		"\n----------------- KUUUUKA -------------\n".postln;
////		this.dumpBackTrace;
//	}
}
//
//BosqueNetTracksEditor : BosqueNetSessionCollectionEditor {
//	var scAll;
//	
//	*new { arg sc, scAll;
//		^super.new( sc ).prInitBNTE( scAll );
//	}
//	
//	prInitBNTE { arg argSCAll;
//		scAll = argSCAll;
//	}
//	
//	protGetObject { arg id;
//		
////		^scAll.detect({ arg x; x.trackID == id });
//	}
//}
