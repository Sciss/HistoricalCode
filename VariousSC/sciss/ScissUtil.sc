/**
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: TypeSafe
 *
 *	@version	0.26, 15-Aug-09
 *	@author	Hanns Holger Rutz
 */
ScissUtil
{
//	classvar <>runPath;

	// String:toUpper is missing!!
	*stringToUpper { arg str;
		^str.collect(_.toUpper);
	}

	*stringToLower { arg str;
		^str.collect(_.toLower);
	}

	*speakerTest { arg numChannels = 8, channelOffset = 0, volume = 0.1, channels, loop = false;
		var s, w, ggPlay, ggLoop, lbChan, fTask, fStop, synth, rout;
	
		s = Server.default;
		if( channels.isNil, {
			channels = Array.series( numChannels, channelOffset );
		});
		
		fStop = {
			rout.stop; rout = nil;
			synth.free; synth = nil;
		};
		w = Window( "Pink Noise", Rect( 40, 200, 250, 64 ));
		w.view.decorator = FlowLayout( w.view.bounds );
		ggPlay = Button( w, Rect( 0, 0, 100, 30 ))
			.states_([[ "Play", Color.black, Color.white ], [ "Stop", Color.white, Color.red ]])
			.action_({ arg view;
				if( view.value == 1, {
					rout = fTask.fork;
				}, fStop );
			});
		ggLoop = Button( w, Rect( 0, 0, 100, 30 ))
			.states_([[ "Loop" ], [ "Loop", Color.white, Color( 1.0, 0.5, 0.0 )]])
			.value_( loop.binaryValue )
			.action_({ arg view; loop = view.value == 1 });
		lbChan = StaticText( w, Rect( 0, 0, 100, 30 ));
		fTask = {
			doWhile {
				channels.do({ arg ch, i;
					{ lbChan.string = "Channel %".format( ch + 1 )}.defer;
					synth = Synth.new( \speakerTest, [ \ch, ch, \vol, volume ], s.asTarget, \addToTail );
					0.5.wait;
					synth.free;
					0.2.wait;
				});
			} {
				loop;
			};
			{ ggPlay.value = 0; lbChan.string = "" }.defer;
		};
		s.waitForBoot({
			SynthDef( \speakerTest, { arg ch, vol = 0.1;
				Out.ar( ch, PinkNoise.ar( vol ));
			}).send( s );
		});
		w.onClose = fStop;
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
		
	
	*fixEmptyAIFF { arg path;
		var f, totalLen, len, magic, isAIFC, chunkLen, numChannels, bitsPerSample, numEssentials,
		    formLengthOffset, commSmpNumOffset, ssndLengthOffset, bytesPerFrame, numFrames;
		
		f = File( path, "rb+" );
		if( f.isOpen.not, {
			Error( "Could not open file '" ++ path ++ "'" ).throw;
		});
	
		protect {
			totalLen = f.length;
			magic = f.getInt32;
			if( magic != 0x464F524D, { Error( "File does not begin with FORM magic (" ++ magic ++ ")" ).throw });
			formLengthOffset = f.pos;
			f.getInt32;
			len		= totalLen - 12;  // use the file length instead of the chunk len
			magic 	= f.getInt32;
			isAIFC   = magic == 0x41494643;
			if( (magic != 0x41494646) && isAIFC.not, {
				Error( "Format is not AIFF or AIFC" ).throw;
			});
					
			chunkLen	= 0;
			numEssentials = 2;
			while({ (numEssentials > 0) && (len > 0) }, {
				if( chunkLen != 0, { f.seek( chunkLen, 1 )});   // skip to next chunk
				
				magic	= f.getInt32;
				chunkLen	= (f.getInt32 + 1) & 0xFFFFFFFE;
				len		= len - (chunkLen + 8);
	//			[ "magic", magic ].postln;
				switch( magic,
				0x434F4D4D, { // 'COMM'
					numChannels		= f.getInt16;
					commSmpNumOffset	= f.pos;	// offset for # of frames
					f.getInt32; // ignore # of frames
					bitsPerSample		= f.getInt16;
					f.getInt32; f.getInt32; f.getInt16; // skip sample rate
					chunkLen = chunkLen - 18;
					
					if( isAIFC, {
						switch( f.getInt32,
						0x4E4F4E45, { }, // NONE_MAGIC
						0x696E3136, { bitsPerSample = 16 }, // in16_MAGIC
						0x696E3234, { bitsPerSample = 24 }, // in24_MAGIC
						0x696E3332, { bitsPerSample = 32 }, // in32_MAGIC
						0x666C3332, { bitsPerSample = 32 }, // fl32_MAGIC
						0x464C3332, { bitsPerSample = 32 }, // FL32_MAGIC
						0x666C3634, { bitsPerSample = 64 }, // fl64_MAGIC
						0x464C3634, { bitsPerSample = 64 }, // FL64_MAGIC
						0x736F7774, { bitsPerSample = 16 }, // 'sowt' (16-bit PCM little endian)
						{
							Error( "Unknown AIFC compression" ).throw;
						}
						);
						chunkLen = chunkLen - 4;
					});
					numEssentials = numEssentials - 1;
				},
				0x53534E44, { // 'SSND'
				ssndLengthOffset = f.pos - 4;
					numEssentials = numEssentials - 1;
				});
			});
	
			if( numEssentials > 0, {
				Error( "Essential chunks (COMM, SSND) missing" ).throw;
			});
	
			bytesPerFrame = (bitsPerSample >> 3) * numChannels;
			numFrames = (totalLen - (ssndLengthOffset + 4)).div( bytesPerFrame );
			"Detected AIFF file with % channels, % bits per sample, guessing % frames.\nFixing header...\n"
				.postf( numChannels, bitsPerSample, numFrames );
	
			f.pos = formLengthOffset;
			f.putInt32( totalLen - 8 );
			f.pos = commSmpNumOffset;
			f.putInt32( numFrames );
			f.pos = ssndLengthOffset;
			f.putInt32( totalLen - (ssndLengthOffset + 4) );
			"Done.".postln;
			
		} { arg error;
			try { f.close };
		};
	}
	
	*forkIfNeeded { arg func ... args;
		if( thisThread.isKindOf( Routine ), {
			func.value;
			^nil;
		}, {
			^func.fork( *args );
		});
	}
	
	*fostexRenamingGUI {
		var w, months, f;
		months = [ "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec" ];
		f = { JSCView.currentDrag.asArray.select( _.isKindOf( PathName )).select({ arg p; "B[0-2][0-9]h[0-5][0-9]m[0-5][0-9]s[0-3][0-9][a-z][a-z][a-z][0-9][0-9][0-9][0-9].wav".matchRegexp( p.fileName )})};
		w = JSCWindow( "Fostex FR-2 Renaming", Rect( 0, 0, 176, 76 ), resizable: false );
		ScissUtil.positionOnScreen( w, 0.8, 0.2 );
		JSCDragSink( w, Rect( 4, 4, 168, 68 ))
			.string_( "Drop FR-2 named WAV files" )
			.canReceiveDragHandler_({ f.value.notEmpty })
			.action_({ arg view;
				var paths, dir, file, cmd, oldPath, newPath, date, year, month, day, hour, minute, second, result;
				paths = f.value;
				view.string = "Processing % files...".format( paths.size );
				paths.do({ arg p;
					dir		= p.pathOnly;
					file		= p.fileName;
					oldPath	= p.fullPath;
					newPath	= oldPath;
					year		= file.copyRange( 15, 18 ).asInteger;
					month	= months.detectIndex({ arg m; m == file.copyRange( 12, 14 )});
					day		= file.copyRange( 10, 11 ).asInteger;
					hour		= file.copyRange( 1, 2 ).asInteger;
					minute	= file.copyRange( 4, 5 ).asInteger;
					second	= file.copyRange( 7, 8 ).asInteger;
					if( month.notNil, {
						month	= month + 1;
						date		= Date( year, month, day, hour, minute, second, 0, 0, 0 );
						newPath	= dir +/+ "Rec%.wav".format( date.stamp );
						cmd		= "mv % %".format( oldPath.escapeChar($ ), newPath.escapeChar($ ));
						cmd.postln;
						result	= cmd.systemCmd;
						if( result != 0, {
							"FAILED with result %!\n".postf( result );
						});
					}, {
						("Filename includes illegal month:" + file).error;
					});
				});
				view.string = "Renamed % files.".format( paths.size );
			});
		^w.front;
	}
}