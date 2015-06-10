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
NuagesUBasicMic : NuagesUGenAudioSynth {
	var mics;
	var micIdx	= nil;
//	var micBus;
	var boost		= 1.0;

	*new { arg server;
		^super.new( server ).prInitBasicMic;
	}
	
	prInitBasicMic {
		this.protPrependAttr( UnitAttr( \boost, ControlSpec( 0.1, 10, \exp ), \normal, \getBoost, \setBoost, nil, false ));
	}
	
	numAudioInputs { ^1 }
	numVisibleAudioInputs { ^0 }

	addMic { arg bus;
		var file, name;
//		this.protCacheDef( bus.numChannels );
		mics = mics.add( bus );
//		micAttrDirty = true;
	}
	
	prSetMics { arg micList;
		mics = micList.copy;
//		micAttrDirty = true;
	}
	
	setMicIndex { arg idx;
		var mic;
		
		mic = mics[ idx ];
		if( mic.notNil, {
			this.setMic( mic );
			micIdx = idx;
		});
	}
	
	getMicIndex {
		^micIdx;
	}
	
	getNumMics {
		^mics.size;
	}

	setBoost { arg vol;
		this.protMakeBundle({ arg bndl; this.setBoostToBundle( bndl, vol )});
	}
	
	setBoostToBundle { arg bndl, vol;
		if( vol != boost, {
			boost = vol;
			if( synth.notNil, {
				bndl.add( synth.setMsg( \boost, vol ));
			});
			this.tryChanged( \attrUpdate, \boost );
		});
	}
	
	getBoost {
		^boost;
	}
	
	setMic { arg bus;
//[ "setMic", bus ].postln;
		this.setAudioInputBus( bus );
//	
//		var wasPlaying;
//	
//		wasPlaying = playing;
//		if( wasPlaying, { this.stop });
////		this.protCacheDef( bus.numChannels );
//		micBus		= bus;
//		numChannels	= micBus.numChannels;
////		defName		= ("micUnit" ++ numChannels ++ if( feedback, "fb", "" )).asSymbol;
//		if( wasPlaying, { this.play; });
	}
	
	protSetAttrToBundle { arg bndl, synth;
//		bndl.add( synth.setMsg( \channel, micBus.index, \boost, boost ));
	}

	protPreferredNumOutChannels { arg idx;
		^this.numInChannels( idx );
	}

	protMakeDef { arg defName, numInChannels, numChannels;
		^SynthDef( defName, {
			arg out, in, boost = 1.0;
			var ins, pureIn;
			
			pureIn	= this.protCreateIn( in, numInChannels, numChannels ) * boost;
			Out.ar( out, pureIn.wrapExtend( numChannels ));
		});
	}

	protDuplicate { arg dup;
		dup.prSetMics( mics );
		dup.setBoost( this.getBoost );
		dup.setMicIndex( this.getMicIndex );
		^super.protDuplicate( dup );
	}
}