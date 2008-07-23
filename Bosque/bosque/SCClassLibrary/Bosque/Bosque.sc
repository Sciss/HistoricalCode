/**
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.27, 19-Jul-08
 */
Bosque : Object {
	classvar <>default;

	// ------------- configure these -------------

	classvar <>soundCard;
	classvar <>numInputBusChannels;
	classvar <>numOutputBusChannels;
	classvar <>workDir;
	classvar <>masterBusNumChannels;
	classvar <>masterChanMap;	// an array of channel indices; array size must equal masterBusNumChannels!
	classvar <>timeBasedJar;

	// -------------------------------------------

	var <swing;
	var <scsynth;
	var <>session;
	var onSwingBoot, onScSynthBoot;

	var <masterGroup, masterSynth, <masterVolume = 1.0;
	var <diskGroup;
	var <preFilterGroup, <postFilterGroup;
	var <panGroup;
	var <mixGroup;
	
	var <masterBus;
	
	var <clipBoard;
	var <cacheManager;
	
	classvar <numFieldsH			= 12;
	classvar <numFieldsV			= 5;
	classvar <numFields			= 60;	// # of neon bulbs
	classvar <numTracks			= 6;
	classvar <tedMicChannel			= 7;

	classvar <>config				= 0; 	// 0 = Q3, 1 = Viehhalle
	
	classvar	<>midiOutDev			= "BCF2000"; // "BCR2000"; // : Port 1";
	classvar	<>midiInDev			= "BCF2000"; // "BCR2000"; // : Port 1";
	classvar <>createCC			= false;

	var <chris;
	var <martin;
	
	// event objects
	var <trackDancer, <trackTrack, <trackField, <trackBang;
	var <trackCurrentDancer, <trackCurrentTrack, <trackCurrentField;
	
	var <>speedBoost	= 1.0;
	var <micGain		= 1.0;
	
	var <subwChannel;
	
	var <midiOut;	// a MIDIOut
	var <midiIn;	// a MIDIEndPoint

	var <master, <app;
	var <timelineEditor;
	
	*initClass {
//		Class.initClassTree( SwingOSC );
//		default = this.new;
	}
	
	*new {
		^super.new.prInit;
	}
	
	prInit {
		if( default.isNil, { default = this });

		swing		= SwingOSC.default;
		scsynth		= Server.default; // Server( \bosque, VerboseNetAddr( "127.0.0.1", 57001 )); // Server.default;
	  swing.doWhenBooted({

		master			= JavaObject( "de.sciss.timebased.net.Master", swing );
		app				= JavaObject( "de.sciss.timebased.Main", swing );

//		soundCard				= [ "Fireface 800 (393)", "Fireface 800 (EB1)" ][ config ]; // "Mobile I/O 2882 [2600]"
//		numInputBusChannels	= [ 10, 10 ][ config ];
//		numOutputBusChannels	= [ 20, 20 ][ config ];
//		masterChanMap			= [[ 0, 1, 2, 3, 4, 5, 6, 7, 12, 13, 14, 15, 16, 17, 18, 19 ],
//							   [ 3, 2, 1, 0, 7, 6, 5, 4, 12, 13, 15, 14, 17, 16, 18, 19 ]][ config ];
		subwChannel			= [ 14, 14 ][ config ];

		// MIO:				   [ 0, 1, 2, 3, 4, 5, 6, 7, 10, 11, 12, 13, 14, 15 ]
	
		clipBoard		= JClipboard( "Bosque" );
		chris		= NetAddr( "192.168.50.1", 7500 );
		martin		= NetAddr( "192.168.50.2", 7700 );

		trackDancer	= Object.new;
		trackTrack	= Object.new;
		trackField	= Object.new;
		trackBang		= Object.new;
		UpdateListener.newFor( trackBang, { ("% bang ("++thisThread.seconds++")").postln; });

		OSCresponderNode( nil, '/dancer', { arg time, resp, msg;
			var cmd, x, y, speed;
			#cmd, x, y, speed = msg;
			if( x.isFloat && y.isFloat && speed.isFloat, {
				if( (x != 0.0) or: { y != 0.0 }, {
					x		= x.clip( 0.0, 1.0 );
					y		= y.clip( 0.0, 1.0 );
					speed	= (speed * speedBoost).clip( 0.0, 1.0 );
					trackCurrentDancer = [ x, y, speed ];
					trackDancer.changed( x, y, speed );
				});
			});
		}).add;

		OSCresponderNode( nil, '/track', { arg time, resp, msg;
			var cmd, field, values;
			#cmd ... field = msg;
			field.tripletsDo({ arg x, y, speed;
				if( x.isFloat && y.isFloat && speed.isFloat, {
					values = values.add([ x.clip( 0.0, 1.0 ), y.clip( 0.0, 1.0 ), (speed * speedBoost).clip( 0.0, 1.0 )]);
				});
			});
			trackCurrentTrack = values;
			if( values.size >= 6, { trackTrack.changed( *values )});
		}).add;

		OSCresponderNode( nil, '/field', { arg time, resp, msg;
			var cmd, x, y, field, values;
			#cmd ... field = msg;
			field.pairsDo({ arg id, speed;
				if( id.isNumber and: { (id >=0) && (id < 60) }, {
					id		= id.asInteger; // "thanks!"
					x		= id % 12;
					y		= id.div( 12 );
					values	= values.add([ x, y, (speed * speedBoost).clip( 0.0, 1.0 )]);
				});
			});
			trackCurrentField = values;
//			if( values.size > 0, { trackField.changed( *values )});
//			if( values.notNil, {
				trackField.changed( *values );
//			}, {
//				trackField.changed;	// *values would produce [ nil ] ?!
//			});
		}).add;

		session = BosqueSession( this );
		session.init;
		UpdateListener.newFor( session.transport, { arg upd, transport, what; if( what === \play, {("% play ("++thisThread.seconds++")").postln; })});
		
//		swing.updateClasses( "file:///Users/rutz/Documents/workspace/TimeBased/TimeBased.jar" );
		if( timeBasedJar.notNil, { swing.addClasses( timeBasedJar )});
		cacheManager = JavaObject( "de.sciss.io.CacheManager", swing );
		cacheManager.setFolderAndCapacity( workDir ++ "cache", 300 );
		cacheManager.setActive( true );
		onSwingBoot.do({ arg x; x.value });
		onSwingBoot = nil;
		timelineEditor = BosqueTimelineEditor( this );
		scsynth.doWhenBooted({
			this.prAudioInit;
			this.prMIDIInit;
			onScSynthBoot.do({ arg x; x.value });
			onScSynthBoot = nil;
		}, inf );
				
	  }, inf ); // swing.doWhenBooted
	}
	
	*track { arg name;
		^this.default.session.tracks.detect({ arg t; t.name == name });
	}

	*mark { arg name;
		// very inefficient !!! XXX
		^this.default.session.markers.getAll.detect({ arg m; m.name == name });
	}
	
	bootScSynth {
		var o;
		
		o					= scsynth.options;
		o.device				= soundCard;
		o.numInputBusChannels	= numInputBusChannels;
		o.numOutputBusChannels	= numOutputBusChannels;
		o.numAudioBusChannels	= 512; // !!!
		
		scsynth.boot;
	}
	
	doWhenSwingBooted { arg onComplete;
		if( swing.serverRunning, onComplete, {
			onSwingBoot = onSwingBoot.add( onComplete );
		});
	}

	doWhenScSynthBooted { arg onComplete;
		if( scsynth.serverRunning, onComplete, {
			onScSynthBoot = onScSynthBoot.add( onComplete );
		});
	}
	
	prAudioInit {
		masterGroup		= Group( scsynth );
		masterBus			= Bus.audio( scsynth, masterBusNumChannels );
		masterSynth		= Synth.head( masterGroup, \bosqueMaster ++ masterBus.numChannels, [ \bus, masterBus.index, \amp, masterVolume ] );
		masterChanMap.do({ arg outputIdx, inputIdx;
			Synth.tail( masterGroup, \bosqueRoute1, [ \inBus, masterBus.index + inputIdx, \outBus, outputIdx ]);
		});
		mixGroup			= Group.before( masterGroup );
		postFilterGroup	= Group.before( mixGroup );
		panGroup			= Group.before( postFilterGroup );
		preFilterGroup	= Group.before( panGroup );
		diskGroup			= Group.before( preFilterGroup );
	}
	
	masterVolume_ { arg val;
		masterVolume = val;
		if( masterSynth.notNil, { masterSynth.set( \amp, masterVolume )});
		this.changed( \masterVolume, masterVolume );
	}
	
	micGain_ { arg val;
		micGain = val;
		this.changed( \micGain, micGain );
	}
	
	*createSynthDefs {
		(1..masterBusNumChannels).do({ arg numChannels;
			SynthDef( \bosqueDiskIn ++ numChannels, { arg out, i_bufNum, i_dur, i_fadeIn, i_fadeOut, amp = 1;
				var env;
				env = EnvGen.kr( Env.linen( i_fadeIn, i_dur - (i_fadeIn + i_fadeOut), i_fadeOut, 1.0, \lin ), doneAction: 2 ) * amp;
				Out.ar( out, DiskIn.ar( numChannels, i_bufNum ) * env );
//				OffsetOut.ar( out, DiskIn.ar( numChannels, i_bufNum ) * env );
			}).writeDefFile;
		});
		
		SynthDef( \bosqueMaster ++ masterBusNumChannels, { arg bus = 0, amp = 1, volBus;
			//            1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16
			amp = amp * [ 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0, 2.0, 1.3, 1.3, 1.3, 1.3, 1.2, 1.2 ].keep( masterBusNumChannels );
			ReplaceOut.ar( bus, Limiter.ar( In.ar( bus, masterBusNumChannels ) * amp * In.kr( volBus ), 0.99 ));
		}, [ nil, 0.1, nil ]).writeDefFile;

		SynthDef( \bosqueRoute1, { arg inBus, outBus;
			Out.ar( outBus, In.ar( inBus ));
		}).writeDefFile;

		SynthDef( \bosqueDur, { arg dur;
			Line.kr( dur: dur, doneAction: 2 );
		}).writeDefFile;
		
		SynthDef( \bosqueEnvWrite, { arg bus, start, end, dur;
			Out.kr( bus, Line.kr( start, end, dur, doneAction: 2 ));
		}).writeDefFile;

		SynthDef( \bosqueXEnvWrite, { arg bus, start, end, dur;
			Out.kr( bus, XLine.kr( start, end, dur, doneAction: 2 ));
		}).writeDefFile;
	}
	
	prMIDIInit {
		var midiOutEP, midiInEP, clpseTrigVal;
		
		MIDIClient.init;
		
		midiOutEP = block { arg break;
			MIDIClient.destinations.do({ arg out;
				if( out.device == midiOutDev, { break.value( out );});
			});
			nil;
		};
		if( midiOutEP.isNil, {
			("MIDI Output Device '"++midiOutDev++"' not found.").warn; // "Using first device".warn;
//			midiOutEP = MIDIClient.destinations.at( 0 );
		});
		if( midiOutEP.notNil, {
			midiOut = MIDIOut( 0, midiOutEP.uid );
		});
		
		midiInEP = block { arg break;
			MIDIClient.sources.do({ arg in;
				if( in.device == midiInDev, { break.value( in );});
			});
			nil;
		};
		if( midiInEP.isNil, {
			("MIDI Input Device '"++midiInDev++"' not found.").warn; // "Using first device".warn;
//			midiInEP = MIDIClient.sources.at( 0 );
		});
		if( midiInEP.notNil, {
			MIDIIn.connect( 0, midiInEP );
			midiIn = midiInEP;
		});

		CCResponder.init;
		
		clpseTrigVal = Collapse({ arg val; chris.sendMsg( '/s_tv', val.linlin( 0, 127, 0.0, 1.0 ))}, 1, AppClock );
		
		BosqueMIDIController( this );
		
if( createCC, {
		
		// master volume
		CCResponder( num: 81, function: { arg src, ch, num, val;
//			midiRec.add([ thisThread.seconds, val ]);
			this.masterVolume = val.linlin( 0, 127, -51.5, 12 ).dbamp;
		});
		
		// tracking sensitivity
		CCResponder( num: 82, function: { arg src, ch, num, val;
			clpseTrigVal.instantaneous( val );
		});
		
		// tracking data boost
		CCResponder( num: 83, function: { arg src, ch, num, val;
			this.speedBoost = val.linlin( 0, 127, 1.0, 8.0 );
			[ "boost", speedBoost ].postln;
		});
		
		// mic gain
		CCResponder( num: 84, function: { arg src, ch, num, val; var gain;
			gain = val.linlin( 0, 127, 0.0, 2.0 );
			this.micGain = gain;
//			micDorfer.set( \gain, gain );
			[ "mic", gain ].postln;
		});
		
		// transport play
		CCResponder( num: 89, value: 127, function: { arg src, ch, num, val;
			if( session.transport.isPaused.not, {
				session.transport.play;
			});
		});

		// transport stop
		CCResponder( num: 90, value: 127, function: { arg src, ch, num, val;
			session.transport.stop;
		});

		// cue resume
		CCResponder( num: 91, value: 127, function: { arg src, ch, num, val;
			trackBang.changed( \bang );
//			if( magma5.synths.size > 0, {
//				magma5.trig;
//			});
//			if( angst2.synths.size > 0, {
//				angst2.trig;
//			});
//			if( urschleim4.synths.size > 0, {
//				urschleim4.trig;
//			});
		});
		
});
		
		chris.sendMsg( '/s_tv', 1.0 );
		if( createCC && midiOut.notNil, {
			midiOut.control( 0, 81, masterVolume.linlin( -51.5, 12, 0, 127 ));
			midiOut.control( 0, 82, 127 );
			midiOut.control( 0, 83, speedBoost.linlin( 1.0, 8.0, 0, 127 ));
			midiOut.control( 0, 84, micGain.linlin( 0.0, 2.0, 0, 127 ));
		});
	}
	
	*createMouseEvent { arg view, x, y, modifiers, buttonNumber, clickCount;
		var e = Event.new, bounds = view.bounds;
		e[ \x ] = x - bounds.left;
		e[ \y ] = y - bounds.top;
		e[ \clickCount ] = clickCount;
		e[ \button ] = buttonNumber;
		e[ \component ] = view;
		e[ \modifiers ] = modifiers;
		e[ \isAltDown ] = (modifiers & 0x00080000) != 0;
		e[ \isShiftDown ] = (modifiers & 0x00020000) != 0;
		e[ \isControlDown ] = (modifiers & 0x00040000) != 0;
		e[ \isMetaDown ] = (modifiers & 0x00100000) != 0;
		^e;
	}
	
	*allGUI {
		this.dancerGUI;
		this.fieldGUI;
		this.trackGUI;
	}
	
	*dancerGUI {
		var w, c, r, v, colors;

		w = GUI.window.new( "dancer", Rect( 40, 380, 360, 150 ), resizable: false );
		w.view.background = Color.black;
		colors = Array.fill( 64, { arg i; Color.hsv( i / 96, 1, 1 )});
		v = GUI.envelopeView.new( w, w.view.bounds )
			.canFocus_( false )
			.editable_( false )
			.thumbSize_( 6 )
			.fillColor_( Color.white );
		c = Collapse({ arg msg; var cmd, x, y, value, colr;
			#cmd, x, y, value = msg;
			if( x.notNil, {  // cz ...................
				v.value_([[ x ], [ 1 - y ]]);
				colr = colors[ ((1 - value.clip(0,1)) * colors.size).asInteger ] ? Color.black;
				v.setFillColor( 0, colr );
			});
		} , 0.05, AppClock );
		r = OSCresponderNode( nil, '/dancer', { arg time, resp, msg;
			c.instantaneous( msg );
		}).add;
		w.onClose = { c.cancel; r.remove };
		w.front;
	}

	*fieldGUI {
		var w, c, r, v, colors;

		w = GUI.window.new( "fields", Rect( 40, 210, 360, 150 ), resizable: false );
		w.view.background = Color.black;
		colors = Array.fill( 64, { arg i; Color.hsv( i / 96, 1, 1 )});
		v = GUI.envelopeView.new( w, w.view.bounds )
			.canFocus_( false )
			.editable_( false )
			.thumbSize_( 30 );
		c = Collapse({ arg msg; var x, y, c; // , num = 0;
			x = Array( 6 );
			y = Array( 6 );
			c = Array( 6 );
			msg.drop(1).pairsDo({ arg field, value, idx;
				if( value > 0, {
					x.add( (field % 12) / 11 );
					y.add( 1 - (field.div( 12 ) / 4) );
	//				num = num + 1;
					c.add( colors[ ((1 - value.clip(0,1)) * colors.size).asInteger ] ? Color.black );
				});
			});
	//		num.postln;
			v.value_([ x, y ]);
			c.do({ arg col, i; v.setFillColor( i, col )});
		} , 0.05, AppClock );
		r = OSCresponderNode( nil, '/field', { arg time, resp, msg;
			c.instantaneous( msg );
		}).add;
		w.onClose = { c.cancel; r.remove };
		w.front;
	}
	
	*trackGUI {
		var w, c, r, v, colors;

		w = GUI.window.new( "track", Rect( 40, 40, 360,150 ), resizable: false );
		w.view.background = Color.black;
		colors = Array.fill( 64, { arg i; Color.hsv( i / 96, 1, 1 )});
		v = GUI.envelopeView.new( w, w.view.bounds )
			.canFocus_( false )
			.editable_( false )
			.thumbSize_( 6 )
			.fillColor_( Color.white );
		c = Collapse({ arg msg; var x, y, c; // , num = 0;
			x = Array( 6 );
			y = Array( 6 );
			c = Array( 6 );
			msg.drop(1).tripletsDo({ arg xval, yval, value, idx;
				if( (xval > 0) or: { yval > 0 }, {
//					x.add( (field % 12) / 11 );
//					y.add( 1 - (field.div( 12 ) / 4) );
					x.add( xval );
					y.add( 1 - yval );
	//				num = num + 1;
					c.add( colors[ ((1 - value.clip(0,1)) * colors.size).asInteger ]);
				});
			});
	//		num.postln;
			v.value_([ x, y ]);
			c.do({ arg col, i; v.setFillColor( i, col )});
		} , 0.05, AppClock );
		r = OSCresponderNode( nil, '/track', { arg time, resp, msg;
			c.instantaneous( msg );
		}).add;
		w.onClose = { c.cancel; r.remove };
		w.front;
	}

	*launch {
		var bosque;
		
		this.prAddSwingClasses;
		
		bosque = this.new;
//		timelineEditor = BosqueTimelineEditor( this );
		bosque.bootScSynth;
//		bosque.scsynth.makeWindow;
//		this.allGUI;
	}

	*writePrefs {
		var f, dict;
		
		dict = IdentityDictionary.new;
		dict.put( \config, config );
		try {
			f = File( workDir ++ "prefs.txt", "w" );
			dict.storeOn( f );
			f.close;
		} {
			arg error; error.postln;
		};
	}

	*readPrefs {
		var f, dict;
		
		try {
			f = File( workDir ++ "prefs.txt", "r" );
			dict = f.readAllString.interpret;
			f.close;
			config = dict[ \config ];
		} {
			arg error; error.reportError;
		};
	}

	*guiLaunch {
		var win, flow, ggLaunch, ggCountDown, ggStopCount, rCount;
		
		this.readPrefs;
		
		win = GUI.window.new( "Phonolithikum", Rect( 0, 0, 400, 100 ), resizable: false );
		ScissUtil.positionOnScreen( win );
		flow	= FlowLayout( win.view.bounds );
		win.view.decorator = flow;
		
		ggLaunch = GUI.button.new( win, Rect( 0, 0, 80, 30 ));
		ggLaunch.states = [[ "Launch" ]];
		ggLaunch.action = {
			this.launch;
			win.close;
		};
		
		ggCountDown = GUI.staticText.new( win, Rect( 0, 0, 40, 30 ));
		
		ggStopCount = GUI.button.new( win, Rect( 0, 0, 120, 30 ));
		ggStopCount.states = [[ "Stop Countdown" ]];
		ggStopCount.action = {
			rCount.stop;
			win.close;
		};
		
		win.onClose	= {
			rCount.stop;
		};
		
		rCount = Routine.run({
			10.do({ arg i;
				ggCountDown.string = (10 - i).asString;
				1.0.wait;
			});
//			ggLaunch.valueAction_( 0 );
			ggLaunch.doAction;
		}, clock: AppClock );

		flow.nextLine;
		flow.shift( 0, 12 );
		
		GUI.staticText.new( win, Rect( 0, 0, 40, 30 )).string_( "Config:" );
		GUI.button.new( win, Rect( 0, 0, 120, 30 ))
			.states_([[ "Q3" ], [ "Viehhalle" ]])
			.value_( config )
			.action_({ arg b;
				config = b.value;
				this.writePrefs;
			});

		// hmmmm, needs to be called twice because of
		// some bug. so once here, then again in the launch ....
		this.prAddSwingClasses;
		
		win.front;
	}
	
	*recorderGUI {
		var rec, f;
		f = Bosque.default;
		rec = SimpleRecorder.new;
		rec.numChannels 	= min( 10, f.masterBus.numChannels );
		rec.channelOffset	= f.masterBus.index;
		rec.folder		= Bosque.workDir ++ "rec/";
		rec.makeWindow;
	}

	*prAddSwingClasses {
		SwingOSC.default.addClasses( "file:///Users/rutz/Documents/workspace/TimeBased/TimeBased.jar" );
	}
}