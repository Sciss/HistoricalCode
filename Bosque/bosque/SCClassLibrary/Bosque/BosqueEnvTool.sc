/*
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 */
 
/**
 *	@author	Hanns Holger Rutz
 *	@version	0.10, 06-Jul-08
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
		
		visiSpan		= doc.timeline.visibleSpan;
		bounds		= e.component.bounds;
		trackPos		= (e.x / bounds.width.max(1) * visiSpan.length).asInteger + visiSpan.start;
		y			= e.y / bounds.height.max(1) / timelinePanel.trackVScale;
		track		= doc.tracks.detect({ arg t; (t.y <= y) and: { t.y + t.height > y }});
		if( track.isNil, { ^this });

		oldPressedStake	= pressedStake;
		pressedStake		= doc.trail.editFilterStakeAt( trackPos, nil, { arg stake; stake.track == track });
		if( pressedStake.isNil or: { pressedStake.span.containsPos( trackPos ).not }, { ^this });

		if( (e.clickCount == 2) and: { oldPressedStake === pressedStake }, {
//			(hndlExtent / bounds.height) / trackVScale;
//			level	= y (e.y / bounds.height).linlin( 0, 1, 12, -48 );
//			stake	= BosqueEnvStake( frame, level );
//			ce		= JSyncCompoundEdit( "Edit Envelope" );
//			doc.volEnv.editBegin( ce );
//			doc.volEnv.editAdd( this, stake, ce );
//			doc.volEnv.editEnd( ce );
//			doc.undoManager.addEdit( ce.performAndEnd );
			"Yepp".postln;
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