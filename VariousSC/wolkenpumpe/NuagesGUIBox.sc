/**
 *	NuagesGUIBox
 *	(Wolkenpumpe)
 *
 *	(C)opyright 2005-2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.14, 26-Aug-09
 *	@author	Hanns Holger Rutz
 */
NuagesGUIBox {
	var java, gui, pompe;
	var <vertex, <meterBus;
	var updProc, mapAttrs;
	var dying = false;
	var hasMix, hasPlay, <isControl, condAdded;
	
	// slider dnd
	var dragAttr, dragStartX, dragAttrStartVal, dragVal;
	var dragType = \none;	// either of \none, \slide, \line, \erase
	
	var tabletFirst = false, lastClickCount = 0;
	
	*new { arg gui, vertex, pt;
		^super.new.prInit( gui, vertex, pt );
	}
	
	prInit { arg argGUI, argVertex, pt;
		var unit;
		gui			= argGUI;
		pompe		= gui.pompe;
		vertex		= argVertex;
		unit			= vertex.proc.unit;
		hasPlay		= unit.isKindOf( NuagesUGen );
		isControl		= unit.isControl;
		
		TypeSafe.checkArgClasses( thisMethod, [ gui,       vertex,  pt    ],
		                                      [ NuagesGUI, NuagesV, Point ],
		                                      [ false,     false,   true  ]);
		
		java			= JavaObjectD( "de.sciss.nuages.Box", nil, unit.name,
				hasPlay.if( 0x01, 0 ) |
				unit.respondsTo( \isRecorder ).if( 0x02, 0 ) |
				isControl.if( 0x04, 0 ),
				isControl.if({ unit.numVisibleControlInputs  }, { unit.numVisibleAudioInputs  }),
				isControl.if({ unit.numVisibleControlOutputs }, { unit.numVisibleAudioOutputs }))
			.destroyAction_({ arg j; j.dispose });
		if( pt.notNil, { java.setCenter( pt.x, pt.y )});
		java.setID( java.id );
		meterBus		= Bus.control( vertex.proc.server, 2 );   // QQQ XXX
		hasMix		= unit.respondsTo( \getMix );
		condAdded		= Condition.new;
		
		this.prInitUnit( unit );

		updProc		= UpdateListener.newFor( vertex.proc, { arg upd, proc, what ... rest;
			switch( what,
			\unitAttrUpdate, {
				this.prUnitAttrUpdate( *rest );
			},
			\disposed, {	// XXX ---> NuagesLinkedGraph
				this.prProcDisposed;
			},
			\paused, {
				// XXX nada
			},
			\dying, {
				this.prProcDying( *rest );
			},
			\soloGained, {
				java.setSolo( true );
			},
			\soloLost, {
				java.setSolo( false );
			},
			\unitPlaying, {
				if( hasPlay, { java.setPlay( rest.first )});
			},
			\unitRecording, {
				java.setRec( rest.first );
			},
			\mapped, {
				this.prProcMapped( *rest );
			},
			\unmapped, {
				this.prProcUnmapped( *rest );
			});
		});
		
		gui.addBox( this );
	}
	
	added {
		condAdded.test = true;
		condAdded.signal;
	}
	
	waitForAdd {
		condAdded.wait;
	}
	
	prProcMapped { arg attr, source;
//		var sourceBox;
//		
//		sourceBox = gui.findBox( source );
//		if( sourceBox.isNil, {
//			TypeSafe.methodError( thisMethod, "Source proc's box is nil" );
//			^this;
//		});
//		NuagesGUIConn( gui, sourceBox, this, attr );
//	
		java.setMapped( attr.name, true );
	}

	prProcUnmapped { arg attr;
		this.notYetImplemented( thisMethod );
//		var conn;
//		
//		conn = targetConns.detect({ arg conn; conn.isControl and: { conn.attr.name == attr.name }});
//		if( conn.isNil, {
//			TypeSafe.methodError( thisMethod, "Connector to source is nil" );
////			
////			[ "attr.name", attr.name ].postcs;
////			targetConns.do({ arg conn;
////				[ "conn.attr.name", if( conn.isControl, { conn.attr.name }), "sourceBox.proc", conn.sourceBox.proc ].postcs;
////			});
////			^this;
//		}, {
//			conn.remove;
//		});
//		
//		java.setMapped( attr.name, false );
	}
	
	prUnitAttrUpdate { arg name, value, normalized;
		java.updateAttr( name, normalized );
	}
	
	prProcDying {
		if( dying.not, {
			dying = true;
			java.setForeground( Color.red );
		});
	}
	
	prProcDisposed {
		this.remove;
	}

	findAttr { arg name;
		^mapAttrs[ name ];
	}
	
	numAttrs { ^mapAttrs.size }
	
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
		var unit, names, selBox;

     	if( tabletFirst, {
     		this.prMouseDown( view, x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ,
		                       rotation, absoluteX, absoluteY, buttonMask, tanPressure, metaData );
		}, {
			tabletFirst = false;
		});
			
		switch( metaData.first,
		\sli, {
			dragAttr = this.findAttr( metaData[ 1 ]);
			unit = vertex.proc.unit;
			if( dragAttr.notNil && unit.notNil, {
			
				// --------------- normal slider motion ---------------
				if( (buttonMask == 1) and: { (gui.isMetaDown || gui.isCtrlDown).not }, {
//[ 1, "isControlMapped", dragAttr ].postln;
					if( vertex.proc.isControlMapped( dragAttr ).not, {
						dragType			= \slide;
						dragAttrStartVal	= dragAttr.getNormalizedValue( unit );
						dragStartX		= x;
						dragVal			= dragAttrStartVal;
						java.startAttrDrag( dragAttr.name, dragAttrStartVal );
					});
					
				// --------------- scheduled line ---------------
				}, { if( ((buttonMask == 4) || gui.isMetaDown) and: { (lastClickCount == 2) and: { dragAttr.shouldFade.not }}, {
					dragType			= \line;
					pompe.fadeTime_( gui.specFadeTime.map( tilty.abs ) * 2 );
					pompe.taskLineAttr( vertex.proc, dragAttr, if( tilty < 0, 0.0, 1.0 ));
					
				// --------------- map / unmap ---------------
				}, { if( ((buttonMask == 2) || gui.isCtrlDown) and: { dragAttr.canMap }, {
//					if( vertex.proc.isControlMapped( dragAttr ), {
//						pompe.taskSched( nil, \taskUnmap, vertex.proc, dragAttr );
//					}, {
//						selBox = gui.panel.selectedBoxes.first;
//						if( selBox.notNil and: { selBox.isControl and: { selBox != this }}, {
//							pompe.taskSched( nil, \taskMap, vertex.proc, dragAttr, selBox.proc );
//						});
//					});
				})})});
			});
		},
		\sol, {
			pompe.taskSched( nil, \taskSolo, if( pompe.solo.current != vertex.proc, vertex.proc ));
		
		},
		\ply, {
			pompe.fadeTime_( gui.specFadeTime.map( tilty.linlin( -1, 1, 1, 0 )));
			pompe.taskSched( nil, \taskPlayStop, vertex );
		},
		\rec, {
			pompe.fadeTime_( gui.specFadeTime.map( tilty.linlin( -1, 1, 1, 0 )));
//			if( pompe.solo.current.notNil, {
//				pompe.taskSched( nil, \taskRecordStartStop, proc, pompe.solo.current, nil, gui.eraser );
//			}, {
//				TypeSafe.methodError( thisMethod, "No record source (solo'ed proc)" );
//			});
			pompe.taskSched( nil, \taskRecordStartStop, vertex, gui.panel.selectedBoxes.first.vertex, nil, gui.eraser );
		},
		\out, {
//			[ "YO OUT", metaData.copyToEnd( 1 )].postln;
			dragVal		= metaData[ 1 ];
			dragType		= \out;
		},
		nil, { // that's us
			if( gui.eraser or: { gui.isAltDown }, {
				dragType = \erase;
			}, {
				if( gui.panel.selectedBoxes.includes( this ).not, {
					gui.panel.selectedBoxes.copy.do( _.deselect );
					this.select;
				});
			});
		});
	}
	
	select {
		gui.panel.selectedBoxes.add( this );
		java.setSelected( true );
	}
	
	deselect {
		gui.panel.selectedBoxes.remove( this );
		java.setSelected( false );
	}

	mouseDrag { arg view, x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ, rotation,
	                absoluteX, absoluteY, buttonMask, tanPressure, metaData;
     	
     	if( tabletFirst, { this.prMouseDown( view, x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ,
     	                                     rotation, absoluteX, absoluteY, buttonMask, tanPressure, metaData.copyToEnd( 2 ))});
     	if( dying, { ^this });
     	
     	switch( dragType,
     	\slide, {
     		dragVal = dragAttr.spec.unmap( dragAttr.spec.map( (dragAttrStartVal + ((x - dragStartX) * 0.005)).clip( 0, 1 )));
			java.updateAttrDrag( dragAttr.name, dragVal );
			if( dragAttr.shouldFade.not, {
				pompe.taskSched( nil, \taskChangeAttr, vertex, dragAttr, dragVal, 0.0 );
			}, {
				pompe.fadeTime_( gui.specFadeTime.map( tilty.linlin( -1, 1, 1, 0 )));
			});
		},
		\erase, {
			pompe.fadeTime_( gui.specFadeTime.map( tilty.linlin( -1, 1, 1, 0 )));
     	});
     }

	mouseUp { arg view, x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ, rotation,
	              absoluteX, absoluteY, buttonMask, tanPressure, metaData;
		var targetBox, attr;

		tabletFirst = false;

		if( dying, {
			dragType = \none;
			^this;
		});
		
		switch( dragType,
		\slide, {
			java.stopAttrDrag( dragAttr.name );
			if( dragAttr.shouldFade, {
				pompe.taskSched( nil, \taskChangeAttr, vertex, dragAttr, dragVal,
					if( hasMix and: { vertex.proc.unit.getMix == 0 }, 0.0 ));
			});
     		dragAttr = nil;
     	},
     	\erase, {
	     	dragType = false;
			pompe.taskSched( nil, \taskFadeRemoveProc, vertex );
     	},
		\out, {
			if( metaData[ 0 ] == \box, {
				targetBox = gui.panel.findBox( metaData[ 1 ]);
				if( targetBox.notNil, {
					switch( metaData[ 2 ], \in, {
						pompe.taskSched( nil, \taskConnect, vertex, dragVal, targetBox.vertex, metaData[ 3 ]);
					},
					\sid, {
//						attr = targetBox.findAttr( metaData[ 3 ]);
						attr = targetBox.vertex.proc.unit.attributes[ metaData[ 3 ]];
//[ "xxxxx1", attr, if( attr.notNil, {ÊtargetBox.vertex.proc.isControlMapped( attr )}), metaData, vertex.controlOutlets.size, dragVal ].postln;
//[ 2, "isControlMapped", targetBox, targetBox.vertex.proc, targetBox.vertex.proc.unit, attr, metaData[ 3 ]].postln;
						if( attr.notNil and: { targetBox.vertex.proc.isControlMapped( attr ).not and: { dragVal < vertex.controlOutlets.size }}, {
//"xxxxx2".postln;
							pompe.taskSched( nil, \taskMap, vertex, dragVal, targetBox.vertex, metaData[ 3 ]);
						});
					});
				});
			});
		});
     	
     	dragType = \none;
     }

	prInitUnit { arg unit;
		var attrs;
		attrs = unit.attributes;
//		attrs.postcs;
		mapAttrs = IdentityDictionary.new;
		attrs.do({ arg attr;
//			[ "YO", attr ].postln;
			java.addAttr( attr.name, attr.type === \pan, attr.getNormalizedValue( unit ), attr.canMap );
			mapAttrs.put( attr.name, attr );
		});
	}
	
	asString {
		^("NuagesGUIBox( id: " ++ this.id ++ "; vertex: " ++ this.vertex ++ " )");
	}
	
//	doesNotUnderstand { arg ... args;
//		var result = java.doesNotUnderstand( *args );
//		^if( result === java, this, result );
//	}
	
	remove {
		dragAttr = nil;
		updProc.remove;
		gui.removeBox( this ); // this removes us from the selection
		java.destroy; java = nil;
		meterBus.free; meterBus = nil;
	}

	asSwingArg { ^java.asSwingArg }
	
	id { ^java.id }
}