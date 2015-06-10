/**
 *	AmpTraumText
 *	(Amplifikation)
 *
 *	(C)opyright 2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.12, 23-Aug-09
 *	@author	Hanns Holger Rutz
 */
AmpTraumTextWort {
	var <>str;		// (String)
	var <>span;		// (Span)
	var <>breath;		// (Symbol)
	var <>bFadeIn;	// (Float) seconds
	var <>bFadeOut;	// (Float) seconds
	
	*new { arg str, span, breath, bFadeIn = 0.01, bFadeOut = 1.0;
		^super.newCopyArgs( str, span, breath );
	}
	
	storeArgs { ^[ str, span, breath, bFadeIn, bFadeOut ]}
	
	asString { ^str }
}

AmpTraumText {
	var <>fileName, <>words, <>gain;
	
	*new { arg fileName, words, gain = 1.0;
		^super.newCopyArgs( fileName, words, gain );
	}

	storeArgs { ^[ fileName, words, gain ]}
}

AmpTraumTexte : AmpProcess {
	classvar <>verbose = false;
	classvar <>gain    = 2;
	
	classvar <>texte;
	classvar <>textGains;
	
	classvar <>minWordSkip = 1;
	classvar <>maxWordSkip = 4;
	
	classvar <>minCommaDur = 5;
	classvar <>maxCommaDur = 14;
	classvar <>minPeriodDur = 14;
	classvar <>maxPeriodDur = 24;
	classvar <>minIrritDur = 0.7;
	classvar <>maxIrritDur = 3;
	
	// for the player
	var <procMinDur	= 120;
	var <procMaxDur	= 240;
	
	*new {
		^super.new.prInit;
	}
	
	prInit {
		this.class.loadTexte;
		textGains = [ -2.2, -3.8, -4, -4.5, -2.9, 0, -4, -4.2, -1.6, -5.6, -4.4, -4.4, -3.9, -2.9, -4.6, -5.1 ];
	}
	
	prTaskBody { arg rmap;
		var numRoutines, cond;
		
		numRoutines = Amplifikation.masterNumChannels;
		numRoutines.do({ arg ch;
			Routine({ this.prSubTaskBody( rmap, ch ); numRoutines = numRoutines - 1; cond.signal }).play( thisThread.clock );
		});
		cond = Condition({ numRoutines == 0 });
		cond.wait;
		TypeSafe.methodInform( thisMethod, "Done" );
	}
	
	*loadTexte {
		texte = (Amplifikation.workDir +/+ "sc" +/+ "AmpTraumTexteNeu.scd").load;
	}
	
	*editor {
	
	}
	
	prSubTaskBody { arg rmap, ch;
		var text, cnt, numWords, defName, bndl, startWord, endWord, buf, synth, def, dur, wordSkip, ampli;

		text		= texte[ ch ];
		numWords	= text.words.size;
//		cnt  	= 0;
		defName	= \ampTraumText;
		ampli	= (gain + textGains[ ch ]).dbamp;

		cnt		= numWords.rand;
		while({ text.words[ cnt ].breath !== \period }, { cnt = (cnt + 1) % numWords });

		dur = exprand( 10, 20 );
		dur.wait;
		
//"_____1".postln;
		
		while({Êrmap.keepRunning }, {
			startWord = text.words[ cnt ];
			wordSkip  = rrand( minWordSkip, maxWordSkip ) - 1;
			cnt = (cnt + wordSkip) % numWords;
//"_____2".postln;
			endWord = text.words[ cnt ];
			if( 0.5.coin, { cnt = (cnt + 1) % numWords });
			while({ text.words[ cnt ].breath == \none }, {
				cnt = (cnt + 1) % numWords;
			});
//[ "_____3", this ].postln;
			bndl = this.newBundle;
//"_____4".postln;
			buf = Buffer( amp.server, 32768, 1 );
			bndl.addPrepare( buf.allocMsg( buf.cueSoundFileMsg( amp.audioDir +/+ text.fileName, startWord.span.start )));
			synth = Synth.basicNew( defName, amp.server );
			if( defWatcher.isOnline( defName ).not, {
				def = SynthDef( defName, { arg i_buf, out, amp = 1.0, i_fadeIn, i_fadeOut, i_dur;
					var env, sig;
					env = EnvGen.kr( Env.linen( i_fadeIn, i_dur - (i_fadeIn + i_fadeOut), i_fadeOut ), doneAction: 2 ) * amp;
					sig = DiskIn.ar( 1, i_buf );
					Out.ar( out, sig * env );
				});
				defWatcher.sendToBundle( bndl, def, synth );
			});
			bndl.add( synth.newMsg( rmap.group, [ \i_buf, buf, \amp, ampli, \out, rmap.bus.index + ch, \i_fadeIn, startWord.bFadeIn,
			                                      \i_fadeOut, endWord.bFadeOut,
			                                      \i_dur, (endWord.span.stop - startWord.span.start) / amp.server.sampleRate ]));
//"----send %\n".postf( ch );
//			joinSync
			bndl.send;
//bndl.preparationMessages.postcs;
//bndl.messages.postcs;
if( verbose, { "--- A %\n".postf( ch )});
			synth.waitForEnd;
if( verbose, { "--- B %\n".postf( ch )});

			// free resources
			buf.close; buf.free;

			if( rmap.keepRunning, {
				dur = switch( endWord.breath,
					\comma, { exprand( minCommaDur, maxCommaDur )},
					\period, { exprand( minPeriodDur, maxPeriodDur )},
					{ exprand( minIrritDur, maxIrritDur )}
				);
			
				if( verbose, {
					"AmpTraum % : Begin wait (% secs)\n".postf( ch, dur.round( 0.1 ));
				});
				dur.wait;
				if( verbose, {
					"AmpTraum % : Done wait\n".postf( ch );
				});
			});		
		});
	}
}