/**
 *	@author	Hanns Holger Rutz
 *	@version	0.10, 30-Jul-08
 */
EGMFullbodyVisualizer {
	var triplets;
	var scale, scaleC;
	var indices, allConn, drawConn;
	var trans;
	var clpse;
	var <win, extent;
	var <>trig = false;
	var <>trigColr, <>trigQuad = 0;
	var <colors;
	var <>text	= " ";
	var upd;
	
	*new { arg extent = 240;
		^super.new.prInit( extent );
	}
	
	redraw { arg newTriplets;
		clpse.instantaneous( newTriplets );
	}
	
	prInit { arg argExtent;
		extent	= argExtent;
		scale	= extent / 6000;
		scaleC	= [ scale, scale, scale.neg ];
		indices = [[ 0, 1 ], [ 0, 2 ], [ 1, 2 ]];
		allConn = [[ 1, 4, 7 ], [ 0, 2 ], [ 1, 3 ], [ 2 ], [ 0, 5 ], [ 4, 6 ], [ 5 ], [ 0, 8, 9, 13 ], [ 7 ], [ 7, 10 ], [ 9, 11 ], [ 10, 12 ], [ 11 ], [ 7, 14 ], [ 13, 15 ], [ 14, 16 ], [ 15 ]];
		drawConn = allConn.collect({ arg pt, i; pt.select({ arg j; j > i })});
		trans = extent / 2;
		clpse = Collapse({ arg newTriplets;
			if( win.isClosed.not, {
				triplets = newTriplets.collect({ arg x; x * scaleC }) + trans;
				win.refresh;
			});
		}, 0.05 );
		
		trigColr = Color.white;
		colors = Color.black ! 17;

		win = JSCWindow( "Positions", Rect( 0, 0, (extent << 1) + 20, (extent << 1) + 20 ), resizable: false );
		ScissUtil.positionOnScreen( win, 0.05, 0.05 );
		win.view.background = Color.white;
		win.drawHook = {
			this.prDrawHook;
		};
		win.onClose = {
			this.prDispose;
		};
		win.front;
		
		upd = UpdateListener.newFor( EGMFullbodyTracker, { arg upd, track, msg;
			this.redraw( msg.data.select({ arg x, i; i.div( 3 ).odd }).clump( 3 ));
		}, \msg );
	}
	
	close { win.close }
	
	prDispose {
		upd.remove;
	}
	
	prDrawHook {
		var xidx, yidx, pts, pt2, r;
		JPen.translate( 10, 10 );
		r = Rect( 0, 0, extent, extent );
		indices.do({ arg ind, j;
			xidx = ind[ 0 ];
			yidx = ind[ 1 ];
			JPen.use({
				JPen.translate( (j & 1) * extent, (j >> 1) * extent );
				if( j == trigQuad, {
					if( trig, {
						trigColr = Color.red;
						trig = false;
					}, {
						trigColr = trigColr.blend( Color.white, 0.3 );
					});
					JPen.fillColor = trigColr;
					JPen.fillRect( r );
					JPen.fillColor = Color.black;
				});
				JPen.strokeRect( r );
				JPen.stringAtPoint( "XYZ"[ xidx ] ++ "-" ++ "XYZ"[ yidx ], 6 @ 4 );
				pts = triplets.collect({ arg triplet; triplet[ xidx ] @ triplet[ yidx ]});
				
if( pts.size > 17, {
	("pts.size is " ++ pts.size).warn;
	pts = pts.keep( 17 );
});
				pts.do({ arg pt, i;
					JPen.fillColor = colors[ i ];
					JPen.fillOval( Rect.aboutPoint( pt, 3, 3 ));
					drawConn.at( i ).do({ arg target;
						pt2 = pts[ target ];
						if( pt2.notNil, {
							JPen.line( pt, pt2 );
						});
					});
				});
				JPen.stroke;
			});
		});
		JPen.stringInRect( text,
			Rect( extent, extent, extent, extent ));
	}
}
