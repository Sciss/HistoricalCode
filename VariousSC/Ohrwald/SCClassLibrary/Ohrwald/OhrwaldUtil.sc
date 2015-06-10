/**
 *	(C)opyright 2009 Hanns Holger Rutz. All rights reserved.
 *
 *	Dependancies: SMPTE (wslib), Eisenkraut, SwingOSC, ScissUtil, ScissPlus
 *
 *	@version	0.11, 28-Jun-09
 */
OhrwaldBallon {
	var <>span;				// (Span)
	var <>fadeIn	= 0.01;		// (Float) seconds
	var <>fadeOut = 1.0;		// (Float) seconds
	var <>gain	= 0.0;		// (Float) decibels

	*new { arg start, stop;
		^super.new.prInit( start, stop );
	}
	
	prInit { arg start, stop;
		span = Span( start, stop );
	}

	storeArgs { ^[ span.start, span.stop ]}

	storeModifiersOn { arg stream;
		if( fadeIn != 0.01, {
			stream << ".fadeIn_( ";
			fadeIn.storeOn( stream );
			stream << " )";
		});
		if( fadeOut != 1.0, {
			stream << ".fadeOut_( ";
			fadeOut.storeOn( stream );
			stream << " )";
		});
		if( gain != 0.0, {
			stream << ".gain_( ";
			gain.storeOn( stream );
			stream << " )";
		});
	}

	printOn { arg stream;
		stream << ("[ " ++ span.start ++ " ... " ++ span.stop ++ "]");
	}
}
 
OhrwaldUtil {
	classvar <>workDir = "/Users/rutz/Desktop/Ohrwald/audio_work";

	classvar <>ballons, <>responses, <>responses2, <>gehirne;
	// note: these appear after half the elements of ball / resp1 / resp2 !
	// (because the groups of four only make up two of the four groups)
	classvar <>responses3;
	classvar <>blaettern;
	
	classvar <>respGainOffset = 15;
	classvar <>resp2GainOffset = 9;
	classvar <>resp3GainOffset = 15;
	
	*loadBallons {
		ballons = (workDir +/+ "BallonsMittel.scd").load;
	}
	
	*loadResps {
		responses = (workDir +/+ "BallonsResp.scd").load;
	}

	*loadResps2 {
		responses2 = (workDir +/+ "BallonsResp2.scd").load;
	}

	*loadResps3 {
		responses3 = (workDir +/+ "BallonsResp3.scd").load;
	}

	*loadGehirne {
		gehirne = (workDir +/+ "GhrnSonaRMos.scd").load;
	}

	*loadBlaettern {
		blaettern = (workDir +/+ "Blaettern.txt").load;
	}
	
	*ballonsCreate {
		var e;
		var msg, rate, num, startIdx, stopIdx, marks, docLength;
	
		e = Eisenkraut.default;	
		e.addr.connect;
	  forkIfNeeded {
		msg = e.query( '/doc/active/timeline', [ \rate, \length ]);
		if( msg.notNil, {
			rate = msg[ 0 ];
			docLength = msg[ 1 ];
			msg = e.query( '/doc/active/markers', \count );
			if( msg.notNil, {
				num = msg[ 0 ];
				("Number of markers: "++num).postln;
				startIdx = 0;
				// maximum 128 markers per query, based on an estimated of maximum marker names ...
				// 128 * (32 + 4 + 5) + headerSize = ca. 5000 bytes
				stopIdx = min( num, startIdx + 128 );
				while({ startIdx < num }, {
					msg = e.get( '/doc/active/markers', [ \range, startIdx, stopIdx ]);
					if( msg.notNil, {
						msg.pairsDo({ arg pos, name;
							("Marker '"++name++"' at frame "++pos++" = "++(pos/rate).asTimeString( 0.001 )).postln;
							marks = marks.add( pos );
						});
					}, {
						"timeout".warn;
					});
					startIdx	= stopIdx;
					stopIdx	= min( num, startIdx + 128 );
				});
				
				marks = marks.add( docLength );
				this.ballons = marks.slide( 2, 1 ).clump( 2 ).collect({ arg x; OhrwaldBallon( x[ 0 ], x[ 1 ])});
				
			}, {
				"timeout".warn;
			});
		}, {
			"timeout".warn;
		});
	  }; // fork
	}
	
	*respCreate {
		responses = ballons.collect({ arg b; b = b.copy; b.span = Span( b.span.start << 1, b.span.stop << 1 ); b });
	}

	*resp2Create {
		responses2 = responses.collect({ arg b;
			b = b.copy;
			b.gain = 0.0;
			b;
		});
	}

	*ballonSynthDefs {
		var result;
		
		result = [ 1, 2 ].collect({ arg numChannels;
			SynthDef( \ohrwaldBallon ++ numChannels, { arg out = 0, buf, amp = 1, length, fadeIn, fadeOut;
				var play, env, dur, envGen;
			
				play		= VDiskIn.ar( numChannels, buf, BufRateScale.ir( buf ));
				dur		= length / BufSampleRate.ir( buf );
				env		= Env.linen( fadeIn, dur - (fadeIn + fadeOut), fadeOut, amp, -4.0 );
				envGen	= EnvGen.ar( env, doneAction: 2 );
				Out.ar( out, Limiter.ar( Mix( play ) * envGen ));	
			});
		});

		result = result ++ [ 1, 2 ].collect({ arg numChannels;
			SynthDef( \ohrwaldGehirne ++ numChannels, { arg out = 0, buf, amp = 1, length, fadeIn, fadeOut;
				var play, env, dur, envGen;
			
				play		= VDiskIn.ar( numChannels, buf, BufRateScale.ir( buf ));
				dur		= length / BufSampleRate.ir( buf );
				env		= Env.linen( fadeIn, dur - (fadeIn + fadeOut), fadeOut, amp, -4.0 );
				envGen	= EnvGen.ar( env, doneAction: 2 );
				Out.ar( out, Limiter.ar( play.asArray.first * envGen ));	
			});
		});
		
		^result;
	}

	/**
	 *	@param	resp		either of \orig, \resp, \resp2, \resp3, \gehirne
	 */
	*ballonEditGUI { arg resp = \orig, param, ch = 0;
		var gui, win, items, ggBallons, cocoa, currentBallon;
		var fPlayBallon, taskPlayBallon, ggPlayBallon;
		var ggDeleteVersion, ggStore, clpseDeleteVersion, clpseStore;
		var oStartTime, oStopTime, oFadeInTime, oFadeOutTime, funcAdjustTime, fUpdateEisKSel;
		var volume = 0.1, ggMasterGain;
		var ggBallonGain;
		var s, e, g, coll, baseFile, baseDefName, baseColl, isBase;
		
		var group;
		
		e = Eisenkraut.default;
		e.addr.connect;
	forkIfNeeded { e.initSwing;
		e.initTree;
		s = e.scsynth;
		s.notify; s.status;
		this.ballonSynthDefs.do({ arg def; def.send( s )});
		group = Group.tail( RootNode( s ));
		s.sync;
		s.sampleRate.postln;
		g = e.swing;
		
		coll = switch( resp,
			\orig, ballons,
			\resp, responses,
			\resp2, responses2,
			\resp3, responses3,
			\gehirne, { gehirne[ param ]},
			{ÊError( "Illegal argument resp = %".format( resp )).throw }
		);
		baseColl = switch( resp, \gehirne, coll, ballons );
		baseFile = switch( resp,
			\gehirne, {
				PathName( OhrwaldUtil.workDir ).files.select({ arg p; "Ghrn[1-9][0-9]?T1S[o]?naRMos"
					.matchRegexp( p.fileName )})
					.detect({ arg p; var i1;
						i1 = p.fileName.indexOf( $T );
						p.fileName.copyRange( 4, i1 - 1 ).asInteger == (param + 1);
					}).fullPath;
			},
			{ OhrwaldUtil.workDir +/+ "BallonsMittelSeg.aif" }
		);
		baseDefName = switch( resp, \gehirne, \ohrwaldGehirne2, \ohrwaldBallon2 );
		isBase = (resp == \orig)Ê||Ê(resp == \gehirne);

"Collection has % elements. Base file is %\n".postf( coll.size, baseFile.asCompileString );
				
//		g.postln;
		
		fPlayBallon = {
			var ballon, ballon2, ballon3, ballon4, buf, offset, synth, amp;
			if( currentBallon.notNil, {
"-----B".postln;
				ballon	= baseColl[ coll.indexOf( currentBallon )];
				ballon2	= responses[ coll.indexOf( currentBallon )];
				ballon3	= responses2[ coll.indexOf( currentBallon )];
				ballon4	= responses3[ coll.indexOf( currentBallon )];
				synth	= Synth.basicNew( baseDefName, s );
				amp		= ballon.gain.dbamp * volume;
				buf		= EisKBuffer.cueSoundFile( s, baseFile, ballon.span.start, 2,
					completionMessage: { arg b;
						synth.newMsg( group, [ \out, ch, \buf, b.bufnum, \amp, amp, \length, ballon.span.length,
							\fadeIn, ballon.fadeIn, \fadeOut, ballon.fadeOut ])
					});
				synth.waitForEnd;
				buf.close; buf.free;
				  if( (resp != \orig) and: { ballon2.notNil }, {
				  	0.5.wait;
"-----R1".postln;
					synth	= Synth.basicNew( \ohrwaldBallon1, s );
					amp		= (ballon2.gain + respGainOffset).dbamp * volume;
					buf		= EisKBuffer.cueSoundFile( s, OhrwaldUtil.workDir +/+ "BgStn8'b3HPFOpBllns'FRsmp.aif", ballon2.span.start, 1,
						completionMessage: { arg b;
							synth.newMsg( group, [ \out, ch, \buf, b.bufnum, \amp, amp, \length, ballon2.span.length,
								\fadeIn, ballon2.fadeIn, \fadeOut, ballon2.fadeOut ])
						});
					synth.waitForEnd;
					buf.close; buf.free;
					
					if( (resp != \resp) and: { ballon3.notNil }, {
				  		0.5.wait;
"-----R2".postln;
						synth	= Synth.basicNew( \ohrwaldBallon1, s );
						amp		= (ballon3.gain + resp2GainOffset).dbamp * volume;
						buf		= EisKBuffer.cueSoundFile( s, OhrwaldUtil.workDir +/+ "BgStn4'CCHPFOpBgStn8'lbLPFMix.aif", ballon3.span.start, 1,
							completionMessage: { arg b;
								synth.newMsg( group, [ \out, ch, \buf, b.bufnum, \amp, amp, \length, ballon3.span.length,
									\fadeIn, ballon3.fadeIn, \fadeOut, ballon3.fadeOut ])
							});
						synth.waitForEnd;
						buf.close; buf.free;
						
						if( (resp != \resp2) and: { ballon4.notNil }, {
					  		0.5.wait;
"-----R3".postln;
							synth	= Synth.basicNew( \ohrwaldBallon1, s );
							amp		= (ballon4.gain + resp3GainOffset).dbamp * volume;
							buf		= EisKBuffer.cueSoundFile( s, OhrwaldUtil.workDir +/+ "BgStn6'CCHlbOpBgStn4'lbLPF.aif", ballon4.span.start, 1,
								completionMessage: { arg b;
									synth.newMsg( group, [ \out, ch, \buf, b.bufnum, \amp, amp, \length, ballon4.span.length,
										\fadeIn, ballon4.fadeIn, \fadeOut, ballon4.fadeOut ])
								});
							synth.waitForEnd;
							buf.close; buf.free;
						});
					});
				});
			});
			taskPlayBallon = nil;
			{ ggPlayBallon.value = 0 }.defer;
		};
		
		fUpdateEisKSel = {
			if( currentBallon.notNil, {
				e.sendMsg( '/doc/active/timeline', \select, currentBallon.span.start, currentBallon.span.stop );
				e.sendMsg( '/doc/active/timeline', \position, currentBallon.span.start );
			});
		};
		
		gui = GUI.get( \swing );
		cocoa = gui.id === \cocoa;
		win = gui.window.new( "Ballon", Rect( 0, 0, 500, 340 ), resizable: false, server: g );

		items = coll.collect({ arg ballon; ballon.asString });

		ggBallons = gui.listView.new( win, Rect( 4, 4, 200, 200 ))
			.items_( items )
			.canFocus_( false )
			.action_({ arg b;
				currentBallon = coll[ b.value ];
				ggBallonGain.value = currentBallon.gain;
				oStartTime.tryChanged( \updateGUI );
				oStopTime.tryChanged( \updateGUI );
				oFadeInTime.tryChanged( \updateGUI );
				oFadeOutTime.tryChanged( \updateGUI );
				fUpdateEisKSel.value;
			});

		ggPlayBallon = gui.button.new( win, Rect( 216, 4, 80, 24 ))
			.states_([[ "Play" ], [ "Play", Color.white, Color.blue ]])
			.action_({ arg b;
				if( taskPlayBallon.notNil, {
					taskPlayBallon.stop;
					taskPlayBallon = nil;
					group.freeAll;
				});
				if( b.value == 1, {
					taskPlayBallon = Routine( fPlayBallon );
//					playWordIdx = nil;
					taskPlayBallon.play( SystemClock );
				});
			});

		oStartTime	= Object.new;
		oStopTime		= Object.new;
		oFadeInTime	= Object.new;
		oFadeOutTime	= Object.new;
		funcAdjustTime = { arg label, what;
			var model, result, amount, newVal, delta;
			
			case
			{ what === \getModel }
			{
				case
				{ label === \start }   { result = oStartTime }
				{ label === \stop }    { result = oStopTime }
				{ label === \fadeIn }  { result = oFadeInTime }
				{ label === \fadeOut } { result = oFadeOutTime }
				{
					("Warning: illegal label '" ++ label ++ "'").error;
				};
			}
			{ what === \getTime }
			{
				case
				{ label === \start }   { result = currentBallon.span.start / s.sampleRate }
				{ label === \stop }    { result = (currentBallon.span.stop) / s.sampleRate }
				{ label === \fadeIn }  { result = currentBallon.fadeIn }
				{ label === \fadeOut } { result = currentBallon.fadeOut }
				{
					("Warning: illegal label '" ++ label ++ "'").error;
				};
			}
			{ [ \bigInc, \inc, \bigDec, \dec ].includes( what )}
			{
				case
				{ what === \bigInc } { amount = 0.1 }
				{ what === \inc }    { amount = 0.01 }
				{ what === \dec }    { amount = -0.01 }
				{ what === \bigDec } { amount = -0.1 };

				case
				{ label === \start }
				{
					newVal = (currentBallon.span.start + (amount * s.sampleRate)).max( 0 ).asInteger;
					delta  = newVal - currentBallon.span.start;
					currentBallon.span = Span( newVal, (currentBallon.span.stop - delta).max( newVal ));
					oStartTime.tryChanged( \updateGUI );
					oStopTime.tryChanged( \updateGUI );
					fUpdateEisKSel.value;
				}
				{ label === \stop }
				{
					newVal = (currentBallon.span.length + (amount * s.sampleRate)).max( 0 ).asInteger;
					delta = newVal - currentBallon.span.length;
					currentBallon.span = Span( currentBallon.span.start, currentBallon.span.start + newVal );
					oStopTime.tryChanged( \updateGUI );
					fUpdateEisKSel.value;
				}
				{ label === \fadeIn }
				{
					newVal = (currentBallon.fadeIn + amount).max( 0.0 );
					currentBallon.fadeIn = newVal;
					oFadeInTime.tryChanged( \updateGUI );
				}
				{ label === \fadeOut }
				{
					newVal = (currentBallon.fadeOut + amount).max( 0.0 );
					currentBallon.fadeOut = newVal;
					oFadeOutTime.tryChanged( \updateGUI );
				}
				{
					("Warning: illegal label '" ++ label ++ "'").error;
				};
			}
			{
				("Warning: illegal function call with '" ++ what ++ "'").warn;
			};
			result;
		};

		this.prMakeTimeAdjust( gui, win, 208, 208, \start, funcAdjustTime );
		this.prMakeTimeAdjust( gui, win, 208, 238, \stop, funcAdjustTime );
		this.prMakeTimeAdjust( gui, win, 208, 268, \fadeIn, funcAdjustTime );
		this.prMakeTimeAdjust( gui, win, 208, 298, \fadeOut, funcAdjustTime );

		ggStore = gui.button.new( win, Rect( 4, 298, 80, 24 ))
			.states_([[ "Store" ], [ "Sure???", Color.white, Color.red ], [ "Storing...", Color.black, Color( 1.0, 0.5, 0.0 )]])
			.action_({ arg b;
				if( clpseStore.notNil, { clpseStore.cancel });
				case
				{ b.value == 1 }
				{
					clpseStore = Collapse({
						ggStore.value = 0;
					}, 0.5, AppClock ).defer;
				}
				{ b.value == 2 }
				{
"STORE : NOT YET IMPLEMENTED".error;
//					this.store( OhrwaldUtil.workDir +/+ "rhizom.xml" );
					{ b.value = 0 }.defer( 0.5 );
				};
			});

		ggBallonGain = gui.numberBox.new( win,  Rect( 90, 208, 50, 24 ))
			.maxDecimals_( 1 )
			.action_({ arg b;
				var clipped;
				clipped = b.value.clip( -40, 20 );
				if( b.value != clipped, {
					b.value = clipped;
				});
				if( currentBallon.notNil, {
					currentBallon.gain = b.value;
				});
			});


		ggMasterGain = gui.numberBox.new( win,  Rect( 90, 298, 50, 24 ))
			.maxDecimals_( 1 )
			.value_( volume.ampdb )
			.action_({ arg b;
				var clipped;
				clipped = b.value.clip( -40, 20 );
				if( b.value != clipped, {
					b.value = clipped;
				});
				volume = clipped.dbamp;
			});

		gui.view.globalKeyDownAction = { arg view, char, modifiers, unicode, keycode;
			case
			{ char == $q } { funcAdjustTime.value( \start, \bigDec )}
			{ char == $w } { funcAdjustTime.value( \start, \dec )}
			{ char == $e } { funcAdjustTime.value( \start, \inc )}
			{ char == $r } { funcAdjustTime.value( \start, \bigInc )}
			{ char == $a } { funcAdjustTime.value( \stop, \bigDec )}
			{ char == $s } { funcAdjustTime.value( \stop, \dec )}
			{ char == $d } { funcAdjustTime.value( \stop, \inc )}
			{ char == $f } { funcAdjustTime.value( \stop, \bigInc )}
//			{ char == $y } { funcAdjustTime.value( \fadeIn, \bigDec )}
//			{ char == $x } { funcAdjustTime.value( \fadeIn, \dec )}
//			{ char == $c } { funcAdjustTime.value( \fadeIn, \inc )}
//			{ char == $v } { funcAdjustTime.value( \fadeIn, \bigInc )}
			{ char == $- } { ggPlayBallon.doAction }
			{ char == $+ } { ggBallons.valueAction = (ggBallons.value - 1).max( 0 )}
			{ char == $# } { ggBallons.valueAction = (ggBallons.value + 1).min( ggBallons.items.size - 1 )};
		};

		ScissUtil.positionOnScreen( win, 0.5, 0.2 );
		win.front;
		
		if( coll.size > 0, {
			ggBallons.value = 0;
			ggBallons.doAction;
		});
	  }; // fork
	}
	
	*prMakeTimeAdjust { arg gui, parent, x, y, label, func;
		var ggTime;
		gui.staticText.new( parent, Rect( x, y, 48, 24 ))
			.string_( label )
			.align_( \right );

		ggTime = gui.staticText.new( parent, Rect( x + 54, y, 74, 24 ))
			.background_( Color.white );

		gui.button.new( parent, Rect( x + 134, y, 20, 24 ))
			.states_([[ "<<" ]])
			.action_({ arg b;
				func.value( label, \bigDec );
			});

		gui.button.new( parent, Rect( x + 158, y, 20, 24 ))
			.states_([[ "<" ]])
			.action_({ arg b;
				func.value( label, \dec );
			});

		gui.button.new( parent, Rect( x + 182, y, 20, 24 ))
			.states_([[ ">" ]])
			.action_({ arg b;
				func.value( label, \inc );
			});

		gui.button.new( parent, Rect( x + 206, y, 20, 24 ))
			.states_([[ ">>" ]])
			.action_({ arg b;
				func.value( label, \bigInc );
			});
			
		UpdateListener.newFor( func.value( label, \getModel ), { arg upd, obj, what;
			if( what == \updateGUI, {
				ggTime.string_( SMPTE( func.value( label, \getTime ), 1000 ).toString );
			});
		});
	}
}
