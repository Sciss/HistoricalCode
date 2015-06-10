/**
 *	AmpFeedback
 *	(Amplifikation)
 *
 *	(C)opyright 2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.12, 29-Aug-09
 *	@author	Hanns Holger Rutz
 *
 *	@todo	mickey mouse
 */
AmpFeedback : AmpProcess {
	classvar <>verbose			= false;
	classvar <>view			= false;
//	classvar <>maxNotchFreqs	= 8;
//	classvar <>maxNotchSynths	= 8;
	classvar <>maxNotchFreqs	= 6;
	classvar <>maxNotchSynths	= 6;
	classvar <>micOffset		= 6;
	
//	classvar <>ceil			= -28; // dB
	classvar <>minCeil			= -28.0; // dB
	classvar <>maxCeil			= -26.0; // dB

	classvar <>dlySecsMin		= 45;
	classvar <>dlySecsMax		= 90;
	classvar <>minFadeTime		= 4;
	classvar <>maxFadeTime		= 16;
	classvar <>maxFadeTimeRand	= 48;
	classvar <>spectSmear		= 0.995;
	classvar <>boost			= 20; // 10; // 12;

	classvar <>minPlayTime		= 120;
	classvar <>maxPlayTime		= 240;
	classvar <>minRlsTime		= 8;
	classvar <>maxRlsTime		= 16;
	classvar <>minPauseTime		= 12;
	classvar <>maxPauseTime		= 24;
	
	var numChannels = 4;
	
	// for the player
	var <procMinDur	= 120;
	var <procMaxDur	= 480;
	
	*new {
		^super.new.prInit;
	}
	
	prInit {
	}
		
	prTaskBody { arg rmap;
		var numTasks, cond;
		
		numTasks = Amplifikation.masterNumChannels.div( numChannels );
		numTasks.do({ arg ch;
			Routine({
				this.prMesoTaskBody( rmap, ch );
				numTasks = numTasks - 1; cond.signal;
			}).play( thisThread.clock );
		});
		cond = Condition({ numTasks == 0 });
		cond.wait;
	}
	
	prMesoTaskBody { arg rmap, ch;
		var numRoutines, cond, routMonitor, routRandom, randomSynth, win, ggSpect, clpseSpectView,
		    scopeResp, avgSpect, fftSize, linear, condScope, notchSynths,
		    notchQ, inIndex, phasorBus, dlySecs, scopeBuf, bndl, buf, bufFrames, synth, def, defName, internalBus,
		    routSynth, routDef, routDefName, subGroup, nyquist, binSize, numFreqs, ceil;

//		numChannels	= 4;
		
		fftSize		= 2048; // 512; // 2048;   // NOT THE CPU BOTTLENECK
		linear		= true;
		notchQ		= 3; // 2; // 3;
		dlySecs		= exprand( dlySecsMin, dlySecsMax ); // 10;
//		inIndex		= amp.server.options.numOutputBusChannels + ch; // .min( 3 );
		inIndex		= amp.server.options.numOutputBusChannels + micOffset + (3 - ch); // .min( 3 );
//		ceil			= 0.1;
		nyquist		= amp.server.sampleRate / 2;
		binSize		= amp.server.sampleRate / fftSize;
		numFreqs		= (fftSize >> 1) - 1;

		notchSynths	= [];
		
		bndl			= this.newBundle;
		bufFrames		= (dlySecs * amp.server.sampleRate).asInteger;
		buf			= Buffer( amp.server, bufFrames, 1 );
		bndl.addPrepare( buf.allocMsg( buf.zeroMsg ));
		scopeBuf		= Buffer( amp.server, (fftSize >> 1), 1 );
		bndl.addPrepare( scopeBuf.allocMsg( scopeBuf.zeroMsg ));
		
		avgSpect		= 0 ! numFreqs;
		condScope		= Condition.new;
		scopeResp		= OSCpathResponder( amp.server.addr, ['/b_setn', scopeBuf.bufnum, 0 ], { arg time, r, msg;
			avgSpect = (avgSpect * spectSmear) + (msg.copyToEnd( 4 ).max( -160 ) * (1 - spectSmear));
			if( view, { clpseSpectView.instantaneous });
			condScope.test = true; condScope.signal;
		}).add;
		
		phasorBus		= Bus.audio( amp.server, 1 );
		internalBus	= Bus.audio( amp.server, numChannels );
		subGroup		= Group( rmap.group );

		if( view, {
			win		= JSCWindow( "Spect", Rect( 20, 20, 1024, 256 ), resizable: false );
			ggSpect	= JSCMultiSliderView( win, win.view.bounds )
				.canFocus_( false )
				.value_( avgSpect )
				.gap_( 0 )
				.indexThumbSize_( 2048 / fftSize )
				.drawLines_( true )
				.drawRects_( false )
				.strokeColor_( Color.black )
				.isFilled_( false );
			win.front;
			clpseSpectView = Collapse({ ggSpect.value = avgSpect.normalize }, 0.2 );
		});
		
		ceil = rrand( minCeil, maxCeil ).dbamp;
		
		defName		= \ampFeedbackIn;
		synth		= Synth.basicNew( defName, amp.server );
		if( defWatcher.isOnline( defName ).not, {
			def = SynthDef( defName, { arg i_buf, in, ceil = 0.1, boost = 1, i_scopeBuf, phasorBus;
				var inp, env, phasorR, phasorW, bufFrames, fftBuf, fftPhase, scopeSig, fftDur, fftPhaseRate;
				
				inp		= LeakDC.ar( In.ar( in ));
				fftBuf	= LocalBuf( fftSize );
				PV_MagSmear( FFT( fftBuf, inp ), 1 );
//				fftPhaseRate = SampleRate.ir * (4 / fftSize);
				fftPhaseRate = SampleRate.ir * (2 / fftSize);
				
				inp		= Limiter.ar( inp * boost, ceil );
				bufFrames	= BufFrames.ir( i_buf );
				phasorW	= Phasor.ar( 0, BufRateScale.ir( i_buf ), 0, bufFrames );
				BufWr.ar( inp, i_buf, phasorW );
				Out.ar( phasorBus, phasorW );
				
				if( linear, {
					fftPhase = LFSaw.ar( fftPhaseRate, 1, numFreqs, numFreqs + 2 );
				}, {
					fftPhase = (fftSize >> 1).pow( LFSaw.ar( fftPhaseRate, 1, 0.5, 0.5 )) * 2; // 2 to bufsize
				});
				fftPhase = fftPhase.round( 2 ); // the evens are magnitude
				scopeSig = ((BufRd.ar( 1, fftBuf, fftPhase, 1, 1 ) * 0.00285).ampdb * 0.01) + 1;
				RecordBuf.ar( scopeSig, i_scopeBuf );
			});
			defWatcher.sendToBundle( bndl, def, synth );
		});
					
		bndl.add( synth.newMsg( rmap.group, [ \i_buf, buf, \in, inIndex,
		                                        \boost, boost.dbamp, \ceil, ceil,
		                                        \i_scopeBuf, scopeBuf, \phasorBus, phasorBus ]));

		routDefName	= \ampFeedbackRoute;
		routSynth		= Synth.basicNew( routDefName, amp.server );
		if( defWatcher.isOnline( routDefName ).not, {
			routDef = SynthDef( routDefName, { arg in, out;
				Out.ar( out, In.ar( in, numChannels ));
			});
			defWatcher.sendToBundle( bndl, routDef, routSynth );
		});
					
//		bndl.add( routSynth.newMsg( rmap.group, [ \in, internalBus, \out, amp.masterBus ], \addToTail ));
		bndl.add( routSynth.newMsg( rmap.group, [ \in, internalBus, \out, rmap.bus.index + (ch * numChannels) ], \addToTail ));

		bndl.send;
		
		routRandom = Routine({ var bndl, freq, q, fadeTime, dur;
			inf.do({
				bndl = this.newBundle;
				if( randomSynth.notNil, {
					fadeTime = exprand( minFadeTime, maxFadeTimeRand );
					bndl.add( randomSynth.releaseMsg( fadeTime ));
					randomSynth = nil;
				});
				randomSynth = this.prCreateNotchSynth( bndl );
				fadeTime = exprand( minFadeTime, maxFadeTimeRand );
				q		= exprand( 0.5, 2.0 );
				freq		= exprand( 100, 18000 );	// mean 3.5 kHz
				bndl.add( randomSynth.newMsg( routSynth, [
					\bus, internalBus, \freq, freq, \rq, q.reciprocal, \i_fadeIn, fadeTime ], \addBefore ));

//				dur = exprand( 10, 40 );
				dur = exprand( fadeTime, fadeTime * 4 );
				dur.wait;
			});
		}).play( SystemClock );

		routMonitor = Routine({ var t1, t2, order, notchSynth, bndl, notchFreq, minSuppressGain, map,
		                            fCheckDur, checkDur, fadeTime;
			t1 = thisThread.seconds;
			minSuppressGain = -120;
//			fCheckDur = { exprand( 1.7, 2.35 )};
			fCheckDur = { exprand( 0.17 * dlySecs, 0.235 * dlySecs )};
			checkDur = fCheckDur.value;
			inf.do({
				amp.server.listSendMsg( scopeBuf.getnMsg( 0, numFreqs ));
				condScope.wait; condScope.test = false;
0.1.wait;
				t2 = thisThread.seconds;
				if( (t2 - t1) > checkDur, {
//					order = avgSpect.neg.order;
					checkDur = fCheckDur.value;
					fadeTime = exprand( minFadeTime, maxFadeTime );
//~neg = avgSpect.neg;
//					order = avgSpect.neg.order.keep( maxNotchFreqs );
					order = avgSpect.neg.order;
					order = order.select({ arg idx; (idx > 0) and: { (avgSpect[ idx - 1 ] < avgSpect[ idx ]) and: { (idx < (order.size - 1)) and: { avgSpect[ idx + 1 ] < avgSpect[ idx ]}}}}).keep( maxNotchFreqs );
					
//Assertion({ order.every({ arg idx; idx < avgSpect.size })});
//					
//[ "HUHU", avgSpect.size * binSize, avgSpect.size, order.size, ~neg.size ].postln;
//~avg = avgSpect;
					
//					order = ((order.size - 9)..(order.size - 1)).collect({ arg pos; order.indexOf( pos )});
//					order = { arg pos; order.indexOf( pos )} ! 8;
//					Assertion({ avgSpect[ order.first ] == avgSpect.maxItem });
//					order.postln;

					if( verbose, {([ "A" ] ++ order.collect({ arg idx; (idx + 1) * binSize }).asArray.sort.round( 1 )).postln });

					bndl = this.newBundle;
//					notchSynths.keys.removeAll( order ).do({ arg idx; })
					notchSynths = notchSynths.select({ arg entry, idx; var ok;
						ok = order.includes( entry.idx );
						if( ok.not,Ê{
//							notchSynths.removeAt( idx );
							bndl.add( entry.synth.releaseMsg( fadeTime ));
//							notchFreq = (idx + 1) * amp.server.sampleRate / fftSize;
							notchFreq = (idx + 1) * binSize;
							if( verbose, { "Removing notch for '%'\n".postf( notchFreq )});
						});
						ok;
					});
//					order = order.keep( maxNotchSynths - notchSynths.size );

//					([ "C" ] ++ order.collect({ arg idx; (idx + 1) * binSize }).asArray.sort.round( 1 )).postln;
					
//					order.do({ arg idx; })
					order.reject({ arg idx; notchSynths.detect({ arg entry; entry.idx == idx }).notNil }).do({ arg idx;
						notchFreq = (idx + 1) * binSize;
//"Checking '%' at '%'... amp %\n".postf( idx, notchFreq,avgSpect[ idx ]);
						if( (avgSpect[ idx ] > minSuppressGain) and: { notchFreq < nyquist }, {
//"....jupp".postln;
//							notchFreq = (idx + 1) * amp.server.sampleRate / fftSize;
							notchSynth = this.prCreateNotchSynth( bndl );
							bndl.add( notchSynth.newMsg( routSynth, [
								\bus, internalBus, \freq, notchFreq, \rq, notchQ.reciprocal, \i_fadeIn, fadeTime ], \addBefore ));
//							notchSynths.put( idx, notchSynth.register );
							map = AnyMap.new;
							map.idx = idx;
							map.synth = notchSynth;
							notchSynths = notchSynths.add( map );
							if( verbose, { "Launching notch for '%'\n".postf( notchFreq )});
						});
					});
					bndl.send;
					t1 = t2;
		//			"Aqui".postln;
		
//					notchSynths.collect({ arg entry; (entry.idx + 1) * amp.server.sampleRate / fftSize }).asArray.sort.round( 1 ).postln;
					if( verbose, {([ "B" ] ++ notchSynths.collect({ arg entry; (entry.idx + 1) * binSize }).asArray.sort.round( 1 )).postln });
				});
			});
		}).play( SystemClock );
				
		numRoutines = numChannels; // Amplifikation.masterNumChannels;
		numRoutines.do({ arg ch;
			Routine({
				this.prSubTaskBody( rmap, subGroup, buf, phasorBus, internalBus.index + ch );
				numRoutines = numRoutines - 1; cond.signal;
			}).play( thisThread.clock );
		});
		cond = Condition({ numRoutines == 0 });
		cond.wait;
		
		routMonitor.stop;
		routRandom.stop;
		
		// freeing nodes
		bndl = this.newBundle;
		notchSynths.do({ arg entry; bndl.add( entry.synth.freeMsg )});
		if( routSynth.notNil, { bndl.add( routSynth.freeMsg )});
		if( randomSynth.notNil, { bndl.add( randomSynth.freeMsg )});
		bndl.add( synth.freeMsg );
		bndl.add( subGroup.freeMsg );
		bndl.send;
		
		// freeing resources
		scopeBuf.free;
		scopeResp.remove;
		phasorBus.free;
		internalBus.free;

		if( view, {
			win.close;
			clpseSpectView.cancel;
		});
		
//		TypeSafe.methodInform( thisMethod, "Done" );
	}
	
	prCreateNotchSynth { arg bndl;
		var synth, defName, def;
		defName = \ampNotch;
		synth = Synth.basicNew( defName, amp.server );
		if( defWatcher.isOnline( defName ).not, {
			def = SynthDef( defName, { arg bus = 0, i_fadeIn = 1, freq = 440, rq = 0.1, gate = 1;
				var inp, flt, env;
				inp = In.ar( bus, numChannels );
				env = EnvGen.kr( Env.asr( i_fadeIn ), gate, doneAction: 2 );
				flt = BRF.ar( inp, freq, rq );
//									flt = MidEQ.ar( In.ar( bus ), freq, rq );
				XOut.ar( bus, env, flt );
//									ReplaceOut.ar( bus, flt );
			});
			defWatcher.sendToBundle( bndl, def, synth );
		});
		^synth;
	}
	
	prSubTaskBody { arg rmap, subGroup, buf, phasorBus, outIndex;
		var defName, dlyFramesMin, dlyFramesMax, synth, bndl, dur, def;		
		defName	= \ampFeedbackOut;
//		outIndex	= amp.masterBus.index + outCh;
		
		while({Êrmap.keepRunning }, {
			bndl		= this.newBundle;
			synth	= Synth.basicNew( defName, amp.server );
			if( defWatcher.isOnline( defName ).not, {
//				def = SynthDef( defName, { arg i_buf, out, amp = 1.0, i_fadeIn, gate = 1, ceil = 0.1, dlyFrames, phasorBus;
//					var inp, env, sig, phasorR, bufFrames;
//					
//					bufFrames	= BufFrames.ir( i_buf );
//					phasorR	= (In.ar( phasorBus ) + dlyFrames) % bufFrames;
//					sig		= BufRd.ar( 1, i_buf, phasorR, 1, 1 );
////					sig = Limiter.ar( sig, 0.2 ); // for testin
//					env		= EnvGen.kr( Env.asr( i_fadeIn, 1, 1, \lin ), gate, doneAction: 2 ) * amp;
//					Out.ar( out, sig * env );
//				});
				def = SynthDef( defName, { arg i_buf, out, amp = 1.0, i_fadeIn, gate = 1, ceil = 0.1, dlyFramesMin, dlyFramesMax, phasorBus, randRate = 0.05;
					var inp, env, sig, phasorR, bufFrames, dlyFrames, mul;
					
					bufFrames	= BufFrames.ir( i_buf );
					mul = (dlyFramesMax - dlyFramesMin) / 2;
					dlyFrames = LFNoise1.kr( randRate, mul, mul + dlyFramesMin );
					phasorR	= (In.ar( phasorBus ) + dlyFrames) % bufFrames;
					sig		= BufRd.ar( 1, i_buf, phasorR, 1, 1 );
//					sig = Limiter.ar( sig, 0.2 ); // for testin
					env		= EnvGen.kr( Env.asr( i_fadeIn, 1, 1, \lin ), gate, doneAction: 2 ) * amp;
					Out.ar( out, sig * env );
				});
				defWatcher.sendToBundle( bndl, def, synth );
			});
						
//			dlyFrames = ((1 - exprand( 0.5, 1.0 )) * (buf.numFrames - 1)).asInteger + 1;
			dlyFramesMin = ((1 - exprand( 0.40, 0.58 )) * (buf.numFrames - 1)).asInteger;
			dlyFramesMax = dlyFramesMin + (((1 - exprand( 0.41, 0.58 )) * (buf.numFrames - 1)).asInteger);
			dur = exprand( minRlsTime, maxRlsTime );
			bndl.add( synth.newMsg( subGroup, [ \i_buf, buf, \phasorBus, phasorBus,
			                                    \out, outIndex, \i_fadeIn, dur,
			                                    \dlyFramesMin, dlyFramesMin, \dlyFramesMax, dlyFramesMax,
//			                                    \randRate, exprand( 1/30, 1/20 )
			                                    \randRate, exprand( 1/60, 1/40 )
			                                  ], \addToTail ));

			bndl.send;
			if( verbose, {
				TypeSafe.methodInform( thisMethod, "Playing (% secs)".format( dur.round( 0.1 )));
			});

			dur = exprand( minPlayTime, maxPlayTime );
			while({ rmap.keepRunning && (dur > 0) }, {
				min( 1, dur ).wait;
				dur = max( 0, dur - 1 );
			});
			
			dur = if( rmap.stopQuickly, 1, { exprand( minRlsTime, maxRlsTime )});
			bndl = this.newBundle;
			bndl.add( synth.releaseMsg( dur ));
			bndl.send;
			synth.waitForEnd;
			
			dur = exprand( minPauseTime, maxPauseTime );
			if( verbose, {
				TypeSafe.methodInform( thisMethod, "Begin wait (% secs)".format( dur.round( 0.1 )));
			});
			dur.wait;
		});
	}
}