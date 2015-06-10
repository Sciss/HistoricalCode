/**
 *	(C)opyright 2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.11, 29-Jun-09
 */
OhrwaldModel {
	classvar <hangle = 0;
	classvar <winCtrl;
	classvar <degUnit, <degSpec;
	
	*initClass {
		Class.initClassTree( ControlSpec );
		degUnit = "" ++ 0xC2.asAscii ++ 0xB0.asAscii;
		degSpec = ControlSpec( -135, 135, default: 0, units: degUnit );
	}

	*setAngle { arg who, newH;
		var diff = false;
		if( newH.notNil, { if( hangle != newH, { diff = true; hangle = newH })});
//		if( newV.notNil, { if( vangle != newV, { diff = true; vangle = newV })});
		if( diff, { this.tryChanged( \angle, who )});
	}
	
//	*pieceIdx { ^degSpec.unmap( hangle ) * (numAudioFiles - 1) }
//	*layerIdx { ^degSpec.unmap( vangle ) * (numLayers - 1) }
	
	*normalizedHAngle { ^degSpec.unmap( hangle )}
	
	*gui {
		var win, ggHorizAngle, clpseUpdate, m, upd;
		
		if( winCtrl.notNil, { ^winCtrl.front });
		
		m			= this;
	
		win			= JSCWindow( "Ohrwald : Control", Rect( 0, 0, 120, 120 ), resizable: false, server: Ohrwald.swing );
		ggHorizAngle	= Ohrwald.prSwing {ÊEZKnob( win, Rect( 8, 8, 60, 100 ), "Horizontal", degSpec, { arg view;
			m.setAngle( view, view.value );
		}, 0, unitWidth: 12 )};
		ggHorizAngle.labelView.align = \center;
//		ggVertAngle	= EZKnob( winCtrl, Rect( 80, 8, 60, 100 ), "Vertical", degSpec, { arg view;
//			model.setAngles( view, nil, view.value );
//		}, 0, unitWidth: 12 );
//		ggVertAngle.labelView.align = \center;
//		StaticText( win, Rect( 152, 8, 60, 18 )).align_( \center ).string_( "Gluion" );
		
		clpseUpdate = Collapse({ ggHorizAngle.value = m.hangle; /* ggVertAngle.value = m.vangle */ }, 0.03 );
		upd = UpdateListener.newFor( m, { arg model, what, who;
			if( who != ggHorizAngle /* and: { who != ggVertAngle } */, {
				clpseUpdate.instantaneous;
			});
		}, \angle );
		clpseUpdate.func.value;

		win.onClose = {
			clpseUpdate.cancel;
			upd.remove;
			winCtrl = nil;
		};
		ScissUtil.positionOnScreen( win, 0.2, 0.2 );
		win.front;
		winCtrl = win;
		^winCtrl;
	}
}