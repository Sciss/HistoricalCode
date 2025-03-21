/**
 *	(C)opyright 2006 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: GeneratorUnit, TypeSafe
 *
 *	Changelog:
 *
 *	@version	0.1, 19-Jul-06
 *	@author	Hanns Holger Rutz
 */
SqrtUnit : GeneratorUnit {
	var defName	= nil;
	var synth		= nil;
	var amount	= 1.0;
	
	classvar <allUnits;				// elem = instances of SqrtUnit

	var attributes;

	*initClass {
		allUnits	= IdentitySet.new;
	}

	*new { arg server;
		^super.new( server ).prInitSqrtUnit;
	}
	
	prInitSqrtUnit {
		allUnits.add( this );

		attributes		= [
			UnitAttr( \amount, ControlSpec( 0, 1, \lin ), \normal, \getAmount, \setAmount, nil )
		];
	}
	
	cmdPeriod {
		var result;
		
		result		= super.cmdPeriod;
		synth		= nil;
		^result;
	}

	getAttributes {
		^attributes;
	}

	play {
		var bndl;

		bndl = List.new;
		this.playToBundle( bndl );
		server.listSendBundle( nil, bndl );
	}
	
	playToBundle { arg bndl;
		var inBus, outBus;

		if( playing, {
			this.stop;
			if( synth.notNil, {�this.protRemoveNode( synth );});
			synth = nil;
		});

		inBus = this.getInputBus;
		if( inBus.notNil, {
			synth	= Synth.basicNew( defName, server );
			outBus	= this.getOutputBus;
			bndl.add( synth.newMsg( target, [�\in, inBus.index, \out, outBus.notNil.if({ outBus.index }, 0 ),
				\amount, amount ], \addToHead ));
			this.protAddNode( synth );
			this.protSetPlaying( true );
		}, {
			TypeSafe.methodWarn( thisMethod, "No input bus has been specified" );
		});
	}

	setInputBus { arg bus;
		var outBus;
		
		outBus = this.getOutputBus;
		
		if( outBus.notNil and: {�outBus.numChannels != bus.numChannels }, {
			TypeSafe.methodError( thisMethod, "Input and output channels cannot be different" );
			^this;
		});

		numChannels	= bus.numChannels;
		defName		= ("sqrtUnit" ++ numChannels).asSymbol;
		this.prCacheDef( numChannels );
		^super.setInputBus( bus );
	}

	setOutputBusToBundle { arg bndl, bus;
		var inBus;
		
		inBus = this.getInputBus;
		
		if( inBus.notNil and: {�inBus.numChannels != bus.numChannels }, {
			TypeSafe.methodError( thisMethod, "Input and output channels cannot be different" );
			^this;
		});
		numChannels	= bus.numChannels;
		defName		= ("sqrtUnit" ++ numChannels).asSymbol;
		this.prCacheDef( numChannels );
		^super.setOutputBusToBundle( bndl, bus );
	}

	setOutputBus { arg bus;
		var inBus;
		
		inBus = this.getInputBus;
		
		if( inBus.notNil and: {�inBus.numChannels != bus.numChannels }, {
			TypeSafe.methodError( thisMethod, "Input and output channels cannot be different" );
			^this;
		});
		numChannels	= bus.numChannels;
		defName		= ("sqrtUnit" ++ numChannels).asSymbol;
		this.prCacheDef( numChannels );
		^super.setOutputBus( bus );
	}

	// note: negative freqs = lpf, positive = hpf
	setAmount { arg amt;
		amount = amt.clip( 0, 1 );
		synth.set( \amount, amount );
	}
	
	getAmount {
		^amount;
	}

	dispose {
		allUnits.remove( this );
		^super.dispose;
	}
	
	*disposeAll {
		var all;
		
		all = List.newFrom( allUnits );
		all.do({ arg unit; unit.dispose; });
	}

	prCacheDef { arg numChannels;
		var defName, def;
		defName	= ("sqrtUnit" ++ numChannels).asSymbol;
		if( defCache.contains( defName ).not, {
			def = SynthDef( defName, { arg in, out, amount = 1.0;
				var inp, hlb, hlb2, flt, dly;

				inp		= In.ar( in, numChannels ).asArray;
//				flt		= Normalizer.ar( LeakDC.ar( inp.abs.sqrt ), inp, 0.05 );
				flt		= inp.abs.sqrt;
				flt		= LeakDC.ar( flt ) * Amplitude.kr( flt );
//				dly		= DelayN.ar( inp, 0.1 );
dly = inp;

				ReplaceOut.ar( out, (dly * (1.0 - amount)) + (flt * amount) );
			}, [ nil, nil, 0.1 ]);
			defCache.add( def );
		});
	}

	n_end { arg node;
		if( node === synth, {
			synth		= nil;
			this.protSetPlaying( false );
		});
		^super.n_end( node );
	}

	protDuplicate {�arg dup;
		dup.setAmount( this.getAmount );
	}
}