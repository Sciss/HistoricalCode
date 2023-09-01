/*
 *	Bosque
 *	(Bosque)
 *
 *	Copyright (c) 2007-2009 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation; either
 *	version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	This software is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public
 *	License (gpl.txt) along with this software; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 *
 *
 *	Changelog:
 */

/**
 *	@author	Hanns Holger Rutz
 *	@version	0.31, 28-Jun-09
 */
Bosque : Object {
	classvar <>default;
	classvar <version	= 0.29;

	// ------------- configure these -------------

	classvar <>soundCard;
	classvar <>numInputBusChannels;
	classvar <>numOutputBusChannels;
	classvar <>workDir;
	classvar <>masterBusNumChannels	= 8;
	classvar <>masterChanMap;	// an array of channel indices; array size must equal masterBusNumChannels!
	classvar <>timeBasedJar;

	classvar	<>midiInDev			= "BCF2000"; // "BCR2000"; // : Port 1";
	classvar	<>midiOutDev			= "BCF2000"; // "BCR2000"; // : Port 1";
	classvar <>useMIDI				= true;

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
	
	var <midiOut;	// a MIDIOut
	var <midiIn;	// a MIDIEndPoint

	var <master;
//	var <app;
	var <timelineEditor;
	
	var didInitAudio = false;

	// ---------- remainders from Forest project ----------

	classvar <numFieldsH			= 12;
	classvar <numFieldsV			= 5;
	classvar <numFields			= 60;	// # of neon bulbs
	classvar <numTracks			= 6;
	classvar <tedMicChannel			= 7;

	classvar <>createCC			= false;

	var <chris;
	var <martin;
	
	// event objects
	var <trackDancer, <trackTrack, <trackField, <trackBang;
	var <trackCurrentDancer, <trackCurrentTrack, <trackCurrentField;

	var <>speedBoost	= 1.0;
	var <micGain		= 1.0;
		
	*initClass {
//		Class.initClassTree( SwingOSC );
//		default = this.new;
		workDir		= "~/Bosque/".absolutePath;
		timeBasedJar	= "file:" ++ thisProcess.platform.userExtensionDir +/+ "TimeBased.jar";
	}
	
	*new {
		^super.new.prInit;
	}
	
	prInit {
		if( default.isNil, { default = this });

		swing		= SwingOSC.default;
		scsynth		= Server.default; // Server( \bosque, VerboseNetAddr( "127.0.0.1", 57001 )); // Server.default;
	  swing.waitForBoot({

// THERE IS A CRAZY BUG WITH de.sciss.gui.NumberField not firing any property changes for "value" any more
// after launching bosque. If we create a numberfield in advance, the bug doesn't show up. Maybe
// a classloader confusion that we cannot see yet...???
{
  var w, b;
    
    w = JSCWindow( "JSCNumberBox" );
    b = JSCNumberBox( w, Rect( 10, 10, 100, 30 ));
//    b.value = rrand( 1, 15 );
//    b.action = { arg numb; numb.value.postln };
//  w.front;
}.value;

		if( timeBasedJar.notNil, {
			swing.addClasses( timeBasedJar );
		});

		swing.protEnsureApplication;
		master			= JavaObject( "de.sciss.timebased.net.Master", swing );
//		app				= JavaObject( "de.sciss.timebased.Main", swing );

//		soundCard				= [ "Fireface 800 (393)", "Fireface 800 (EB1)" ][ config ]; // "Mobile I/O 2882 [2600]"
//		numInputBusChannels	= [ 10, 10 ][ config ];
//		numOutputBusChannels	= [ 20, 20 ][ config ];
//		masterChanMap			= [[ 0, 1, 2, 3, 4, 5, 6, 7, 12, 13, 14, 15, 16, 17, 18, 19 ],
//							   [ 3, 2, 1, 0, 7, 6, 5, 4, 12, 13, 15, 14, 17, 16, 18, 19 ]][ config ];
//		subwChannel			= [ 14, 14 ][ config ];

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
					trackDancer.tryChanged( x, y, speed );
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
			if( values.size >= 6, { trackTrack.tryChanged( *values )});
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
//			if( values.size > 0, { trackField.tryChanged( *values )});
//			if( values.notNil, {
				trackField.tryChanged( *values );
//			}, {
//				trackField.tryChanged;	// *values would produce [ nil ] ?!
//			});
		}).add;

		session = BosqueSession( this );
		session.init;
		UpdateListener.newFor( session.transport, { arg upd, transport, what; if( what === \play, {("% play ("++thisThread.seconds++")").postln; })});
		
//		swing.updateClasses( "file:///Users/rutz/Documents/workspace/TimeBased/TimeBased.jar" );
		cacheManager = JavaObject( "de.sciss.io.CacheManager", swing );
		cacheManager.setFolderAndCapacity( workDir ++ "cache", 300 );
		cacheManager.setActive( true );
		onSwingBoot.do({ arg x; x.value });
		onSwingBoot = nil;
		timelineEditor = BosqueTimelineEditor( this );
		timelineEditor.init;
		scsynth.doWhenBooted({
			{
				this.prAudioInit;
				didInitAudio = true;
				if( useMIDI, { this.prMIDIInit });
				onScSynthBoot.do({ arg x; x.value });
				onScSynthBoot = nil;
				this.tryChanged( \booted );
			}.fork( AppClock );
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
	
	*followCursor { arg func, rate = 30, offline = true;
		var updTransp, updCsr, map, doc, rout, period;
		period	= rate.reciprocal;
		doc		= this.default.session;
		map		= AnyMap.new;
		updTransp	= UpdateListener.newFor( doc.transport, { arg upd, t, what, param;
			if( (what === \play) or: { what === \resume }, {
				rout = fork { inf.do({
					func.value( t.currentFrame, true );
					period.wait;
				})};
			}, { if( (what === \stop) or: { what === \pause }, {
				rout.stop; rout = nil;
			})});
		});
		if( offline, {
			updCsr = UpdateListener.newFor( doc.timelineView.cursor, { arg upd, csr, pos;
				func.value( pos, false );
			}, \changed );
		});
		map.remove = { arg m;
			rout.stop; rout = nil;
			if( updTransp.notNil, { updTransp.remove; updTransp = nil });
			if( updCsr.notNil, { updCsr.remove; updCsr = nil });
			m;
		};
		^map;
	}
	
	/**
	 *	@param	pos	either a position (frame) or a MarkerStake as returned
	 *				by the mark method, or a Symbol that is a marker's name
	 */ 
	*jump { arg pos;
		if( pos.isKindOf( Symbol ), {
			pos = this.mark( pos ).pos;
		}, { if( pos.isKindOf( MarkerStake ), {
			pos = pos.pos;
		})});
		this.default.session.editPosition( this, pos );
	}
	
	bootScSynth {
		var o;
		
		o = scsynth.options;
		if( soundCard.notNil, { o.device = soundCard });
		if( numInputBusChannels.notNil, { o.numInputBusChannels = numInputBusChannels });
		if( numOutputBusChannels.notNil, { o.numOutputBusChannels = numOutputBusChannels });
		o.numAudioBusChannels	= o.numAudioBusChannels.max( 512 ); // !!!
		
		scsynth.boot;
	}
	
	doWhenSwingBooted { arg onComplete;
		if( swing.serverRunning, onComplete, {
			onSwingBoot = onSwingBoot.add( onComplete );
		});
	}

	doWhenScSynthBooted { arg onComplete;
		if( scsynth.serverRunning && didInitAudio, onComplete, {
			onScSynthBoot = onScSynthBoot.add( onComplete );
		});
	}
	
	prAudioInit {
		this.prSendSynthDefs;
		scsynth.sync;
		
		masterGroup		= Group( scsynth );
		masterBus			= Bus.audio( scsynth, masterBusNumChannels );
		masterChanMap		= masterChanMap ?? { (0..(masterBusNumChannels-1)) };
		if( masterChanMap.size != masterBusNumChannels, {
			("Bosque : channel mismatch! masterBusNumChannels is " ++ masterBusNumChannels ++
				" but masterChanMap.size is " ++ masterChanMap.size).warn;
		});
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
		this.tryChanged( \masterVolume, masterVolume );
	}
	
	micGain_ { arg val;
		micGain = val;
		this.tryChanged( \micGain, micGain );
	}
	
	*createSynthDefs { arg debug = false;
		"Meta_Bosque:createSynthDefs - this method is not used any more!".warn;
	}
	
	prSendSynthDefs {
		var mbc = masterBusNumChannels;
		if( mbc.isNil, {
			"Meta_Bosque:createSynthDefs - masterBusNumChannels is nil".warn;
			mbc = 8;
		});
	
		(1..mbc).do({ arg numChannels;
			SynthDef( \bosqueDiskIn ++ numChannels, { arg out, i_bufNum, i_dur, i_fadeIn, i_fadeOut, amp = 1,
			                                              i_finTyp = 1, i_foutTyp = 1;
				var env, envGen;
//				env = Env.linen( i_fadeIn, i_dur - (i_fadeIn + i_fadeOut), i_fadeOut, 1.0, \lin );
				env = Env([ 0, 1, 1, 0 ],
				          [ i_fadeIn, i_dur - (i_fadeIn + i_fadeOut), i_fadeOut ],
				          [ i_finTyp, 1, i_foutTyp ]);
				envGen = EnvGen.kr( env, doneAction: 2 ) * amp;
				Out.ar( out, DiskIn.ar( numChannels, i_bufNum ) * envGen );
//				OffsetOut.ar( out, DiskIn.ar( numChannels, i_bufNum ) * env );
			}).send( scsynth );
		});
		
		SynthDef( \bosqueMaster ++ mbc, { arg bus = 0, amp = 1, volBus;
			//            1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16
//			amp = amp * [ 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0, 2.0, 1.3, 1.3, 1.3, 1.3, 1.2, 1.2 ].keep( mbc );
			ReplaceOut.ar( bus, Limiter.ar( In.ar( bus, mbc ) * amp * In.kr( volBus ), 0.99 ));
		}, [ nil, 0.1, nil ]).send( scsynth );

		SynthDef( \bosqueRoute1, { arg inBus, outBus;
			Out.ar( outBus, In.ar( inBus ));
		}).send( scsynth );

		SynthDef( \bosqueDur, { arg dur;
			Line.kr( dur: dur, doneAction: 2 );
		}).send( scsynth );
		
		SynthDef( \bosqueEnvWrite, { arg bus, start, end, dur;
			Out.kr( bus, Line.kr( start, end, dur, doneAction: 2 ));
		}).send( scsynth );

		SynthDef( \bosqueXEnvWrite, { arg bus, start, end, dur;
			Out.kr( bus, XLine.kr( start, end, dur, doneAction: 2 ));
		}).send( scsynth );

		[ \DbFaderWarpP, \DbFaderWarpN, \ExponentialWarp, \LinearWarp, \FaderWarpP, \FaderWarpN, \CurveWarp, \SineWarp, \CosineWarp ].do({ arg warp;
		  SynthDef( "bosqueEnv" ++ warp, { arg i_bufNum, out, i_off = 0, i_atk = 0.0, i_dur,
			specMin = 0.0, specMax = 1.0, specCurve = 0.0;
			var segmFrames, val, inp, line, warped, specGrow, specA, specB, sig;
			
			Line.kr( dur: i_dur, doneAction: 2 );
			val		= Dbufrd( i_bufNum, Dseries( i_off, 2, inf ));
			segmFrames = Dbufrd( i_bufNum, Dseries( i_off + 1, 2, inf ));
	//		val	 	= DemandEnvGen.ar( val, dur );
			val	 	= DemandEnvGen.kr( val, segmFrames, timeScale: SampleDur.ir );
			
			warped	= switch( warp,
			\DbFaderWarpP, {
//				(val.squared * (specMax - specMin) + specMin.dbamp).ampdb;
//				(val * val * (specMax.dbamp - specMin.dbamp) + (pow( 10.0, specMin * 0.05))).ampdb;
				(val * val * (specMax.dbamp - specMin.dbamp) + specMin.dbamp).ampdb;
			},
			\DbFaderWarpN, {
				((1 - (1 - val).squared) * (specMax.dbamp - specMin.dbamp) + specMin.dbamp).ampdb;
			},
			\ExponentialWarp, {
				((specMax / specMin) ** val) * specMin;
			},
			\LinearWarp, {
				val * (specMax - specMin) + specMin;
			},
			\FaderWarpP, {
				val.squared * (specMax - specMin) + specMin;
			},
			\FaderWarpN, {
				(1 - (1 - val).squared) * (specMax - specMin) + specMin;
			},
			\CurveWarp, {
				specGrow	= exp( specCurve );
				specA	= (specMax - specMin) / (1.0 - specGrow);
				specB	= specMin + specA;
				specB - (specA * pow( specGrow, val ));
			},
			\SineWarp, {
				sin( 0.5pi * val ) * (specMax - specMin) + specMin;
			},
			\CosineWarp, {
				(0.5 - (cos( pi * val ) * 0.5)) * (specMax - specMin) + specMin;
			}, {
				Assertion( false, warp.asString );
			});

			inp		= In.kr( out );
			line		= Line.kr( 0, 1, i_atk );
			sig		= (warped * line) + (inp * (1 - line));

//if( debug, {
//	warped.poll( 10, "warped" );
//	sig.poll( 10, "sig" );
//	line.poll( 10, "line" );
//	inp.poll( 10, "inp" );
//});

			Out.kr( out, sig );
	//		XOut.kr( i_outbus, Line.kr( 0, 1, 0.1 ), val );
		  }).send( scsynth );
		});
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
			trackBang.tryChanged( \bang );
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
		var e = AnyMap.new /*, bounds = view.bounds */;
		e[ \x ] = x; // - bounds.left;
		e[ \y ] = y; // - bounds.top;
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
	
//	*allGUI {
//		this.dancerGUI;
//		this.fieldGUI;
//		this.trackGUI;
//	}
//	
//	*dancerGUI {
//		var w, c, r, v, colors;
//
//		w = GUI.window.new( "dancer", Rect( 40, 380, 360, 150 ), resizable: false );
//		w.view.background = Color.black;
//		colors = Array.fill( 64, { arg i; Color.hsv( i / 96, 1, 1 )});
//		v = GUI.envelopeView.new( w, w.view.bounds )
//			.canFocus_( false )
//			.editable_( false )
//			.thumbSize_( 6 )
//			.fillColor_( Color.white );
//		c = Collapse({ arg msg; var cmd, x, y, value, colr;
//			#cmd, x, y, value = msg;
//			if( x.notNil, {  // cz ...................
//				v.value_([[ x ], [ 1 - y ]]);
//				colr = colors[ ((1 - value.clip(0,1)) * colors.size).asInteger ] ? Color.black;
//				v.setFillColor( 0, colr );
//			});
//		} , 0.05, AppClock );
//		r = OSCresponderNode( nil, '/dancer', { arg time, resp, msg;
//			c.instantaneous( msg );
//		}).add;
//		w.onClose = { c.cancel; r.remove };
//		w.front;
//	}
//
//	*fieldGUI {
//		var w, c, r, v, colors;
//
//		w = GUI.window.new( "fields", Rect( 40, 210, 360, 150 ), resizable: false );
//		w.view.background = Color.black;
//		colors = Array.fill( 64, { arg i; Color.hsv( i / 96, 1, 1 )});
//		v = GUI.envelopeView.new( w, w.view.bounds )
//			.canFocus_( false )
//			.editable_( false )
//			.thumbSize_( 30 );
//		c = Collapse({ arg msg; var x, y, c; // , num = 0;
//			x = Array( 6 );
//			y = Array( 6 );
//			c = Array( 6 );
//			msg.drop(1).pairsDo({ arg field, value, idx;
//				if( value > 0, {
//					x.add( (field % 12) / 11 );
//					y.add( 1 - (field.div( 12 ) / 4) );
//	//				num = num + 1;
//					c.add( colors[ ((1 - value.clip(0,1)) * colors.size).asInteger ] ? Color.black );
//				});
//			});
//	//		num.postln;
//			v.value_([ x, y ]);
//			c.do({ arg col, i; v.setFillColor( i, col )});
//		} , 0.05, AppClock );
//		r = OSCresponderNode( nil, '/field', { arg time, resp, msg;
//			c.instantaneous( msg );
//		}).add;
//		w.onClose = { c.cancel; r.remove };
//		w.front;
//	}
//	
//	*trackGUI {
//		var w, c, r, v, colors;
//
//		w = GUI.window.new( "track", Rect( 40, 40, 360,150 ), resizable: false );
//		w.view.background = Color.black;
//		colors = Array.fill( 64, { arg i; Color.hsv( i / 96, 1, 1 )});
//		v = GUI.envelopeView.new( w, w.view.bounds )
//			.canFocus_( false )
//			.editable_( false )
//			.thumbSize_( 6 )
//			.fillColor_( Color.white );
//		c = Collapse({ arg msg; var x, y, c; // , num = 0;
//			x = Array( 6 );
//			y = Array( 6 );
//			c = Array( 6 );
//			msg.drop(1).tripletsDo({ arg xval, yval, value, idx;
//				if( (xval > 0) or: { yval > 0 }, {
////					x.add( (field % 12) / 11 );
////					y.add( 1 - (field.div( 12 ) / 4) );
//					x.add( xval );
//					y.add( 1 - yval );
//	//				num = num + 1;
//					c.add( colors[ ((1 - value.clip(0,1)) * colors.size).asInteger ]);
//				});
//			});
//	//		num.postln;
//			v.value_([ x, y ]);
//			c.do({ arg col, i; v.setFillColor( i, col )});
//		} , 0.05, AppClock );
//		r = OSCresponderNode( nil, '/track', { arg time, resp, msg;
//			c.instantaneous( msg );
//		}).add;
//		w.onClose = { c.cancel; r.remove };
//		w.front;
//	}

	*launch {
		var bosque;
		
		bosque = this.new;
//		timelineEditor = BosqueTimelineEditor( this );
		if( Bosque.soundCard.notNil, { bosque.bootScSynth });
//		bosque.scsynth.makeWindow;
//		this.allGUI;
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
}