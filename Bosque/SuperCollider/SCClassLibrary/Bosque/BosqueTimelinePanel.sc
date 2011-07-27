/*
 *	BosqueTimelinePanel
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
 *	@version	0.19, 23-Jul-08
 */
BosqueTimelinePanel {
	var <view;
	var <doc;
	var <jTrailView;
	var mapUpdTrack;
	var envTool;
	var <liveEnvPainter;
	
	// dnd
	var dragStartX, dragStartY, dragConstrainH, dragConstrainV, dragStartPos, dragStartStake, dragStartTrack, dragValid = false, dragStarted = false;
	var dragResizeEdge;
	var ggVolEnv;

  	var <trackVScale = 1;
  	
  	var <tool = \move;
  	
  	var volEnvDispValues;
  	var volEnvDispStakes;
  	var volEnvDispFakeStart = true, volEnvDispFakeStop = true;
  	
	*new { arg doc, parent, bounds;
		^super.new.prInit( doc, parent, bounds );
	}
	
	prInit { arg argDoc, parent, bounds;
		var updSelection, updTransport, updTracks, fntSmall, bosque, jTimelinePanel, ggScrollPane, view;
		var updTrackSel, ggVolLab, jTimeAxis, jMarkAxis;
		var jTrackPanel, jMarkerEditor, jTimeEditor, markerEditorMon, timelineEditorMon;
		var jSelTracksEditor, selTracksEditorMon, jTrailViewListener, respTrailViewListener;
		var jTrackRowHeaderFactory, jStakeRenderer, routLevelStrings, updTimelineCursor;
		
		doc		= argDoc;
		bosque	= doc.bosque;
		envTool	= BosqueEnvTool( doc, this );

		fntSmall = JFont( "Helvetica", 10 );
//		ggScrollPane = JSCScrollPane( parent, bounds ).resize_( 5 )
//			.verticalScrollBarShown_( \always )
//			.horizontalScrollBarShown_( \never );
		jTimelinePanel = JavaObject( "de.sciss.timebased.gui.TimelinePanel", bosque.swing, doc.timelineView );
		jTrackPanel	 = JavaObject( "de.sciss.timebased.gui.TrackPanel", bosque.swing, jTimelinePanel );
		jTrackRowHeaderFactory = JavaObject( "de.sciss.timebased.bosque.BosqueTrackRowHeaderFactory", bosque.swing );
		jTrackRowHeaderFactory.setLevelFont( JFont( "Arial Narrow", 12 ));
		jTrackRowHeaderFactory.setLevelColor( Color.new255( 62, 100, 85 ));
		jTrackPanel.setTrackRowHeaderFactory( jTrackRowHeaderFactory );
		jSelTracksEditor = JavaObject( "de.sciss.timebased.net.NetSessionCollectionEditor", bosque.swing, bosque.master, doc.tracks );
		jSelTracksEditor.setID( doc.selectedTracks.java.id );
		selTracksEditorMon = BosqueNetEditorMonitor( bosque.swing, '/coll', doc.selectedTracks.java.id, doc.undoManager,
			BosqueNetSessionCollectionEditor( doc.selectedTracks, doc.tracks ));
		jTrackPanel.setTracksEditor( jSelTracksEditor );
		jTimeAxis = jTimelinePanel.getTimelineAxis__;
		jMarkAxis = jTimelinePanel.getMarkerAxis__;
		jTimeEditor = JavaObject( "de.sciss.timebased.net.NetTimelineViewEditor", bosque.swing, bosque.master );
		jTimeEditor.setID( doc.timelineView.java.id );
		timelineEditorMon = BosqueNetEditorMonitor( bosque.swing, '/time', doc.timelineView.java.id, doc.undoManager,
			BosqueNetTimelineViewEditor( doc.timelineView ));
		jTimeAxis.setEditor( jTimeEditor );
//		jTimeAxis.setFont( fntSmall );
//		jMarkAxis.setFont( fntSmall );
//		BosqueTimelineAxis( doc, this, jTimeAxis );
//		jMarkAxis.setTrail( doc.markers );
//		jTimelinePanel.setMarkerTrail( doc.markers );
		jTrackPanel.setTracks( doc.tracks, doc.selectedTracks );
		jTrackPanel.setMarkerTrack( doc.markerTrack );
		jMarkerEditor = JavaObject( "de.sciss.timebased.net.NetMarkerTrailEditor", bosque.swing, bosque.master );
		jMarkerEditor.setID( doc.markers.java.id );
		markerEditorMon = BosqueNetEditorMonitor( bosque.swing, '/trail', doc.markers.java.id, doc.undoManager,
			BosqueNetMarkerTrailEditor( doc.markers ));
		jMarkAxis.setEditor( jMarkerEditor );
		jMarkAxis.destroy;
//		view = JSCPlugView( ggScrollPane, bounds, jTimelinePanel ); // .resize_( 5 );
		view = JSCPlugView( parent, bounds.insetAll( 0, 0, 0, 60 ), jTrackPanel ).resize_( 5 );
		view.onClose = { jTimeAxis.destroy; jMarkerEditor.dispose; jMarkerEditor.destroy };
		jTrailView = JavaObject( "de.sciss.timebased.gui.TrailView", bosque.swing, jTimelinePanel, doc.timelineView );
//		jTrailView.setFont( fntSmall );
		jTrailView.setTrail( doc.trail );
		jStakeRenderer = JavaObject( "de.sciss.timebased.bosque.BosqueStakeRenderer", bosque.swing ); 
		jTrailView.setStakeRenderer( jStakeRenderer );
//		jTimelinePanel.add( jTrailView );
		jTrackPanel.setMainView( jTrailView );
		jTrailView.setTracksTable( jTrackPanel );
		
		jTrailViewListener	= JavaObject( "de.sciss.timebased.net.NetTrailViewListener", bosque.swing, bosque.master, jTrailView );
		jTrailViewListener.setID( jTrailView.id );
		
		liveEnvPainter = JavaObject( "de.sciss.timebased.bosque.LiveEnvPainter", bosque.swing, jTrackPanel, jTimelinePanel, doc.timelineView );
		
		UpdateListener.newFor( bosque.timelineEditor, { arg upd, ed, tool;
			jTimelinePanel.server.sendMsg( '/set', jTimelinePanel.id, \cursor, '[', '/new', "java.awt.Cursor",
				switch( tool, \move, 0, \resize, 11, \env, 1, 0 ), ']' );
		}, \tool );
		
		respTrailViewListener = ScissOSCPathResponder( bosque.swing.addr, [ '/trail', jTrailView.id, \mouse ], {
			arg time, resp, msg;
			var cmd, oscID, mouse, action, frame, level, innerLevel, hitIdx, trackIdx, stakeIdx, x, y, modifiers, button, clickCount;
			var e;
			
			#cmd, oscID, mouse, action, frame, level, innerLevel, hitIdx, trackIdx, stakeIdx, x, y, modifiers, button, clickCount = msg;
			
			e = Event( 16 );
			e[ \x ] = x;
			e[ \y ] = y;
			e[ \clickCount ] = clickCount;
			e[ \button ] = button;
			e[ \component ] = view;
			e[ \modifiers ] = modifiers;
			e[ \isAltDown ] = (modifiers & 0x08) != 0;
			e[ \isShiftDown ] = (modifiers & 0x1) != 0;
			e[ \isControlDown ] = (modifiers & 0x02) != 0;
			e[ \isMetaDown ] = (modifiers & 0x04) != 0;
			e[ \frame ] = frame;
			e[ \level ] = level;
			e[ \innerLevel ] = innerLevel;
			e[ \hitIdx ] = hitIdx;
			e[ \track ] = doc.tracks.at( trackIdx );
			e[ \stake ] = doc.trail.get( stakeIdx );
			
			switch( action,
			\moved, { this.mouseDragged( e )},
			\pressed, { this.mousePressed( e )},
			\released, { this.mouseReleased( e )}
			);
		}).add;
		
//~jTimelinePanel = jTimelinePanel;
//~jTrailView = jTrailView;

		ggVolLab = JSCStaticText( parent, Rect( bounds.left, view.bounds.bottom, 60, 60 ))
			.resize_( 7 )
			.align_( \center )
			.font_( fntSmall );
		ggVolEnv = JSCEnvelopeView( parent, Rect( view.bounds.left + 62, view.bounds.bottom, view.bounds.width - 62, 60 ))
			.drawLines_( true )
			.selectionColor_( Color.red )
			.resize_( 8 )
			.thumbSize_( 5 )
//			.value_([[ 0.0, 0.1, 0.5, 1.0 ], [ 0.1, 1.0, 0.8, 0.0 ]])  // set the initial node values (x and y)
			.clipThumbs_( true )
			.horizontalEditMode_( \relay )
			.canFocus_( false )
			.action_({ arg b;
				ggVolLab.string = b.value[ 1 ][ b.index ].linlin( 0, 1, -48, 12 ).round( 0.1 ).asString ++ " dB";
			});

		mapUpdTrack = IdentityDictionary.new;

//		ggTrackHeader = JSCUserView( parent, bounds.resizeTo( 60, bounds.height - 60 )).resize_( 4 )
//			.canFocus_( false )
//			.drawFunc_({ arg b; var y, h, bounds, vscale;
//				bounds = b.bounds;
//				vscale = trackVScale * bounds.height;
//				JPen.translate( bounds.left + 0.5, bounds.top + 0.5 );
//				JPen.line( (bounds.width - 1) @ 0, (bounds.width - 1) @ (bounds.height - 1) );
//				JPen.strokeColor = Color.black;
//				JPen.stroke;
//				doc.tracks.do({ arg track, i;
////				[ i, track.y, track.height, trackVScale ].postln;
//					y = track.y * vscale;
//					h = track.height * vscale;
//					if( doc.selectedTracks.includes( track ), {
//						JPen.fillColor = Color.blue( 1, 0.2 );
//						JPen.addRect( Rect( 0, y, bounds.width - 1, h ));
//						JPen.fill;
//					});
//					JPen.line( 0 @ y, (bounds.width - 1) @ y );
//					JPen.stroke;
//					JPen.fillColor = track.muted.if( Color.red, Color.black );
//					JPen.stringAtPoint( track.name, 4 @ (y + 4 ));
//					JPen.fill;
//				});
//			});
//
		updTimelineCursor = UpdateListener.newFor( doc.timelineView.cursor, { arg upd, csr, what;
			this.prUpdateLevelStrings;
		}, \changed );
		
		updSelection = UpdateListener.newFor( doc.selectedRegions, { arg upd, sc, what ... coll;
			switch( what,
			\add, {
				jTrailView.addToSelection( coll.asList );
			},
			\remove, {
				jTrailView.removeFromSelection( coll.asList );
			});
		});
		
		updTransport = UpdateListener.newFor( doc.transport, { arg upd, transport, what, param1, param2;
			switch( what,
			\play, {
//				jTimelinePanel.setPosition( param1 );
				jTimelinePanel.play( param1, param2 );
				routLevelStrings = this.prStartRoutLevelStrings;
			},
			\stop, {
//				("jTimelinePanel.stop").postln;
				jTimelinePanel.stop;
				routLevelStrings.stop; routLevelStrings = nil;
			},
			\pause, {
//				"---pause".postln;
				jTimelinePanel.stop;
				routLevelStrings.stop; routLevelStrings = nil;
			},
			\resume, {
//				"---resume".postln;
//				jTimelinePanel.setPosition( param1 );
				jTimelinePanel.play( param1, param2 );
				routLevelStrings = this.prStartRoutLevelStrings;
			},
			\rate, {
				jTimelinePanel.setPlayRate( param1, param2 );
			}
			);
		});

		updTracks = UpdateListener.newFor( doc.tracks, { arg upd, sc, what ... coll;
			var upd2;
			switch( what,
			\add, {
				coll.do({ arg t;
//					jTrailView.addTrack( t );
					upd2 = UpdateListener.newFor( t, { arg upd, track, what; /* if( what == \muted, { ggTrackHeader.refresh })*/ });
					mapUpdTrack[ t ] = upd2;
				});
//				this.prRecalcTrackBounds;
			},
			\remove, {
				coll.do({ arg t;
//					jTrailView.removeTrack( t );
					upd2 = mapUpdTrack.removeAt( t );
					if( upd2.notNil, { upd2.remove });
				});
//				this.prRecalcTrackBounds;
			});
		});
		
//		updTrackSel = UpdateListener.newFor( doc.selectedTracks, { arg upd, sc, what ... coll;
//			if( (what === \add) or: { what === \remove }, {
//				ggTrackHeader.refresh;
//			});
//		});
		
		doc.volEnv.addListener( this );
		
		view.onClose = {
			jTimelinePanel.dispose;
//			updTimeline.remove;
			updSelection.remove;
			updTransport.remove;
			updTracks.remove;
			updTrackSel.remove;
			doc.volEnv.removeListener( this );
		};
		
//		view.mouseDownAction	= { arg ... args; this.mousePressed( Bosque.createMouseEvent( *args ))};
//		view.mouseMoveAction	= { arg ... args; this.mouseDragged( Bosque.createMouseEvent( *args ))};
//		view.mouseUpAction		= { arg ... args; this.mouseReleased( Bosque.createMouseEvent( *args ))};

//		ggTrackHeader.mouseDownAction  = { arg ... args; this.prMousePressedTrackHeader( Bosque.createMouseEvent( *args ))};
//
		ggVolEnv.mouseDownAction = { arg ... args; this.prMousePressedVolEnv( Bosque.createMouseEvent( *args ))};
		ggVolEnv.mouseUpAction = { arg ... args; ggVolLab.string = ""; this.prMouseReleasedVolEnv( Bosque.createMouseEvent( *args ))};
	}
	
	prStartRoutLevelStrings {
		^{
			inf.do({
				this.prUpdateLevelStrings;
				0.03.wait;
			});
		}.fork;
	}
	
	prUpdateLevelStrings {
		var frame = doc.transport.currentFrame;
		doc.tracks.do({ arg t; if( t.trackID >= 0, { t.updateLevelString( frame )})});
	}

//	prRecalcTrackBounds {
//		var y = 0;
//		doc.tracks.do({ arg t; t.y = y; y = y + t.height; /* [ t, t.y, t.height ].postln */});
//		trackVScale = if( y == 0, 1, { 1/y });
////		[ "trackVScale", trackVScale ].postln;
//		ggTrackHeader.refresh;
//	}
	
	tool_ { arg type;
		tool = type;
	}

	mousePressed { arg e;
		switch( tool,
		\move,   { this.prMousePressedMoveResize( e )},
		\resize, { this.prMousePressedMoveResize( e )},
		\env,    { envTool.mousePressed( e )}
		);
	}

	mouseDragged { arg e;
		switch( tool,
		\move,   { this.prMouseDraggedMove( e )},
		\resize, { this.prMouseDraggedResize( e )},
		\env,    { envTool.mouseDragged( e )}
		);
	}
	
	mouseReleased { arg e;
		switch( tool,
		\move,   { this.prMouseReleasedMove( e )},
		\resize, { this.prMouseReleasedResize( e )},
		\env,    { envTool.mouseReleased( e )}
		);
	}
	
	// -------------------------------- Volume Envelope --------------------------------

	prMousePressedVolEnv { arg e;
		var frame, level, bounds, visiSpan, stake, ce, sel, stakes, indices;
		
		if( e.clickCount == 2, {
			bounds	= e.component.bounds;
			visiSpan	= doc.timelineView.span;
			frame	= ((e.x / bounds.width) * visiSpan.length).asInteger + visiSpan.start;
			level	= (e.y / bounds.height).linlin( 0, 1, 12, -48 );
			stake	= BosqueEnvStake( frame, level );
			ce		= JSyncCompoundEdit( "Edit Envelope" );
			doc.volEnv.editBegin( ce );
			doc.volEnv.editAdd( this, stake, ce );
			doc.volEnv.editEnd( ce );
			doc.undoManager.addEdit( ce.performAndEnd );
		}, { if( e.isAltDown, {
			sel		= e.component.selection;
			if( volEnvDispFakeStart, { sel[0] == false });
			if( volEnvDispFakeStop,  { sel[sel.size-1] == false });
			indices	= sel.collect({ arg val, idx; if( val, idx )}).reject({ arg x; x.isNil });
			stakes	= volEnvDispStakes.select({ arg stake, idx; indices.includes( idx )});
			if( stakes.size > 0, {
				ce		= JSyncCompoundEdit( "Edit Envelope" );
				doc.volEnv.editBegin( ce );
				doc.volEnv.editRemoveAll( this, stakes, ce );
				doc.volEnv.editEnd( ce );
				doc.undoManager.addEdit( ce.performAndEnd );
			});
		})});
	}
	
	prMouseReleasedVolEnv { arg e;
		var sel, newVals, frame, level, visiSpan, stakesRemove, stakesAdd, ce;
		sel = e.component.selection;
		if( volEnvDispFakeStart, { sel[0] == false });
		if( volEnvDispFakeStop,  { sel[sel.size-1] == false });
		newVals	= e.component.value;
		visiSpan	= doc.timelineView.span;
//		[ "SEL", sel ].postln;
//		[ "OLD", volEnvDispValues ].postln;
//		[ "NEW", newVals ].postln;
		newVals[0].size.do({ arg i; if( sel[i] and: {
			(newVals[0][i] != volEnvDispValues[0][i]) or: { newVals[1][i] != volEnvDispValues[1][i] }}, {
//				("YES AT " ++ i).postln;
				stakesRemove	= stakesRemove.add( volEnvDispStakes[i] );
				frame	= (newVals[0][i] * visiSpan.length).asInteger + visiSpan.start;
				level	= newVals[1][i].linlin( 0, 1, -48, 12 );
				stakesAdd	= stakesAdd.add( BosqueEnvStake( frame, level ));
			});
		});
		if( stakesRemove.size > 0, {
			ce		= JSyncCompoundEdit( "Edit Envelope" );
			doc.volEnv.editBegin( ce );
			doc.volEnv.editRemoveAll( this, stakesRemove, ce );
			doc.volEnv.editAddAll( this, stakesAdd, ce );
			doc.volEnv.editEnd( ce );
			doc.undoManager.addEdit( ce.performAndEnd );
		});
	}
	
	prSetVolEnv {
		var visiSpan, scale, offset, volEnvDispFakeStart, volEnvDispFakeStop, idx, stake, stake2, volEnv, level;
		visiSpan 			= doc.timelineView.span;
		volEnv			= doc.volEnv;
		volEnvDispStakes	= volEnv.getRange( visiSpan );
		stake			= volEnvDispStakes.first;
		volEnvDispFakeStart	= stake.notNil and: { stake.pos != visiSpan.start };
		if( volEnvDispFakeStart, {
			idx	= volEnv.indexOf( stake ) - 1;
			if( idx >= 0, {
				stake2	= volEnv.get( idx );
				level	= stake2.level + ((stake.level - stake2.level) * ((visiSpan.start - stake2.pos) / (stake.pos - stake2.pos)));
			}, {
				level	= stake.level;
			});
			volEnvDispStakes = [ BosqueEnvStake( visiSpan.start, level )] ++ volEnvDispStakes;
		});
		stake			= volEnvDispStakes.last;
		volEnvDispFakeStop	= stake.notNil and: { stake.pos != visiSpan.stop };
		if( volEnvDispFakeStop, {
			idx	= volEnv.indexOf( stake ) + 1;
			if( idx < volEnv.numStakes, {
				stake2	= volEnv.get( idx );
				level	= stake.level + ((stake2.level - stake.level) * ((visiSpan.stop - stake.pos) / (stake2.pos - stake.pos)));
			}, {
				level	= stake.level;
			});
			volEnvDispStakes = volEnvDispStakes ++ BosqueEnvStake( visiSpan.stop, level );
		});
		scale	= visiSpan.length.max( 1 ).reciprocal;
		offset	= visiSpan.start.neg;
		volEnvDispValues	= [	volEnvDispStakes.collect({ arg stake; (stake.pos + offset) * scale }),
							volEnvDispStakes.collect({ arg stake; stake.level.linlin( -48, 12, 0, 1 )})];
		ggVolEnv.value = volEnvDispValues;
// too complicated, just forget about it
//		ggVolEnv.setEditable( 0, volEnvDispFakeStart.not );
//		ggVolEnv.setEditable( stakes.size - 1, volEnvDispFakeStop.not );
		ggVolEnv.setThumbSize( -1, 5 );
		if( volEnvDispFakeStart, { ggVolEnv.setThumbSize( 0, 0 )});
		if( volEnvDispFakeStop, { ggVolEnv.setThumbSize( volEnvDispStakes.size - 1, 0 )});
		
//		[ volEnvDispFakeStart, volEnvDispFakeStop, stakes.class ].postln;
	}
	
	trailModified { arg e;
//		if( e.span.touches( doc.timelineView.span )) ...
		this.prSetVolEnv;
	}

	// -------------------------------- Track Header --------------------------------

//	prMousePressedTrackHeader { arg e;
//		var t, y, edit;
//		
//		y	= e.y / e.component.bounds.height.max(1) / trackVScale;
//		t	= doc.tracks.detect({ arg t; (t.y <= y) and: { t.y + t.height > y }});
//
//		if( t.isNil, { ^this });
//		
//		if( e.isAltDown, {	// toggle mute
//			edit		= JSyncCompoundEdit( "Change Track Mute" );
//			t.editMute( this, t.muted.not, edit );
//			edit.performEdit.end;
//		}, {
//			if( doc.selectedTracks.includes( t ), {
//				if( e.isShiftDown, {   // deselect single
//					edit = BosqueEditRemoveSessionObjects( this, doc.selectedTracks, [ t ], false ).performEdit;
//				});
//			}, {
//				if( e.isShiftDown or: { doc.selectedTracks.isEmpty }, {  // add single to selection
//					edit = BosqueEditAddSessionObjects( this, doc.selectedTracks, [ t ], false).performEdit;
//				}, {					// replace selection with single
//					edit = JSyncCompoundEdit.new;
//					edit.addPerform( BosqueEditRemoveSessionObjects( this, doc.selectedTracks, doc.selectedTracks.getAll, false ));
//					edit.addPerform( BosqueEditAddSessionObjects( this, doc.selectedTracks, [ t ], false));
//					edit.performEdit.end;
//				});
//			});
//		});
//		if( edit.notNil, {
//			doc.undoManager.addEdit( edit );
//		});
//	}

	// -------------------------------- Move + Resize Tool --------------------------------

	prMousePressedMoveResize { arg e;
		var edit;
		
		dragStartX	= e.x;
		dragStartY	= e.y;
		dragStarted	= false;
		dragStartPos	= e.frame;
		dragStartTrack= e.track;

		dragValid		= dragStartTrack.notNil;
		if( dragValid.not, { ^this });	// invalid

		dragStartStake = e.stake;
		if( dragStartStake.notNil, {
			if( doc.selectedRegions.includes( dragStartStake ), {
				if( e.isShiftDown, {   // deselect single
					edit = BosqueEditRemoveSessionObjects( this, doc.selectedRegions, [ dragStartStake ], false ).performEdit;
				});
			}, {
				if( e.isShiftDown or: { doc.selectedRegions.isEmpty }, {  // add single to selection
					edit = BosqueEditAddSessionObjects( this, doc.selectedRegions, [ dragStartStake ], false).performEdit;
				}, {					// replace selection with single
					edit = JSyncCompoundEdit.new;
					edit.addPerform( BosqueEditRemoveSessionObjects( this, doc.selectedRegions, doc.selectedRegions.getAll, false ));
					edit.addPerform( BosqueEditAddSessionObjects( this, doc.selectedRegions, [ dragStartStake ], false));
					edit.performEdit.end;
				});
			});
		}, {
			if( e.isShiftDown.not and: { doc.selectedRegions.isEmpty.not }, {   // deselect all
				edit = BosqueEditRemoveSessionObjects( this, doc.selectedRegions, doc.selectedRegions.getAll, false ).performEdit;
			});
		});
		if( edit.notNil, {
			doc.undoManager.addEdit( edit );
		});
		dragValid = dragStartStake.notNil and: { doc.selectedRegions.includes( dragStartStake )};
	}

	// -------------------------------- Move Tool --------------------------------
	
	prMouseDraggedMove { arg e;
		var dx, dy;

		if( dragValid.not, { ^this });
		
		dx = e.x - dragStartX;
		dy = e.y - dragStartY;
		if( dragStarted.not, {
			if( (dx.squared + dy.squared) > 16, {
				dragStarted = true;
				dragConstrainH = false;
				dragConstrainV = false;
				if( e.isShiftDown, {
					if( abs( dy ) > abs( dx ), {
						dragConstrainV = true;
						dx = 0;
					}, {
						dragConstrainH = true;
						dy = 0;
					});
				});
				jTrailView.setDrag( dx, dy, 0, 0 );
				jTrailView.setDragPainted( true );
			});
		}, {
			if( dragConstrainV, { dx = 0 });
			if( dragConstrainH, { dy = 0 });
			jTrailView.setDrag( dx, dy, 0, 0 );	// XXX
		});
	}
	
	prMouseReleasedMove { arg e;
		var dragTrack, deltaTrack, deltaTime, ce, sel, duplicate, newStart, newTrack, disposeStake;
		
		dragValid = false;
		if( dragStarted, {
			dragTrack = e.track;
			deltaTrack = if( dragConstrainH, 0, { if( dragTrack.notNil, { doc.tracks.indexOf( dragTrack ) - doc.tracks.indexOf( dragStartTrack )}, 0 )});
			
			jTrailView.setDragPainted( false );
			dragStarted = false;
			deltaTime = if( dragConstrainV, 0, { e.frame - dragStartPos });
			if( (deltaTime != 0) or: { deltaTrack != 0 }, {
				ce = JSyncCompoundEdit.new;
				sel = doc.selectedRegions.getAll;
//				sel.postln;
				duplicate = e.isAltDown && e.isMetaDown;
				ce.addPerform( BosqueEditRemoveSessionObjects( this, doc.selectedRegions, sel, false ));
//				"////////////// OK : voher:".postln; doc.trail.debugDump;
				doc.trail.editBegin( ce );
				if( duplicate.not, {
					doc.trail.editRemoveAll( this, sel, ce );
				});
//				"////////////// after removeAll".postln; doc.trail.debugDump;
				sel = sel.collect({ arg stake;
					newStart = (stake.span.start + deltaTime).clip( 0, doc.timeline.span.length - stake.span.length );
					newTrack = doc.tracks[ (doc.tracks.indexOf( stake.track ) + deltaTrack).clip( 0, doc.tracks.size - 1 )];
					disposeStake = nil;

// THIS IS FAULTY FOR SOME REASON (REPLACETRACK) XXX
//					if( newStart != stake.span.start, {
//						stake = disposeStake = stake.shiftVirtual( newStart - stake.span.start );
//					});
//					if( newTrack != stake.track, {
//						stake = stake.replaceTrack( newTrack );
//						if( disposeStake.notNil, { disposeStake.dispose });
//						disposeStake = stake;
//					});

					stake = disposeStake = stake.replaceTrackAndMove( newTrack, newStart );

					if( duplicate and: { disposeStake.isNil }, {
						stake = disposeStake = stake.duplicate;
					});
					stake;
				});
				sel.do({ arg stake; doc.trail.editClearSpan( this, stake.span, nil, ce, { arg stake2; stake.track == stake2.track })});
//				"////////////// after clear".postln;
				doc.trail.editAddAll( this, sel, ce );
//				"////////////// after addAll".postln; doc.trail.debugDump;
				doc.trail.editEnd( ce );
				ce.addPerform( BosqueEditAddSessionObjects( this, doc.selectedRegions, sel, false));
				ce.performEdit.end;
//				"////////////// after perform".postln; doc.trail.debugDump;
				doc.undoManager.addEdit( ce );
			});
		});
	}

	// -------------------------------- Resize Tool --------------------------------
	
	prMouseDraggedResize { arg e;
		var dx;

		if( dragValid.not, { ^this });
		
		dx = e.x - dragStartX;
		if( dragStarted.not, {
			if( dx.abs > 4, {
				dragStarted = true;
				dragResizeEdge = if( dragStartPos < (dragStartStake.span.start + dragStartStake.span.length.div(2)), \left, \right );
				if( dragResizeEdge === \left, {
					jTrailView.setDrag( dx, 0, dx.neg, 0 );
				}, {
					jTrailView.setDrag( 0, 0, dx, 0 );
				});
				jTrailView.setDragPainted( true );
			});
		}, {
			if( dragResizeEdge === \left, {
				jTrailView.setDrag( dx, 0, dx.neg, 0 );
			}, {
				jTrailView.setDrag( 0, 0, dx, 0 );
			});
		});
	}
	
	prMouseReleasedResize { arg e;
		var deltaTime, ce, sel, disposeStake, newStart, newStop, oldFade;
		dragValid = false;
		if( dragStarted, {
			jTrailView.setDragPainted( false );
			dragStarted = false;
			deltaTime = e.frame - dragStartPos;
			if( deltaTime != 0, {
				ce = JSyncCompoundEdit.new;
				sel = doc.selectedRegions.getAll;
				ce.addPerform( BosqueEditRemoveSessionObjects( this, doc.selectedRegions, sel, false ));
				doc.trail.editBegin( ce );
				doc.trail.editRemoveAll( this, sel, ce );
				sel = sel.collect({ arg stake;
					if( dragResizeEdge === \left, {
						newStart = (stake.span.start + deltaTime).clip( 0, stake.span.stop - 1000 );
						if( stake.isKindOf( BosqueAudioRegionStake ), {
							newStart = newStart.max( stake.span.start - stake.fileStartFrame );
						});
						if( newStart != stake.span.start, {
							oldFade	= stake.fadeIn;
							stake	= disposeStake = stake.replaceStart( newStart );
							if( oldFade.numFrames != stake.fadeIn.numFrames, {
								stake = stake.replaceFadeIn( oldFade.replaceFrames( min( oldFade.numFrames, stake.span.length - stake.fadeOut.numFrames )));
								disposeStake.dispose;
							});
						});
					}, {
						newStop = (stake.span.stop + deltaTime).clip( stake.span.start + 1000, doc.timeline.span.length );
						if( stake.isKindOf( BosqueAudioRegionStake ), {
							newStop = newStop.min( stake.span.start + stake.audioFile.numFrames - stake.fileStartFrame );
						});
						if( newStop != stake.span.stop, {
							oldFade	= stake.fadeOut;
							stake	= disposeStake = stake.replaceStop( newStop );
							if( oldFade.numFrames != stake.fadeOut.numFrames, {
								stake = stake.replaceFadeOut( oldFade.replaceFrames( min( oldFade.numFrames, stake.span.length - stake.fadeIn.numFrames )));
								disposeStake.dispose;
							});
						});
					});
					stake;
				});
				sel.do({ arg stake; doc.trail.editClearSpan( this, stake.span, nil, ce, { arg stake2; stake.track == stake2.track })});
				doc.trail.editAddAll( this, sel, ce );
				doc.trail.editEnd( ce );
				ce.addPerform( BosqueEditAddSessionObjects( this, doc.selectedRegions, sel, false));
				ce.performEdit.end;
				doc.undoManager.addEdit( ce );
			});
		});
	}
}
