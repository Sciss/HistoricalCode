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
GendyUnit : GeneratorUnit {
	var defName	= nil;
	var synth		= nil;
	var amount	= 1.0;
	
	classvar <allUnits;				// elem = instances of GendyUnit

	var attributes;

	*initClass {
		allUnits	= IdentitySet.new;
	}

	*new { arg server;
		^super.new( server ).prInitGendyUnit;
	}
	
	prInitGendyUnit {
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
		defName		= ("gendyUnit" ++ numChannels).asSymbol;
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
		defName		= ("gendyUnit" ++ numChannels).asSymbol;
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
		defName		= ("gendyUnit" ++ numChannels).asSymbol;
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
		defName	= ("gendyUnit" ++ numChannels).asSymbol;
		if( defCache.contains( defName ).not, {
			def = SynthDef( defName, { arg in, out, amount;
				var ins, outs, laggo, minFreq, scale;
				
				ins		= In.ar( in, numChannels );
				laggo	= Lag.kr( amount );
				minFreq	= laggo * 69 + 12;
				scale	= laggo * 13 + 0.146;
				outs		= Gendy1.ar( 2, 3, 1, 1,
							minfreq: minFreq, maxfreq: minFreq * 8,
							ampscale: scale, durscale: scale,
							initCPs: 7, mul: ins );
				outs		= Compander.ar( outs, outs, 0.7, 1, 0.1, 0.001, 0.02 );
				ReplaceOut.ar( out, outs );
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