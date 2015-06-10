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
NuagesUOutput : NuagesU {
	var synth		= nil;
//	var azi		= 0.0; // 0.001;
//	var spread	= 0.25;
	
	var prefNumInChannels	= 2;
	var prefNumOutChannels	= 2;
	
	var volumeAttr;

	// ----------- instantiation -----------
	
	*new { arg server;
		^super.new( server ).prInitOutput;
	}
	
	prInitOutput {
//		this.protPrependAttr( UnitAttr( \volume, ControlSpec( -96.0, 12.0, \db, default: 0, units: " dB"), \normal, \getGain, \setGain, nil, false ));
		volumeAttr   = this.protMakeAttr( \volume,   ControlSpec( 1.5e-05, 4.0, \exp, default: 1.0 ));
	}
	
	// ----------- public instance methods -----------

	numAudioInputs { ^1 }
	numAudioOutputs { ^1 }
	numVisibleAudioOutputs { ^0 }
	getAudioInputName { arg idx; ^nil }

	numControlInputs { ^0 }
	numControlOutputs { ^0 }

	isControl { ^false }
	
//	cmdPeriod {
//		var result;
//		
//		result		= super.cmdPeriod;
//		synth		= nil;
//		^result;
//	}

//	setGain { arg vol;
//		this.protMakeBundle({ arg bndl; this.setGainToBundle( bndl, vol )});
//	}
//	
//	setGainToBundle { arg bndl, vol;
//		this.setVolumeToBundle( bndl, vol.dbamp );
//		this.tryChanged( \attrUpdate, \gain );
//	}
//	
//	getGain { ^this.getVolume.ampdb }

	play {
		var bndl;

		bndl = OSCBundle.new;
		this.playToBundle( bndl );
		bndl.send( server );
	}
	
	playToBundle { arg bndl;
		var inBus, outBus, defName, numChannels;

		if( playing, {
			this.stopToBundle( bndl );
			synth = nil;
		});

		inBus = this.getAudioInputBus;
		if( inBus.isNil, {
			TypeSafe.methodWarn( thisMethod, "No input bus has been specified" );
			^this;
		});

		numChannels = this.numOutChannels;
		defName	= this.protCreateDefName( inBus.numChannels, numChannels );
		synth	= Synth.basicNew( defName, server );
		this.protNewSynthToBundle( bndl, synth, { arg defName; this.protMakeDef( defName, inBus.numChannels,
			numChannels )});
		outBus	= this.getAudioOutputBus;
		bndl.add( synth.newMsg( target, [ \in, inBus.index, \out, outBus.notNil.if({ outBus.index }, 0 ),
			/* \azi, azi, \spread, spread, */ \volume, volume ], \addToHead ));

		UpdateListener.newFor( synth, { arg upd, node;
			upd.remove;
			if( node === synth, {
				synth = nil;
				this.protSetPlaying( false );
			});
		}, \n_end );

		this.protSetAttrsToBundle( bndl, synth );
		this.protSetPlaying( true );
	}

//	protSetAttrToBundle { arg bndl, synth;
//		// nada
//	}
	
	setPreferredNumInChannels { arg numCh;
		prefNumInChannels = numCh;
	}

	setPreferredNumOutChannels { arg numCh;
		prefNumOutChannels = numCh;
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
			var pre, post, sig, peak, monoPeak, volume;
			volume	= volumeAttr.kr( lag: 0.5 );
			pre		= In.ar( in, inChannels );
			post		= (pre * volume).asArray;
			sig		= Array.fill( outChannels, { arg ch; post[ ch % inChannels ]});
			Out.ar( out, sig );
		}, [ nil, nil, 0.05 ]);
	}

	protDuplicate { arg dup;
		dup.setPreferredNumInChannels(  prefNumInChannels );
		dup.setPreferredNumOutChannels( prefNumOutChannels );
//		dup.setAzimuth( this.getAzimuth );
//		dup.setSpread( this.getSpread );
		^super.protDuplicate( dup );
	}
}