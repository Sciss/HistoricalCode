/**
 *	NuagesUFactory
 *	(Wolkenpumpe)
 *
 *	(C)opyright 2005-2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.15, 31-Aug-09
 *	@author	Hanns Holger Rutz
 */
NuagesUFactory {
	var <pompe;
	var audioGenerators, audioFilters, audioOutputs;
	var controlGenerators, controlFilters;
	var initializers, prefAudioOutput;

	*new { arg pompe;
		^super.new.prInit( pompe );
	}
	
	prInit { arg argPompe;
		pompe			= argPompe;
		audioGenerators	= [ NuagesUTape, NuagesULoop, NuagesUMic ];
		audioFilters		= [ NuagesUFreqFilter, NuagesUGain, NuagesUPitch, NuagesUFragment, 
		   NuagesUGendy, NuagesUMagAbove, NuagesUMagBelow, NuagesUAchilles,
		   NuagesUAutoGate, NuagesUAutoHilbert, NuagesURenoise, NuagesUSqrt,
		   NuagesUReverb, NuagesUConvolve, NuagesUHilbert, NuagesUZero ];
		audioOutputs		= []; // [ NuagesUOutput ];
		controlGenerators	= [ NuagesUCOsc, NuagesUCNoise ];
		initializers	= IdentityDictionary.new;
	}
	
	audioGenerators { ^audioGenerators.copy }
	audioFilters { ^audioFilters.copy }
	audioOutputs { ^audioOutputs.copy }
	
	controlGenerators { ^controlGenerators.copy }
	controlFilters { ^controlFilters.copy }
	
	addAudioGenerator { arg gen, init;
		audioGenerators = audioGenerators.add( gen );
		if( init.notNil, { initializers.put( gen.name, init )});
	}

	addAudioFilter { arg flt, init;
		audioFilters = audioFilters.add( flt );
		if( init.notNil, { initializers.put( flt.name, init )});
	}
	
	addControlGenerator { arg gen, init;
		controlGenerators = controlGenerators.add( gen );
		if( init.notNil, { initializers.put( gen.name, init )});
	}

	addControlFilter { arg flt, init;
		controlFilters = controlFilters.add( flt );
		if( init.notNil, { initializers.put( flt.name, init )});
	}
	
	addAudioOutput { arg out, init;
		audioOutputs = audioOutputs.add( out );
		if( init.notNil, { initializers.put( out.name, init )});
	}
	
	preferredAudioOutput_ { arg clazz;
		prefAudioOutput = clazz;
	}
	
	preferredAudioOutput {
		^(prefAudioOutput ?? { audioOutputs[ audioOutputs.size.rand ]});
	}
	
	makeUnit { arg clazz, metaData;
		var unit, name, init;

		TypeSafe.checkArgClass( thisMethod, clazz, Class, false );
		
		unit = clazz.new( pompe.server );
		name = clazz.name;

		switch( name,
		\NuagesUTape, {
			pompe.tapeCues.do({ arg cue;	// XXX
				unit.addCue( cue, nil );
			});
			unit.setCueIndex( if( pompe.tapeCueVariation >= 0, pompe.tapeCueVariation,
				{ pompe.tapeCues.size.rand }));
//			unit.loopAll;
		},
		\NuagesULoop, {
			unit.setNumFrames( (pompe.server.sampleRate * 30).asInteger );
			unit.setNowNumFrames( unit.getNumFrames );
//			unit.setNumChannels( 2 );	// XXX
			unit.setPreferredNumOutChannels( 2 );
		},
		\NuagesUMic, {
			unit.addMic( Bus( \audio, pompe.server.options.numOutputBusChannels, 2 ));
//			unit.addMic( Bus( \audio, pompe.server.options.numOutputBusChannels + 6, 2 ));
			unit.setMicIndex( 0 );
		},
//		\NuagesUPitch, {
//			unit.setPitchDispersion( 0.1 );
//			unit.setTimeDispersion( 0.1 );
//		},
		\NuagesUOutput, {
			unit.setPreferredNumOutChannels( Wolkenpumpe.masterNumChannels );
//			[ "metaData", metaData ].postln;
//			if( metaData.isKindOf( NuagesProc ) and: { metaData.unit.isKindOf( NuagesUMic )}, {
//				unit.setGain( -96 );
//			});
		});

		init = initializers[ name ];
		if( init.notNil, {
			init.value( unit, metaData );
		});

		if( unit.isKindOf( NuagesUFilter ), {
			unit.setMix( 0.0 );
		});
		
		^unit;
	}
}