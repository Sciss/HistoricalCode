/**
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies:
 *
 *	Changelog:
 *
 *	@version	0.28, 26-Aug-09
 *	@author	Hanns Holger Rutz
 */ 
NuagesUFilterSynth : NuagesUFilter {
	var synth 	= nil;
	
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
	
	playToBundle { arg bndl;
		var inBus, outBus, defName, numInChannels, numChannels, args, bufs;

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
		if( volume != 1.0, { args = args.addAll([ \volume, volume ])});
		defName	= this.protCreateDefName( numInChannels, numChannels );
		synth	= Synth.basicNew( defName, server );
		this.protNewSynthToBundle( bndl, synth, { arg defName; this.protMakeDef( defName, numInChannels , numChannels )});
		bndl.add( synth.newMsg( target, args, \addToHead ));
		paused = if( neverPause, false, { mix == 0 });
		this.protRunToBundle( bndl, paused.not );
		bufs = this.protCreateBuffersToBundle( bndl, synth, inBus.numChannels, numChannels );
		UpdateListener.newFor( synth, { arg upd, node;
			upd.remove;
			bufs.do({ arg buf; buf.close; buf.free });
			if( node === synth, {
				synth = nil;
				this.protSetPlaying( false );
			});
		}, \n_end );

		this.protSetAttrsToBundle( bndl );
		this.protSetMixToBundle( bndl, mix );
		this.protSetPlaying( true );
	}
		
	protCreateBuffersToBundle { arg bndl, synth, numInChannels, numChannels;
		^nil;
	}
	
//	setInputBusToBundle { arg bndl, bus, idx = 0;
//		var outBus;
//		
//		outBus = this.getAudioOutputBus;
//		
//		if( outBus.notNil and: { outBus.numChannels != bus.numChannels }, {
//			TypeSafe.methodError( thisMethod, "Input and output channels cannot be different" );
//			^this;
//		});
//		numChannels = bus.numChannels;
//		^super.setInputBusToBundle( bndl, bus, idx );
//	}
//	
//	setOutputBusToBundle { arg bndl, bus, idx = 0;
//		var inBus;
//		
//		inBus = this.getAudioInputBus;
//		
//		if( inBus.notNil and: { inBus.numChannels != bus.numChannels }, {
//			TypeSafe.methodError( thisMethod, "Input and output channels cannot be different" );
//			^this;
//		});
//		numChannels = bus.numChannels;
//		^super.setOutputBusToBundle( bndl, bus, idx );
//	}
	
	protMakeDef { arg defName, numInChannels, numChannels;
		^this.subclassResponsibility( thisMethod );
	}
	
	/**
	 *	@param	numChannels	if not nil, the number of channels will be
	 *						adjusted to match this amount of channels
	 */
	protCreateXOut { arg flt, numChannels;
		var out, mix, pan, w, ch2;
		out = Control.names( \out ).kr( 0 );
		mix = LagControl.names( \mix ).kr( 0.0, 0.1 );
		flt = flt.asArray;
		if( numChannels.notNil and: { numChannels != flt.size }, {
			pan = 0.0.dup( numChannels );
			flt.do({ arg sig, ch;
				ch	= ch.linlin( 0, flt.size - 1, 0, numChannels - 1 );
				w	= ch % 1.0;
				if( w == 0, {
					ch = ch.asInteger;
					pan[ ch ] = pan[ ch ] + sig;
				}, {
					ch2	= ch.ceil.asInteger;
					ch	= ch.floor.asInteger;
					pan[ ch ]  = pan[ ch ]  + (sig * (1.0 - w).sqrt);
					pan[ ch2 ] = pan[ ch2 ] + (sig * w.sqrt);
				});
			});
		});
//[ "protCreateXOut", out, mix, pan, flt ].postln;
		XOut.ar( out, mix, pan ? flt );
	}
}