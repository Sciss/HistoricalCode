/**
 *	AmpDeconv
 *	(Amplifikation)
 *
 *	(C)opyright 2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.12, 22-Aug-09
 *	@author	Hanns Holger Rutz
 */
AmpDeconv : AmpProcess {
	classvar <>verbose	= false;
	classvar <>gain	= 18; // dB (normal)
	classvar <>gainAtt	= 9;  // dB (att)
	
	classvar <>minFadeTime = 4;
	classvar <>maxFadeTime = 16;

	var rmsH, rmsV;
	
	// for the player
	var <procMinDur	= 60;
	var <procMaxDur	= 120;
	var <procMinPause	= 60;
	var <procMaxPause	= 120;
	
	var cDomBus, group;

	*new {
		^super.new.prInit;
	}
	
	prInit {
		var numRoutines;
	
		cDomBus		= Bus.control( amp.server, 2 );
		group		= player.fltGroup2; // Group.before( amp.masterGroup );
		numRoutines	= Amplifikation.masterNumChannels;
		numRoutines.do({ arg ch;
			Routine({
				this.prSubTaskBodyGAGA( ch, cDomBus );
//				numRoutines = numRoutines - 1;
//				cond.signal;
			}).play( thisThread.clock );
		});

	}
		
	prTaskBody { arg rmap;
		var numRoutines, cond, /* cDomBus,*/ bndl, domSynth, domDefName, domDef;
		
		bndl			= this.newBundle;
//		cDomBus		= Bus.control( amp.server, 2 );
		domDefName	= \ampDeconvDom;
		domSynth		= Synth.basicNew( domDefName, amp.server );
		if( defWatcher.isOnline( domDefName ).not, {
			domDef = SynthDef( domDefName, { arg cDomBus, posFreq = 0.07;
				var x, y, emph, amt, sig;
				x		= LFNoise1.kr( posFreq / 7, 5, 5 );
				y		= LFNoise1.kr( posFreq, 0.5, 0.5 );
				sig		= [ x, y ];
//				sig.poll( 1, "dom" );
				Out.kr( cDomBus, sig );
			});
			defWatcher.sendToBundle( bndl, domDef, domSynth );
		});
		bndl.add( domSynth.newMsg( rmap.group, [ \cDomBus, cDomBus ]));
		bndl.send;
		
		numRoutines = Amplifikation.masterNumChannels;
		numRoutines.do({ arg ch;
			Routine({
				this.prSubTaskBody( rmap, ch, cDomBus );
				numRoutines = numRoutines - 1;
				cond.signal;
			}).play( thisThread.clock );
		});
		cond = Condition({ numRoutines == 0 });
		cond.wait;
		TypeSafe.methodInform( thisMethod, "Done" );

		// freeing nodes
		bndl = this.newBundle;
		bndl.add( domSynth.freeMsg );
		bndl.send;

		// freeing resources
//		cDomBus.free;
	}
	
	prSubTaskBodyGAGA { arg ch, cDomBus;
		var defName, buf, bndl, synth, def, path;

		defName	= \ampDeconv;

		bndl		= OSCBundle.new; // this.newBundle;
		buf		= Buffer( amp.server, 2048, 1 );
		path		= Amplifikation.audioDir +/+ "Plate%Corr.aif".format( ch + 1 );
		synth	= Synth.basicNew( defName, amp.server );
		if( defWatcher.isOnline( defName ).not, {
			def = SynthDef( defName, { arg i_buf, bus, x, y, cDomBus, fadeIn = 1, gate = /* 1 */ 0, ampBus;
				var trig, size, cDomX, cDomY, dist, dlyTime, sigDly, sig, wet, mix, amp;
				#cDomX, cDomY = In.kr( cDomBus, 2 );
				dist		= ((x - cDomX).squared + (y - cDomY).squared).sqrt.min( 1 );
				trig		= Impulse.kr( 0 );
				size		= 2048;
				dlyTime	= size / SampleRate.ir;
				sig		= In.ar( bus );
				sigDly	= DelayN.ar( sig, dlyTime, dlyTime );
				amp		= Lag.kr( In.kr( ampBus ), 4 );
				wet		= Convolution2.ar( sig * amp, i_buf, trig, size );
				
				mix		= (1 - dist) * EnvGen.kr( Env.asr( fadeIn ), gate /*, doneAction: 2*/ );
				
				ReplaceOut.ar( bus, (sigDly * (1 - mix)) + (wet * mix) );
			});
			defWatcher.sendToBundle( bndl, def, synth );
		});
		bndl.add( buf.allocMsg( buf.readMsg( path, completionMessage: synth.newMsg(
			group, [ \x, ch.div( 2 ) + ch.div( 4 ), \y, ch & 1, \cDomBus, cDomBus,
			         \i_buf, buf, \bus, amp.masterBus.index + ch,
			         \ampBus, player.ampBus ]))));
		bndl.send( amp.server, amp.server.latency + (ch * (amp.server.options.blockSize / amp.server.sampleRate * 2.0)) );
		
//UpdateListener.newFor( synth.register, { arg upd; upd.remove; "Woooooo, synth died (%)\n".postf( ch )}, \n_end );
	}
	
	prSubTaskBody { arg rmap, ch, cDomBus;
		var bndl, dur;

		dur	= exprand( minFadeTime, maxFadeTime );
		bndl	= this.newBundle;
		bndl.add( group.setMsg( \fadeIn, dur, \gate, 1 ));
		bndl.send;
		
		while({Êrmap.keepRunning }, {
			1.wait;
		});

		// freeing nodes
		bndl = this.newBundle;
		dur = if( rmap.stopQuickly, 1, { exprand( minFadeTime, maxFadeTime )});
		bndl.add( group.releaseMsg( dur ));
		bndl.send;
//		synth.waitForEnd;
(dur + 1).wait;

		// freeing resources
//		buf.free;
	}
}