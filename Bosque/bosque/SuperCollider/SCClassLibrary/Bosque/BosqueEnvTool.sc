/*
 *	BosqueEnvTool
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
 *	@author	Hanns Holger Rutz
 *	@version	0.12, 23-Jul-08
 */
BosqueEnvTool {
	var hndlExtent	= 13;

	var doc, timelinePanel;
	var pressedStake, dragHitIdx, dragValid = false, dragStarted = false;
	var dragConstrainH, dragStartX, dragStartY, dragStartTrack;
	
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
		pressedStake		= if( e.stake.isKindOf( BosqueEnvRegionStake ), e.stake );
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
//				doc.trail.modified( this, modSpan );
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
//			doc.trail.modified( this, modSpan );
		}, {
		
			dragHitIdx	= e.hitIdx;
			if( dragHitIdx >= 0, {
//				dragHitStake	= env.get( hitIdx );
				dragStartX	= e.x;
				dragStartY	= e.y;
				dragStartTrack= e.track;
				dragStarted	= false;
				dragValid		= true;
				dragConstrainH = (dragHitIdx == 0) || (dragHitIdx == env.numStakes);
			});
		})});

//		if( edit.notNil, {
//			doc.undoManager.addEdit( edit );
//		});
//		dragValid = dragStartStake.notNil and: { doc.selectedRegions.includes( dragStartStake )};
	}

	mouseDragged { arg e;
		var dx, dy, level;

		if( dragValid.not, { ^this });
		
		if( dragStarted.not, {
			dx = e.x - dragStartX;
			dy = e.y - dragStartY;
			if( (dx.squared + dy.squared) > 16, {
				dragStarted = true;
//				jTrailView.setDrag( dx, dy, 0, 0 );
//				jTrailView.setDragPainted( true );
			});
		}, {
			if( dragConstrainH, { dy = 0 });
//			jTrailView.setDrag( dx, dy, 0, 0 );	// XXX
		});
		
		if( dragStarted, {
			level = if( e.track.isNil || (e.track === dragStartTrack), {
				e.innerLevel.clip( 0, 1 );
			}, { if( doc.tracks.indexOf( e.track ) < doc.tracks.indexOf( dragStartTrack ), 1.0, 0.0 )});
			dragStartTrack.displayLevel( if( dragStartTrack.ctrlSpec.isNil, level, { dragStartTrack.ctrlSpec.map( level )}));
		});
	}

	mouseReleased { arg e;
		var ce, env, modSpan, hitStake1, hitStake2, newStake1, newStake2, hitIdx;
		var level, frame;
		
		dragValid = false;
		if( dragStarted, {
//			jTrailView.setDragPainted( false );
			ce = JSyncCompoundEdit( "Edit Envelope" );
			level = if( e.track.isNil || (e.track === dragStartTrack), {
				e.innerLevel.clip( 0, 1 );
			}, { if( doc.tracks.indexOf( e.track ) < doc.tracks.indexOf( dragStartTrack ), 1.0, 0.0 )});
			frame = pressedStake.span.clip( e.frame ) - pressedStake.span.start;
			env = pressedStake.env;
			env.editBegin( ce );
			if( dragHitIdx == 0, { // level contrained
				hitStake1 = env.editGet( dragHitIdx, true, ce );
				env.editRemove( this, hitStake1, ce );
				newStake1 = hitStake1.replaceStartLevel( level );
				env.editAdd( this, newStake1, ce );
				modSpan = newStake1.span;
			}, { if( dragHitIdx ==  env.numStakes, {
				hitStake1 = env.editGet( dragHitIdx - 1, true, ce );
				env.editRemove( this, hitStake1, ce );
				newStake1 = hitStake1.replaceStopLevel( level );
				env.editAdd( this, newStake1, ce );
				modSpan = newStake1.span;
			}, {
				// to make it more simple, we perform a successive delete and
				// insert point operation...
				// delete:
				hitStake1 = env.editGet( dragHitIdx - 1, true, ce );
				hitStake2 = env.editGet( dragHitIdx, true, ce );
				newStake1	 = hitStake1.replaceStopWithLevel( hitStake2.span.stop, hitStake2.stopLevel );
				env.editRemoveAll( this, [ hitStake1, hitStake2 ], ce );
				env.editAdd( this, newStake1, ce );
				modSpan = Span( hitStake1.span.start, hitStake2.span.stop );
				// insert:
				hitIdx = env.editIndexOf( frame, true, ce ); // !! editIndexOf !!
				if( hitIdx == 0, { 	// wooop, we hit another point
					hitStake1 = env.editGet( hitIdx, true, ce );
					env.editRemove( this, hitStake1, ce );
					newStake1 = hitStake1.replaceStartLevel( level );
					env.editAdd( this, newStake1, ce );
					modSpan = modSpan.union( newStake1.span );
				}, { if( hitIdx > 0, {	// wooop, we hit another point
					hitStake1 = env.editGet( hitIdx - 1, true, ce );
					hitStake2 = env.editGet( hitIdx, true, ce );
					env.editRemoveAll( this, [ hitStake1, hitStake2 ], ce );
					newStake1 = hitStake1.replaceStopLevel( level );
					newStake2 = hitStake2.replaceStartLevel( level );
					env.editAddAll( this, [ newStake1, newStake2 ], ce );
					modSpan = modSpan.union( Span( newStake1.span.start, newStake2.span.stop ));
				}, {
//[ "Removed", hitStake1.span, hitStake2.span, "added", newStake1.span, "frame", frame, "hitIdx", hitIdx ].postcs;
					hitStake1		= env.editGet( (hitIdx + 2).neg, true, ce );  // !!! editGet !!!
//[ "New hit", hitStake1.span ].postcs;
					Assertion({ hitStake1.span.containsPos( frame )});
					newStake1		= hitStake1.replaceStopWithLevel( frame, level );
					newStake2		= hitStake1.replaceStartWithLevel( frame, level );
					env.editRemove( this, hitStake1, ce );
					env.editAddAll( this, [ newStake1, newStake2 ], ce );
					modSpan		= modSpan.union( hitStake1.span );
				})});
			})});
			env.editEnd( ce );
			doc.undoManager.addEdit( ce.performAndEnd );
			modSpan = modSpan.shift( pressedStake.span.start );
//			doc.trail.modified( this, modSpan );
			dragStarted = false;
		});
	}
}