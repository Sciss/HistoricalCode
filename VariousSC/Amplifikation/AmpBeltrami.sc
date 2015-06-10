/**
 *	AmpBeltrami
 *	(Amplifikation)
 *
 *	(C)opyright 2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.11, 21-Aug-09
 *	@author	Hanns Holger Rutz
 */
AmpBeltrami : AmpProcess {
	classvar <>verbose = true;
	classvar <>volume = 0.4;
	classvar <>downDamp = -3;  // dB
	classvar <>domBoost = 12;	// dB
	
	classvar <>minFadeTime = 10;
	classvar <>maxFadeTime = 60;
	
	var rmsH, rmsV;
	
	// for the player
	var <procMinDur	= 120;
	var <procMaxDur	= 480;
	
	*new {
		^super.new.prInit;
	}
	
	prInit {
		var rms, add;
		rms = [ -27.6, -27.8, -27.2, -26.8, -26.5, -26.7, -27.7, -26.4, -27.7, -26.3, -26.6, -26.2, -28.0, -29.8, -26.9, -26.4, -36.2, -34.3, -34.5, -34.9, -27.6, -28.3, -28.1, -26.7, -26.3, -26.0, -28.4, -27.4, -26.4, -26.2, -26.9, -26.3 ];
		add	= rms.minItem;
		rmsH = (add - rms.select({ arg val, i; i.even })).dbamp;
		rmsV = (add - rms.select({ arg val, i; i.odd  })).dbamp;
		
//		rmsH.postln;
	}
	
	prTaskBody { arg rmap;
		var numRoutines, cond, cDomBus, aDomBus, subGroup, bndl, domSynth, domDefName, domDef, orient;
		
		bndl			= this.newBundle;
		cDomBus		= Bus.control( amp.server, 4 );
		aDomBus		= Bus.audio( amp.server, 1 );
		subGroup		= Group( rmap.group );
		domDefName	= \ampBeltramiDom;
		domSynth		= Synth.basicNew( domDefName, amp.server );
		if( defWatcher.isOnline( domDefName ).not, {
			domDef = SynthDef( domDefName, { arg cDomBus, posFreq = 0.1, emphFreq = 0.09, amtFreq = 0.08;
				var x, y, emph, amt, sig;
				x		= LFNoise1.kr( posFreq / 7, 5, 5 );
				y		= LFNoise1.kr( posFreq, 0.5, 0.5 );
				emph		= LFNoise1.kr( emphFreq, 2, 3 );
				amt		= LFNoise1.kr( amtFreq, 0.5, 0.5 );
				sig		= [ x, y, emph, amt ];
//				sig.poll( 1, "dom" );
				Out.kr( cDomBus, sig );
			});
			defWatcher.sendToBundle( bndl, domDef, domSynth );
		});
		bndl.add( domSynth.newMsg( subGroup, [ \cDomBus, cDomBus ], \addBefore )); // actually addBefore not necessary
		bndl.send;
		
		orient = [ \h, \v ].choose;
		
		numRoutines = Amplifikation.masterNumChannels;
		numRoutines.do({ arg ch;
			Routine({
				this.prSubTaskBody( rmap, ch, subGroup, cDomBus, aDomBus, orient );
				numRoutines = numRoutines - 1; cond.signal;
			}).play( thisThread.clock );
		});
		cond = Condition({ numRoutines == 0 });
		cond.wait;

		// freeing nodes
		bndl = this.newBundle;		
		bndl.add( domSynth.freeMsg );
		bndl.add( subGroup.freeMsg );
		bndl.send;
		
		// freeing resources
		cDomBus.free;
		aDomBus.free;
		
//		TypeSafe.methodInform( thisMethod, "Done" );
	}
	
	prSubTaskBody { arg rmap, ch, subGroup, cDomBus, aDomBus, orient;
		var defName, bndl, buf, synth, def, dur, sf, startFrame, path, rms;

		defName	= \ampBeltrami;

		bndl = this.newBundle;
		buf  = Buffer( amp.server, 32768, 1 );
		path = amp.audioDir +/+ "ScanP%%.aif".format( ch + 1, orient.asString.first.toUpper );
		sf   = SoundFile.openRead( path );
		sf.close;
		startFrame = sf.numFrames.div( 2 ).rand;
		bndl.addPrepare( buf.allocMsg( buf.cueSoundFileMsg( path, startFrame )));
		rms   = switch( orient, \h, rmsH, \v, rmsV, {ÊError( "Illegal orient" ).throw }).at( ch );
		synth = Synth.basicNew( defName, amp.server );
		if( defWatcher.isOnline( defName ).not, {
			def = SynthDef( defName, { arg i_buf, out, amp = 1.0, i_fadeIn, gate = 1, x, y, cDomBus, aDomBus, domBoost, domLag = 0.05;
				var env, sig, cDomX, cDomY, cDomEmph, cDomAmt, dist, domSig, selfDom;
				#cDomX, cDomY, cDomEmph, cDomAmt = In.kr( cDomBus, 4 );
//				dist		= (x @Êy).dist( cDomX, cDomY ).min( 1 );
				dist		= ((x - cDomX).squared + (y - cDomY).squared).sqrt.min( 1 );
				selfDom	= 1 - dist;
				amp		= amp * ((selfDom * cDomEmph) + dist);
				env		= EnvGen.kr( Env.asr( i_fadeIn ), gate, doneAction: 2 ) * amp;
				sig		= DiskIn.ar( 1, i_buf, 1 );
				domSig	= InFeedback.ar( aDomBus );
				Out.ar( aDomBus, Lag.ar( sig.squared, domLag ) * selfDom * domBoost );
				cDomAmt	= cDomAmt * dist;
				sig		= ((sig * domSig) * cDomAmt) + (sig * (1 - cDomAmt));
				Out.ar( out, sig * env );
			});
			defWatcher.sendToBundle( bndl, def, synth );
		});
//		[ "rmsH", rmsH[ ch ]].postln;
//		bndl.add( synth.newMsg( player.group, [ \i_buf, buf, \out, rmap.bus.index + ch, \i_fadeIn, rmap.fadeTime,
//		                                        \amp, volume * rmsH[ ch ] * if( ch.even, 1, { downDamp.dbamp })]));
		dur = exprand( minFadeTime, maxFadeTime );
		bndl.add( synth.newMsg( subGroup, [ \x, ch.div( 2 ) + ch.div( 4 ), \y, ch & 1, \cDomBus, cDomBus, \aDomBus, aDomBus,
		                                    \i_buf, buf, \out, rmap.bus.index + ch, \i_fadeIn, dur,
		                                    \domBoost, domBoost.dbamp,
		                                    \amp, volume * rms * if( ch.even, 1, { downDamp.dbamp })]));
		bndl.send;
		
		while({Êrmap.keepRunning }, {
			1.wait;
		});

		// freeing nodes
		bndl = this.newBundle;
		dur = if( rmap.stopQuickly, 1, { exprand( minFadeTime, maxFadeTime )});
		bndl.add( synth.releaseMsg( dur ));
		bndl.send;
		synth.waitForEnd;

		// freeing resources
		buf.close; buf.free;
	}
}