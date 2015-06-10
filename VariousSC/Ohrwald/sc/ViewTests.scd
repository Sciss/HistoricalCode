//o = SwingOptions.new.loopBack_( false );
//g = SwingOSC( \remote, NetAddr( "192.168.5.1", 57111 ), o );

//g.dumpOSC( 0, 0 )

(
var win, imgAmp;
		
win = JSCWindow( "Ohrwald : Amps", Rect( 0, 0, 611, 336 ), resizable: false, server: g );
		
imgAmp = JSCImage.openURL( "file:" ++ Ohrwald.workDir +/+ "resources" +/+ "amps.png" );
win.drawHook = { JPen.imageAtPoint(ÊimgAmp, 0 @ 0 )};
~meterGroup = Group.tail( s );

~meters = [ 526 @ 145, 412 @ 145, 526 @Ê191, 412 @ 191, 526 @ 237, 412 @Ê237, 526 @Ê283, 412 @ 283, 216 @Ê146, 102 @ 146, 216 @Ê192, 102 @ 192, 216 @ 237, 102 @ 237, 216 @ 283, 102 @ 283, 216 @ 100, 102 @ 100, 526 @ 23, 412 @ 23, 526 @ 84, 412 @ 84 ].collect({ arg pt, i; JSCPeakMeter( win, Rect( pt.x, pt.y, 15, 40 )).rmsPainted_( false ).group_( ~meterGroup ).bus_( Bus( \audio, if( i < 8, i, i + 4 ), 1, s ))});

win.onClose = { ~meterGroup.free };
win.front;
)

///////////////////

(
SynthDef( \noiseTest, { arg bus = 0, amp = 0.1, gate = 1;
	var sig, env;
	sig = PinkNoise.ar;
	env = EnvGen.ar( Env.asr( 0.01, 1.0, 0.1, \lin ), gate, doneAction: 2 );
	Out.ar( bus, sig * env * amp );
}).send( s );

// well... we could add delays
SynthDef( \simu, { arg gate = 1, hiDamp = -3, loDamp = -6;
	var busses, distLog, hiAmp, loAmp, xfade, sig;
	busses   = Control.names([ \busses ]).kr( 0 ! 21 );
	distLog  = LagControl.names([ \distLog ]).kr( 1 ! 21, 0.1 ! 21 );
	loAmp    = (distLog * loDamp).dbamp;
	hiAmp    = (distLog * hiDamp).dbamp;
//	loGains  = LagControl.names([ \loGains ]).kr( 1 ! 21, 0.1 ! 21 );
	xfade    = EnvGen.kr( Env.asr( 1.0, 1.0, 1.0, \lin ), gate, doneAction: 2 );
	busses.do({ arg idx, k;
		sig = In.ar( idx );
		sig = (HPZ1.ar( sig ) * hiAmp[ k ]) + (LPZ1.ar( sig ) * loAmp[ k ]);
		XOut.ar( idx, xfade, sig );
	});
}).send( s );
)


(
~chanIdx = nil;
~circChans = [ 8, 9, 0, 10, 1, 11, 2, 12, 3, 13, 4, 14, 15, 5, 16, 6, 17, 18, 7, 19, 20, 21 ];
~spkrPathIndices = (1..6) ++ (8..22);

~win = JSCWindow( "Ohrwald : Pfad", Rect( 50, 50, 853, 700 ), resizable: false, server: g );
		
~imgAmp = JSCImage.openURL( "file:" ++ Ohrwald.workDir +/+ "resources" +/+ "pfad.png" );
~meterGroup = Group.tail( s );

~meters = [ 740 @ 379, 616 @ 514, 384 @ 592, 237 @ 593, 92 @ 449, 32 @ 171, 195 @ 83, 467 @ 24, 800 @ 167, 784 @ 279, 660 @ 468, 563 @ 558, 294 @ 593, 170 @ 591, 31 @ 336, 3 @ 224, 82 @ 109, 307 @ 61, 412 @ 5, 516 @ 44, 623 @ 37, 791 @ 41 ].collect({ arg pt, i; JSCPeakMeter( ~win, Rect( pt.x, pt.y, 15, 40 )).rmsPainted_( false ).group_( ~meterGroup ).bus_( Bus( \audio, if( i < 8, i, i + 4 ), 1, s ))});

~ggMode = JSCPopUpMenu( ~win, Rect( 400, 250, 100, 24 )).canFocus_( false );
~ggMode.items = [ "Normal", "Simu", "Noise" ];
JSCStaticText( ~win, Rect( 320, 280, 76, 24 )).align_( \right ).string_( "Noise Amp:" );
~ggNoiseVol = JSCNumberBox( ~win, Rect( 400, 280, 70, 24 )).clipHi_( 0 ).value_( -20 ).minDecimals_( 1 ).maxDecimals_( 1 );
JSCStaticText( ~win, Rect( 475, 280, 25, 24 )).string_( "dB" );
//~ggNoiseCycle = JSCButton( ~win, Rect( 500, 280, 50, 24 )).states_([[ "Cycle" ], [ "Cycle", Color.white, Color.blue ]]);

~path = [ Point(757, 66), Point(787, 187), Point(770, 297), Point(721, 394), Point(644, 473), Point(604, 509), Point(560, 546), Point(457, 581), Point(394, 579), Point(303, 578), Point(245, 580), Point(190, 580), Point(122, 466), Point(61, 350), Point(31, 242), Point(64, 199), Point(102, 159), Point(204, 137), Point(323, 116), Point(421, 60), Point(469, 80), Point(521, 101), Point(633, 94), Point(757, 66) ];
~pathPairs = ~path.slide( 2, 1 ).clump( 2 );
// pixels per meter
~scale = ~pathPairs.collect({Êarg x; (x[ 1 ] - x[ 0 ]).rho }).sum / 308;

	~win.drawHook = { var extent;
		JPen.imageAtPoint(Ê~imgAmp, 0 @ 0 );
		if( (~ggMode.value == 1 and: { ~pathPt.notNil }) or: {Ê~ggMode.value == 2 and: {Ê~chanIdx.notNil }}, {
//			JPen.strokeColor = ~pathPtColr;
			JPen.fillColor = ~pathPtColr;
			JPen.width = 2;
			extent = 7;
			JPen.fillOval( Rect.aboutPoint( ~pathPt, extent, extent ));
//			JPen.line( ~pathPt + (extent.neg @ extent.neg), ~pathPt + (extent @ extent) );
//			JPen.line( ~pathPt + (extent.neg @ extent), ~pathPt + (extent @ extent.neg) );
//			JPen.stroke;
		});
	};
	
	~ggMode.action = { arg view; var chans;
		switch( view.value,
		0, { // Normal
		},
		1, { // Simu
			~pathPtColr = Color.blue;
		},
		2, { // Noise
			~pathPtColr = Color.yellow;
		});
		~pathPt  = nil;
		~chanIdx = nil;
		~synth.free; ~synth = nil;
		if( view.value == 1, { // Simu
	        	  chans = ~circChans.collect({ arg ch; if( ch > 7, {Êch + 4 }, ch )}); // XXX remove this when going to master bus
			~synth = Synth.tail( s, \simu, [ \busses, chans ]);
		});
		~win.refresh;
	};

    ~mouseFunc = { arg view, x, y, modifiers, buttonNumber, clickCount;
        var mpt, dx, dy, lnP1, lnP2, linePos, lineLenSq, closeP1,
            proj, dist, ch, minDist = inf, distLog;
    
    	    if( ~ggMode.value != 0, {
		mpt = x @ y;
		
        ~pathPairs.do({ arg pair, i;
            #lnP1, lnP2 = pair;
            dx          = lnP2.x - lnP1.x;
            dy          = lnP2.y - lnP1.y;
            lineLenSq   = (dx*dx) + (dy*dy);
            dist        = (((x - lnP1.x) * dx) + ((y - lnP1.y) * dy)) / lineLenSq;
            proj        = (lnP1.x + (dist * dx)) @ (lnP1.y + (dist * dy));
            if( lnP1.x != lnP2.x, {
                linePos = (proj.x - lnP1.x) / dx;
            }, {
                linePos = (proj.y - lnP1.y) / dy;
            });
            if( linePos < 0, {
                proj = lnP1;
            }, { if( linePos > 1, {
                proj = lnP2;
            })});
            dist = proj.dist( mpt );
            if( dist < minDist, {
                minDist = dist;
                closeP1 = proj.dist( lnP1 ) < proj.dist( lnP2 );
                ~chanIdx = if( closeP1, i - 1, i );
                ~chanIdx = if( ~chanIdx != 6, {
                	if( ~chanIdx < 6, ~chanIdx, ~chanIdx - 1 ).wrap( 0, 21 );
                });
//                ~chanIdx.postln;
                switch( ~ggMode.value,
                1, { // Simu
                	~pathPt  = proj;
                },
                2, { // Noise
                	~pathPt  = if( closeP1, lnP1, lnP2 );
                });
            });
        });
        ~win.refresh;
        switch( ~ggMode.value,
        1, {
	        distLog = ~spkrPathIndices.collect({ arg n; (~path[ n ].dist( ~pathPt ) / ~scale).max( 1 ).log2 });
	        distLog.postln;
        		~synth.setn( \distLog, distLog );
        },
        2, {
        		if( ~chanIdx.notNil, {
	        	   ch = ~circChans[ ~chanIdx ];
	        	   if( ch > 7, {Êch = ch + 4 }); // XXX remove this when going to master bus
	        	   ~synth.set( \bus, ch, \amp, ~ggNoiseVol.value.dbamp );
	        	}, {
	        	   ~synth.set( \amp, 0 );
	        	});
        });
      });
    };
    ~win.view.mouseDownAction = { arg view, x, y, modifiers, buttonNumber, clickCount;
    		if( ~ggMode.value == 2, {
    			~synth.free;
    			~synth = Synth( \noiseTest, [ \amp, 0 ], s );
    		});
		~mouseFunc.value( view, x, y, modifiers, buttonNumber, clickCount );
	};
    ~win.view.mouseMoveAction = ~mouseFunc;
    ~win.view.mouseUpAction = { arg view, x, y;
    		if( ~ggMode.value == 2, {
    			~pathPt  = nil;
    			~chanIdx = nil;
    			~synth.release; ~synth = nil;
    			~win.refresh;
    		});
    };

~win.onClose = {
	~meterGroup.free; ~meterGroup = nil;
	~synth.free; ~synth = nil;
};
~win.front;
)


/// TO EDIT THE POSITIONS
(
~meters.do({ arg m;
	var loc;
	m.mouseDownAction = { arg view, x, y;
		loc = view.bounds.leftTop + (x @ y);
	};
	m.mouseMoveAction = { arg view, x, y;
		var newLoc, delta;
		newLoc	= view.bounds.leftTop + (x @ y);
		delta	= newLoc - loc;
		view.bounds = view.bounds.moveBy( delta.x, delta. y );
		loc		= newLoc;
	};
});
)
// read them out:
//~meters.collect({ arg m; m.bounds.leftTop });
~meters.collect({ arg m; "% @ %".format( m.bounds.left, m.bounds.top )});


/// TO DEFINE THE PATH
~win.view.mouseDownAction = { arg view, x, y; ~pt = ~pt.add( x @ y ); [ x , y ].postln };
~win.view.mouseDownAction = nil;

~path = ~pt;
~path = ~path.add( ~path.first );
