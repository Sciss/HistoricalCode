/**
 *	AmpPlayer
 *	(Amplifikation)
 *
 *	(C)opyright 2009 Hanns Holger Rutz. All rights reserved.
 *
 *	Dependancies: ScissPlus
 *
 *	@version	0.12, 21-Aug-09
 *	@author	Hanns Holger Rutz
 */
AmpPlayer {
	classvar <group, <fltGroup1, <fltGroup3, <fltGroup2; // , <busA, <busB;
	classvar <>verbose = true;
	classvar <>auto = true;

	classvar <>minFadeStepTime	= 2;
	classvar <>maxFadeStepTime	= 5;
	classvar <>minLagTime		= 2;
	classvar <>maxLagTime		= 10;

	classvar <internalBusses, <numChannels, synthXFader, <>busIdx, <>nextBusIdx;
	classvar <>procFeedBack, <>procBeltrami, <>procTexte, <>procDeconv, <>procZeroCross, <>procMedian;
	classvar <ampBus;

	*init {
		var s, bndl;
		s 				= Amplifikation.scsynth;
		group			= Group.basicNew( s );
		fltGroup1			= Group.basicNew( s );
		fltGroup3			= Group.basicNew( s );
		fltGroup2			= Group.basicNew( s );
		numChannels		= Amplifikation.masterNumChannels;
		internalBusses	= { Bus.audio( s, numChannels )} ! 2;
		ampBus			= Bus.control( s, 1 );
		ampBus.set( AmpDeconv.gain.dbamp );

		bndl				= this.newBundle;
		bndl.add( group.newMsg( s ));
		bndl.add( fltGroup1.newMsg( group, \addAfter ));
		bndl.add( fltGroup3.newMsg( fltGroup1, \addAfter ));
		bndl.add( fltGroup2.newMsg( fltGroup3, \addAfter ));
		this.createDefs.do({ arg def;
			bndl.addPrepare( def.recvMsg );
		});
		synthXFader		= Synth.basicNew( \ampPlayerRoute, s );
		bndl.add( synthXFader.newMsg( group, [ \in1, internalBusses[ 0 ], \in2, internalBusses[ 1 ], \out, Amplifikation.masterBus ], \addAfter ));
		bndl.send;
		
//		s.sync;

		if( auto, {

		Routine({
			1.0.wait;
//			s.sync;
			this.prRoutBodyGen;
		}).play( SystemClock );

		Routine({
			4.0.wait;
//			s.sync;
			this.prRoutBodyFlt1;
		}).play( SystemClock );

		Routine({
			7.0.wait;
//			s.sync;
			this.prRoutBodyFlt2;
		}).play( SystemClock );
		
		});
	}
	
	*prRoutBodyFltGeneral { arg proc, procGroup;
		var dur;
		if( 0.5.coin, {
			dur = exprand( proc.procMinPause, proc.procMaxPause ) * AmpProcess.procDurScale;
			if( verbose, { "pausing % for % secs\n".postf( proc.name, dur )});
			dur.wait;
		});
		inf.do({
			if( verbose, { "start'n %...\n".postf( proc.name )});
			this.prStartProc( proc, Amplifikation.masterBus, procGroup );
			dur = exprand( proc.procMinDur, proc.procMaxDur ) * AmpProcess.procDurScale;
			if( verbose, { "playing % for % secs\n".postf( proc.name, dur )});
			dur.wait;
			if( verbose, { "stop'n %...\n".postf( proc.name )});
			proc.stop;
			proc.waitForEnd;
			dur = exprand( proc.procMinPause, proc.procMaxPause ) * AmpProcess.procDurScale;
			if( verbose, { "pausing % for % secs\n".postf( proc.name, dur )});
			dur.wait;
		});
	}
	
	*prRoutBodyFlt1 {		
		procZeroCross = AmpZeroCross.new;
		this.prRoutBodyFltGeneral( procZeroCross, fltGroup1 );
	}
	
	*prRoutBodyFlt3 {		
		procMedian = AmpMedian.new;
		this.prRoutBodyFltGeneral( procMedian, fltGroup3 );
	}
	
	*prRoutBodyFlt2 {
		procDeconv = AmpDeconv.new;
		this.prRoutBodyFltGeneral( procDeconv, fltGroup2 );
	}
	
	*prRoutBodyGen {
		var dur, procs, proc, oldProc;
	
		procTexte		= AmpTraumTexte.new;
		procFeedBack	= AmpFeedback.new;
		procBeltrami	= AmpBeltrami.new;
		procs		= Urn.newUsing([ÊprocTexte, procFeedBack, procBeltrami ]).avoidRepeats_( 1 ).autoReset_( true );
		
		UpdateListener.newFor( procTexte, { arg upd, proc, what;
			var gain;
			switch( what, \started, {
				gain = AmpDeconv.gainAtt;
				if( verbose, { "Setting deconv amp to % dB\n".postf( gain )});
				ampBus.set( gain.dbamp );
			}, \stopped, {
				gain = AmpDeconv.gain;
				if( verbose, { "Setting deconv amp to % dB\n".postf( gain )});
				ampBus.set( gain.dbamp );
			});
		});
	
		busIdx = 1;
		inf.do({
//			procFeedBack.start;
			proc = procs.next;
			if( verbose, { "start'n %...\n".postf( proc.name )});
			this.prStartProc( proc, internalBusses[ busIdx ], group );
			this.prMakeFade;
			if( oldProc.isPlaying, {
				if( verbose, { "stop'n %...\n".postf( oldProc.name )});
				oldProc.stop;
				oldProc.waitForEnd;
				if( verbose, { "% stopped\n".postf( oldProc.name )});
			});
			dur = exprand( proc.procMinDur, proc.procMaxDur ) * AmpProcess.procDurScale;
			if( verbose, { "playing % for % secs\n".postf( proc.name, dur )});
			dur.wait;
			oldProc = proc;
		});
	}

	*prMakeFade {
		var fadeTime, lagTime;
		nextBusIdx	= (busIdx + 1) % 2;
		fadeTime		= exprand( minFadeStepTime, maxFadeStepTime );
//		lagTime		= fadeTime * exprand( minLagFactor, maxLagFactor );
		lagTime		= exprand( minLagTime, maxLagTime );
		synthXFader.set( \lagTime, lagTime );
		numChannels.do({ arg ch;
			synthXFader.setn( \fades, { arg i; if( i <= ch, busIdx, nextBusIdx )} ! numChannels );
			fadeTime.wait;
		});
		busIdx = nextBusIdx;
	}
	
	*prStartProc { arg proc, bus, group;
		var rmap;
		rmap			= AnyMap.new;
		rmap.bus		= bus;
		rmap.group	= group;
		proc.start( rmap );
	}

	*newBundle { ^Amplifikation.newBundle }
	
	*createDefs {
		var result, numChannels;
		
		numChannels = Amplifikation.masterNumChannels;
		result = result.add( SynthDef( \ampPlayerRoute, { arg in1, in2, out, lagTime = 3;
			var inp1, inp2, fades, outp;
			
			inp1  = In.ar( in1, numChannels );
			inp2  = In.ar( in2, numChannels );
			fades = LagControl.names([ \fades ]).kr( 0 ! numChannels, lagTime ! numChannels );
			outp  = (inp1 * (1 - fades)) + (inp2 * fades); // simply linear
			Out.ar( out, outp );
		}));
		^result;
	}
}