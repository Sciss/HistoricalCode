/**
 *	(C)opyright 2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.14, 01-Jul-09
 */
Ohrwald {
	classvar <>workDir              = "/Volumes/KarminUser/Projekte/Ohrwald/sc";
	classvar <>numPathChans;
	classvar <>masterNumChannels;
	classvar <>c1Chans;
	classvar <>c5Chans;
	classvar <>islandChans;
	classvar <>circChans;
	classvar <>circChansT;  // with tower
	classvar <>soundCard            = "Fireface 800 (EB1)";
	classvar <>numOutputBusChannels = 28;
	classvar <>numAudioBusChannels  = 1024;	// this high is actually only needed for some Bosque sessions...

	classvar <scsynth, <swing;
	classvar <pathGUIWin, <ampGUIWin, <curveGUIWin;
//	classvar <model, <player;
	classvar <>soundCardChans;
	classvar <>escapeKey	= true;	// true to have pressing ESCAPE shut down the computer

	classvar <masterBus, <masterGroup, <masterSynth, <masterGain = -6.0;
	classvar <curve;		// Env: day correction gain curve (dB)
	classvar curveTimeSpec;
	
	classvar <>openingHour	= 10;
	classvar <>closingHour	= 22; // open times sometimes extended past 19h;
	
	// day curve
		
	*initClass {
		circChans = [ 8, 9, 0, 10, 1, 11, 2, 12, 3, 13, 4, 14, 15, 5, 16, 6, 17, 18, 7, 19, 20 ];
		c1Chans = (8..20);
		c5Chans = (0..7);
		islandChans = [[ 10, 1, 11 ], [ 12, 3, 13 ], [ 15, 5, 16 ], [ 18, 7, 19 ]];
		numPathChans = circChans.size;
		Assertion({ numPathChans == 21 });
		circChansT = circChans ++ [ 21 ];
		masterNumChannels = circChansT.size;
		
		soundCardChans = Array.fill( circChansT.size, { arg ch; if( ch < 8, ch, ch + 4 )});
		
		// TEST
		Class.initClassTree( Env );
//		curve = Env([ 0, 0 ], [ 1 ]);
	}

//	*new {
//		^super.new.prInit;
//	}
//	
//	prInit {
//	
//	}
	
	*guiLaunch {
		var win, flow, ggLaunch, ggCountDown, ggStopCount, rCount;
//		var respCancel;
//		var ggManualMode;
		
		win = Window( "Ohrwald : Launcher", Rect( 0, 0, 400, 100 ), resizable: false );
		ScissUtil.positionOnScreen( win );
		flow	= FlowLayout( win.view.bounds );
		win.view.decorator = flow;
		
		ggLaunch = Button( win, Rect( 0, 0, 96, 30 ));
		ggLaunch.states = [[ "Launch" ]];
		ggLaunch.action = {
			this.boot;
			win.close;
		};
		
		ggCountDown = StaticText( win, Rect( 0, 0, 40, 30 ));
		
		ggStopCount = Button( win, Rect( 0, 0, 120, 30 ));
		ggStopCount.states = [[ "Stop Countdown" ]];
		ggStopCount.action = {
			rCount.stop;
//			win.close;
		};
		
		flow.nextLine;
		flow.shift( 0, 8 );
		
//		ggManualMode = Button( win, Rect( 0, 0, 96, 30 ))
//			.states_([[ "Manual Mode" ], [ "Manual Mode", Color.white, Color.blue ]]);
		
		win.onClose	= {
			rCount.stop;
//			respCancel.remove;
		};
		
		rCount = Routine.run({
			10.do({ arg i;
				ggCountDown.string = (10 - i).asString;
				1.0.wait;
			});
//			ggLaunch.valueAction_( 0 );
			ggLaunch.doAction;
		}, clock: AppClock );
		
//		respCancel = OSCresponderNode( nil, '/cancel', { arg time, resp, msg;
//			var host, port;
//			rCount.stop;
//			"Received /cancel".inform;
//			
//			host 	= msg[ 1 ];
//			port 	= msg[ 2 ];
//			NetAddr( host, port ).sendMsg( '/cancel.done' );
//		}).add;
		
		win.front;
	}
	
	*boot { arg doneAction, launch = true;
		var o, clpseFailed, dat, elapsed, timMin, timMax, timDiff;
		scsynth                = Server.default;
		o                      = scsynth.options;
		o.device               = soundCard;
		o.numOutputBusChannels = numOutputBusChannels;
		o.numAudioBusChannels  = numAudioBusChannels;
		
		
		dat					= Date.getDate;
		elapsed				= Main.elapsedTime;
		timDiff				= (((dat.hour * 60) + dat.minute) * 60) + dat.second - elapsed;
		curveTimeSpec			= ControlSpec( openingHour * 3600 - timDiff, closingHour * 3600 - timDiff );
	
		curve				= (workDir +/+ "curve.scd").loadPath;
		if( curve.isNil, {
			curve			= Env([ 0, 0 ], [ 1 ]);
		});

		// sometimes the server fails to start...
		clpseFailed			= Collapse({
			"\n\nWARNING : EITHER SCSYNTH OR SWING WAS NOT PROPERLY STARTED!\nRecompiling...".postln;
			thisProcess.recompile;
		}, 20.0 ).defer;
		scsynth.waitForBoot({
			swing			= SwingOSC.default;
			swing.waitForBoot({
				Routine({
					// there is a problem with SwingOSC sometimes connecting twice...
					1.0.wait;
					swing.sync( timeout: 21 ); // note: the clpseFailed will be faster
					clpseFailed.cancel;
					this.gui;
					this.initAudio( launch );
					doneAction.value;
				}).play( AppClock );
			});
		});
	}
	
	*setMasterGain { arg who, newVal;
		if( masterGain != newVal, {
			masterGain = newVal;
			masterSynth.set( \amp, masterGain.dbamp );
			this.tryChanged( \gain, who, masterGain );
		});
	}

	// called when the ESCAPE key is pressed
	// ; disposes all machines and shuts down the computer (when powerDown == true)
	//
	// WARNING: must be in App thread because of storePostWin
	*shutDown { arg powerDown = true;
		"Cleaning up...".inform;

		"Quitting server...".inform;
		scsynth.quit;
		"Storing post window log...".inform;
		this.storePostWin;
		if( powerDown, {
			"Shutting down computer...".inform;
			unixCmd( "osascript -e 'tell application \"Finder\" to shut down'" );
		});
	}

	/**
	 *	Stores the contents of the post window
	 *	in a text file <workDir>/log<YYMMDD>.txt
	 *
	 *	must be called in Cocoa thread
	 */
	*storePostWin {
		var d, f;
		
		d = Document.listener;
		try {
			f = File( workDir +/+ "log" +/+ "log%.txt".format( Date.getDate.stamp ), "w" );
			f.write( d.text );
			f.close;
		} {
			arg error; error.postln;
		};
	}
	
	*initAudio { arg launch;
		var shutDownSynth, routCurve;
		if( escapeKey, {
			shutDownSynth = SynthDef( \owShutDown, {
				SendTrig.kr( KeyState.kr( 53, lag: 0 ));
			}).play( scsynth );
		
			OSCpathResponder( scsynth.addr, [ '/tr', shutDownSynth.nodeID ], { arg time, resp, msg;
				resp.remove;
				{Êthis.shutDown }.defer;
			}).add;
		});
		
		masterBus		= Bus.audio( scsynth, masterNumChannels );
		masterGroup	= Group( scsynth );
		masterSynth	= SynthDef( \owMaster, { arg in, amp = 1, curve = 1;
			var inp, outs;
			inp  = Limiter.ar( In.ar( in, masterNumChannels ) * (amp * curve) );
			outs = Control.names([ \outs ]).kr( Array.series( masterNumChannels ));
			outs.do({ arg ch, i; Out.ar( ch, inp[ i ])});
		}).play( masterGroup, [ \in, masterBus, \outs, soundCardChans, \amp, masterGain.dbamp ]);
	
if( launch, {		
		routCurve = Routine({
			var c, t;
			inf.do({
				t = curveTimeSpec.unmap( thisThread.seconds );
				c = curve.at( t ).dbamp;
//				[ t, c ].postln;
				masterGroup.set( \curve, c );
				1.0.wait;
			});
		}).play( AppClock );
		
		OhrwaldPlayer.init;
});
	}
	
	*prSwing { arg func;
		var oldDef, oldKit;
		oldDef = SwingOSC.default;
		oldKit = GUI.current;
		SwingOSC.default = swing;
		GUI.swing;
		^func.protect { SwingOSC.default = oldDef; GUI.set( oldKit )}
	}
	
	*gui {
		var win, w, h, gainSpec, ggGain, clpseGainUpd;
		w = 192;
		win = JSCWindow( "Ohrwald", Rect( 0, 0, w + 8, 174 ), resizable: false, server: swing )
			.userCanClose_( false );
			
		h = 4;
		JSCButton( win, Rect( 4, h, w, 24 ))
			.canFocus_( false )
			.states_([[ "Path GUI" ]])
			.action_({ arg view; this.pathGUI });
		h = h + 28;
		JSCButton( win, Rect( 4, h, w, 24 ))
			.canFocus_( false )
			.states_([[ "Model GUI" ]])
			.action_({ arg view; OhrwaldModel.gui });
		h = h + 28;
		JSCButton( win, Rect( 4, h, w, 24 ))
			.canFocus_( false )
			.states_([[ "Player GUI" ]])
			.action_({ arg view; OhrwaldPlayer.gui });
		h = h + 28;
		JSCButton( win, Rect( 4, h, w, 24 ))
			.canFocus_( false )
			.states_([[ "Curve GUI" ]])
			.action_({ arg view; this.curveGUI });
		h = h + 28;
		JSCButton( win, Rect( 4, h, w, 24 ))
			.canFocus_( false )
			.states_([[ "Amp GUI" ]])
			.action_({ arg view; this.ampGUI });
		h = h + 28;
		gainSpec = ControlSpec( -36, 12, units: " dB", default: 0 );
		this.prSwing {ÊggGain = EZSlider( win, Rect( 4, h, w, 24 ), "Gain:", gainSpec, { arg ez;
				this.setMasterGain( ez, ez.value );
			}, masterGain, false, 34, unitWidth: 24 )};

		clpseGainUpd = Collapse({ ggGain.value = masterGain }, 0.05 );		UpdateListener.newFor( this, { arg upd, ow, who, param;
			if( who != ggGain, { clpseGainUpd.instantaneous });
		}, \gain );
			
		ScissUtil.positionOnScreen( win, 0.92, 0.08 );
		win.front;
		^win;
	}
	
	*curveGUI {
		var win, ggEnv, ggTimeLine, tlx = -10, routTimeLine, colrGreen, colrGray, w, h;
		var hours, gains, fnt, clpseEnvUpd, clpseEnvSave, fEnvUpdate;
		
		if( curveGUIWin.notNil, { ^curveGUIWin.front });
	
		w = 384;
		h = 324;
		hours = (openingHour..closingHour).collect( _.asString );
		gains = Array.series( 7, 9, -3 ).collect({ arg v; if( v > 0, "+%", "%" ).format( v )});
		fnt = JFont( "Lucida Grande", 9 );
		win = JSCWindow( "Ohrwald : Curve", Rect( 0, 0, w + 48, h + 48 ), resizable: false, server: swing );

		colrGreen = Color.green( 0.6 );
		colrGray  = Color.gray( 0.5 );
		ggTimeLine = JSCUserView( win, Rect( 0, 0, w + 48, h + 48 ))
			.canFocus_( false )
			.drawFunc_({ arg view; var x, y;
				JPen.translate( 24, 24 );
				JPen.font        = fnt;
				JPen.lineDash    = [ 2.0, 2.0 ];
				JPen.strokeColor = colrGray;
				hours.do({ arg hour, i;
					x = i.linlin( 0, hours.size - 1, 0, w );
					JPen.line( x @Ê0, x @ h - 1 );
//					JPen.stringAtPoint( hour, (x + 2) @ (h - 16) );
					JPen.stringCenteredIn( hour, Rect( x - 12, h, 24, 20 ));
				});
				gains.do({ arg gain, i;
					y = i.linlin( 0, gains.size - 1, 0, h );
					JPen.line( 0 @Êy, w - 1 @Êy );
					JPen.stringRightJustIn( gain, Rect( -24, y - 11, 19, 20 ));
				});
				JPen.stroke;
				JPen.lineDash	   = [ 1.0 ];
				JPen.strokeColor = colrGreen;
				JPen.width       = 2;
				JPen.line( tlx @Ê0, tlx @ h - 1 );
				JPen.stroke;

			});
			
		routTimeLine = Routine({ var newTLX;
			inf.do({
				newTLX = (curveTimeSpec.unmap( thisThread.seconds ) * w).asInteger;
				if( tlx != newTLX, {
					tlx = newTLX;
					ggTimeLine.refresh;
				});
				1.wait;
			});
		}).play( AppClock );
			
//		updEnv = UpdateListener.newFor( this, { arg upd, who, what;
//		
//		}, \curve );

		ggEnv = JSCEnvelopeView( win, Rect( 24, 24, w, h ))
			.drawLines_( true )
			.clipThumbs_( true )
			.selectionColor_( Color.red )
//			.resize_( 5 )
			.action_({ arg b;
			
			})
			.thumbSize_( 5 )
			.lockBounds_( true )
			.horizontalEditMode_( \relay )
			.mouseDownAction_({ arg view, x, y, mod, but, clickCount;
				var selIdx, sel, tim, val, times, values, insIdx, b, changed = false;
				sel    = view.selection;
				selIdx = sel.detectIndex( _.value );
				if( selIdx.isNil, {
					if( clickCount == 2, {  // --> insert
						b           = view.bounds;
						tim         = x.linlin( 0, b.width - 1, 0.0, 1.0 );
						#times, values = view.value;
						insIdx      = times.detectIndex({ arg t; t >= tim });
						if( times[ insIdx ] != tim, {
							val         = y.linlin( 0, b.height - 1, 1.0, 0.0 );
//							[ y, b.height, val ].postln;
							times       = times.copyFromStart( insIdx - 1 ) ++ [ tim ] ++ times.copyToEnd( insIdx );
							values      = values.copyFromStart( insIdx - 1 ) ++ [ val ] ++ values.copyToEnd( insIdx );
							view.value  = [ times, values ];
							changed     = true;
						});
					});
				}, {
					if( (mod & 0x080000) != 0, { // alt down --> delete
						if( (selIdx > 0) && (selIdx < (sel.size - 1)), {
							#times, values = view.value;
							times       = times.copyFromStart( selIdx - 1 ) ++ times.copyToEnd( selIdx + 1 );
							values      = values.copyFromStart( selIdx - 1 ) ++ values.copyToEnd( selIdx + 1 );
							view.value  = [ times, values ];
							changed     = true;
						});
					});
				});
				if( changed, fEnvUpdate );
			})
			.action_({ clpseEnvUpd.instantaneous });
//		
		clpseEnvSave = Collapse({
			var f;
			f = File( workDir +/+ "curve.scd", "w" );
			f.put( curve.asCompileString );
			f.close;
		}, 1.0 );
		fEnvUpdate = {
			curve = ggEnv.asEnv( -9, 9, 1 );
			clpseEnvSave.defer;
		};
		clpseEnvUpd = Collapse( fEnvUpdate, 0.5 );
		ggEnv.setEnv( curve, -9, 9, 0, 1 );

		win.onClose = {
			clpseEnvUpd.cancel;
			clpseEnvSave.cancel;
//			updEnv.remove;
			curveGUIWin = nil;
		};
		ScissUtil.positionOnScreen( win, 0.90, 0.80 );
		win.front;
		curveGUIWin = win;
		^curveGUIWin;
	}
	
	*pathGUI {
		var chanIdx, spkrPathIndices, win, imgPath, meters, ggMode, ggNoiseVol, path, pathPairs, scale,
		    synth, pathPt, pathPtColr, meterGroup, mouseFunc;
		
		if( pathGUIWin.notNil, { ^pathGUIWin.front });
	
		// ------------- SynthDefs -------------
		SynthDef( \pgNoiseTest, { arg bus = 0, amp = 0.1, gate = 1;
			var sig, env;
			sig = PinkNoise.ar;
			env = EnvGen.ar( Env.asr( 0.01, 1.0, 0.1, \lin ), gate, doneAction: 2 );
			Out.ar( bus, sig * env * amp );
		}).send( scsynth );

		// well... we could add delays
		SynthDef( \pgSimu, { arg gate = 1, hiDamp = -3, loDamp = -6;
			var busses, distLog, hiAmp, loAmp, xfade, sig;
			busses   = Control.names([ \busses ]).kr( 0 ! 21 );
			distLog  = LagControl.names([ \distLog ]).kr( 1 ! numPathChans, 0.1 ! numPathChans );
			loAmp    = (distLog * loDamp).dbamp;
			hiAmp    = (distLog * hiDamp).dbamp;
			xfade    = EnvGen.kr( Env.asr( 1.0, 1.0, 1.0, \lin ), gate, doneAction: 2 );
			busses.do({ arg idx, k;
				sig = In.ar( idx );
				sig = (HPZ1.ar( sig ) * hiAmp[ k ]) + (LPZ1.ar( sig ) * loAmp[ k ]);
				XOut.ar( idx, xfade, sig );
			});
		}).send( scsynth );

		spkrPathIndices = (1..6) ++ (8..22);
		Assertion({ spkrPathIndices.size == circChans.size });

		win = JSCWindow( "Ohrwald : Pfad", Rect( 0, 0, 853, 700 ), resizable: false, server: swing );
		
		imgPath		= JSCImage.openURL( "file:" ++ Ohrwald.workDir +/+ "resources" +/+ "pfad.png" );
		meterGroup	= Group.tail( scsynth );

		meters		= [ 740 @ 379, 616 @ 514, 384 @ 592, 237 @ 593, 92 @ 449, 32 @ 171, 195 @ 83, 467 @ 24, 800 @ 167, 784 @ 279,
					    660 @ 468, 563 @ 558, 294 @ 593, 170 @ 591, 31 @ 336, 3 @ 224, 82 @ 109, 307 @ 61, 412 @ 5, 516 @ 44,
					    623 @ 37, 791 @ 41 ]
			.collect({ arg pt, i;
				JSCPeakMeter( win, Rect( pt.x, pt.y, 15, 40 ))
					.rmsPainted_( false )
					.group_( meterGroup )
					.bus_( Bus( \audio, if( i < 8, i, i + 4 ), 1, scsynth ))});

		ggMode = JSCPopUpMenu( win, Rect( 400, 250, 100, 24 )).canFocus_( false );
		ggMode.items = [ "Normal", "Simu", "Noise" ];
		JSCStaticText( win, Rect( 320, 280, 76, 24 )).align_( \right ).string_( "Noise Amp:" );
		ggNoiseVol = JSCNumberBox( win, Rect( 400, 280, 70, 24 )).clipHi_( 0 ).value_( -20 ).minDecimals_( 1 ).maxDecimals_( 1 );
		JSCStaticText( win, Rect( 475, 280, 25, 24 )).string_( "dB" );
		//ggNoiseCycle = JSCButton( win, Rect( 500, 280, 50, 24 )).states_([[ "Cycle" ], [ "Cycle", Color.white, Color.blue ]]);

		path	= [ 757 @ 66, 787 @ 187, 770 @ 297, 721 @ 394, 644 @ 473, 604 @ 509, 560 @ 546, 457 @ 581, 394 @ 579, 303 @ 578,
			    245 @ 580, 190 @ 580, 122 @ 466, 61 @ 350, 31 @ 242, 64 @ 199, 102 @ 159, 204 @ 137, 323 @ 116, 421 @ 60, 469 @ 80,
			    521 @ 101, 633 @ 94, 757 @ 66 ];
		pathPairs = path.slide( 2, 1 ).clump( 2 );
		// pixels per meter (c. 7)
		scale = pathPairs.collect({Êarg x; (x[ 1 ] - x[ 0 ]).rho }).sum / 308;

		win.drawHook = { var extent;
			JPen.imageAtPoint(ÊimgPath, 0 @ 0 );
			if( (ggMode.value == 1 and: { pathPt.notNil }) or: {ÊggMode.value == 2 and: {ÊchanIdx.notNil }}, {
				JPen.fillColor = pathPtColr;
				JPen.width = 2;
				extent = 7;
				JPen.fillOval( Rect.aboutPoint( pathPt, extent, extent ));
			});
		};
	
		ggMode.action = { arg view; var chans;
			switch( view.value,
			0, { // Normal
			},
			1, { // Simu
				pathPtColr = Color.blue;
			},
			2, { // Noise
				pathPtColr = Color.yellow;
			});
			pathPt  = nil;
			chanIdx = nil;
			synth.free; synth = nil;
			if( view.value == 1, { // Simu
		        	  chans = circChans.collect({ arg ch; if( ch > 7, {Êch + 4 }, ch )}); // XXX remove this when going to master bus
				synth = Synth.tail( scsynth, \pgSimu, [ \busses, chans ]);
			});
			win.refresh;
		};

	    mouseFunc = { arg view, x, y, modifiers, buttonNumber, clickCount;
			var mpt, dx, dy, lnP1, lnP2, linePos, lineLenSq, closeP1,
			    proj, dist, ch, minDist = inf, distLog;
    
			if( ggMode.value != 0, {
				mpt = x @ y;
		
				pathPairs.do({ arg pair, i;
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
						chanIdx = if( closeP1, i - 1, i );
						chanIdx = if( chanIdx != 6, {
							if( chanIdx < 6, chanIdx, chanIdx - 1 ).wrap( 0, numPathChans );
						});
						switch( ggMode.value,
							1, { // Simu
							pathPt  = proj;
						},
						2, { // Noise
							pathPt  = if( closeP1, lnP1, lnP2 );
						});
					});
				});
				win.refresh;
				switch( ggMode.value,
				1, {
					distLog = spkrPathIndices.collect({ arg n; (path[ n ].dist( pathPt ) / scale).max( 1 ).log2 });
//					distLog.postln;
					synth.setn( \distLog, distLog );
				},
				2, {
					if( chanIdx.notNil, {
						ch = circChansT[ chanIdx ];
						if( ch > 7, {Êch = ch + 4 }); // XXX remove this when going to master bus
						synth.set( \bus, ch, \amp, ggNoiseVol.value.dbamp );
					}, {
						synth.set( \amp, 0 );
					});
				});
			});
		};
		
		win.view.mouseDownAction = { arg view, x, y, modifiers, buttonNumber, clickCount;
			if( ggMode.value == 2, {
				synth.free;
				synth = Synth( \pgNoiseTest, [ \amp, 0 ], scsynth );
			});
			mouseFunc.value( view, x, y, modifiers, buttonNumber, clickCount );
		};
		win.view.mouseMoveAction = mouseFunc;
		win.view.mouseUpAction = { arg view, x, y;
			if( ggMode.value == 2, {
				pathPt  = nil;
				chanIdx = nil;
				synth.release; synth = nil;
				win.refresh;
			});
		};

		win.onClose = {
			meterGroup.free; meterGroup = nil;
			synth.free; synth = nil;
			imgPath.destroy;
			pathGUIWin = nil;
		};
		ScissUtil.positionOnScreen( win, 0.575, 0.43 );
		win.front;
		pathGUIWin = win;
		^pathGUIWin;
	}
	
	*ampGUI {
		var win, imgAmp, meterGroup, meters;
		
		if( ampGUIWin.notNil, { ^ampGUIWin.front });
		
		win		= JSCWindow( "Ohrwald : Amps", Rect( 0, 0, 611, 336 ), resizable: false, server: swing );
		imgAmp	= JSCImage.openURL( "file:" ++ Ohrwald.workDir +/+ "resources" +/+ "amps.png" );
		win.drawHook = { JPen.imageAtPoint(ÊimgAmp, 0 @ 0 )};
		meterGroup = Group.tail( scsynth );
		meters	= [ 526 @ 145, 412 @ 145, 526 @Ê191, 412 @ 191, 526 @ 237, 412 @Ê237, 526 @Ê283, 412 @ 283, 216 @Ê146,
				    102 @ 146, 216 @Ê192, 102 @ 192, 216 @ 237, 102 @ 237, 216 @ 283, 102 @ 283, 216 @ 100, 102 @ 100,
				    526 @ 23, 412 @ 23, 526 @ 84, 412 @ 84 ]
			.collect({ arg pt, i;
				JSCPeakMeter( win, Rect( pt.x, pt.y, 15, 40 ))
					.rmsPainted_( false )
					.group_( meterGroup ).bus_( Bus( \audio, if( i < 8, i, i + 4 ), 1, scsynth ))});

		win.onClose = {
			meterGroup.free;
			imgAmp.destroy;
			ampGUIWin = nil;
		};
		ScissUtil.positionOnScreen( win, 0.92, 0.92 );
		win.front;
		ampGUIWin = win;
		^ampGUIWin;
	}
}