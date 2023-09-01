/*
 *	BosqueTimelineEditor
 *	(Bosque)
 *
 *	Copyright (c) 2007-2009 Hanns Holger Rutz. All rights reserved.
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
 *	Class dependancies: ScissUtil, ScissPlus, BosqueBoxGrid
 *
 *	@author	Hanns Holger Rutz
 *	@version	0.37, 27-Jun-09
 */
BosqueTimelineEditor : Object {
	var <bosque;
	var <winTimeline;
	var <winMenu;
	var <winCollections;
	var <winTransport;
	var <observer;
	var <panel;
//	var doc;

	*new { arg bosque;
		^super.new.prInit( bosque );
	}
	
	prInit { arg argBosque;
		bosque	= argBosque ?? { Bosque.default };
//		bosque.doWhenSwingBooted({ this.prMakeGUI });
//		doc		= bosque.session;
//		this.prMakeGUI;
	}
	
	init {
		var view, view2, updAudioFiles, updTimeline, ggFileList, ggBusList;
		var fntSmall, fntBig, flow;
		var ggPlay, ggStop, ggLoop, updTransport, updDirty, updSelectedTracks, updTracks;
		var vx, vw, clpseYZoom, ggTabColl, updBusConfigs, ggBusMatrix, ggBusGain, ggBusApply, busSelCol, busSelRow, busConn;
		var ggPlayPos, taskPlayPos, fTaskPlayPos, fPlayPosStr, ggTrackList, ggVolume, updVolume, clpseVolume;
		var scrB = JSCWindow.screenBounds;
		var mg, ggToolName;
		var doc = bosque.session;
		
		winTimeline = JSCWindow( "Timeline", Rect( 410, 134, scrB.width - 580, scrB.height - 180 )).userCanClose_( false );
//		ScissUtil.positionOnScreen( winTimeline, 0.333, 0.333 );
		winCollections = JSCWindow( "Collections", Rect( winTimeline.bounds.right + 10, winTimeline.bounds.bottom - 100, 240, 390 )).userCanClose_( false ).alwaysOnTop_( true );
		winMenu = JSCWindow( "Menu", Rect( winTimeline.bounds.right + 4, winTimeline.bounds.top + winTimeline.bounds.height - 200, 128, 200 ), resizable: false ).alwaysOnTop_( true ).userCanClose_( false );
		winTransport = JSCWindow( "Transport", Rect( winTimeline.bounds.left, winTimeline.bounds.top - 106, 344, 80 ), resizable: false ).alwaysOnTop_( true ).userCanClose_( false ); // .alwaysOnTop_( true );
		observer = BosqueObserver.new;
//		view = JSCVLayoutView( winCollections, winCollections.view.bounds ).resize_( 5 );

		fntSmall	= JFont( "Lucida Grande", 10 );
		fntBig	= JFont( "Helvetica", 24 );
				
		// ----------------------------- Collections Window -----------------------------

		ggTabColl = JSCTabbedPane( winCollections, winCollections.view.bounds ).resize_( 5 );
		////// Audio File Tab //////
		view = JSCCompositeView( ggTabColl, ggTabColl.bounds ).resize_( 5 );
//		JSCStaticText( view, Rect( 2, 2, 80, 20 )).string_( "Files" );
		JSCButton( view, Rect( 160, 2, 36, 20 )).resize_( 3 )
			.canFocus_( false )
			.states_([[ "+" ]])
			.action_({ arg b;
				SwingDialog.getPaths({ arg paths;
					paths.do({ arg path; var ce;
						ce = JSyncCompoundEdit( "Add Audio File" );
						this.prAudioFileAdd( doc, path, ce, { arg af; doc.undoManager.addEdit( ce.performAndEnd )});
					});
				});
			});
		JSCButton( view, Rect( 200, 2, 36, 20 )).resize_( 3 )
			.canFocus_( false )
			.states_([[ "-" ]])
			.action_({ arg b; var af = doc.audioFiles[ ggFileList.value ? -1 ], stakes;
				if( af.notNil, {
					stakes = doc.trail.getAllAudioStakes( af );
					if( stakes.isEmpty, {
						this.prAudioFileRemove( af, doc );
					}, {
						this.showConfirmDialog( "Remove Audio File", "The Audio File is in use (" ++ stakes.size ++
							" regions).\nRemove anyway?", \yesno, \question, { arg result; if( result === \yes, {
								this.prAudioFileRemove( af, doc )})});
					});
				});
			});
		ggFileList = JSCListView( view, Rect( 0, 24, 240, 340 )).resize_( 5 )
			.allowsDeselection_( true )
			.action_({ arg b;
//				("Hoscho " ++ b.items[ b.value ]).postln;
			});
		updAudioFiles = UpdateListener.newFor( doc.audioFiles, { arg upd, obj, what ... params;
			if( (what === \add) or: { what === \remove }, {
				ggFileList.items = obj.getAll.collect({ arg x; x.name });
			});
		});
		ggFileList.onClose = { updAudioFiles.remove };

		JSCPopUpMenu( view, Rect( 160, 368, 76, 24 )).canFocus_( false ).font_( fntSmall ).resize_( 9 )
			.items_([ "Action", "--------", "Drop Region", "File Replace File", "Regions Replace File", "Consolidate" ])
			.action_({ arg b; var value = b.value, af, track;
				b.value = 0;
				af 		= doc.audioFiles[ ggFileList.value ? -1 ];
				track	= doc.selectedTracks.detect({ arg x; x.trackID >= 0 });
				switch( value - 2,
				0, { this.prAudioFileAddRegion( af, doc, doc.timelineView.cursor.position, track )},
				1, { this.prAudioFileReplaceFile( af, doc )},
				2, { this.prAudioFileReplaceRegions( af, doc )},
				3, { this.prAudioFileConsolidate( doc )}
				);
			});
	
//		JSCButton( view, Rect( 160, 268, 36, 20 ))
//			.canFocus_( false )
//			.states_([[ "-" ]])
//			.action_({ arg b;
//			
//			});

		////// Busses Tab //////
		view = JSCCompositeView( ggTabColl, ggTabColl.bounds ).resize_( 5 );

		JSCButton( view, Rect( 160, 2, 36, 20 )).resize_( 3 )
			.canFocus_( false )
			.states_([[ "+" ]])
			.action_({ arg b;
				this.prBusAddDlg( doc );
			});
		JSCButton( view, Rect( 200, 2, 36, 20 )).resize_( 3 )
			.canFocus_( false )
			.states_([[ "-" ]])
			.action_({ arg b; var cfg = doc.busConfigs[ ggBusList.value ? -1 ], tracks;
				if( cfg.notNil, {
					tracks = doc.tracks.select({ arg t; t.busConfig == cfg });
					if( tracks.isEmpty, {
						this.prBusConfigRemove( cfg, doc );
					}, {
						this.showConfirmDialog( "Remove Bus Config", "The Bus Config is in use (" ++ tracks.size ++
							" tracks).\nRemove anyway?", \yesno, \question, { arg result; if( result === \yes, {
								this.prBusConfigRemove( cfg, doc )})});
					});
				});
			});
//		JSCStaticText( view, Rect( 2, 2, 80, 20 )).string_( "Busses" );
		ggBusList = JSCListView( view, Rect( 0, 24, 240, 140 )).resize_( 5 )
			.allowsDeselection_( true )
//			.value_( nil )
			.beginDragAction_({ arg b;
				doc.busConfigs[ b.value ? -1 ];
			})
			.action_({ arg b; var cfg, bounds, fNode;
				cfg = doc.busConfigs[ b.value ? -1 ];
				busSelCol		= nil;
				busSelRow		= nil;
				busConn		= nil;
				if( ggBusMatrix.notNil, {
					ggBusMatrix.asView.remove;
					ggBusMatrix = nil;
				});
				ggBusGain.enabled = false;
				ggBusApply.enabled = false;
				if( cfg.notNil, {
					GUI.useID( \swing, {
						bounds = b.bounds;
						bounds = Rect( bounds.left + 34, bounds.bottom + 4, bounds.width - 30, 220 );
						ggBusMatrix = BosqueBoxGrid( b.parent, bounds, columns: cfg.numOutputs, rows: cfg.numInputs );
						ggBusMatrix.setFillColor_( observer.colors[0] );
						ggBusMatrix.setTrailDrag_( true, false );
						ggBusMatrix.asView.resize_( 8 );
						busConn	= cfg.connections ?? { 0 ! cfg.numOutputs ! cfg.numInputs };
						cfg.numOutputs.do({ arg col;
							cfg.numInputs.do({ arg row;
								if( busConn[row][col] > 0, {
									ggBusMatrix.setBoxColor_( col, row, observer.colors[ busConn[row][col].ampdb.linlin( -10, 0, 18, 0 ).asInteger ]);
									ggBusMatrix.setState_( col, row, true );
								}, {
									ggBusMatrix.setState_( col, row, false );
								});
							});
						});
						fNode	= { arg nodeLoc, state;
							state 	= ggBusMatrix.getState( nodeLoc[0], nodeLoc[1] );
							ggBusGain.enabled = state;
							busSelCol = nodeLoc[0];
							busSelRow = nodeLoc[1];
							if( state, {
								busConn[ busSelRow ][ busSelCol ] = 1.0;
								ggBusGain.valueAction = 1.0;
							}, {
								busConn[ busSelRow ][ busSelCol ] = 0.0;
							});
							ggBusApply.enabled = true;
						};
						ggBusMatrix.nodeDownAction_( fNode );
						ggBusMatrix.nodeTrackAction_( fNode );
					});
				});
			});
		ggBusGain = JSCSlider( view, Rect( 0, 170, 26, 192 )).resize_( 7 )
			.step_( 0.1 )
			.enabled_( false )
			.action_({ arg b; var db;
				if( busSelCol.notNil && busSelRow.notNil && busConn.notNil, {
					db = b.value.linlin( 0, 1, -10, 0 );
					db.postln;
					busConn[ busSelRow ][ busSelCol ] = db.dbamp;
					ggBusMatrix.setBoxColor_( busSelCol, busSelRow, observer.colors[ b.value.linlin( 0, 1, 18, 0 ).asInteger ]);
				});
			});
		ggBusApply = JSCButton( view, Rect( 0, 366, 30, 20 )).resize_( 7 )
			.font_( fntSmall )
			.enabled_( false )
			.states_([[ "Apply" ]])
			.action_({ arg b; var cfg, ce;
				cfg = doc.busConfigs[ ggBusList.value ? -1 ];
				if( cfg.notNil and: { busConn.notNil }, {
					ce = JSyncCompoundEdit( "Change Bus Connections" );
					cfg.editConnections( this, busConn, ce );
					doc.undoManager.addEdit( ce.performEdit.end );
					ggBusApply.enabled = false;
				});
			});

		updBusConfigs = UpdateListener.newFor( doc.busConfigs, { arg upd, obj, what ... coll;
			if( (what === \add) or: { what === \remove }, {
				ggBusList.items = obj.getAll.collect({ arg x; x.name });
				ggBusList.valueAction = obj.size - 1;
			});
		});
//		updSelectedTracks = UpdateListener.newFor( doc.selectedTracks, { arg upd, sc, what ... coll;
//			if( (what === \add) or: { what === \remove }, {
//				ggBusList.value = if( doc.selectedTracks.notNil, { doc.tracks.indexOf( doc.selectedTracks[ 0 ])});
//			});
//		});
		ggBusList.onClose = { updBusConfigs.remove; /* updSelectedTracks.remove */};

		////// Tracks Tab //////
		view = JSCCompositeView( ggTabColl, ggTabColl.bounds ).resize_( 5 );

		JSCButton( view, Rect( 160, 2, 36, 20 )).resize_( 3 )
			.canFocus_( false )
			.states_([[ "+" ]])
			.action_({ arg b;
				this.addTrack( doc );
			});
		JSCButton( view, Rect( 200, 2, 36, 20 )).resize_( 3 )
			.canFocus_( false )
			.states_([[ "-" ]])
			.action_({ arg b;
				this.prTrackRemove( doc );
			});
//		JSCStaticText( view, Rect( 2, 2, 80, 20 )).string_( "Tracks" );
		ggTrackList = JSCListView( view, Rect( 0, 24, 240, 340 )).resize_( 5 )
			.allowsDeselection_( true )
//			.value_( nil )
			.beginDragAction_({ arg b, trackOff;
				trackOff = block { arg break; doc.tracks.do({ arg t, i; if( t.trackID >= 0, { break.value( i )})}); nil };
				if( b.value >= 0, {
					doc.tracks[ b.value + trackOff ];
				});
			})
			.action_({ arg b; var t, ce, trackOff;
				trackOff = block { arg break; doc.tracks.do({ arg t, i; if( t.trackID >= 0, { break.value( i )})}); nil };
				t = if( b.value >= 0, {
					doc.tracks[ b.value + trackOff ];
				});
				ce = JSyncCompoundEdit.new;
				ce.addPerform( BosqueEditRemoveSessionObjects( this, doc.selectedTracks, doc.selectedTracks.getAll, false ));
				ce.addPerform( BosqueEditAddSessionObjects( this, doc.selectedTracks, [ t ], false));
				doc.undoManager.addEdit( ce.performEdit.end );
			});
			
		updTracks = UpdateListener.newFor( doc.tracks, { arg upd, obj, what ... coll;
			if( (what === \add) or: { what === \remove }, {
				ggTrackList.items = obj.getAll.select({ arg x; x.trackID >= 0 }).collect({ arg x; x.name });
//				ggBusList.valueAction = obj.size - 1;
			});
		});
		updSelectedTracks = UpdateListener.newFor( doc.selectedTracks, { arg upd, sc, what ... coll; var trackOff, idx;
			if( (what === \add) or: { what === \remove }, {
				trackOff = block { arg break; doc.tracks.do({ arg t, i; if( t.trackID >= 0, { break.value( i )})}); nil };
				ggTrackList.value = if( doc.selectedTracks.notNil, {
					idx = doc.tracks.indexOf( doc.selectedTracks.detect({ arg x; x.trackID >= 0 }));
					if( idx.notNil, { idx = idx - trackOff });
					idx;
				});
			});
		});
		ggTrackList.onClose = { updTracks.remove; updSelectedTracks.remove };

		ggTabColl.setTitleAt( 0, "Audio Files" );
		ggTabColl.setTitleAt( 1, "Busses" );
		ggTabColl.setTitleAt( 2, "Tracks" );

		// ----------------------------- Timeline Window -----------------------------
		
		view = winTimeline.view;

//		BosqueTimelineAxis( doc, view, Rect( 60, 20, view.bounds.width - 60, 15 ));
		panel = BosqueTimelinePanel( doc, view, Rect( 0, 35, view.bounds.width, view.bounds.height - 35 ));
//		BosqueTimelineScroll( doc, view, Rect( 0, view.bounds.height - 16, view.bounds.width - 16, 16 ));

		vx = 1; vw = 80;
//		JSCPopUpMenu( view, Rect( vx, 1, vw, 16 )).font_( fntSmall ).canFocus_( false )
//			.items_([ "File", "--------", "New", "Open...", "Append...", "--------", "Save", "Save As..." ])
//			.action_({ arg b; var value = b.value;
//				b.value = 0;
//				switch( value - 2,
//				0, { this.prFileNew( doc )},
//				1, { this.prFileOpen( doc )},
//				2, { this.prFileAppend( doc )},
//				4, { if( doc.isDirty, {
//					if( doc.path.notNil, {
//						this.prFileSave( doc, doc.path );
//					}, {
//						this.prFileSaveAs( doc );
//					});
//				})},
//				5, { this.prFileSaveAs( doc )}
//				);
//			});

		mg = JSCMenuGroup( JSCMenuRoot( bosque.swing ), "File" );
		JSCMenuItem( mg, "New" ).action_({ this.prFileNew( doc )});
		JSCMenuItem( mg, "Open..." ).shortcut_( "meta O" ).action_({ this.prFileOpen( doc )});
		JSCMenuItem( mg, "Append..." ).action_({ this.prFileAppend( doc )});
		JSCMenuSeparator( mg );
		JSCMenuItem( mg, "Save" ).shortcut_( "meta S" ).action_({
			if( doc.isDirty, {
				if( doc.path.notNil, {
					this.prFileSave( doc, doc.path );
				}, {
					this.prFileSaveAs( doc );
				});
			});
		});
		JSCMenuSeparator( mg );
		JSCMenuItem( mg, "Save As..." ).shortcut_( "shift S" ).action_({ this.prFileSaveAs( doc )});

//		vx = vx + vw + 4; vw = 80;
//		JSCPopUpMenu( view, Rect( vx, 1, vw, 16 )).font_( fntSmall ).canFocus_( false )
//			.items_([ "Track", "--------", "Insert", "Remove" ])
//			.action_({ arg b; var value = b.value;
//				b.value = 0;
//				switch( value - 2,
//				0, { this.editAddTrack( doc )},
//				1, { this.prTrackRemove( doc )}
//				);
//			});

//		vx = vx + vw + 4; vw = 80;
//		JSCPopUpMenu( view, Rect( vx, 1, vw, 16 )).font_( fntSmall ).canFocus_( false )
//			.items_([ "Timeline", "--------", "Insert Span...", "Clear Span", "Remove Span", "--------", "Split Objects", "Glue Objects", "--------", "Insert Env", "Insert Func", "Change Gain" ])
//			.action_({ arg b; var value = b.value;
//				b.value = 0;
//				switch( value - 2,
//				0, { this.prTimelineInsertSpan( doc )},
//				1, { this.prTimelineClearSpan( doc )},
//				2, { this.prTimelineRemoveSpan( doc )},
//				4, { this.prTimelineSplitObjects( doc )},
//				5, { this.prTimelineGlueObjects( doc )},
//				7, { this.prTimelineInsertEnv( doc )},
//				8, { this.prTimelineInsertFunc( doc )},
//				9, { this.prTimelineChangeGain( doc )}
//				);
//			});
			
		mg = JSCMenuGroup( JSCMenuRoot( bosque.swing ), "Edit" );
		JSCMenuItem( mg, "Undo" ).shortcut_( "meta Z" ).action_({ this.prEditUndo( doc )});
		JSCMenuItem( mg, "Redo" ).shortcut_( "meta shift Z" ).action_({ this.prEditRedo( doc )});
		JSCMenuSeparator( mg );
		JSCMenuItem( mg, "Cut" ).shortcut_( "meta X" ).action_({ this.prEditCut( doc )});
		JSCMenuItem( mg, "Copy" ).shortcut_( "meta C" ).action_({ this.prEditCopy( doc )});
		JSCMenuItem( mg, "Paste" ).shortcut_( "meta V" ).action_({ this.prEditPaste( doc )});
		JSCMenuItem( mg, "Delete" ).shortcut_( "BACK_SLASH" ).action_({ this.prEditDelete( doc )});
//		JSCMenuSeparator( mg );
		
//		vx = vx + vw + 4; vw = 80;
//		JSCPopUpMenu( view, Rect( vx, 1, vw, 16 )).font_( fntSmall ).canFocus_( false )
//			.items_([ "Edit", "--------", "Undo", "Redo", "--------", "Cut", "Copy", "Paste", "Delete" ])
//			.action_({ arg b; var value = b.value;
//				b.value = 0;
//				switch( value - 2,
//				0, { this.prEditUndo( doc )},
//				1, { this.prEditRedo( doc )},
//				3, { this.prEditCut( doc )},
//				4, { this.prEditCopy( doc )},
//				5, { this.prEditPaste( doc )},
//				6, { this.prEditDelete( doc )}
//				);
//			});

		mg = JSCMenuGroup( JSCMenuRoot( bosque.swing ), "Timeline" );
		JSCMenuItem( mg, "Insert Span" ).shortcut_( "meta shift E" ).action_({ this.prTimelineInsertSpan( doc )});
		JSCMenuItem( mg, "Clear Span" ).shortcut_( "meta BACK_SLASH" ).action_({ this.prTimelineClearSpan( doc )});
		JSCMenuItem( mg, "Remove Span" ).shortcut_( "meta shift BACK_SLASH" ).action_({ this.prTimelineRemoveSpan( doc )});
		JSCMenuSeparator( mg );
		JSCMenuItem( mg, "Split Objects" ).shortcut_( "meta2 X" ).action_({ this.prTimelineSplitObjects( doc )});
		JSCMenuItem( mg, "Glue Objects" ).shortcut_( "meta2 Y" ).action_({ this.prTimelineGlueObjects( doc )});
		JSCMenuSeparator( mg );
		JSCMenuItem( mg, "Insert Env" ).shortcut_( "meta2 E" ).action_({ this.prTimelineInsertEnv( doc )});
		JSCMenuItem( mg, "Insert Func" ).shortcut_( "meta2 F" ).action_({ this.prTimelineInsertFunc( doc )});
		JSCMenuItem( mg, "Change Gain" ).action_({ this.prTimelineChangeGain( doc )}); // .shortcut_( "meta2 G" )
			
		vx = vx + vw + 4; vw = 80;
		JSCDragSink( view, Rect( vx, 1, vw, 16 ))
			.canReceiveDragHandler_({ arg b; b.class.currentDrag.isString })
			.interpretDroppedStrings_( false )
			.action_({ arg b; var split, path, ce, start, stop;
				split = b.object.split( $: );
				b.object = "";
				if( split.size == 3, {
					path		= split[0];
					start	= split[1].asInteger;
					stop		= split[2].asInteger;
					ce		= JSyncCompoundEdit( "Drag Audio Region" );
					if( (start >= 0) && (start + 1000 < stop), {
						this.prAudioFileAdd( doc, path, ce, { arg af;
							if( stop <= af.numFrames, {
								doc.undoManager.addEdit( ce.performAndEnd );
								this.prCreateClipBoardStake( doc, af, Span( start, stop ));
								b.object = af.name;
							});
						});
					});
				});
			});
			
		updDirty = UpdateListener.newFor( doc, { arg upd, doc, what, param1;
			if( (what === \dirty) or: { what === \path }, {
				winTimeline.name = if( doc.isDirty, { "" ++ 0xE2.asAscii ++ 0x97.asAscii ++ 0x8F.asAscii }, "" )
					++ "Timeline - " ++ doc.name;
			});
		});
		winTimeline.onClose = { updDirty.remove };

		clpseYZoom = Collapse({ arg yZoom;
			panel.jTrailView.server.sendBundle( nil,
				[ '/methodr', '[', '/method', panel.jTrailView.id, \getStakeRenderer, ']',
					\setYZoom, yZoom ],
			[ '/method', panel.jTrailView.id, \triggerRedisplay ]);
		}, 0.2 );
		vx = vx + vw + 4; vw = 80;
		JSCSlider( view, Rect( vx, 1, vw, 20 )).canFocus_( false ).value_( 0 ).action_({ arg b;
			clpseYZoom.instantaneous( b.value.linexp( 0, 1, 1, 16 ));
		});

		vx = vx + vw + 4; vw = 40;
		ggToolName = JSCStaticText( view, Rect( vx, 1, vw, 16 )).font_( fntSmall ).string_( "move" );
		UpdateListener.newFor( this, { arg upd, obj, tool;
			ggToolName.string = tool.asString;
		}, \tool );

		mg = JSCMenuGroup( JSCMenuRoot( bosque.swing ), "Tool" );
		JSCMenuItem( mg, "Move" ).shortcut_( "meta 1" ).action_({ panel.tool = \move; this.tryChanged( \tool, panel.tool )});
		JSCMenuItem( mg, "Resize" ).shortcut_( "meta 2" ).action_({ panel.tool = \resize; this.tryChanged( \tool, panel.tool )});
		JSCMenuItem( mg, "Env" ).shortcut_( "meta 3" ).action_({ panel.tool = \env; this.tryChanged( \tool, panel.tool )});
		
//~ggScroll = ggScrollPane;
//~ggCompo = view2;
//~ggTrail = ggTrailView;

		view.keyDownAction = { arg view, char, modifiers, lala, key;
			var timelineView, length, pos;
//			[ "key", key ].postln;
			if( (modifiers & 0x00040000) != 0, {	// ctrl down
				switch( key,
				37, {				// csr left
					timelineView = doc.timelineView;
					length = timelineView.span.length;
					if( length > 0 and: { length < doc.timeline.span.length }, {
						pos = timelineView.cursor.position.clip( timelineView.span.start, timelineView.span.stop ) - timelineView.span.start;
						length = (length << 1).min( doc.timeline.span.length );
						pos = (timelineView.span.start - pos).clip( 0, doc.timeline.span.length - length );
//						timelineView.span = Span( pos, pos + length );
						doc.editScroll( this, Span( pos, pos + length ));
					});
				},
				39, {				// csr right
					timelineView = doc.timelineView;
					length = timelineView.span.getLength;
					if( length > 256, {
						pos = timelineView.cursor.position.clip( timelineView.span.start, timelineView.span.stop ) - timelineView.span.start;
						length = length >> 1;
//						pos = ((timelineView.span.start + pos) >> 1).clip( 0, timeline.span.length - length );
						pos = (timelineView.span.start + (pos >> 1)).clip( 0, doc.timeline.span.length - length );
//						timelineView.span = Span( pos, pos + length );
						doc.editScroll( this, Span( pos, pos + length ));
					});
				});
			}, { if( (modifiers & 0x00100000) != 0, {	// meta down
				switch( key,
				37, {				// csr left
					timelineView = doc.timelineView;
					doc.editScroll( this, Span( 0, doc.timeline.span.length ));
				});
			})});
		};

		// ----------------------------- Transport Window -----------------------------
		
		view = winTransport.view;
		view.decorator = flow = FlowLayout( view.bounds );
		ggPlay = JSCButton( view, Rect( 0, 0, 40, 40 )).canFocus_( false )
			.font_( fntBig )
			.states_([[ "" ++ 0xE2.asAscii ++ 0x96.asAscii ++ 0xB6.asAscii ]]) // Triangle pointing to the right
			.action_({ arg b;
				doc.transport.play;
			});
		
		ggStop = JSCButton( view, Rect( 0, 0, 40, 40 )).canFocus_( false )
			.font_( fntBig )
			.states_([[ "" ++ 0xE2.asAscii ++ 0x97.asAscii ++ 0xBC.asAscii ]])  // Solid square
			.action_({ arg b;
				doc.transport.stop;
			});
		
		ggLoop = JSCButton( view, Rect( 0, 0, 40, 40 )).canFocus_( false )
			.font_( fntBig )
			.states_([[ "" ++ 0xE2.asAscii ++ 0x99.asAscii ++ 0xBB.asAscii ]])  // Recycling
			.action_({ arg b;
				if( doc.transport.loop.isNil, {
					if( doc.timelineView.selection.span.length >= doc.timeline.rate, {
						doc.transport.loop = doc.timelineView.selection.span;
					});
				}, {
					doc.transport.loop = nil;
				});
//			
//				if( b.value == 0, {
//					doc.transport.loop = nil;
//				}, { if( doc.timelineView.selection.span.length >= doc.timeline.rate, {
//					doc.transport.loop = doc.timelineView.selection.span;
//				}, {
//					b.value = 0;
//				})});
			});
		
		ggPlayPos = JSCStaticText( view, Rect( 0, 0, 160, 40 )).background_( Color.black )
			.stringColor_( Color.yellow ).align_( \center ).font_( JFont( "Andale Mono", 22, 1 )); // .string_( "00:00:00.000" );
		fPlayPosStr = { arg frame; ScissUtil.toTimeString( frame / doc.timeline.rate )};
		updTimeline = UpdateListener.newFor( doc.timelineView, { arg upd, timelineView, what, what2;
			if( taskPlayPos.isPlaying.not and: { (what === \positioned) or: { (what === \changed) && (what2 === \rate) }}, {
				ggPlayPos.string_( fPlayPosStr.value( doc.timelineView.cursor.position ));
			});
		});
		fTaskPlayPos = { inf.do({ ggPlayPos.string_( fPlayPosStr.value( doc.transport.currentFrame )); 0.05.wait })};
		
		updTransport = UpdateListener.newFor( doc.transport, { arg upd, transport, what;
			switch( what,
			\play, {
				ggPlay.states = [[ ggPlay.states.first.first, Color.white, Color.black ]];
				taskPlayPos.stop; taskPlayPos = fTaskPlayPos.fork( AppClock );
			},
			\stop, {
				taskPlayPos.stop;
				ggPlay.states = [[ ggPlay.states.first.first ]];
			},
			\pause, {
				taskPlayPos.stop;
			},
			\resume, {
				taskPlayPos.stop; taskPlayPos = fTaskPlayPos.fork( AppClock );
			},
			\loop, {
				if( transport.loop.isNil, {
					ggLoop.states = [[ ggLoop.states.first.first ]];
				}, {
					ggLoop.states = [[ ggLoop.states.first.first, Color.white, Color.blue ]];
				});
			});
		});
		
		ggPlay.onClose = { updTransport.remove; updTimeline.remove };		
		JSCButton( view, Rect( 0, 0, 40, 40 )).canFocus_( false )
			.font_( fntBig )
			.states_([[ "" ++ 0xE2.asAscii ++ 0x9C.asAscii ++ 0xB6.asAscii ]])  // Solid star
			.action_({ arg b;
				bosque.trackBang.tryChanged;
			});
			
		flow.nextLine;
		
//		ggVolume = JEZSlider( view, 250 @ 20, "Master Gain", ControlSpec( 0.ampdb, 4.ampdb, \db, units: " dB"), { arg ez;
//			bosque.masterVolume = ez.value.dbamp;
//		}, bosque.masterVolume.ampdb, false, 70, 50 );
		GUI.useID( \swing, { ggVolume = EZSlider( view, 250 @ 20, "Master Gain", ControlSpec( 0.ampdb, 4.ampdb, \db, units: " dB"), { arg ez;
			bosque.masterVolume = ez.value.dbamp;
		}, bosque.masterVolume.ampdb, false, 70, 50 ); });
		ggVolume.numberView.maxDecimals_( 1 );
		
		clpseVolume = Collapse({ arg value; ggVolume.value = value.ampdb; }, 0.15, AppClock );
		updVolume = UpdateListener.newFor( bosque, { arg upd, obj, what, param1;
			if( what === \masterVolume, { clpseVolume.instantaneous( param1 )});
		});
		ggVolume.numberView.onClose = { updVolume.remove };
		
		// ----------------------------- Menu Window -----------------------------

		view = JSCVLayoutView( winMenu, winMenu.view.bounds );
		JSCButton( view, Rect( 0, 0, 40, 20 ))
			.states_([[ "Timeline" ], [ "Timeline", Color.white, Color.blue ]])
			.canFocus_( false )
			.action_({ arg b;
				winTimeline.visible = b.value == 1;
				winTimeline.view.focus;
			})
			.valueAction_( 1 );
		JSCButton( view, Rect( 0, 0, 40, 20 ))
			.states_([[ "Collections" ], [ "Collections", Color.white, Color.blue ]])
			.canFocus_( false )
			.action_({ arg b;
				winCollections.visible = b.value == 1;
				winCollections.view.focus;
			});
		JSCButton( view, Rect( 0, 0, 40, 20 ))
			.states_([[ "Transport" ], [ "Transport", Color.white, Color.blue ]])
			.canFocus_( false )
			.action_({ arg b;
				winTransport.visible = b.value == 1;
				winTransport.view.focus;
			})
			.valueAction_( 1 );
		JSCButton( view, Rect( 0, 0, 40, 20 ))
			.states_([[ "Observer" ], [ "Observer", Color.white, Color.blue ]])
			.canFocus_( false )
			.action_({ arg b;
				observer.window.visible = b.value == 1;
				observer.window.view.focus;
			});
			
		JSCPopUpMenu( view, Rect( 0, 0, 40, 20 ))
			.items_([ "Action", "--------", /* "All GUI",*/ "Audio Rec.", "OSC Mem Play", "OSC Rec", "Fullbody" ])
			.canFocus_( false )
			.action_({ arg b; var value = b.value;
				b.value = 0;
				switch( value - 2,
//				0, { Bosque.allGUI },
				0, { Bosque.recorderGUI },
				1, { BosqueOSCMemory.new.addr_( NetAddr.localAddr ).synced_( true ).makeGUI },
				2, { BosqueOSCRecorder.new.synced_( true ).makeGUI },
				3, { EGMFullbodyTracker.start; ~egm_visualizer = EGMFullbodyVisualizer.new }
				);
			});
			
		winMenu.front;
	}
	
	prFileNew { arg doc, okFunc, cancelFunc, ignoreDirty = false;
		if( ignoreDirty.not and: { doc.isDirty }, {
			this.prConfirmUnsaved( doc, { this.prFileNew( doc, okFunc, cancelFunc, true )});
		}, {
			doc.clear;
			okFunc.value;
		});
	}
	
	prCreateClipBoardStake { arg doc, af, afSpan;
		var transferable, owner;
		
		transferable = ();
//		transferable.object = copy;
		transferable.transferDataFlavors = [ \stakeList ];
		transferable.isDataFlavorSupported = { arg thisF, flavor; thisF.transferDataFlavors.includes( flavor )};
		transferable.getTransferData = { arg thisF, flavor, track;
			if( flavor === \stakeList, {
				track = doc.tracks.detect({ arg x; x.trackID >= 0 });
				if( track.notNil, {
					[ BosqueAudioRegionStake( Span( 0, afSpan.length ), af.name, track, fileStartFrame: afSpan.start, audioFile: af )];
				}, {
					[];
				});
			}, { Error( "Unsupported Flavor " ++ flavor ).throw });
		};
		owner = ();
		owner.lostOwnership = { arg thisF, clipBoard, contents; };
		doc.bosque.clipBoard.setContents( transferable, owner );
	}
	
	prFileOpen { arg doc, ignoreDirty = false;
		doc.transport.stop;
		if( ignoreDirty.not and: { doc.isDirty }, {
			this.prConfirmUnsaved( doc, { this.prFileOpen( doc, true )});
		}, {
			SwingDialog.getPaths({ arg result; var path = result.first;
				this.openFile( doc, path );
			}, nil, 1 );
		});
	}
	
	openFile { arg doc, path;
		var f, fPath, text, found;
		this.prFileNew( doc, ignoreDirty: true );
		try {
			f = File( path, "r" );
			text = "{ arg doc; doc" ++ f.readAllString ++ "}";
			f.close;
//					text.postln;
			text.interpret.value( doc );
			doc.path = path;
			found = block { arg break;
				[ "Func.scd", "Func.rtf" ].do({ arg suffix;
					fPath = path.splitext.first ++ suffix;
					if( File.exists( fPath ), {
						("Opening func file '" ++fPath++"'...").postln;
						{ var doc = Document.open( fPath );
						  { doc.text.interpret }.fork( AppClock )
						}.defer;
						break.value( true );
					});
				});
				false;
			};
			if( found.not, {
					("\nFunc file for '" ++path++"' not found!").postln;
			});
		} { arg error;
			error.reportError;
			this.showMessageDialog( "File Open", "Failed to load file\n" ++ path ++ "\n" ++ error.errorString,\error );
		};
	}

	prFileAppend { arg doc, ignoreDirty = false;
		var f, text, oldStakes, oldTracks, oldBusConfigs, oldTimelineLen, ce, ids, id, names, name;
		doc.transport.stop;
		if( ignoreDirty.not and: { doc.isDirty }, {
			this.prConfirmUnsaved( doc, { this.prFileOpen( doc, true )});
		}, {
			SwingDialog.getPaths({ arg result; var path = result.first;
				try {
					f = File( path, "r" );
					text = "{ arg doc; doc" ++ f.readAllString ++ "}";
					f.close;
					
					// temporary clear stuff
					doc.bosque.clipBoard.clear;
					doc.undoManager.discardAllEdits;
					doc.selectedRegions.clear( this );
					doc.selectedTracks.clear( this );
					oldStakes = doc.trail.getAll;
					doc.trail.clear( this );
					oldTimelineLen = doc.timeline.span.length;
					doc.timeline.clear;
					oldTracks = doc.tracks.getAll;
					doc.tracks.clear( this );
					oldBusConfigs = doc.busConfigs.getAll;
					doc.busConfigs.clear(  this );
					
					// now "load" the appended doc
					text.interpret.value( doc );
					
					// now shift all dem stuff
					ce = JSyncCompoundEdit( "Insert Time Span" );
					doc.editInsertTimeSpan( this, Span( 0, oldTimelineLen ), ce );
					doc.undoManager.addEdit( ce.performAndEnd );
					doc.undoManager.discardAllEdits;
					
					// now unificate our old tracks and busconfigs
					oldBusConfigs.do({ arg b;
						if( doc.busConfigs.detect({ arg b2; b2.busCfgID == b.busCfgID }).notNil, {
							ids		= doc.busConfigs.collect({ arg cfg; cfg.busCfgID });
							id		= 0;
							while({ ids.indexOf( id ).notNil }, { id = id + 1 });
							b.busCfgID = id;
						});
						if( doc.busConfigs.find( b.name ).notNil, {
							b.name	= doc.busConfigs.createUniqueName( b.name.asString ++ "_%", 1 );
						});
						doc.busConfigs.add( this, b );
					});
					oldTracks.do({ arg t;
						if( doc.tracks.detect({ arg t2; t2.trackID == t.trackID }).notNil, {
							ids		= doc.tracks.collect({ arg track; track.trackID });
							id		= 0;
							while({ ids.indexOf( id ).notNil }, { id = id + 1 });
							t.trackID	= id;
//							t.name	= doc.tracks("Track " ++ (id + 1)).asSymbol;
//							t.name	= doc.tracks.createUniqueName( "Track_%", id + 1 );
						});
						if( doc.tracks.find( t.name ).notNil, {
							t.name	= doc.tracks.createUniqueName( t.name.asString ++ "_%", 1 );
						});
						doc.tracks.add( this, t );
					});
					doc.trail.addAll( this, oldStakes );

				} { arg error;
					this.showMessageDialog( "File Append", "Failed to load file\n" ++ path ++ "\n" ++ error.errorString,\error );
				};
			}, nil, 1 );
		});
	}
	
	prConfirmUnsaved { arg doc, okFunc, cancelFunc;
		this.showConfirmDialog( "Open", "Current document contains\nunsaved edits.\nSave document first?",
		  \yesnocancel, \warn, { arg result;
			if( result === \yes, {
				if( doc.path.isNil, {
					this.prFileSaveAs( doc, { arg result; if( result, okFunc, cancelFunc )});
				}, {
					if( this.prFileSave( doc, doc.path ), okFunc, cancelFunc );
				});
			}, { if( result === \no, okFunc, cancelFunc )});
		});
	}
	
	prFileSave { arg doc, path;
		var existed, savePath, f;
		try {
			existed	= File.exists( path );
			savePath	= if( existed, { path ++ ".tmp" }, path );
			f		= File( savePath, "w" );
			doc.storeModifiersOn( f );
			f.close;
			if( existed, {
				if( ("rm \"" ++ path ++ "\"").systemCmd != 0, {
					this.showMessageDialog( "File Save As", "Failed to delete previous version of file\n" ++ path ++ "\n\nNew version remains at\n" ++ savePath, \error );
					^false;
				});
				if( ("mv \"" ++ savePath ++ "\" \"" ++ path ++ "\"").systemCmd != 0, {
					this.showMessageDialog( "File Save As", "Failed to rename file to\n" ++ path ++ "\n\nNew version remains at\n" ++ savePath, \error );
					^false;
				});
			});
			doc.path = path;
			doc.undoManager.discardAllEdits;
			^true;
		} { arg error;
			error.reportError;
			this.showMessageDialog( "File Save", "Failed to save file to\n" ++ path ++ "\n" ++ error.errorString,\error );
			^false;
		};
	}
	
	prFileSaveAs { arg doc, onComplete;
		SwingDialog.savePanel({ arg path; onComplete.value( this.prFileSave( doc, path ))});
	}
	
//	prBusAdd { arg doc, numInputs, numOutputs;
//		var ce, busConfig, id, ids;
//		
//		ce		= JSyncCompoundEdit( "Add Bus" );
//		ids		= doc.busConfigs.collect({ arg busConfig; busConfig.busConfigID });
//		id		= 0;
//		while({ ids.indexOf( id ).notNil }, { id = id + 1 });
//		busConfig	= BosqueTrack( id, numInputs, numOutputs ).name_( "Bus " ++ (id + 1) );
////		ce.addPerform( BosqueEditRemoveSessionObjects( this, doc.selectedTracks, doc.selectedTracks.getAll, false ));
//		ce.addPerform( BosqueEditAddSessionObjects( this, doc.busConfigs, [ busConfig ], true )); // ( .onDeath_({ arg edit; track.dispose }););
////		ce.addPerform( BosqueEditAddSessionObjects( this, doc.selectedTracks, [ track ], false ));
//		doc.undoManager.addEdit( ce.performAndEnd );
//	}

	replaceStake { arg doc, oldStake, newStake, editName;
		var ce, selected;
		editName	= editName ? "Modify Stake";
		ce		= JSyncCompoundEdit( editName );
		selected	= doc.selectedRegions.includes( oldStake );
		if( selected, {
			ce.addPerform( BosqueEditRemoveSessionObjects( this, doc.selectedRegions, [ oldStake ], false ));
		});
		doc.trail.editBegin( ce );
		doc.trail.editRemove( this, oldStake, ce );
		doc.trail.editAdd( this, newStake, ce );
		doc.trail.editEnd( ce );
		if( selected, {
			ce.addPerform( BosqueEditAddSessionObjects( this, doc.selectedRegions, [ newStake ], false ));
		});
		doc.undoManager.addEdit( ce.performEdit.end );
	}
	
	addStake { arg doc, stake, select = true;
		var clearSpan, ce, span, trail;
		
		span		= stake.span;
		ce		= JSyncCompoundEdit( "Add Stake" );
		clearSpan = Span( span.start, min( span.stop, doc.timeline.span.length ));
		if( clearSpan.isEmpty.not, {
			doc.editClearTimeSpan( this, clearSpan, ce, { arg stake2; stake2.track == stake.track });
		});
		trail = doc.trail;
		trail.editBegin( ce );
		trail.editAdd( this, stake, ce );
		trail.editEnd( ce );
		if( select, {
			ce.addPerform( BosqueEditAddSessionObjects( this, doc.selectedRegions, [ stake ], false ));
		});
		doc.undoManager.addEdit( ce.performAndEnd );
		if( stake.isKindOf( BosqueEnvRegionStake ), { stake.java.setRegionTrail( trail )}); // XXX dirty dirty
	}
	
	
	addTrack { arg doc, name;
		var track, ce;
		ce = JSyncCompoundEdit( "Insert Track" );
		track = this.editAddTrack( this, doc, ce, name );
		doc.undoManager.addEdit( ce.performAndEnd );
		^track;
	}
	
	addMarker { arg doc, pos, name;
		var ce, mark;
		ce   = JSyncCompoundEdit( "Add Marker" );
		mark = BosqueMarkerStake( pos, name );
		doc.markers.editBegin( ce );
		doc.markers.editAdd( bosque.master, mark, ce );
		doc.markers.editEnd( ce );
		doc.undoManager.addEdit( ce.performAndEnd );
		^mark;
	}
		
	editAddTrack { arg source, doc, ce, name;
		var track, id, ids;
		
		ids		= doc.tracks.collect({ arg track; track.trackID });
		id		= 0;
		while({ ids.indexOf( id ).notNil }, { id = id + 1 });
		name		= name ?? { doc.tracks.createUniqueName( "Track_%", id + 1 )};
		track	= BosqueTrack( id, doc.trail ).name_( name );
		ce.addPerform( BosqueEditRemoveSessionObjects( source, doc.selectedTracks, doc.selectedTracks.getAll, false ));
//		doc.trackMap[ id ] = track;
		ce.addPerform( BosqueEditAddSessionObjects( source, doc.tracks, [ track ], true )); // ( .onDeath_({ arg edit; track.dispose }););
		ce.addPerform( BosqueEditAddSessionObjects( source, doc.selectedTracks, [ track ], false ));
		^track;
	}

	prTrackRemove { arg doc;
		var ce, tracks, trackIndices, stakes;
				
		tracks		= doc.selectedTracks.getAll.select({ arg x; x.trackID >= 0 });
		if( tracks.isEmpty, { ^this });
		ce			= JSyncCompoundEdit( "Remove Tracks" );
		ce.addPerform( BosqueEditRemoveSessionObjects( this, doc.selectedTracks, tracks, false ));
//		trackIndices	= tracks.collect(track{ arg track; doc.tracks.indexOf( track )});
		stakes		= doc.trail.getAll.select({ arg stake; tracks.includes( stake.track )});
		ce.addPerform( BosqueEditRemoveSessionObjects( this, doc.selectedRegions, stakes, false ));
		doc.trail.editBegin( ce );
		doc.trail.editRemoveAll( this, stakes, ce );
		doc.trail.editEnd( ce );
		ce.addPerform( BosqueEditRemoveSessionObjects( this, doc.tracks, tracks, true ).onDeath_({ arg edit; tracks.do({ arg x; x.dispose })}));
//		tracks.do({ arg track; doc.trackMap.removeAt( track.id )});
		doc.undoManager.addEdit( ce.performAndEnd );
	}
	
	prTimelineInsertSpan { arg doc;
		this.queryStringDialog( "Insert Time", "Amount of seconds", "60.0", { arg result;
			var span;
			result = (result.asFloat * doc.timeline.rate).asInteger;
			if( (result > 0) and: { result < 1e9 }, {  // filter out +-inf!
				span = Span( doc.timelineView.cursor.position, doc.timelineView.cursor.position + result );
				this.insertSpan( doc, span );
			});
		});
	}
	
	insertSpan { arg doc, span, touchMode;
		var ce;
		ce = JSyncCompoundEdit( "Insert Time Span" );
		doc.editInsertTimeSpan( this, span, ce, touchMode );
		ce.addPerform( BosqueTimelineViewEdit.select( this, doc.timelineView, span ));
		doc.undoManager.addEdit( ce.performAndEnd );
	}

	prTimelineClearSpan { arg doc;
		if( doc.timelineView.selection.span.length > 0, {
			this.clearSpan( doc, doc.timelineView.selection.span );
		});
	}
	
	clearSpan { arg doc, span;
		var ce;
		ce = JSyncCompoundEdit( "Clear Time Span" );
		doc.editClearTimeSpan( this, span, ce );
		doc.undoManager.addEdit( ce.performAndEnd );
	}
	
	prTimelineRemoveSpan { arg doc;
		if( doc.timelineView.selection.span.length > 0, {
			this.removeSpan( doc, doc.timelineView.selection.span );
		});
	}

	removeSpan { arg doc, span;
		var ce;
		ce = JSyncCompoundEdit( "Remove Time Span" );
		doc.editRemoveTimeSpan( this, span, ce );
		ce.addPerform( BosqueTimelineViewEdit.select( this, doc.timelineView, Span.new ));
		doc.undoManager.addEdit( ce.performAndEnd );
	}

	prTimelineSplitObjects { arg doc;
		var pos, ce;
		pos	= doc.timelineView.cursor.position;
		ce = JSyncCompoundEdit( "Split Objects" );
		doc.editClearTimeSpan( this, Span( pos, pos ), ce, { arg stake; doc.selectedRegions.includes( stake )});
		doc.undoManager.addEdit( ce.performAndEnd );
	}
	
	prTimelineGlueObjects { arg doc;
		var pos, ce, sel, flt, env, coll, firstSegm, lastSegm, newSpan, offset, firstReg, didBegin = false, newReg, collNewRegs, cutSpan;
		
		pos	= doc.timelineView.cursor.position;
		sel	= doc.selectedRegions.getAll.select({ arg stake; stake.isKindOf( BosqueEnvRegionStake )});
		doc.tracks.do({ arg track;
			flt = sel.select({ arg x; x.track === track });
			if( flt.size >= 2, {
				if( didBegin.not, {
					ce = JSyncCompoundEdit( "Glue Objects" );
					doc.trail.editBegin( ce );
					didBegin = true;
				});
				flt		= flt.sort({ arg a, b; a.span.start <= b.span.start });
				newSpan	= nil;
				flt.do({ arg stake; newSpan = stake.span.union( newSpan )});
				firstReg	= flt.first;
				offset	= firstReg.span.start;
				env		= firstReg.env.duplicate;
				lastSegm	= env.get( env.numStakes - 1 );
//("Doing track " ++ track.name ++ " --> offset is " ++ offset ++ "; newSpan is " ++ newSpan.asCompileString).postln;
				flt.copyToEnd( 1 ).do({ arg reg;
					cutSpan = if( reg.span.start == (lastSegm.span.stop + offset), {
						if( reg.env.numStakes > 1, {
							reg.env.span.replaceStart( reg.env.get( 0 ).span.stop );
						}, {
							reg.env.span.replaceStart( (reg.env.get( 0 ).span.start + reg.env.get( 0 ).span.stop + 1) >> 1 );
						});
					}, {
						reg.env.span;
					});
//[ "cutSpan", cutSpan ].postcs;
					if( cutSpan.isEmpty.not, {
						coll = Trail.getCuttedRange( reg.env.getAll, cutSpan, true, Trail.kTouchSplit, reg.span.start - offset );
						firstSegm = coll.at( 0 );
						if( lastSegm.span.stop < firstSegm.span.start, {
							env.add( nil, BosqueEnvSegmentStake( Span( lastSegm.span.stop, firstSegm.span.start ),
								lastSegm.stopLevel, firstSegm.startLevel ));
						});
						env.addAll( nil, coll );
						lastSegm = coll.last;
					});
				});
				doc.trail.editRemoveAll( this, flt, ce );
				ce.addPerform( BosqueEditRemoveSessionObjects( this, doc.selectedRegions, flt, false ));
				newReg = BosqueEnvRegionStake( newSpan, firstReg.name, track, firstReg.colr, firstReg.fadeIn,
					flt.last.fadeOut, firstReg.gain, env );
				collNewRegs = collNewRegs.add( newReg );
				doc.trail.editAdd( this, newReg, ce );
			});
		});

		if( didBegin, {
			doc.trail.editEnd( ce );
			ce.addPerform( BosqueEditAddSessionObjects( this, doc.selectedRegions, collNewRegs, false ));
			doc.undoManager.addEdit( ce.performAndEnd );
		});
	}

	prTimelineInsertFunc { arg doc;
		var span, track, name, stake;
		
		span 	= doc.timelineView.selection.span;
		track	= doc.selectedTracks.detect({ arg x; x.trackID >= 0 });
		if( span.isEmpty or: { track.isNil }, { ^this });

		name		= "Func";
		// In 2007: WARNING: Symbol -> asCompileString is broken, doesn't escape apostroph!!!!
		// therefore we force the name to be a String here !!!
		// In 2008: Apostroph is properly escaped, we stick to Symbol now!
		stake	= BosqueFuncRegionStake( span, name.asSymbol, track );
		
		this.addStake( doc, stake );
	}
	
	prTimelineInsertEnv { arg doc;
		var span, track, name, stake;
		
		span 	= doc.timelineView.selection.span;
		track	= doc.selectedTracks.detect({ arg x; x.trackID >= 0 });
		if( span.isEmpty or: { track.isNil }, { ^this });

		name		= "Env";
		// WARNING: Symbol -> asCompileString is broken, doesn't escape apostroph!!!!
		// therefore we force the name to be a String here !!!
		stake = BosqueEnvRegionStake( span, name.asString, track );
		
		this.addStake( doc, stake );
	}
	
	prTimelineChangeGain { arg doc;
		var ce, stakes, trail;
		this.queryStringDialog( "Change Gain", "Boost/Cut in Decibels", "0.0", { arg result;
			var timeline, span, ce;
			timeline	= doc.timeline;
			result	= result.asFloat;
			if( result != 0.0, {
				ce	= JSyncCompoundEdit( "Change Gain" );
				trail = doc.volEnv;
				trail.editBegin( ce );
				stakes = trail.editGetRange( doc.timelineView.selection.span, true, ce );
				trail.editClearSpan( this, doc.timelineView.selection.span, nil, ce );
				stakes = stakes.collect({ arg stake; stake.replaceLevel( stake.level + result )});
				stakes = trail.editAddAll( this, stakes, ce );
				trail.editEnd( ce );
				doc.undoManager.addEdit( ce.performAndEnd );
			});
		});
	}
	
	addAudioFile { arg doc, path;
		var ce, af;

		ce = JSyncCompoundEdit( "Add Audio File" );
		af = this.editAddAudioFile( this, doc, ce, path );
//		if( ce.isSignificant, { doc.undoManager.addEdit( ce.performAndEnd )});
		doc.undoManager.addEdit( ce.performAndEnd );
		^af;
	}
	
	editAddAudioFile { arg source, doc, ce, path;
		var af;
		af = BosqueAudioFile( path );  // may return existing object!!!
		if( doc.audioFiles.includes( af ).not, {
			ce.addPerform( BosqueEditAddSessionObjects( source, doc.audioFiles, [ af ]));
		});
		^af;
	}

	prAudioFileAdd { arg doc, path, ce, onComplete, onFailure;
		var af;
		try {
			af = this.editAddAudioFile( this, doc, ce, path );
			onComplete.value( af );
		} { arg error;
//			error.reportError;
			this.showMessageDialog( "Add Audio File", "Error while opening\n" ++ path ++ "\n: " ++ error.what, \error );
			onFailure.value;
		};
	}

	prAudioFileAddRegion { arg af, doc, pos, track;
		var stake, trail, ce, names, name, idx, clearSpan, insertSpan, span;
		
		if( af.notNil and: { track.notNil }, {
			ce = JSyncCompoundEdit( "Add Audio Region" );
// af.regions NOT MAINTAINED AT THE MOMENT XXX
//			names = af.regions.collect({ arg stake; stake.name.asSymbol }); // .asSet;
//			idx = 1;
//			name = (af.name ++ "." ++ idx).asSymbol; // for indexOf asSymbol is crucial !!!
//			while({ names.indexOf( name ).notNil }, { idx = idx + 1; name = (af.name ++ "." ++ idx).asSymbol });
name = af.name.asSymbol;
			span = Span( pos, pos + af.numFrames );
			// WARNING: Symbol -> asCompileString is broken, doesn't escape apostroph!!!!
			// therefore we force the name to be a String here !!!
			stake = BosqueAudioRegionStake( span, name.asString, track, audioFile: af );
			clearSpan = Span( span.start, min( span.stop, doc.timeline.span.length ));
			if( clearSpan.isEmpty.not, {
				doc.editClearTimeSpan( this, clearSpan, ce, { arg stake; stake.track == track });
			});
			if( span.stop > doc.timeline.span.length, {
				insertSpan = Span( doc.timeline.span.length, span.stop );
				doc.editInsertTimeSpan( this, insertSpan, ce );
			});
//			ce.addPerform( BosqueEditAddSessionObjects( this, af.regions, [ stake ]));
			ce.addPerform( BosqueEditAddSessionObjects( this, doc.selectedRegions, [ stake ], false ));
			trail = doc.trail;
			trail.editBegin( ce );
			trail.editAdd( this, stake, ce );
			trail.editEnd( ce );
			doc.undoManager.addEdit( ce.performAndEnd );
		});
	}
	
	prAudioFileReplaceFile { arg af, doc;
		SwingDialog.getPaths({ arg paths;
			paths.do({ arg path; var sf, af2, ce, stakes, selectedStakes, isNew;
				try {
					sf = SoundFile.openRead( path );
					sf.close;
					if( sf.numFrames < af.numFrames, {
						this.showMessageDialog( "Replace Audio File", "Replacement file must have\nat least as many frames as original file", \error );
					}, {
						af2 = BosqueAudioFile( path );  // may return existing object!!!
						isNew = doc.audioFiles.includes( af2 ).not;
						ce = JSyncCompoundEdit( "Replace Audio File" );
						if( isNew, { ce.addPerform( BosqueEditAddSessionObjects( this, doc.audioFiles, [ af2 ]))});
//						stakes = af.regions.getAll;
						stakes = doc.trail.getAllAudioStakes( af );
						selectedStakes = doc.selectedRegions.getAll;
//						stakes.postcs;
						ce.addPerform( BosqueEditRemoveSessionObjects( this, doc.selectedRegions, selectedStakes, false ));
						doc.trail.editBegin( ce );
						doc.trail.editRemoveAll( this, stakes, ce );
						stakes = stakes.collect({ arg stake; stake.replaceFile( af2 )});
						doc.trail.editAddAll( this, stakes, ce );
						doc.trail.editEnd( ce );
						ce.addPerform( BosqueEditRemoveSessionObjects( this, doc.audioFiles, [ af ]).onDeath_({ arg edit; af.dispose }));
						ce.addPerform( BosqueEditAddSessionObjects( this, doc.selectedRegions, stakes, false ));
						doc.undoManager.addEdit( ce.performAndEnd );
					});
				} { arg error;
					error.reportError;
					this.showMessageDialog( "Replace Audio File", "Error while opening\n" ++ path ++ "\n: " ++ error.what, \error );
				};
			});
		});
	}
	
	prAudioFileReplaceRegions { arg af, doc;
		var ce, stakes, isAudio;
		
		ce = JSyncCompoundEdit( "Replace Regions File" );
		stakes = doc.selectedRegions.getAll.select({ arg stake;
			isAudio = stake.isKindOf( BosqueAudioRegionStake );
			if( isAudio, {
				if( (stake.fileStartFrame + stake.span.length) > af.numFrames, {
					("Omitting stake '" ++ stake.name ++ "' - exceeds new file's numFrames").warn;
					false;
				}, true );
			}, isAudio );
		});
		ce.addPerform( BosqueEditRemoveSessionObjects( this, doc.selectedRegions, stakes, false ));
		doc.trail.editBegin( ce );
		doc.trail.editRemoveAll( this, stakes, ce );
		stakes = stakes.collect({ arg stake; stake.replaceFile( af )});
		doc.trail.editAddAll( this, stakes, ce );
		doc.trail.editEnd( ce );
		ce.addPerform( BosqueEditAddSessionObjects( this, doc.selectedRegions, stakes, false ));
		doc.undoManager.addEdit( ce.performAndEnd );
	}
	
	prAudioFileConsolidate { arg doc;
		var ce, stakes, path, split, version, chunkLen, buf, offset, stop, toDispose, lastT, stakesNew, af2, sf1, sf2, af, ext;
		
	  fork {
		ce = JSyncCompoundEdit( "Consolidate" );
		stakes = doc.selectedRegions.getAll.select({ arg x; x.isKindOf( BosqueAudioRegionStake )});
		ce.addPerform( BosqueEditRemoveSessionObjects( this, doc.selectedRegions, stakes, false ));
		doc.trail.editBegin( ce );
		doc.trail.editRemoveAll( this, stakes, ce );
		stakes = stakes.collect({ arg stake, i;
			("Processing '" ++ stake.name ++ "' ("++(i+1)++"/"++stakes.size++")...").postln;
			af 		= stake.audioFile;
			version	= 1;
			split	= af.path.splitext;
			ext		= if( split[1].notNil, { "." ++ split[1] }, "" );
			split	= split[0];
			path		= split ++ "Ct" ++ version ++ ext;
			while({ File.exists( path )}, {
				version	= version + 1;
				path		= split ++ "Ct" ++ version ++ ext;
			});
			sf1				= SoundFile.openRead( af.path );
			sf2				= SoundFile.new;
			sf2.headerFormat	= sf1.headerFormat;
			sf2.sampleFormat	= sf1.sampleFormat;
			sf2.numChannels	= sf1.numChannels;
			sf2.sampleRate	= sf1.sampleRate;
			sf2.path			= path;
			sf2.openWrite;
			
			offset			= stake.fileStartFrame; // * sf1.numChannels;
			stop				= offset + stake.span.length; // (stake.span.length * sf1.numChannels);
			lastT			= thisThread.seconds;
			while({ offset < stop }, {
				chunkLen		= min( 8192, stop - offset );
				buf			= FloatArray.newClear( chunkLen * sf1.numChannels );
				sf1.seek( offset, 0 );  // frames not samples!!
				sf1.readData( buf );
				sf2.writeData( buf );
				offset		= offset + chunkLen;
				if( (thisThread.seconds - lastT) > 1, {
					("  " ++ (((offset - stake.fileStartFrame) / stake.span.length) * 100).round( 0.1 ) ++ "%").postln;
					0.02.wait;
					lastT = thisThread.seconds;
				});
			});
		
			sf2.close;
			sf1.close;
			
			af2		= BosqueAudioFile( path );
			ce.addPerform( BosqueEditAddSessionObjects( this, doc.audioFiles, [ af2 ]));
			
			stake	= stake.rename( path.basename.splitext.first );
			toDispose	= stake;
			stake	= stake.replaceFileStartFrame( 0 );
			toDispose.dispose;
			toDispose	= stake;
			stake	= stake.replaceFile( af2 );
			toDispose.dispose;
			
			stake;
		});
		
		doc.trail.editAddAll( this, stakes, ce );
		doc.trail.editEnd( ce );
		ce.addPerform( BosqueEditAddSessionObjects( this, doc.selectedRegions, stakes, false ));
		doc.undoManager.addEdit( ce.performAndEnd );
		
		"Done.".postln;
	  }
	}
	
	prAudioFileRemove { arg af, doc;
		var ce, stakes;
		ce = JSyncCompoundEdit( "Remove Audio File" );
		stakes = doc.trail.getAllAudioStakes( af );
		ce.addPerform( BosqueEditRemoveSessionObjects( this, doc.selectedRegions, stakes, false ));
		doc.trail.editBegin( ce );
		doc.trail.editRemoveAll( this, stakes, ce );
		doc.trail.editEnd( ce );
		ce.addPerform( BosqueEditRemoveSessionObjects( this, doc.audioFiles, [ af ]).onDeath_({ arg edit; af.dispose }));
		doc.undoManager.addEdit( ce.performAndEnd );
	}

	prBusConfigRemove { arg cfg, doc;
		var ce, tracks;
		ce = JSyncCompoundEdit( "Remove Bus Config" );
		tracks = doc.tracks.select({ arg t; t.busConfig == cfg });
		tracks.do({ arg t; t.editBusConfig( this, nil, ce )});
//		ce.addPerform( BosqueEditRemoveSessionObjects( this, doc.selectedBusConfigs, [ cfg ], false ));
		ce.addPerform( BosqueEditRemoveSessionObjects( this, doc.busConfigs, [ cfg ]).onDeath_({ arg edit; cfg.dispose }));
		doc.undoManager.addEdit( ce.performAndEnd );
	}

	prEditUndo { arg doc;
		if( doc.undoManager.canUndo, { doc.undoManager.undo });
	}

	prEditRedo { arg doc;
		if( doc.undoManager.canRedo, { doc.undoManager.redo });
	}
	
	prEditCut { arg doc;
		this.prEditCopy( doc );
		this.prEditDelete( doc );
	}

	prEditCopy { arg doc;
		var sel, copy, ce, owner, transferable;
		sel = doc.selectedRegions.getAll;
		if( sel.isEmpty, { ^this });
		
		copy = sel.collect({ arg stake; stake.duplicate });
		transferable = ();
		transferable.object = copy;
		transferable.transferDataFlavors = [ \stakeList ];
		transferable.isDataFlavorSupported = { arg thisF, flavor; thisF.transferDataFlavors.includes( flavor )};
		transferable.getTransferData = { arg  thisF, flavor;
			if( flavor === \stakeList, {
				thisF.object.collect({ arg stake; stake.duplicate });
			}, { Error( "Unsupported Flavor " ++ flavor ).throw });
		};
		owner = ();
		owner.lostOwnership = { arg thisF, clipBoard, contents;
//			"Edit Copy: lost ownership".inform;
			contents.object.do({ arg x; x.dispose })};
//		[ transferable, owner ].postcs;
		doc.bosque.clipBoard.setContents( transferable, owner );
	}

	prEditPaste { arg doc;
		var ce, stakes, stake, origStart, origStop, delta, newStart, newStop;
	
		if( doc.bosque.clipBoard.isDataFlavorAvailable( \stakeList ).not, { ^this });
		stakes = doc.bosque.clipBoard.getData( \stakeList );
		origStart	= stakes.minItem({ arg stake; stake.span.start }).span.start;
		origStop	= stakes.maxItem({ arg stake; stake.span.stop }).span.stop;
		newStart	= doc.timelineView.cursor.position;
		delta	= newStart - origStart;
		newStop	= origStop + delta;
		ce		= JSyncCompoundEdit.new;
		if( newStop > doc.timeline.span.length, {
			doc.editInsertTimeSpan( this, Span( doc.timeline.span.length, newStop ), ce );
		});
		if( delta != 0, {
			stakes = stakes.collect({ arg oldStake; stake = oldStake.shiftVirtual( delta ); oldStake.dispose; stake });
		});
		doc.trail.editBegin( ce );
		stakes.do({ arg stake; doc.trail.editClearSpan( this, stake.span, nil, ce,
			{ arg stake2; stake.track == stake2.track })});
		doc.trail.editAddAll( this,stakes, ce );
		doc.trail.editEnd( ce );
		ce.addPerform( BosqueEditAddSessionObjects( this, doc.selectedRegions, stakes, false ));
		doc.undoManager.addEdit( ce.performAndEnd );
	}

	prEditDelete { arg doc;
		var sel, ce;
		sel = doc.selectedRegions.getAll;
		if( sel.isEmpty, { ^this });
		
		ce = JSyncCompoundEdit( "Delete Objects" );
		ce.addPerform( BosqueEditRemoveSessionObjects( this, doc.selectedRegions, sel, false ));
		doc.trail.editBegin( ce );
		doc.trail.editRemoveAll( this, sel, ce );
		doc.trail.editEnd( ce );
		doc.undoManager.addEdit( ce.performAndEnd );
	}

	prBusAddDlg { arg doc;
		{
			var result, jOptionPane, jName, jNumInputs, jNumOutputs, numInputs, numOutputs,
			    jLayout, jPanel, jLab3, jLab1, jLab2, name, ids, id;
			
			ids			= doc.busConfigs.collect({ arg b; b.busCfgID });
			id			= 0;
			while({ ids.indexOf( id ).notNil }, { id = id + 1 });
			jOptionPane	= JavaObject.getClass( "javax.swing.JOptionPane", bosque.swing );
			jName		= JavaObject( "javax.swing.JTextField", bosque.swing, "Bus "++(id+1), 10 );
			jNumInputs	= JavaObject( "javax.swing.JTextField", bosque.swing, Bosque.masterBusNumChannels.asString, 3 );
			jNumOutputs	= JavaObject( "javax.swing.JTextField", bosque.swing, Bosque.masterBusNumChannels.asString, 3 );
			jLab1		= JavaObject( "javax.swing.JLabel", bosque.swing, "Name" );
			jLab2		= JavaObject( "javax.swing.JLabel", bosque.swing, "Input Channels" );
			jLab3		= JavaObject( "javax.swing.JLabel", bosque.swing, "Output Channels" );
			jLayout		= JavaObject( "java.awt.GridLayout", bosque.swing, 3, 2 );
			jPanel		= JavaObject( "javax.swing.JPanel", bosque.swing, jLayout );
			jPanel.add( jLab1 );
			jPanel.add( jName );
			jPanel.add( jLab2 );
			jPanel.add( jNumInputs );
			jPanel.add( jLab3 );
			jPanel.add( jNumOutputs );
//			result = JavaObject.withTimeOut( 60.0, { jOptionPane.showOptionDialog_( nil, "Enter # of Inputs and Outputs", "Add Bus", 2, 3, nil, [ jNumInputs, jNumOutputs ], nil ); });
			result = JavaObject.withTimeOut( 60.0, { jOptionPane.showConfirmDialog_( nil, jPanel, "Add Bus", 2, 3, nil ); });
			jOptionPane.destroy;
			if( result == 0, {
				name			= jName.getText_;
				numInputs		= jNumInputs.getText_.asInteger;
				numOutputs	= jNumOutputs.getText_.asInteger;
				this.addBusConfig( doc, numInputs, numOutputs, name );
			});
			jNumInputs.destroy; jNumOutputs.destroy; jLab1.destroy; jLab2.destroy; jPanel.destroy; jLayout.destroy;
		}.fork( SwingOSC.clock );
	}
	
	addBusConfig { arg doc, numInputs, numOutputs, name;
		var cfg, ce;
		ce = JSyncCompoundEdit( "Add Bus" );
		cfg = this.editAddBusConfig( this, doc, ce, numInputs, numOutputs, name );
		doc.undoManager.addEdit( ce.performAndEnd );
		^cfg;
	}
	
	editAddBusConfig { arg source, doc, ce, numInputs, numOutputs, name;
		var id, ids, cfg;
		
		ids			= doc.busConfigs.collect({ arg b; b.busCfgID });
		id			= 0;
		while({ ids.indexOf( id ).notNil }, { id = id + 1 });
		name			= name ?? { ("Bus " ++ (id + 1)).asSymbol };
		cfg			= BosqueBusConfig( id, numInputs, numOutputs ).name_( name );
		ce.addPerform( BosqueEditAddSessionObjects( source, doc.busConfigs, [ cfg ], true ));
		^cfg;
	}
	
//	prOSCRec {
//		if( ~updTransp.notNil, {
//			"WARNING: OSC-Rec already running. killing previous one!".warn;
//			~updTransp.remove;
//			if( ~oscRec.notNil, {
//				try {
//					~oscRec.closeFile;
//				} { arg error;
//					error.reportError;
//				};
//				~oscRec = nil;
//			});
//		});
//	
//		~oscName = "Durchlauf"; // "Magma";
//		~oscRecOffset = nil;
//		~updTransp = UpdateListener.newFor( Bosque.default.session.transport, { arg upd, transport, what, param1;
//try {
//			switch( what,
//			\play, {
//				~oscRec = FileNetAddr.openWrite( "/Users/rutz/Desktop/Bosque2/osc/"++~oscName++Date.getDate.stamp++".osc" );
//				~oscRecOffset = (param1 / Bosque.default.session.timeline.rate) - thisThread.seconds;
//					("--- OSC REC START (" ++ ~oscRecOffset ++ ")").postln;
//			},
//			\stop, {
//				if( ~oscRec.notNil, {
//					~oscRecOffset = nil;
//					"--- OSC REC DONE".postln;
//					~oscRec.closeFile;
//					~oscRec = nil;
//				});
//			},
//			\resume, {
//				try {
//					upd.update( transport, \stop );
//					upd.update( transport, \play, param1 );
//				} { arg error;
//					error.reportError;
//				};
//			});
//} { arg error;
//	error.reportError;
//};
//		});
//		~updOSC = UpdateListener.newFor( Main, { arg upd, obj, what, time, replyAddr, msg;
//try {
//			if( ~oscRecOffset.notNil and: { msg[0] === '/dancer' or: { (msg[0] === '/field') or: { msg[0] === '/track' }}}, {
//	//			~oscRec.sendMsg( *msg );
//	//			~oscRec.sendBundle( time, msg );
//				~oscRec.sendBundle( thisThread.seconds + ~oscRecOffset, msg );
//			});
//} { arg error;
//	error.reportError;
//};
//		});
//		
//		"\n---- OSC Recorder ready.".postln;
//	}

	showMessageDialog { arg title, text, type = \info;
		bosque.swing.sendMsg( '/method', "javax.swing.JOptionPane", \showMessageDialog, '[', '/ref', \null, ']', text, title, [ \error, \info, \warn, \question ].indexOf( type ) ? -1 );
	}
	
	queryStringDialog { arg title, text, value, onComplete, parent;
		{
			var result, jOptionPane;
			jOptionPane = JavaObject.getClass( "javax.swing.JOptionPane", bosque.swing );
			result = JavaObject.withTimeOut( 60.0, { jOptionPane.showInputDialog_( parent, text, title, 3, nil, nil, value ); });
			jOptionPane.destroy;
			if( result.notNil, { onComplete.value( result )});
		}.fork( SwingOSC.clock );
	}

	showConfirmDialog { arg title, text, options = \yesno, type = \question, onComplete, parent;
		{
			var result, jOptionPane;
			jOptionPane = JavaObject.getClass( "javax.swing.JOptionPane", bosque.swing );
			result = JavaObject.withTimeOut( 60.0, { jOptionPane.showConfirmDialog_( parent, text, title, [ \yesno, \yesnocancel, \okcancel ].indexOf( options ) ? -1, [ \error, \info, \warn, \question ].indexOf( type ) ? -1 ); });
			jOptionPane.destroy;
			if( result.notNil, { onComplete.value( [ \yes, \no, \cancel ][ result ])});
		}.fork( SwingOSC.clock );
	}
}