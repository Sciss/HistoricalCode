/*
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 */
 
/**
 *	@version	0.10, 10-Jul-08
 */
BosqueMarkerStake : MarkerStake {
	var <java;	// subclasses fill this in
	
	*new { arg pos, name;
		^super.new( pos, name ).prInitBMS;
	}
	
	prInitBMS {
		java	= JavaObject( "de.sciss.timebased.MarkerStake", Bosque.default.swing, pos, name );
	}

	asSwingArg {
		^java.asSwingArg;
	}
	
	protRemoved { /* ... */ }

	dispose {
		java.dispose; java.destroy;
//		^super.dispose;
	}
}