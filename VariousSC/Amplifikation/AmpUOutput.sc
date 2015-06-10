/**
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: GeneratorUnit, TypeSafe
 *
 *	Changelog:
 *
 *	@version	0.16, 02-Aug-09
 *	@author	Hanns Holger Rutz
 */
AmpUOutput : NuagesU {

	var glassAttr;
	var subwAttr;
	var volumeAttr;
	
	var prefNumInChannels	= 2;
	var prefNumOutChannels	= 9;

	var synth 	= nil;
	
	*displayName { ^\Output }
	
	// ----------- instantiation -----------
	
	*new { arg server;
		^super.new( server ).prInitOutput;
	}
	
	protRunToBundle { arg bndl, flag;
		if( synth.notNil, {
			bndl.add( synth.runMsg( flag ));
		});
	}

	protSetMixToBundle { arg bndl, mix;
		if( synth.notNil, {
			bndl.add( synth.setMsg( \mix, mix ));
		});
	}
	
	prInitOutput {
		volumeAttr   = this.protMakeAttr( \volume,   ControlSpec( 1.5e-05, 4.0, \exp, default: 1.0 ));
		subwAttr     = this.protMakeAttr( \sub,      ControlSpec( 0, 1, \lin, default: 0.0 ));
		glassAttr    = this.protMakeAttr( \glass,    ControlSpec( 0, 1, \lin, default: 0.0 ));
//		grainAttr    = this.protMakeAttr( \grain,    ControlSpec( 0, 1, \lin, default: 0.5 ));
//		feedBackAttr = this.protMakeAttr( \feedBack, ControlSpec( 0, 1, \lin, default: 0.0 ));
	
//		this.protPrependAttr( UnitAttr( \gain, ControlSpec( -96.0, 12.0, \db, default: 0, units: " dB"), \normal, \getGain, \setGain, nil, false ));
	}
	
	// ----------- public instance methods -----------

	numAudioInputs { ^1 }
	numAudioOutputs { ^1 }
	numVisibleAudioOutputs { ^0 }
	getAudioInputName { arg idx; ^nil }
	getAudioOutputName { arg idx; ^nil }
//	isAudioInputReadOnly { arg idx = 0; ^(idx > 0) }  // default contract: left input is mangled

	isControl { ^false }
	numControlInputs { ^0 }
	numControlOutputs { ^0 }

//	play {
//		var bndl;
//
//		bndl = OSCBundle.new;
//		this.playToBundle( bndl );
//		bndl.send( server );
//	}
	
	// XXX SOLLTE IN prMakeAudioBusToBundle GEAENDERT WERDEN
	isAudioInputReadOnly { arg idx = 0; ^true }
	
	playToBundle { arg bndl;
		var inBus, outBus, defName, numInChannels, numChannels, args;

		if( playing, {
			this.stopToBundle( bndl );
			synth = nil;
		});

		this.numAudioInputs.do({ arg idx;
			inBus = this.getAudioInputBus( idx );
			if( inBus.isNil, {
				TypeSafe.methodWarn( thisMethod, "No input bus (" ++ this.name ++ ", " ++ idx ++ ")" );
				^this;
			});
			args			= args.addAll([ if( idx == 0, \in, { "in" ++ (idx+1) }), inBus.index ]);
			numInChannels	= if( idx == 0, inBus.numChannels, { numInChannels.asArray.add( inBus.numChannels )});
		});
		this.numAudioOutputs.do({ arg idx;
			outBus		= this.getAudioOutputBus( idx );
			if( outBus.notNil, {
				args			= args.addAll([ if( idx == 0, \out, { "out" ++ (idx+1) }), outBus.index ]);
				numChannels	= if( idx == 0, outBus.numChannels, { numChannels.asArray.add( outBus.numChannels )});
			}, {
				numChannels	= if( idx == 0, { this.numOutChannels( idx )}, { numChannels.asArray.add( this.numOutChannels( idx ))});
			});
		});
		
[ "AmpUOutput", args, numChannels ].postln;
		
		if( volume != 1.0, { args = args.addAll([ \volume, volume ])});
		defName	= this.protCreateDefName( numInChannels, numChannels );
		synth	= Synth.basicNew( defName, server );
		this.protNewSynthToBundle( bndl, synth, { arg defName; this.protMakeDef( defName, numInChannels , numChannels )});
		bndl.add( synth.newMsg( target, args, \addToHead ));
//		paused = if( neverPause, false, { mix == 0 });
//		this.protRunToBundle( bndl, paused.not );
//		bufs = this.protCreateBuffersToBundle( bndl, synth, inBus.numChannels, numChannels );
		UpdateListener.newFor( synth, { arg upd, node;
			upd.remove;
//			bufs.do({ arg buf; buf.close; buf.free });
			if( node === synth, {
				synth = nil;
				this.protSetPlaying( false );
			});
		}, \n_end );

		this.protSetAttrsToBundle( bndl );
//		this.protSetMixToBundle( bndl, mix );
		this.protSetPlaying( true );
	}

//	protSetAttrToBundle { arg bndl, synth;
//		// nada
//	}
	
	setPreferredNumInChannels { arg numCh;
		prefNumInChannels = numCh;
	}

	setPreferredNumOutChannels { arg numCh;
		if( numCh != prefNumOutChannels, {
			MethodError( "Number of output channels is fixed (% != %)".format( numCh, prefNumOutChannels ), thisMethod ).throw;
		});
	}

//	setOutputBusToBundle { arg bndl, bus, idx = 0;
//		numChannels = bus.numChannels;
//		^super.setOutputBusToBundle( bndl, bus, idx );
//	}

//	setAzimuth { arg az;
//		"OutputUnitNuages.setAzimuth : not working".warn;
////		azi = az;
////		synth.set( \azi, az );
//	}
//	
//	getAzimuth {
//		^azi;
//	}
//
//	setSpread { arg argSpread;
//		"OutputUnitNuages.setSpread : not working".warn;
////		spread = argSpread;
////		synth.set( \spread, spread );
//	}
//	
//	getSpread {
//		^spread;
//	}

	// ----------- protected instance methods -----------
	
	protPreferredNumInChannels { arg idx; ^prefNumInChannels }
	protPreferredNumOutChannels { arg idx; ^prefNumOutChannels }
	
	protCreateDefName { arg inChannels, outChannels;
		^"nuages-%%x%".format( this.name, inChannels, outChannels ).asSymbol;
	}
		
	protMakeDef { arg defName, inChannels, outChannels;
		^SynthDef( defName, { arg in, out;
			var pre, post, sig, normMix, glassMix, subwMix, volume, glassAmt, subwAmt;
			
			volume	= volumeAttr.kr( lag: 0.5 );
			glassAmt	= glassAttr.kr( lag: 0.5 );
			subwAmt	= subwAttr.kr( lag: 0.5 );
			
			pre		= In.ar( in, inChannels );
			post		= (pre * volume).asArray;
			sig		= Array.fill( 4, { arg ch; post[ ch % inChannels ]});
//			normMix	= (1 - glassAmt).sqrt * (1 - subwAmt).sqrt;
			normMix	= ((1 - glassAmt) * (1 - subwAmt)).sqrt;
			glassMix	= (glassAmt * (1 - subwAmt)).sqrt;
			subwMix	= subwAmt.sqrt;
			Out.ar( out, (sig * normMix) ++ (sig * glassMix) ++ [ Mix( sig ) * subwMix ]);
		});
	}

	protDuplicate { arg dup;
		dup.setPreferredNumInChannels(  prefNumInChannels );
		dup.setPreferredNumOutChannels( prefNumOutChannels );
//		dup.setAzimuth( this.getAzimuth );
//		dup.setSpread( this.getSpread );
		^super.protDuplicate( dup );
	}
}