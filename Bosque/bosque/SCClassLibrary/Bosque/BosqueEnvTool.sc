/*
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 */
 
/**
 *	@author	Hanns Holger Rutz
 *	@version	0.11, 10-Jul-08
 */
BosqueEnvTool {
	var hndlExtent	= 13;

	var doc, timelinePanel;
	var pressedStake;
	
	*new { arg doc, timelinePanel;
		^super.new.prInitEnvTool( doc, timelinePanel );
	}
	
	prInitEnvTool { arg argDoc, argTimelinePanel;
		doc			= argDoc;
		timelinePanel	= argTimelinePanel;
	}
	
	mousePressed { arg e;
		var oldPressedStake, bounds, track, trackPos, visiSpan, coll, edit, y, level;
		var ty1, ty2, yscale, ce, frame, env, idxHit, hitStake;
		var splitStake1, splitStake2, modSpan;
		
		visiSpan		= doc.timeline.visibleSpan;
		bounds		= e.component.bounds;
		trackPos		= (e.x / bounds.width.max(1) * visiSpan.length).asInteger + visiSpan.start;
		yscale		= bounds.height.max(1) * timelinePanel.trackVScale;
		y			= e.y / yscale;
		track		= doc.tracks.detect({ arg t; (t.y <= y) and: { t.y + t.height > y }});
		if( track.isNil, { ^this });

		oldPressedStake	= pressedStake;
		pressedStake		= doc.trail.editFilterStakeAt( trackPos, nil, { arg stake; stake.track == track });
		if( pressedStake.isNil or: { pressedStake.span.containsPos( trackPos ).not }, { ^this });

		ty1		= track.y * yscale + hndlExtent;
		ty2		= (track.y + track.height) * yscale;
		level	= ((ty2 - e.y) / (ty2 - ty1)).clip( 0, 1 );
		frame	= trackPos - pressedStake.span.start;
		env		= pressedStake.env;

		if( (e.clickCount == 2) and: { oldPressedStake === pressedStake }, {
//			segmStake		= BosqueEnvSegmentStake.sc( frame, level );
			idxHit		= env.indexOf( frame );
			if( idxHit >= -1, { ^this });	// don't put another point on dem una same position
			hitStake		= env.get( (idxHit + 2).neg );
			Assertion({ hitStake.span.containsPos( frame )});
			splitStake1	= hitStake.replaceStopWithLevel( frame, level );
			splitStake2	= hitStake.replaceStartWithLevel( frame, level );
//			segmStake		= BosqueEnvSegmentStake.sc( span!!!, level, stopLevel!!! );
			ce			= JSyncCompoundEdit( "Edit Envelope" );
			env.editBegin( ce );
			env.editRemove( this, hitStake, ce );
			env.editAddAll( this, [ splitStake1, splitStake2 ], ce );
			env.editEnd( ce );
			doc.undoManager.addEdit( ce.performAndEnd );
			modSpan		= splitStake1.span.union( splitStake2.span ).shift( pressedStake.span.start );
//			modSpan.postcs;
//			doc.trail.prDispatchModification( this, modSpan );
			doc.trail.modified( this, modSpan );
//			"Yepp".postln;
		});

//		if( edit.notNil, {
//			doc.undoManager.addEdit( edit );
//		});
//		dragValid = dragStartStake.notNil and: { doc.selectedRegions.includes( dragStartStake )};
	}

	mouseDragged { arg e;
	
	}

	mouseReleased { arg e;
	
	}
}