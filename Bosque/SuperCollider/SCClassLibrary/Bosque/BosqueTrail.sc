/*
 *	BosqueTrail
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
 *	A server-synced trail, that is one that maintains
 *	a structural copy on the SwingOSC server, using
 *	the RegionTrail class of package timebased.
 *	There are some additional utility methods, e.g.
 *	getAllAudioStakes.
 *
 *	@author	Hanns Holger Rutz
 *	@version	0.17, 18-Aug-08
 */
BosqueTrail : Trail {
	var <bosque;
	var <java;
	var updStakes;
	
	var debugUpd = false;
	
	var <>undoMgr;
	
	*new { arg touchMode = kTouchSplit, upd = true;
		^super.new( touchMode ).prInitBosqueTrail( upd );
	}
	
	prInitBosqueTrail { arg upd;
		bosque	= Bosque.default;
		java		= JavaObject( "de.sciss.timebased.RegionTrail", bosque.swing );
		this.prPostInit( upd );
 	}
 	
 	createEmptyCopy {
 		^this.class.new( touchMode );
 	}
 	
 	*prNew { arg touchMode, java, upd;
 		^super.new( touchMode ).prInitDirect( java, upd );
 	}
 	
 	prInitDirect { arg argJava, upd;
		bosque	= Bosque.default;
		java		= argJava;
		this.prPostInit( upd );
 	}
 	
 	prPostInit { arg upd;
 		if( upd, {
			updStakes = UpdateListener({ arg upd, stake, span; var edit;
				if( undoMgr.notNil, {
//					[ "newDispatch", span ].postln;
					edit = BosqueTrailEdit.newDispatch( this, span );
//					[ "newDispatch EDIT" ].postln;
					edit.performEdit;
//					[ "newDispatch PERFORM" ].postln;
					undoMgr.addEdit( edit );
//					[ "newDispatch ADDED" ].postln;
				}, {
//					this.modified( stake, span );
					this.prDispatchModification( this, span );
					java.modified( java, span );
				});
			}, \modified );
		});
 	}
 	
 	duplicate {
 		var newTJ, newT;

 		newTJ = java.duplicate__;
 		newT = this.class.prNew( touchMode, newTJ, updStakes.notNil );
		newT.prEditGetCollByStart.addAll( collStakesByStart );
		collStakesByStart.do({ arg stake; newT.protStakeAdded( stake )});
		newT.prEditGetCollByStop.addAll( collStakesByStop );
		^newT;
 	}

	clear { arg source;
//		collStakesByStart.do({ arg stake; updStakes.removeFrom( stake )});
		java.clear( this.prJavaSource( source ));
		^super.clear( source );
	}
	
	dispose {
		updStakes.remove;
		java.dispose;
		java.destroy;
		^super.dispose;
	}

//	getCuttedTrail { arg span, touchMode, shiftVirtual = 0;
//		var trail;
//		trail = super.getCuttedTrail( span, touchMode, shiftVirtual );
//		trail.prEditGetCollByStart.do({ arg stake; trail.updStakes.addTo( stake )});
//	}
	
	getAllAudioStakes { arg af;
		^this.getAll.select({ arg stake; stake.isKindOf( BosqueAudioRegionStake ) and:
			{ stake.audioFile == af }});
	}

	editInsertSpan { arg source, span, touchMode, ce;
		if( ce.isNil, {
			java.insert( this.prJavaSource( source ), span, touchMode ?? { this.getDefaultTouchMode });
//		}, {
//			java.editInsert( this.prJavaSource( source ), span, touchMode, ce );
		});
		^super.editInsertSpan( source, span, touchMode, ce );
	}
	
	editRemoveSpan { arg source, span, touchMode, ce;
		if( ce.isNil, {
			java.remove( this.prJavaSource( source ), span, touchMode ?? { this.getDefaultTouchMode });
//		}, {
//			java.editRemove( this.prJavaSource( source ), span, touchMode, ce );
		});
		^super.editRemoveSpan( source, span, touchMode, ce );
	}

	editClearSpan { arg source, span, touchMode, ce, filter;
		if( ce.isNil, {
			if( filter != true, {
				"editClearSpan : cannot use filter != true on SwingOSC".error;
			});
			java.clear( this.prJavaSource( source ), span, touchMode ?? { this.getDefaultTouchMode });
//		}, {
//			java.editClear( this.prJavaSource( source ), span, touchMode, ce );
		});
		^super.editClearSpan( source, span, touchMode, ce, filter );
	}
	
	editAddAll { arg source, stakes, ce;
		if( ce.isNil, {
			java.addAll( this.prJavaSource( source ), this.prJavaList( stakes ));
//		}, {
//			java.editAddAll( this.prJavaSource( source ), this.prJavaList( stakes ), ce );
		});
		^super.editAddAll( source, stakes, ce );
	}

	editRemoveAll { arg source, stakes, ce;
		if( ce.isNil, {
			java.removeAll( this.prJavaSource( source ), this.prJavaList( stakes ));
//		}, {
//			java.editRemoveAll( this.prJavaSource( source ), this.prJavaList( stakes ), ce );
		});
		^super.editRemoveAll( source, stakes, ce );
	}
	
//	modified { arg source, span;
//		this.prDispatchModification( source, span );
//		java.modified( java, span );
//	}

	protStakeAdded { arg stake;
		if( updStakes.notNil, {
			if( debugUpd, { ("protStakeAdded( " ++ stake ++ " )").postln });
			updStakes.addTo( stake );
		});
	}

	protStakeRemoved { arg stake;
		if( updStakes.notNil, {
			if( debugUpd, { ("protStakeRemoved( " ++ stake ++ " )").postln });
			updStakes.removeFrom( stake );
			stake.protRemoved;	// to kill playing audio regions
		});
	}

	
	protTrailEdit { arg stakes, span, cmd;
		^BosqueTrailEdit( this, stakes, span, cmd );
	}

	prJavaSource { arg source;
		if( source.isNil, { ^nil });
		if( source.isKindOf( JavaObject ), { ^source });
		^java;
	}

	prJavaList { arg list;
//		^list.asArray;
		if( list.isKindOf( List ), { ^list });   // prevent List asList to make an unnecessary copy!
		^list.asList;
	}
	
	asSwingArg {
		^java.asSwingArg;
	}
}

BosqueTrailEdit : TrailEdit {
	performEdit {
		^super.performEdit;
	}

	protDispatchModification {
		trail.prDispatchModification( trail, span );
		trail.java.modified( trail.java, span );
	}
	
	prAddAll {
		trail.java.addAll( trail.java, trail.prJavaList( stakes ));
		^super.prAddAll;
	}

	prRemoveAll {
		trail.java.removeAll( trail.java, trail.prJavaList( stakes ));
		^super.prRemoveAll;
	}
}
