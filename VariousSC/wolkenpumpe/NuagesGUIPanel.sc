/**
 *	NuagesGUIPanel
 *	(Wolkenpumpe)
 *
 *	(C)opyright 2005-2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.12, 17-Apr-09
 *	@author	Hanns Holger Rutz
 */
NuagesGUIPanel : JSCTabletView {
	var java;
	var <jSCSynth;
	var boxes, connectors;
	var pendingBoxes, <selectedBoxes;
	
	prInitView {
		var bndl;
		
		boxes			= IdentityDictionary.new;
		connectors		= IdentityDictionary.new;
		pendingBoxes		= IdentityDictionary.new;
		selectedBoxes		= List.new;
		
		relativeOrigin	= true;
		cocoaBorder		= -2; // if( parent.prGetWindow.border, 20, -2 );
//		jinsets			= Insets( 3, 3, 3, 3 );
		bndl				= List.new;
		bndl.add([ '/local', this.id, '[', '/new', "de.sciss.nuages.Panel", ']' ]);
		this.prCreateTabletResponder( bndl );
		java				= JavaObjectD.basicNew( this.id, this.server )
			.destroyAction_({ arg j; j.dispose });
		^this.prSCViewNew( bndl );
	}

	doesNotUnderstand { arg ... args;
		var result = java.doesNotUnderstand( *args );
		^if( result === java, this, result );
	}
	
	addBox { arg box;
		var func;
		
		func = { arg upd;
			~box = box;
//			[ "############# addBox ", box.vertex.proc.group, box.vertex.proc.getAudioOutputBus ].postln;
			upd.remove;
			java.addBox( box, if( box.isControl.not, { NuagesBusProxy( box.vertex.proc.getAudioOutputBus, jSCSynth )}),
			                  NuagesGroupProxy( box.vertex.proc.group, jSCSynth ),
			                  NuagesBusProxy( box.meterBus, jSCSynth ), true );
			box.added;
		};
		
		if( box.vertex.proc.group.isPlaying, func , {
//			[ "############# jo, pending! ", box.vertex.proc.group ].postln;
			pendingBoxes.put( box, UpdateListener.newFor( box.vertex.proc.group, func, \n_go ));
			
		});
		boxes.put( box.id, box );
	}
	
	removeBox { arg box;
		var upd;
		java.removeBox( box );
		boxes.removeAt( box.id );
		selectedBoxes.remove( box );
		upd = pendingBoxes.removeAt( box );
		if( upd.notNil, { upd.remove });
	}

	addConnector { arg conn;
		java.addConnector( conn );
		connectors.put( conn.id, conn );
	}

	removeConnector { arg conn;
		java.removeConnector( conn );
		connectors.removeAt( conn.id );
	}
	
	findBox { arg id;
		^boxes.at( id );
	}

	findConnector { arg id;
		^connectors.at( id );
	}

//	placeBoxAfter { arg targetBox, sourceBox;
//		var conn;
//		java.placeBoxAfter( targetBox, sourceBox );
////		conn      = targetBox.sourceConns.reject( _.isControl ).choose;
////		if( conn.notNil, { // recursion: shift the rest of the chain
////			this.placeBoxAfter( conn.targetBox, targetBox );
////		});
//	}
	
	scsynth_ { arg scsynth;
		var jSCSynthOptions;
		if( jSCSynth.notNil, {
			jSCSynth.destroy;
			jSCSynth = nil;
		});
		if( scsynth.notNil, {
			jSCSynthOptions = JavaObject( "de.sciss.jcollider.ServerOptions", this.server );
			if( scsynth.options.protocol !== \udp, { jSCSynthOptions.setProtocol( scsynth.options.protocol )});
			// clientID := 1 !!
			jSCSynth = JavaObjectD( "de.sciss.jcollider.Server", this.server, scsynth.name, scsynth.addr, jSCSynthOptions, 1 )
				.destroyAction_({ arg j; j.dispose });
			jSCSynthOptions.destroy;
			jSCSynth.start;
		});
		java.setServer( jSCSynth );
	}

	prCreateTabletResponder { arg bndl;
		var msg, win;
	
		if( tabletResp.notNil, {
			(thisMethod.asString ++ " - already created!").warn;
			^nil;
		});
	// [ "/nuages", <componentID>, <state>, <deviceID>, <localX>, <localY>, <pressure>,
	//   <tiltX>, <tiltY>, <rota>, <tanPressure>, <absX>, <absY>, <absZ>,
	//   <buttonMask>, <clickCount>, <data...>
		tabletResp		= OSCpathResponder( server.addr, [ '/nuages', this.id ], { arg time, resp, msg;
			var state, deviceID, x, y, pressure, tiltx, tilty, rotation, tanPressure, absoluteX, absoluteY, absoluteZ,
			    buttonMask, clickCount, buttonNumber, bounds, entering, systemTabletID, tabletID, pointingDeviceType,
			    uniqueID, pointingDeviceID, metaData, cmd, id;
		
			state = msg[2];
			
			if( state === \proximity, {
				#cmd, id, state, deviceID, entering, systemTabletID, tabletID, pointingDeviceType, uniqueID, pointingDeviceID = msg;
				entering = entering != 0;
				proximityAction.value( this, entering, deviceID, pointingDeviceType, systemTabletID, pointingDeviceID, tabletID, uniqueID );

			}, {	// from tabletEvent
				#cmd, id, state, deviceID, x, y, pressure, tiltx, tilty, rotation, tanPressure, absoluteX, absoluteY, absoluteZ, buttonMask, clickCount ... metaData = msg;
				
				bounds		= this.bounds;
				x			= x - bounds.left;
				y			= bounds.bottom - y + cocoaBorder; // sucky cocoa
				buttonNumber	= (buttonMask & 2) >> 1;  // hmmm...
	
//([ "metaData" ] ++ metaData).postln;
	
				case { state === \pressed }
				{
					{ this.mouseDown( x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ, rotation,
			                           absoluteX, absoluteY, buttonMask, tanPressure, metaData )}.defer;
				}
				{ state === \released }
				{
					{ this.mouseUp( x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ, rotation,
			                         absoluteX, absoluteY, buttonMask, tanPressure, metaData )}.defer;
				}
				{ state === \moved }
				{
	//				{ this.mouseMoved( x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ, rotation,
	//		                            absoluteX, absoluteY, buttonMask, tanPressure )}.defer;
				}
				{ state === \dragged }
				{
					{ this.doAction( x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ, rotation,
			                          absoluteX, absoluteY, buttonMask, tanPressure, metaData )}.defer;
				};
	// note: entered is followed by moved with equal coordinates
	// so we can just ignore it
	//			{ state === \entered }
	//			{
	//				{ this.mouseOver( x, y, modifiers )}.defer;
	//			};
			});
		});
		tabletResp.add;
		msg = [ '/local', "tab" ++ this.id, '[', '/new', "de.sciss.nuages.PanelResponder", this.id, parent.prGetWindow.id, ']' ];
		if( bndl.notNil, {
			bndl.add( msg );
		}, {
			server.sendBundle( nil, msg );
		});
	}
}