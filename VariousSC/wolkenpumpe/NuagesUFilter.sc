/**
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: GeneratorUnit, TypeSafe
 *
 *	Changelog:
 *
 *	@version	0.14, 16-Apr-09
 *	@author	Hanns Holger Rutz
 */
NuagesUFilter : NuagesU {
	var mix				= 1.0;
	var clpsePause, paused = false;
	
	var neverPause		= false;

	// ----------- instantiation -----------
	
	*new { arg server;
		^super.new( server ).prInitFilter;
	}
	
	prInitFilter {
	
		this.protPrependAttr( UnitAttr( \mix, ControlSpec( 0, 1, default: 1 ), \normal, \getMix, \setMix, nil, false ));
		clpsePause = Collapse({ var bndl;
			bndl = OSCBundle.new;
			this.protRunToBundle( bndl, false );
			bndl.send( server );
			paused = true;
		}, 0.2 );
		
		this.init;
	}
	
	init {}

	// ----------- public instance methods -----------

	numAudioInputs { ^1 }
	numAudioOutputs { ^1 }
	getAudioInputName { arg idx; ^nil }
	getAudioOutputName { arg idx; ^nil }
	isAudioInputReadOnly { arg idx = 0; ^(idx > 0) }  // default contract: left input is mangled

	isControl { ^false }		// QQQ XXX
	numControlInputs { ^0 }		// QQQ XXX
	numControlOutputs { ^0 }	// QQQ XXX

	disposeToBundle { arg ... args;
		clpsePause.cancel;
		^super.disposeToBundle( *args );
	}
	
	getMix { ^mix }
	
	setMix { arg value;
		this.protMakeBundle({ arg bndl; this.setMixToBundle( bndl, value )});
	}
	
	setMixToBundle { arg bndl, value;
		if( value != mix, {
			mix = value;
//			bndl = OSCBundle.new;
			if( mix == 0, {
				this.protSetMixToBundle( bndl, mix );
				if( neverPause.not, { clpsePause.reschedule });
			}, {
				clpsePause.cancel;
				if( paused, {
					this.protRunToBundle( bndl, true );
					this.protSetMixToBundle( bndl, mix );
					paused = false;
				}, {
					this.protSetMixToBundle( bndl, mix );
				});
			});
//			bndl.send( server );
			this.tryChanged( \attrUpdate, \mix );
		});
	}
	
	// ----------- protected instance methods -----------
	
	protPreferredNumInChannels { arg idx;
		var bus;
		bus = this.getAudioOutputBus;
		^if( bus.notNil, { bus.numChannels }, 1 );
	}

	protPreferredNumOutChannels { arg idx;
		var bus, result = 1;
		this.numAudioInputs.do({ arg ch;
			bus = this.getAudioInputBus( ch );
			if( bus.notNil, { result = max( result, bus.numChannels )});
		});
		^result;
	}
	
	protRunToBundle { arg bndl, flag;
		^this.subclassResponsibility( thisMethod );
	}

	protSetMixToBundle { arg bndl, mix;
		^this.subclassResponsibility( thisMethod );
	}

	protDuplicate { arg dup;
		dup.prSetMix( this.getMix );
		^super.protDuplicate( dup );
	}

	// ----------- private instance methods -----------
	
	prSetMix { arg value;
		mix = value;
	}
}