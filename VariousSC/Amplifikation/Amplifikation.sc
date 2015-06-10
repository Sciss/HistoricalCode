/**
 *	Amplifikation
 *
 *	(C)opyright 2009 Hanns Holger Rutz. All rights reserved.
 *
 *	Dependancies: NuagesOSCBundle, ScissUtil
 *
 *	@version	0.12, 31-Aug-09
 *	@author	Hanns Holger Rutz
 */
Amplifikation {
	classvar <>workDir;
	classvar <>masterNumChannels; //    = 4;
	classvar <>soundCard; //           = "MOTU 828mk2";
	classvar <>numInputBusChannels = 22;
	classvar <>numOutputBusChannels = 22;
	classvar <>numAudioBusChannels  = 512;

	classvar <scsynth, <swing;
	classvar <>soundCardChans;
	classvar <>escapeKey	= true;	// true to have pressing ESCAPE shut down the computer

	classvar <masterBus, <masterGroup, <masterSynth, <masterGain = -7.0;
	classvar <defWatcher;
		
	classvar respSync, condSync, currentSyncID;
	classvar prefix = "amp";	// common class prefix
	
	*initClass {
		workDir			= "~/Desktop/TMA09".standardizePath;
		
		if( workDir.contains( "rutz" ), {
			soundCard			= "MOTU 828mk2";
			masterNumChannels	= 4;
			soundCardChans	= Array.series( masterNumChannels, 2 ); // eventually, deal with adat offset...
		}, {
			soundCard			= "Fireface 800 (EB1)";
			masterNumChannels	= 16;
//			soundCardChans	= (0..7) ++ (14..21); // eventually, deal with adat offset...
			soundCardChans	= (0..7) ++ (12..19); // eventually, deal with adat offset...
			numInputBusChannels  = 28;
			numOutputBusChannels = 28;
		});
	}

	*forkSync {
		currentSyncID = UniqueID.next;
		condSync.test = false;
		^[ '/sync', currentSyncID ];
	}
	
	*server { ^scsynth }
	
	*joinSync {
		condSync.wait;
	}
	
	*speakerTest {
		ScissUtil.speakerTest( channels: soundCardChans );
	}
	
	*audioDir { ^(workDir +/+ "audio_work") }
	
	*guiLaunch {
		var win, flow, ggLaunch, ggCountDown, ggStopCount, rCount;
		
		win = Window( "% : Launcher".format( this.name.asString ), Rect( 0, 0, 400, 100 ), resizable: false );
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
		};
		
		flow.nextLine;
		flow.shift( 0, 8 );
		
		win.onClose	= {
			rCount.stop;
		};
		
		rCount = Routine.run({
			10.do({ arg i;
				ggCountDown.string = (10 - i).asString;
				1.0.wait;
			});
			ggLaunch.doAction;
		}, clock: AppClock );
		
		win.front;
	}
	
	*newBundle { ^NuagesOSCBundle( this )} // eventually we should copy that class...
	
	*boot { arg doneAction, launch = true;
		var o, clpseFailed;
		scsynth                = Server.default;
		o                      = scsynth.options;
		o.device               = soundCard;
		o.numInputBusChannels  = numInputBusChannels;
		o.numOutputBusChannels = numOutputBusChannels;
		o.numAudioBusChannels  = numAudioBusChannels;

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

	*initAudio { arg launch;
		var shutDownSynth, routCurve;

		condSync			= Condition.new;
		condSync.test		= true; // initially no need to wait

		respSync			= OSCresponderNode( scsynth.addr, '/synced', { arg time, resp, msg;
			if( msg[ 1 ] == currentSyncID, {
				condSync.test = true; condSync.signal;
			});
		}).add;
		
		defWatcher		= NuagesSynthDefWatcher.newFrom( scsynth );
		
		if( escapeKey, {
			shutDownSynth = SynthDef( "%ShutDown".format( prefix ), {
				SendTrig.kr( KeyState.kr( 53, lag: 0 ));
			}).play( scsynth );
		
			OSCpathResponder( scsynth.addr, [ '/tr', shutDownSynth.nodeID ], { arg time, resp, msg;
				resp.remove;
				{Êthis.shutDown }.defer;
			}).add;
		});
		
		masterBus		= Bus.audio( scsynth, masterNumChannels );
		masterGroup	= Group( scsynth );
		masterSynth	= SynthDef( "%Master".format( prefix ), { arg in, amp = 1, curve = 1;
			var inp, outs;
			inp  = Limiter.ar( In.ar( in, masterNumChannels ) * (amp * curve) );
			outs = Control.names([ \outs ]).kr( Array.series( masterNumChannels ));
			outs.do({ arg ch, i; Out.ar( ch, inp[ i ])});
		}).play( masterGroup, [ \in, masterBus, \outs, soundCardChans, \amp, masterGain.dbamp ]);
	
if( launch, {		
//		routCurve = Routine({
//			var c, t;
//			inf.do({
//				t = curveTimeSpec.unmap( thisThread.seconds );
//				c = curve.at( t ).dbamp;
////				[ t, c ].postln;
//				masterGroup.set( \curve, c );
//				1.0.wait;
//			});
//		}).play( AppClock );
		
		AmpPlayer.init;
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
	
	*setMasterGain { arg who, newVal;
		if( masterGain != newVal, {
			masterGain = newVal;
			masterSynth.set( \amp, masterGain.dbamp );
			this.tryChanged( \gain, who, masterGain );
		});
	}

	*gui {
		var win, w, h, gainSpec, ggGain, clpseGainUpd;
		w = 192;
		win = JSCWindow( this.name.asString, Rect( 0, 0, w + 8, 174 ), resizable: false, server: swing )
			.userCanClose_( false );
			
		h = 4;
//		JSCButton( win, Rect( 4, h, w, 24 ))
//			.canFocus_( false )
//			.states_([[ "Path GUI" ]])
//			.action_({ arg view; this.pathGUI });
//		h = h + 28;
//		JSCButton( win, Rect( 4, h, w, 24 ))
//			.canFocus_( false )
//			.states_([[ "Model GUI" ]])
//			.action_({ arg view; OhrwaldModel.gui });
//		h = h + 28;
//		JSCButton( win, Rect( 4, h, w, 24 ))
//			.canFocus_( false )
//			.states_([[ "Player GUI" ]])
//			.action_({ arg view; OhrwaldPlayer.gui });
//		h = h + 28;
//		JSCButton( win, Rect( 4, h, w, 24 ))
//			.canFocus_( false )
//			.states_([[ "Curve GUI" ]])
//			.action_({ arg view; this.curveGUI });
//		h = h + 28;
//		JSCButton( win, Rect( 4, h, w, 24 ))
//			.canFocus_( false )
//			.states_([[ "Amp GUI" ]])
//			.action_({ arg view; this.ampGUI });
//		h = h + 28;
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
}