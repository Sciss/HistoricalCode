/*
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 */
 
/**
 *	A server-synced trail, that is one that maintains
 *	a structural copy on the SwingOSC server, using
 *	the RegionTrail class of package timebased.
 *	There are some additional utility methods, e.g.
 *	getAllAudioStakes.
 *
 *	@author	Hanns Holger Rutz
 *	@version	0.14, 06-Jul-08
 */
BosqueTrail : Trail {
	var <forest;
	var <java;
	
	*new { arg touchMode = kTouchSplit;
		^super.new( touchMode ).prInitBosqueTrail;
	}
	
	prInitBosqueTrail {
		forest	= Bosque.default;
		java		= JavaObject( "de.sciss.timebased.RegionTrail", forest.swing );
 	}
 	
 	createEmptyCopy {
 		^this.class.new( touchMode );
 	}

	clear { arg source;
		java.clear( this.prJavaSource( source ));
		^super.clear( source );
	}
	
	dispose {
		java.dispose;
		java.destroy;
		^super.dispose;
	}
	
	getAllAudioStakes { arg af;
		^this.getAll.select({ arg stake; stake.isKindOf( BosqueAudioRegionStake ) and:
			{ stake.faf == af }});
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
	
	modifiedÊ{Êarg source, span;
		this.prDispatchModification( source, span );
		java.modified( span );
	}

	prSortRemoveStake { arg stake, ce;
		super.prSortRemoveStake( stake, ce );
		if( ce.isNil, {
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
	
	prAddAll {
		trail.java.addAll( trail.java, trail.prJavaList( stakes ));
		^super.prAddAll;
	}

	prRemoveAll {
		trail.java.removeAll( trail.java, trail.prJavaList( stakes ));
		^super.prRemoveAll;
	}
}
