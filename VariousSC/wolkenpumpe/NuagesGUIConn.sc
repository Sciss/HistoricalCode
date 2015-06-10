/**
 *	NuagesGUIConn
 *	(Wolkenpumpe)
 *
 *	(C)opyright 2005-2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.13, 31-Jul-09
 *	@author	Hanns Holger Rutz
 */
NuagesGUIConn {
	var java, gui, pompe;
	var <edge, <attr, <isControl;
	
	*new { arg gui, edge, attr;
		^super.new.prInit( gui, edge, attr );
	}
	
	prInit { arg argGUI, argEdge, argAttr;
		var targetVertex;
	
		gui		= argGUI;
		pompe	= gui.pompe;
		edge		= argEdge;
		attr		= argAttr;
		isControl	= attr.notNil;

		TypeSafe.checkArgClasses( thisMethod, [ gui,       edge,     attr ],
		                                      [ NuagesGUI, NuagesE,  UnitAttr ],
		                                      [ false,     false,    true ]);
		
//		[ "target", targetBox.proc.unit.attributes.collect(_.name) ].postcs;
//		[ "name", if( isControl, { attr.name })].postcs;
		targetVertex = edge.target.vertex;
		java = JavaObjectD( "de.sciss.nuages.AttachedConnector", nil, gui.findBox( edge.source.vertex ), edge.source.index,
			gui.findBox( targetVertex ),
			if( isControl, {
				(targetVertex.proc.unit.attributes.detectIndex({ arg a; a.name == attr.name }) + 1).neg
			}, {
				edge.target.index
			}),
			gui.panel )
			.destroyAction_({ arg j; j.dispose });
		java.setID( java.id );

		gui.addConnector( this );
	}
		
	asString {
		^("NuagesGUIConn( id: " ++ this.id ++ "; source: " ++ this.sourceBox ++ "; target: " ++ this.targetBox ++ " )");
	}
	
//	doesNotUnderstand { arg ... args;
//		var result = java.doesNotUnderstand( *args );
//		^if( result === java, this, result );
//	}

	alignTarget { java.alignTarget }
	
	mouseDown { arg view, x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ, rotation,
	                absoluteX, absoluteY, buttonMask, tanPressure, metaData;
    		var classes;
    		
    		if( isControl, { ^this });
		
		if( clickCount == 2, {
			classes = pompe.uf.audioFilters;
			NuagesGUI.showPopup( view, (x - 4) @ (y - 4), classes.collect( _.displayName ), { arg value;
				pompe.taskSched( nil, \taskInsertFilter, classes[ value ].name, edge, x @ y );
			} /*, { "Cancelled!".postln } */ );
		});
	}

	mouseDrag { arg view, x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ, rotation,
     	absoluteX, absoluteY, buttonMask, tanPressure, metaData;
     	
     }

	mouseUp { arg view, x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ, rotation,
     	absoluteX, absoluteY, buttonMask, tanPressure, metaData;
     	
     }

	remove {
		gui.removeConnector( this );
		java.destroy; java = nil;
	}

	asSwingArg { ^java.asSwingArg }
	
	id { ^java.id }
}