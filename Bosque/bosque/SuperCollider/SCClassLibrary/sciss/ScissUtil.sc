/**
 *	(C)opyright 2006-2008 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: TypeSafe
 *
 *	@version	0.20, 29-Jul-08
 *	@author	Hanns Holger Rutz
 */
ScissUtil
{
	classvar <>runPath;

	// String:toUpper is missing!!
	*stringToUpper { arg str;
		^str.collect(_.toUpper);
	}

	*speakerTest { arg numChannels = 8, channelOffset = 0, volume = 0.1;
		var s, w, b, t, x, d;
	
		s = Server.local;
		w = SCWindow( "Pink Noise", Rect( 40, 200, 250, 64 ));
		w.view.decorator = FlowLayout( w.view.bounds );
		b = SCButton( w, Rect( 0, 0, 100, 30 ));
		b.states = [["Play", Color.black, Color.white ], ["Stop", Color.white, Color.red]];
		d = SCStaticText( w, Rect( 0, 0, 100, 30 ));
		t = Task({
			numChannels.do({ arg i;
				var ch = i + channelOffset;
				{ d.string = "Channel " ++ (ch + 1); }.defer;
				x = Synth.new( \speakerTest, [ \ch, ch, \volume, volume ], s.asTarget, \addToTail );
				0.5.wait;
				x.free;
				0.2.wait;
			});
			{ b.value = 0; d.string = ""; }.defer;
		});
		b.action = {
			if( b.value == 1, {
				t.reset;
				t.play;
			}, {
				t.stop;
			})
		};
		s.waitForBoot({
			SynthDef( \speakerTest, { arg ch, vol = 0.1;
				Out.ar( ch, PinkNoise.ar );
			}).send( s );
		});
		w.front;
	}

	*renderSpeech { arg text, path, rate, pitchShift, volume = 0.975;
		var bndl;
		
		text		= (text ?? "Test").asString;
		path		= path ?? { this.createUniquePathName( "~/Desktop/Speech".absolutePath, ".aif" ); };
		
		TypeSafe.checkArgClasses( thisMethod,
			[ text,   path,   rate,   pitchShift, volume ],
			[ String, String, Number, Number,     Number ],
			[ false,  false,  true,   true,       false  ]);
			
//		if( style.notNil, {
//			if([ \business, \casual, \robotic, \breathy ].includes( style ).not, {
//				("Unknown style '" ++ style ++ "'").warn;
//			});
//		});
		
		if( path.endsWith( ".aif" ), {
			path = path.copyRange( 0, path.size - 5 );
		});
		
		bndl = List.new;	
		bndl.add([ "/local", \ttsVM, [ "/method", "com.sun.speech.freetts.VoiceManager", \getInstance ],
				\ttsVoice, [ "/method", \ttsVM, \getVoice, "kevin16" ]]);
		bndl.add([ "/free", \ttsVM ]);
		bndl.add([ "/local", \ttsFile, [ "/new", "com.sun.speech.freetts.audio.SingleFileAudioPlayer", path,
							   [ "/field", "javax.sound.sampled.AudioFileFormat$Type", \AIFF ]]]);
		bndl.add([ "/set", \ttsVoice, \volume, volume, \audioPlayer, [ "/ref", \ttsFile ]]);
		if( rate.notNil, {
			bndl.add([ "/set", \ttsVoice, \rate, rate ]);
		});
		if( pitchShift.notNil, {
			bndl.add([ "/set", \ttsVoice, \pitchShift, pitchShift ]);
		});
		if( volume.notNil, {
		});
		if( rate.notNil, {
			bndl.add([ "/set", \ttsVoice, \rate, rate ]);
		});
// setStyle doesn't seem to have any effect (FreeTTS 1.2)
//		if( style.notNil, {
//			bndl.add([ "/set", \ttsVoice, \style, style ]);
//		});
		bndl.add([ "/method", \ttsVoice, \allocate ]);
		bndl.add([ "/method", \ttsVoice, \speak, text ]);
		bndl.add([ "/method", \ttsFile, \close ]);
		bndl.add([ "/method", \ttsVoice, \deallocate ]);
		bndl.add([ "/free", \ttsVoice, \ttsFile ]);

		("Rendering speech to '" ++ path ++ ".aif'").inform;

		SwingOSC.default.listSendBundle( nil, bndl );
	}
	
	*hhmmss { arg date;
		var s;
		date	= date ?? { Date.localtime };
		s	= date.secStamp;
		^(s.copyRange( 0, 1 ) ++ ":" ++ s.copyRange( 2, 3 ) ++ ":" ++ s.copyRange( 4, 5 ));
	}
	
	*toTimeString { arg seconds;
		var time, str, millis, secs, mins, hours;
		time		= (seconds.abs * 1000).asInteger;
		millis	= time % 1000;
		str		= (millis + 1000).asString.copyToEnd( 1 );
		time		= time.div( 1000 );
		secs		= time % 60;
		str		= (secs + 100).asString.copyToEnd( 1 ) ++ "." ++ str;
		time		= time.div( 60 );
		mins		= time % 60;
		str		= (mins + 100).asString.copyToEnd( 1 ) ++ ":" ++ str;
		time		= time.div( 60 );
		hours	= time;
		str		= (hours + 100).asString.copyToEnd( 1 ) ++ ":" ++ str;
		^if( seconds >= 0, str, { "-" ++ str });
	}
	
	*fromTimeString { arg str;
		var seconds, idx;
		str		= str.split( $: );
		idx		= str.size - 1;
		seconds	= (str[ idx ] ? 0).asFloat * if( str[0][0] == $-, -1, 1 );
		idx		= idx - 1;
		if( idx < 0, { ^seconds });
		seconds	= seconds + (str[ idx ].asInteger * 60); // minutes
		idx		= idx - 1;
		if( idx < 0, { ^seconds });
		seconds	= seconds + (str[ idx ].asInteger * 3600); // hours
		^seconds;
	}

	*createUniquePathName { arg prefix, suffix;
		var testName;
		
		prefix = prefix ?? "";
		suffix = suffix ?? "";
	
		inf.do({ arg idx;
			testName = prefix ++ (idx + 1) ++ suffix;
			if( File.exists( testName ).not, {
//				("'"++testName++"' doesn't exist").postln;
				^testName;
			});
//			("'"++testName++"' exists").postln;
		});
	}

	*trackIncomingOSC { arg printTime = true;
		var funcOSC;
//		~trackIncomingOSC = Updater( Main, { arg obj, what, time, replyAddr, msg;
//			if( what === \osc and: { msg.first !== 'status.reply' }, {
//				("r: " ++ msg).postln;
//			});
//		});
		funcOSC = { arg time, replyAddr, msg;
			if( msg.first !== 'status.reply', {
				("r: " ++ if( printTime, { time.round( 0.0001 ).asString ++ " " }, "" ) ++ msg).postln;
			});
		};
		if( ~trackIncomingOSC.isKindOf( Event ), {
			~trackIncomingOSC.stop;
		});
		~trackIncomingOSC = Event.new;
		~trackIncomingOSC.put( \stop, { thisProcess.recvOSCfunc = thisProcess.recvOSCfunc.removeFunc( funcOSC )});
		thisProcess.recvOSCfunc = thisProcess.recvOSCfunc.addFunc( funcOSC );
		"ScissUtil.trackIncomingOSC : to stop, execute ~trackIncomingOSC.stop !".postln;
	}
	
	*positionOnScreen { arg win, x = 0.5, y = 0.5;
		var wb, sb;
		wb 	= win.bounds;
		sb	= win.class.screenBounds;
		
		win.bounds_( Rect( ((sb.width - wb.width) * x).asInt, ((sb.height - wb.height) * (1 - y)).asInt,
				    wb.width, wb.height ));
	}
	
	*readMarkersFromAIFF { arg path;
		var f, len, magic, numMarkers, markPos, markName, markers, done, chunkLen;
		
		f = File( path, "rb" );
		protect {
			magic = f.getInt32;
			if( magic != 0x464F524D, { Error( "File does not begin with FORM magic" ).throw });
			// trust the file length more than 32 bit form field which breaks for > 2 GB (> 1 GB if using signed ints)
			f.getInt32;
			magic 	= f.getInt32;
			len		= f.length - 12;
			if( (magic != 0x41494646) && (magic != 0x41494643), {
				Error( "Format is not AIFF or AIFC" ).throw;
			});
			
			done 	= false;
			chunkLen	= 0;
			while({ done.not && (len > 0) }, {
				if( chunkLen != 0, { f.seek( chunkLen, 1 )});   // skip to next chunk
				
				magic	= f.getInt32;
				chunkLen	= (f.getInt32 + 1) & 0xFFFFFFFE;
				len		= len - (chunkLen + 8);
				switch( magic,
				0x4D41524B, {	// 'MARK' 
					numMarkers	= f.getInt16;
					// getInt16 is signed, so for 32767 < numMarkers < 65536 we need to fix it
					if( numMarkers < 0, { numMarkers = numMarkers + 0x10000 });
					markers 		= Array.new( numMarkers );
					numMarkers.do({
						f.getInt16;			  // marker ID (ignore)
						markPos	= f.getInt32;	  // sample frames
						markName	= f.getPascalString;
						if( markName.size.even, { f.seek( 1, 1 )}); // next marker chunk on even offset
						// ignore padding space created by Peak
						if( markName.last == $\ , { markName = markName.copyFromStart( markName.size - 1 )});
//						markers.add( MarkerStake( markPos, markName ));
						markers.add( Event[ \name -> markName, \pos -> markPos ]);
					});
					done 		= true;
				});
			});
		} { arg error;
			try { f.close };
		};
		^markers;
	}
	
	*forkIfNeeded { arg func ... args;
		if( thisThread.isKindOf( Routine ), {
			func.value;
			^nil;
		}, {
			^func.fork( *args );
		});
	}
}