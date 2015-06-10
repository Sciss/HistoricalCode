AmpUGlassOut : NuagesUOutput {
//	var prefNumOutChannels	= 16;

	*displayName { ^\GlassOut }
	
	protPreferredNumInChannels { arg idx; ^16 }
	protPreferredNumOutChannels { arg idx; ^16 }
	
	setPreferredNumOutChannels { arg numCh;
		if( numCh != prefNumOutChannels, {
			MethodError( "Number of output channels is fixed (% != %)".format( numCh, prefNumOutChannels ), thisMethod ).throw;
		});
	}

	preferredPlayBus {
		^Bus( \audio, Wolkenpumpe.default.masterBus.index, 16 );
	}
	
	hasPreferredNumInChannels { ^true }

//	protMakeDef { arg defName, numInChannels, numChannels;
//[ "GLASS_OUT", numInChannels, numChannels ].postln;
//		^super.protMakeDef( defName, numInChannels, numChannels );
//	}
}