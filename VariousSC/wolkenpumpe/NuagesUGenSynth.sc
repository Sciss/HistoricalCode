/**
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: GeneratorUnit, TypeSafe
 *
 *	Changelog:
 *
 *	@version	0.13, 26-Aug-09
 *	@author	Hanns Holger Rutz
 */
NuagesUGenSynth : NuagesUGen {
	var synth = nil;

	playToBundle { arg bndl;
		var inBus, outBus, defName, numInChannels, numChannels, args, bufs;

		if( playing, {
			this.stopToBundle( bndl );
			synth = nil;
		});

		this.numAudioInputs.do({ arg idx;
			inBus = this.getAudioInputBus( idx );
			if( inBus.isNil, {
				TypeSafe.methodWarn( thisMethod, "No input bus has been specified" );
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
		
		this.numControlInputs.do({ arg idx;
			inBus = this.getControlInputBus( idx );
			if( inBus.isNil, {
				TypeSafe.methodWarn( thisMethod, "No input bus has been specified" );
				^this;
			});
			args			= args.addAll([ if( idx == 0, \kin, { "kin" ++ (idx+1) }), inBus.index ]);
//			numInChannels	= if( idx == 0, inBus.numChannels, { numInChannels.asArray.add( inBus.numChannels )});
		});
		this.numControlOutputs.do({ arg idx;
			outBus			= this.getControlOutputBus( idx );
			if( outBus.notNil, {
				args			= args.addAll([ if( idx == 0, \kout, { "kout" ++ (idx+1) }), outBus.index ]);
//				numChannels	= if( idx == 0, outBus.numChannels, { numChannels.asArray.add( outBus.numChannels )});
//			}, {
//				numChannels	= if( idx == 0, { this.numChannels( idx )}, { numChannels.asArray.add( this.numChannels( idx ))});
			});
		});
		
		if( volume != 1.0, { args = args.addAll([ \volume, volume ])});
		defName	= this.protCreateDefName( numInChannels, numChannels );
		synth	= Synth.basicNew( defName, server );
		this.protNewSynthToBundle( bndl, synth, { arg defName; this.protMakeDef( defName, numInChannels, numChannels )});
		bndl.add( synth.newMsg( target, args, \addToHead ));
//		paused = if( neverPause, false, { mix == 0 });
//		this.protRunToBundle( bndl, paused.not );
		bufs = this.protCreateBuffersToBundle( bndl, synth, inBus.numChannels, numChannels );
		UpdateListener.newFor( synth, { arg upd, node;
			upd.remove;
			bufs.do({ arg buf; buf.close; buf.free });
			if( node === synth, {
				synth = nil;
				this.protSetPlaying( false );
			});
		}, \n_end );

		this.protSetAttrsToBundle( bndl, synth );
//		this.protSetMixToBundle( bndl, mix );
		this.protSetPlaying( true );
	}

//	protSetAttrToBundle { arg bndl, synth;
//		^this.subclassResponsibility( thisMethod );
//	}

	protCreateBuffersToBundle { arg bndl, synth, numInChannels, numChannels;
		^nil;
	}
	
//	setOutputBusToBundle { arg bndl, bus, idx = 0;
//		numChannels	= bus.numChannels;
//		^super.setOutputBusToBundle( bndl, bus, idx );
//	}
	
	protMakeDef { arg defName, numInChannels, numChannels;
		^this.subclassResponsibility( thisMethod );
	}
}


// damn... scala's mixins would be useful...
NuagesUGenAudioSynth : NuagesUGenSynth {
	numAudioOutputs { ^1 }
	getAudioOutputName { arg idx; ^nil }

	numControlOutputs { ^0 }
	isControl { ^false }
}

NuagesUGenControlSynth : NuagesUGenSynth {
	numControlOutputs { ^1 }
	getControlOutputName { arg idx; ^nil }

	numAudioOutputs { ^0 }
	protPreferredNumOutChannels { arg idx; ^0 }
	isControl { ^true }
}
