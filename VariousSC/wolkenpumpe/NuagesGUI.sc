/**
 *	NuagesGUI
 *	(Wolkenpumpe)
 *
 *	(C)opyright 2005-2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.16, 31-Aug-09
 *	@author	Hanns Holger Rutz
 */
NuagesGUI {
	classvar <>tabletLeft	=   50;
	classvar <>tabletTop	=   50;
	classvar <>tabletBottom	=  824;
	classvar <>tabletRight	= 1390;
	
	var <pompe, swing;
	var <win, <panel;
	var <eraser = false;
	var dragHandler, dragHandlerMetaOff;

	var <specFadeTime, clpseFadeTime;
	
	var boxes, conns;
	
	var tabletFirst = false, lastClickCount = 0;
//	var nextPressIsEraser = false;
	var <isShiftDown = false, <isCtrlDown = false, <isAltDown = false, <isMetaDown = false;
	
	*new { arg pompe;
		^super.new.prInit( pompe );
	}
	
	prInit { arg argPompe;
		pompe		= argPompe;
		specFadeTime	= ControlSpec( 0.1, 20, \exp );
		boxes		= IdentityDictionary.new;
		conns		= IdentityDictionary.new;
	}
	
	makeGUI {	
		var scrB, ggFadeSlid, ggFadeLabel, fntSmall, updPompe, ggTapeCues, ggMasterVolume, ggSoloVolume, ggSoloClear, ggAuxFade;
		var specFader, ggOutputs;
var panelRight;
		
		if( win.notNil, { ^this });
		
		swing	= pompe.swing;
//		win		= JSCWindow( "Wolkenpumpe IV", Rect( 0, 0, 600, 400 ), server: swing );
		scrB		= JSCWindow.screenBounds;
		win		= JSCWindow( "Wolkenpumpe IV", Rect( tabletLeft, scrB.height - tabletBottom - 2,
								   	tabletRight - tabletLeft + 2, tabletBottom - tabletTop + 2 ), false, false, server: swing );
		win.view.background = Color.gray( 0.2 );
		fntSmall	= JFont( JFont.defaultSansFace, 10 );
panelRight = win.view.bounds.width - 120;
		panel	= NuagesGUIPanel( win, win.view.bounds.insetAll( 0, 0, 120, 0 ))
			.resize_( 5 )
			.keyModifiersChangedAction_({ arg view, mod;
				isShiftDown = (mod & 0x020000) != 0;
				isCtrlDown  = (mod & 0x040000) != 0;
				isAltDown   = (mod & 0x080000) != 0;
				isMetaDown  = (mod & 0x100000) != 0;
			})
			.proximityAction_({ arg view, entering, deviceID, pointingDeviceType, systemTabletID, pointingDeviceID, tabletID, uniqueID;
				eraser = pointingDeviceType == 3;
			})
//// quick hack to allow mouse usage!
//.keyDownAction_({ arg view, char;
//	if( char == $<, { nextPressIsEraser = true });
//})
			.mouseDownAction_({ arg view, x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ, rotation,
			                        absoluteX, absoluteY, buttonMask, tanPressure, metaData;
				var conn;
				
if( x >= panelRight, { dragHandler = nil }, {

// quick hack to allow mouse usage!
if( (absoluteX == 0) and: { (absoluteY == 0) and: { buttonMask == 0 }}, {
	buttonMask = 1; // if( nextPressIsEraser, 4, 1 );
});
//nextPressIsEraser = false;
                 
				switch( metaData.first,
				\box, {
					dragHandler = panel.findBox( metaData[ 1 ]);
					if( dragHandler.isNil, {
						("Box with ID " ++ metaData[ 1 ] ++ " not found!").error;
					});
				},
				\con, {
					dragHandler = panel.findConnector( metaData[ 1 ]);
					if( dragHandler.isNil, {
						("Connector with ID " ++ metaData[ 1 ] ++ " not found!").error;
					});
				}, {
					dragHandler = this;
				});
				if( dragHandler.notNil, {
					dragHandler.mouseDown( view, x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ, rotation,
					                       absoluteX, absoluteY, buttonMask, tanPressure, metaData.copyToEnd( 2 ));
				});
});
			})
			.mouseMoveAction_({ arg view, x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ, rotation,
			                        absoluteX, absoluteY, buttonMask, tanPressure, metaData;

				if( dragHandler.notNil, {
					dragHandler.mouseDrag( view, x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ, rotation,
					                       absoluteX, absoluteY, buttonMask, tanPressure, metaData /*.copyToEnd( 2 )*/);
				});
			})
			.mouseUpAction_({ arg view, x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ, rotation,
			                      absoluteX, absoluteY, buttonMask, tanPressure, metaData;
			                      
				if( dragHandler.notNil, {
					dragHandler.mouseUp( view, x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ, rotation,
					                     absoluteX, absoluteY, buttonMask, tanPressure, metaData /*.copyToEnd( 2 )*/);
					dragHandler = nil;
				});
			})
			.scsynth_( pompe.server );
		
		ggFadeSlid = JSCMultiSliderView( win, Rect( panel.bounds.right + 4, 4, 15, 100 ))
			.elasticMode_( 1 )
			.resize_( 3 )
			.isFilled_( true )
			.valueThumbSize_( 0 )
			.fillColor_( Color.white )
			.readOnly_( true )
			.canFocus_( false )
			.value_([ 0.0 ]);
			
		ggFadeLabel = JSCStaticText( win, Rect( ggFadeSlid.bounds.right + 4, ggFadeSlid.bounds.top, 40, 24 ))
			.resize_( 3 )
			.stringColor_( Color.white )
			.font_( fntSmall );
			
		ggTapeCues = JSCListView( win, Rect( ggFadeSlid.bounds.left, ggFadeSlid.bounds.bottom + 4, 112, 300 ))
			.items_([ "<< RANDOM >> " ] ++ pompe.tapeCues.collect({ arg fullPath; fullPath.basename.splitext.first }))
			.font_( fntSmall )
			.action_({ arg b;
				pompe.tapeCueVariation = b.value - 1;
			});

		ggOutputs = JSCListView( win, Rect( ggTapeCues.bounds.left, ggTapeCues.bounds.bottom + 4, 112, 112 ))
			.items_( pompe.uf.audioOutputs.collect( _.displayName ))
			.font_( fntSmall )
			.action_({ arg b;
				pompe.uf.preferredAudioOutput = pompe.uf.audioOutputs[ b.value ];
			});
		ggOutputs.doAction; // !initialize

		clpseFadeTime = Collapse({
			ggFadeSlid.value_([ specFadeTime.unmap( pompe.fadeTime )]);
			ggFadeLabel.string_( "% s".format( pompe.fadeTime.round( 0.01 )));
		}, 0.05 );
		
		updPompe = UpdateListener.newFor( pompe, { arg upd, pompe, what ... rest;
			switch( what,
			\fadeTime, {
				clpseFadeTime.instantaneous;
			},
			\vertexAdded, {
				this.prVertexAdded( *rest );
			},
			\edgeAdded, {
				this.prEdgeAdded( *rest );
			},
			\edgeRemoved, {
				this.prEdgeRemoved( *rest );
			});
		});
		
		specFader = ControlSpec( -60.dbamp, 18.dbamp, \exp );
		
		ggMasterVolume = JSCMultiSliderView( win, Rect( ggOutputs.bounds.left + 4, ggOutputs.bounds.bottom + 8, 20, 100 ))
			.elasticMode_( 1 )
			.resize_( 3 )
			.indexThumbSize_( 18 )
			.isFilled_( true )
			.valueThumbSize_( 0 )
			.fillColor_( Color.white )
			.canFocus_( false )
			.value_([ specFader.unmap( 1.0 )])
			.action_({ arg b;
				pompe.masterVolume = specFader.map( b.value.first );
			});
		JSCStaticText( win, Rect( ggMasterVolume.bounds.left, ggMasterVolume.bounds.bottom + 4, 20, 20 ))
			.resize_( 3 )
			.stringColor_( Color.white )
			.align_( \center )
			.font_( fntSmall )
			.string_( "M" );

		ggSoloVolume = JSCMultiSliderView( win, Rect( ggOutputs.bounds.left + 40, ggOutputs.bounds.bottom + 8, 20, 100 ))
			.elasticMode_( 1 )
			.resize_( 3 )
			.indexThumbSize_( 18 )
			.isFilled_( true )
			.valueThumbSize_( 0 )
			.fillColor_( Color.white )
			.canFocus_( false )
			.value_([ specFader.unmap( 1.0 )])
			.action_({ arg b;
				pompe.solo.volume = specFader.map( b.value.first );
			});
		JSCStaticText( win, Rect( ggSoloVolume.bounds.left, ggSoloVolume.bounds.bottom + 4, 20, 20 ))
			.resize_( 3 )
			.stringColor_( Color.white )
			.align_( \center )
			.font_( fntSmall )
			.string_( "S" );

		JSCButton( win, Rect( ggSoloVolume.bounds.left + 30, ggSoloVolume.bounds.bottom + 4, 40, 22 ))
			.resize_( 3 )
			.font_( fntSmall )
			.canFocus_( false )
			.states_([[ "CLEAR", Color.black, Color.white ]])
			.action_({ arg b;
				pompe.taskSched( nil, \taskSolo, nil );
			});

		if( Wolkenpumpe.useAux, {
			ggAuxFade = JSCMultiSliderView( win, Rect( ggMasterVolume.bounds.left, ggMasterVolume.bounds.bottom + 40, 20, 100 ))
				.elasticMode_( 1 )
				.resize_( 3 )
				.indexThumbSize_( 18 )
				.isFilled_( true )
				.valueThumbSize_( 0 )
				.fillColor_( Color.white )
				.canFocus_( false )
				.value_([ 0.0 ])
				.action_({ arg b;
					pompe.auxFade = b.value.first;
				});
			JSCStaticText( win, Rect( ggAuxFade.bounds.left, ggAuxFade.bounds.bottom + 4, 40, 20 ))
				.resize_( 3 )
				.stringColor_( Color.white )
				.align_( \center )
				.font_( fntSmall )
				.string_( "Aux" );
		});
		
		win.userCanClose = false;
		win.onClose = {
//			routTask.stop; routTask = nil;
			updPompe.remove;
			win = nil;
		};
//		ScissUtil.positionOnScreen( win );
		win.front;
	}
	
	findBox { arg vertex;
	
		TypeSafe.checkArgClass( thisMethod, vertex, NuagesV, false );
		
		^boxes[ vertex ];
	}
	
	findConnector { arg edge;
	
		TypeSafe.checkArgClass( thisMethod, edge, NuagesE, false );
		
		^conns[ edge ];
	}
	
	removeBox { arg box;
	
		TypeSafe.checkArgClass( thisMethod, box, NuagesGUIBox, false );
		
		boxes.removeAt( box.vertex );
		panel.removeBox( box );
	}
	
	addBox { arg box;
//"NuagesGUI.addBox".postln;
	
		TypeSafe.checkArgClass( thisMethod, box, NuagesGUIBox, false );
		
		boxes.put( box.vertex, box );
		panel.addBox( box );
	}
	
	addConnector { arg conn;

		TypeSafe.checkArgClass( thisMethod, conn, NuagesGUIConn, false );
		
//		[ "addConnector", conn.sourceBox.asString, conn.targetBox.asString ].postln;
		
		conns.put( conn.edge, conn );
		panel.addConnector( conn );
	}

	removeConnector { arg conn;
	
		TypeSafe.checkArgClass( thisMethod, conn, NuagesGUIConn, false );
		
//		[ "removeConnector", conn.sourceBox.asString, conn.targetBox.asString ].postln;
		
		conns.removeAt( conn.edge );
		panel.removeConnector( conn );
	}

	prVertexAdded { arg vertex, pt;
		var box;
		
		TypeSafe.checkArgClasses( thisMethod, [ vertex, pt ], [ NuagesV, Point ], [ false, true  ]);

//"--------- NuagesGUI:prVertexAdded 1".postln;
		box = NuagesGUIBox( this, vertex, pt );
//"--------- NuagesGUI:prVertexAdded 2".postln;
		boxes.put( vertex, box );
	}

	prEdgeAdded { arg edge;
		var conn, box1, box2;

		TypeSafe.checkArgClasses( thisMethod, [ edge ],
		                                      [ NuagesE ],
		                                      [ false ]);
		
		NuagesGUIConn( this, edge, if( edge.target.type == \sid, { edge.target.attr }));
		if( edge.target.index == 0, {
			box1 = this.findBox( edge.source.vertex );
			box2 = this.findBox( edge.target.vertex );
			conn = this.findConnector( edge );
			if( edge.target.type !== \sid, {
				fork { box1.waitForAdd; box2.waitForAdd; 0.05.wait; conn.alignTarget }; // XXX tricky shit
			});
		});
	}

	prEdgeRemoved { arg edge;
		var conn;

		TypeSafe.checkArgClasses( thisMethod, [ edge ],
		                                      [ NuagesE ],
		                                      [ false ]);
		
		conn = this.findConnector( edge );
		if( conn.isNil, {
			TypeSafe.methodError( thisMethod, "Connector is nil" );
			^this;
		});
		conn.remove;
	}
	
	mouseDown { arg view, x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ, rotation,
	                absoluteX, absoluteY, buttonMask, tanPressure, metaData;
	                
		// BUG in wacom drivers (since pro608 i think)
		lastClickCount = clickCount;
		if( buttonMask == 0, {
			tabletFirst		= true;
		}, {
			this.prMouseDown( view, x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ, rotation,
			                  absoluteX, absoluteY, buttonMask, tanPressure, metaData );
		});
	}

	prMouseDown { arg view, x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ, rotation,
	                  absoluteX, absoluteY, buttonMask, tanPressure, metaData;
		var classes;

     	tabletFirst = false;

//[ "prMouseDown", lastClickCount, buttonMask ].postln;
		
		if( ((buttonMask & 2) == 2) or: { isCtrlDown }, {
			classes = pompe.uf.controlGenerators;
			NuagesGUI.showPopup( view, (x - 4) @ (y - 4), classes.collect( _.displayName ), { arg value;
				pompe.taskSched( nil, \taskInsertGenerator, classes[ value ].name, x @ y );
			} /*, { "Cancelled!".postln } */ );
		}, { if( lastClickCount == 2, {
			if( (buttonMask == 1) and: { isMetaDown.not }, {
				classes = pompe.uf.audioGenerators;
				NuagesGUI.showPopup( view, (x - 4) @ (y - 4), classes.collect( _.displayName ), { arg value;
					pompe.taskSched( nil, \taskInsertGenerator, classes[ value ].name, x @ y );
				} /*, { "Cancelled!".postln } */ );
			}, { if( ((buttonMask == 4) || isMetaDown) and: { panel.selectedBoxes.notEmpty }, { // pompe.solo.current
				pompe.taskSched( nil, \taskDuplicateProcs, panel.selectedBoxes.first.vertex.proc, x @ y );
			})});
		})});
	}

	mouseDrag { arg view, x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ, rotation,
     	absoluteX, absoluteY, buttonMask, tanPressure, metaData;
     	
     	if( tabletFirst, { this.prMouseDown( view, x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ,
     	                                     rotation, absoluteX, absoluteY, buttonMask, tanPressure, metaData )});
     }

	mouseUp { arg view, x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ, rotation,
     	absoluteX, absoluteY, buttonMask, tanPressure, metaData;
     	
     	if( tabletFirst, {
     		this.prMouseDown( view, x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ,
		                       rotation, absoluteX, absoluteY, buttonMask, tanPressure, metaData );
		}, {
			tabletFirst = false;
		});

//		lastButtonMask = buttonMask;
//[ "lastButtonMask", lastButtonMask ].postln;
     }
	
	*showPopup { arg parent, location, items, doneAction, cancelAction;
		var pop, acs, acResp, acResps, jAcResp, jAcResps, ac, jpopResp, popResp, cancelled = false, value, fDestroy;
		pop		= JavaObject( "javax.swing.JPopupMenu", parent.server );
		acs		= Array( items.size );
		jAcResps	= Array( items.size );
		acResps	= Array( items.size );
		items.do({ arg item, i;
			ac		= JavaObject( "de.sciss.swingosc.DispatchAction", nil, item );
			jAcResp	= JavaObject( "de.sciss.swingosc.ActionResponder", nil, ac.id );
			pop.add( ac );
			acs.add( ac );
			jAcResps.add( jAcResp );
			acResp = OSCpathResponder( pop.server.addr, [ '/action', ac.id ], { arg time, resp, msg;
				if( msg[ 2 ] === \performed, {
					value = i;
					fDestroy.value;
				});
			}).add;
			acResps.add( acResp );
		});
		jpopResp = JavaObject( "de.sciss.nuages.PopupMenuResponder", nil, pop.id );
		fDestroy = {
			acResps.do({ arg resp; resp.remove });
			jAcResps.do({ arg resp; resp.remove; resp.destroy });
			acs.do({ arg ac; ac.destroy });
			jpopResp.remove; jpopResp.destroy;
			pop.destroy;
			popResp.remove;
			if( value.notNil, {
				doneAction.value( value );
			}, cancelAction );
		};
		popResp = OSCpathResponder( pop.server.addr, [ '/popup', pop.id ], { arg time, resp, msg;
			switch( msg[ 2 ],
			\cancelled, { cancelled = true },
			\invisible, { if( cancelled, fDestroy )}
			);
		}).add;
		pop.show( parent, location.x, location.y );
	}
}