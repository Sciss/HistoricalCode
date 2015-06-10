/**
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: GeneratorUnit, TypeSafe
 *
 *	Changelog:
 *
 *	@version	0.11, 26-Apr-09
 *	@author	Hanns Holger Rutz
 */
NuagesUCNoise : NuagesUGenControlSynth {
	var wave		= 0;
	var freq		= 1.0;
	var curve		= 1.0;
	var low		= 0.0;
	var high		= 1.0;

	*new { arg server;
		^super.new( server ).prInitCNoise;
	}
	
	prInitCNoise {
		this.protPrependAttr(
			UnitAttr( \freq, ControlSpec( 0.01, 100, \exp ), \normal, \getFreq, \setFreq, nil, false ),
			UnitAttr( \wave, ControlSpec( 0, 2, \lin ), \normal, \getWave, \setWave, nil, false ),
			UnitAttr( \curve, ControlSpec( 0.25, 4, \exp ), \normal, \getCurve, \setCurve, nil, false ),
			UnitAttr( \low, ControlSpec( 0, 1, \lin ), \normal, \getLow, \setLow, nil, false ),
			UnitAttr( \high, ControlSpec( 0, 1, \lin ), \normal, \getHigh, \setHigh, nil, false )
		);
	}
	
	setLow { arg value;
		this.protMakeBundle({ arg bndl; this.setLowToBundle( bndl, value )});
	}
	
	setLowToBundle { arg bndl, value;
		if( value != low, {
			low = value;
			if( synth.notNil, {
				bndl.add( synth.setMsg( \low, value ));
			});
			this.tryChanged( \attrUpdate, \low );
		});
	}
	
	getLow {
		^low;
	}
	
	setHigh { arg value;
		this.protMakeBundle({ arg bndl; this.setHighToBundle( bndl, value )});
	}
	
	setHighToBundle { arg bndl, value;
		if( value != high, {
			high = value;
			if( synth.notNil, {
				bndl.add( synth.setMsg( \high, value ));
			});
			this.tryChanged( \attrUpdate, \high );
		});
	}
	
	getHigh {
		^high;
	}
	
	setFreq { arg f;
		this.protMakeBundle({ arg bndl; this.setFreqToBundle( bndl, f )});
	}
	
	setFreqToBundle { arg bndl, f;
		if( f != freq, {
			freq = f;
			if( synth.notNil, {
				bndl.add( synth.setMsg( \freq, f ));
			});
			this.tryChanged( \attrUpdate, \freq );
		});
	}
	
	getFreq {
		^freq;
	}

	setWave { arg w;
		this.protMakeBundle({ arg bndl; this.setWaveToBundle( bndl, w )});
	}
	
	setWaveToBundle { arg bndl, w;
		if( w != wave, {
			wave = w;
			if( synth.notNil, {
				bndl.add( synth.setMsg( \wave, w ));
			});
			this.tryChanged( \attrUpdate, \wave );
		});
	}
	
	getWave {
		^wave;
	}

	setCurve { arg value;
		this.protMakeBundle({ arg bndl; this.setCurveToBundle( bndl, value )});
	}
	
	setCurveToBundle { arg bndl, value;
		if( value != curve, {
			curve = value;
			if( synth.notNil, {
				bndl.add( synth.setMsg( \curve, value ));
			});
			this.tryChanged( \attrUpdate, \curve );
		});
	}
	
	getCurve {
		^curve;
	}

	protSetAttrToBundle { arg bndl, synth;
		bndl.add( synth.setMsg( \freq, freq, \wave, wave, \curve, curve, \low, low, \high, high ));
	}
	
	protMakeDef { arg defName, numInChannels, numChannels;
		^SynthDef( defName, {
			arg kout, freq = 1.0, wave = 0, low = 0, high = 1, curve = 1;
			var cubic, linear, step, sig;
			
			cubic	= LFDNoise3.kr( freq );
			linear	= LFDNoise1.kr( freq );
			step		= LFDNoise0.kr( freq );
			
			sig		= (SelectX.kr( wave, [ cubic, linear, step ]) * 0.5 + 0.5).pow( curve );
			sig		= LinLin.kr( sig, 0, 1, low, high );
			
			Out.kr( kout, sig );
		});
	}

	protDuplicate { arg dup;
		dup.setFreq( this.getFreq );
		dup.setWave( this.getWave );
		dup.setCurve( this.getCurve );
		dup.setLow( this.getLow );
		dup.setHigh( this.getHigh );
		^super.protDuplicate( dup );
	}
}
