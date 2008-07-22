/*
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 */
 
/**
 *	@author	Hanns Holger Rutz
 *	@version	0.12, 23-Jul-08
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
		var oldPressedStake, track, trackPos, coll, edit, level;
		var ce, frame, env, hitIdx, hitStake1, hitStake2;
		var newStake1, newStake2, modSpan;

		trackPos			= e.frame;
		track			= e.track;
		oldPressedStake	= pressedStake;
		pressedStake		= e.stake;
		if( pressedStake.isNil or: { pressedStake.span.containsPos( trackPos ).not }, { ^this });

		level	= e.innerLevel.clip( 0, 1 );
		frame	= trackPos - pressedStake.span.start;
		env		= pressedStake.env;

		if( e.isAltDown, {
//			e.hitIdx.postln;
			hitIdx = e.hitIdx;
			if( (hitIdx > 0) and: {hitIdx < env.numStakes }, {
				hitStake1 = env.get( hitIdx - 1 );
				hitStake2 = env.get( hitIdx );
				newStake1	 = hitStake1.replaceStopWithLevel( hitStake2.span.stop, hitStake2.stopLevel );
				ce		= JSyncCompoundEdit( "Edit Envelope" );
				env.editBegin( ce );
				env.editRemoveAll( this, [ hitStake1, hitStake2 ], ce );
				env.editAdd( this, newStake1, ce );
				env.editEnd( ce );
				doc.undoManager.addEdit( ce.performAndEnd );
				modSpan		= newStake1.span.shift( pressedStake.span.start );
				doc.trail.modified( this, modSpan );
			});
			
		}, { if( (e.clickCount == 2) and: { oldPressedStake === pressedStake }, {
//			segmStake		= BosqueEnvSegmentStake.sc( frame, level );
//			hitIdx		= e.hitIdx; // env.indexOf( frame );
//			if( hitIdx < 0, {
				hitIdx = env.indexOf( frame );
//			});
			if( hitIdx >= -1, { ^this });	// don't put another point on dem una same position
			hitStake1		= env.get( (hitIdx + 2).neg );
			Assertion({ hitStake1.span.containsPos( frame )});
			newStake1		= hitStake1.replaceStopWithLevel( frame, level );
			newStake2		= hitStake1.replaceStartWithLevel( frame, level );
//			segmStake		= BosqueEnvSegmentStake.sc( span!!!, level, stopLevel!!! );
			ce			= JSyncCompoundEdit( "Edit Envelope" );
			env.editBegin( ce );
			env.editRemove( this, hitStake1, ce );
			env.editAddAll( this, [ newStake1, newStake2 ], ce );
			env.editEnd( ce );
			doc.undoManager.addEdit( ce.performAndEnd );
			modSpan		= hitStake1.span.shift( pressedStake.span.start );
//			modSpan.postcs;
//			doc.trail.prDispatchModification( this, modSpan );
			doc.trail.modified( this, modSpan );
//			"Yepp".postln;
		})});

//		if( edit.notNil, {
//			doc.undoManager.addEdit( edit );
//		});
//		dragValid = dragStartStake.notNil and: { doc.selectedRegions.includes( dragStartStake )};
	}

	mouseDragged { arg e;
		// nada
	}

	mouseReleased { arg e;
		// nada
	}
}