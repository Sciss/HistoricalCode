/**
 *	NuagesProc
 *	(Wolkenpumpe)
 *
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: UpdateListener, TypeSafe, SynthDefCache
 *
 *	Kind of a replacement for wolkenpumpe 2005 SynthProxy
 *	New version for Wolkenpumpe IV
 *
 *	Changelog:
 *		12-Aug-08  created from PingProc
 *
 *	@version	0.20, 02-Aug-09
 *	@author	Hanns Holger Rutz
 *
 *	@todo	when stop is called and the fadein not yet completed, the fade level should not jump
 *			; therefore : maybe just afford one fade synth playing all the time with gated env
 *	@todo	use SynthDefCache
 *	@todo	lazy tempAudioBus creation (optional)
 */
NuagesProc {
	classvar fadeShapes;

	var <>verbose 	= false;

	var <nuages, <server, <unit;
	
	var audioOutBusses;
	var madeAudioOutBusses;			// Array of Symbol (re: audioOutBusses), where values are \no, \yes, \alias
	var audioInBusses;
	var madeAudioInBusses;			// Array of Symbol (re: audioInBusses), where values are \no, \yes, \alias

	var proxyControlBus;
	var tempControlBus;
	var weMadeProxyControlBus = false;	// re: proxyControlBus
	
	var <group, <playGroup1, <playGroup2, fadeSynths;
	
	var <>fadeTime 	= 0.1;

//	var mapPlayBusToMonitor;		// Bus: to which to play, to Group: group inside playGroup
	var mapPlayBusToGroup;			// Bus: to which to play, to Group: group inside playGroup

	var unitAttrMap;				// IdentityDictionary
	
	var pending		= false;
//	var pendingUnit;
	var pendingAttr;				// IdentityDictionary
	var pendingFadeTime;			// Number
	var pendingFadeType;			// Symbol: either of \none, \lin, \eqp
	var pendingDispose;			// Boolean

	var defWatcher;

	var neverPause	= false;
	
	var eqPFadeDefName, linFadeDefName;
	
	var mapAttrName;		// Symbol: name of attr which is mapped, to Array[ Attr, Synth, Bus ]: mapped

	*initClass {
		fadeShapes	= IdentityDictionary.new;
		fadeShapes.put( \lin, 1 );
		fadeShapes.put( \sin, 3 );
		fadeShapes.put( \eqp, 4 );	// welch
	}

	// -------------- instantiation --------------

	*new { arg nuages;
		var bndl, result;

		bndl		= nuages.newBundle;
		result	= this.newToBundle( bndl, nuages );
		bndl.send;
		^result;
	}
	
	*newToBundle { arg bndl, nuages;
		^super.new.prInitNuagesProc( bndl, nuages );
	}
	
	asString {
		^("NuagesProc( unit: " ++ unit ++ " )");
	}
	
	isFading { ^fadeSynths.notNil }

	prInitNuagesProc { arg bndl, argNuages;
		TypeSafe.checkArgClasses( thisMethod, [ bndl, argNuages ], [ OSCBundle, Wolkenpumpe ], [ false, false ]);

		nuages				= argNuages;
		server				= nuages.server;
//		audioOutBusses		= [];
//		madeAudioOutBusses		= [];
//		audioInBusses			= [];
//		madeAudioInBusses		= [];
		defWatcher			= NuagesSynthDefWatcher.newFrom( server );
		unitAttrMap			= IdentityDictionary.new;
		mapAttrName			= IdentityDictionary.new;
	
		this.prInitToBundle( bndl );
	}
	
	// duplicates unit and attribute map ;
	// does not duplicate busses!
	// does not duplicate ctrl mappings!
	duplicate {
		var dup;
		
		dup = this.class.new( nuages );
		dup.setUnit( this.unit.duplicate );
		dup.prAddAttr( unitAttrMap );
		dup.setNeverPausing( this.isNeverPausing );
		^dup;
	}

	prAddAttr { arg attr;
		unitAttrMap.putAll( attr );
	}
	
	// -------------- public class methods --------------

	// -------------- public instance methods --------------

	addControlMap { arg attr, srcProc;
		var bndl;
		
		bndl = this.newBundle;
		this.addControlMapToBundle( bndl, attr, srcProc );
		bndl.send;
	}

	addControlMapToBundle { arg bndl, attr, srcProc;
		var defName, warpName, synth, inBus, outBus, upd2, clpseWatcher;
		
		TypeSafe.checkArgClasses( thisMethod, [ bndl,      attr,     srcProc ],
		                                      [ OSCBundle, UnitAttr, NuagesProc ],
		                                      [ false,     false,    false ]);
		         
		if( mapAttrName.includes( attr.name ), {
			TypeSafe.methodError( thisMethod, "Already mapped: " ++ attr.name );
			^this;
		});
		
		inBus = srcProc.unit.getControlOutputBus;
		if( inBus.isNil, {
			TypeSafe.methodError( thisMethod, "Source proc does not provide output kbus: " ++ srcProc );
			^this;
		});
		outBus = Bus.control( server );
		if( outBus.isNil, {
			TypeSafe.methodError( thisMethod, "Ran out of kbusses" );
			^this;
		});
		
		warpName	= attr.spec.warp.asSpecifier;
		defName	= ("nuages-kspec" ++ warpName).asSymbol;
		synth	= Synth.basicNew( defName, server ).register;
		if( defWatcher.isOnline( defName ).not, {
			// note: works only with LinearWarp, CurveWarp, ExponentialWarp,
			// but _not_ (Db)FaderWarp due to "if" statements that don't
			// transform into proper UGens!
			if( warpName.isNumber.not and: {[ \lin, \linear, \exp ].includes( warpName ).not }, {
				TypeSafe.methodError( thisMethod, "Illegal Warp Type: " ++ warpName );
				^this;
			});
			defWatcher.sendToBundle( bndl, SynthDef( defName, {
				arg i_kin, i_kout, i_start, i_atk, i_cmin = 0, i_cmax = 1, i_cstep = 0;
				var krSpec, inp, mapped, faded;
				krSpec	= ControlSpec( i_cmin, i_cmax, warpName, i_cstep );
				inp		= In.kr( i_kin );
				mapped	= krSpec.map( inp );
				faded	= LinXFade2.kr( i_start, mapped, Line.kr( -1, 1, i_atk ));
				Out.kr( i_kout, faded );
			}), synth );
		});
		
		// note: we add it to the main group, _not_ to the playGroups.
		// this way, the mapping synths stay alive even during unit crossfades
		// etc.; the synth and the corresponding bus will be freed when
		// the proc is disposed, or the source proc
		bndl.add( synth.newMsg( group, [ \i_kin, inBus.index, \i_kout, outBus.index, \i_start,
			attr.getValue( this.unit ), \i_atk, this.fadeTime, \i_cmin, attr.spec.minval,
			\i_cmax, attr.spec.maxval, \i_cstep, attr.spec.step ]));
		unit.addControlMapToBundle( bndl, attr, outBus );
		
		upd2 = UpdateListener.newFor( srcProc, { arg upd;
			upd.remove;
			forkIfNeeded {Êthis.removeControlMap( attr )};  // this implies that the synth upd is invoked
		}, \disposed );
		
//		addTime = SystemClock.seconds;
		clpseWatcher = Collapse({
			nuages.addControlMapWatcher( outBus, { arg val;
				// this way the GUI reflects the current mapped values
				this.tryChanged( \unitAttrUpdate, attr.name, val, attr.spec.unmap( val ));
			});
		}, server.latency, SystemClock ).defer;
		
		UpdateListener.newFor( synth, { arg upd, node;
			upd.remove;
			upd2.remove; // don't care any more for srcProc
			
			forkIfNeeded {
				mapAttrName.removeAt( attr.name ); // so we don't call n_free again
				// the bus-freeing should be scheduled with the removeControlMap OSC bundle...
				// ... for less complexity, just schedule it on the language side
				// (instead of using like a sync / synced construct)
//				SystemClock.sched( server.latency, {
					outBus.free;	// YYY
//				});
				clpseWatcher.cancel;
				nuages.removeControlMapWatcher( outBus );
				this.removeControlMap( attr );
				this.tryChanged( \unmapped, attr );
			};
		}, \n_end );
		
		mapAttrName.put( attr.name, [ attr, synth, outBus ]);
		this.tryChanged( \mapped, attr, srcProc );
	}

	prAddControlMapsToBundle { arg bndl, unit;
		var attr, synth, outBus;
		mapAttrName.do({ arg value;
			#attr, synth, outBus = value;
			unit.addControlMapToBundle( bndl, attr, outBus );
		});
	}
	
	removeControlMap { arg attr;
		var bndl;
		
		bndl = this.newBundle;
		this.removeControlMapToBundle( bndl, attr );
		bndl.send;
	}
	
	// (note: the real functionality appears in
	//  the UpdateListener in addControlMapToBundle)
	removeControlMapToBundle { arg bndl, attr;
		var value, attr2, synth, outBus;
		
		value = mapAttrName.removeAt( attr.name );
		if( value.notNil, {
			#attr2, synth, outBus = value;
			bndl.add( synth.freeMsg );  // this implies freeing the associated bus
		});
		if( unit.notNil, {
			unit.removeControlMapToBundle( bndl, attr );
		});
	}
	
	isControlMapped { arg attr;
		TypeSafe.checkArgClass( thisMethod, attr, UnitAttr, false );
		
		^mapAttrName.includesKey( attr.name );
	}
	
	getAttr {
		^IdentityDictionary.newFrom( unitAttrMap );
	}
	
	setUnit { arg argUnit, disposeOld = false;
		var bndl;
		
		bndl = this.newBundle;
		this.setUnitToBundle( bndl, argUnit, disposeOld );
		bndl.send;
	}
	
	setUnitToBundle { arg bndl, argUnit, disposeOld = false;
		var hadOld;

		TypeSafe.checkArgClasses( thisMethod, [ bndl, argUnit ], [ OSCBundle, NuagesU ], [ false, false ]);

		hadOld = unit.notNil;
		if( hadOld and: { argUnit.notNil and: { (unit.numAudioOutputs != argUnit.numAudioOutputs) or: {
			(0..(unit.numAudioOutputs-1)).any({ arg idx; unit.numOutChannels( idx ) != argUnit.numOutChannels( idx )})}}}, {
			
			TypeSafe.methodError( thisMethod, "Trying to change # of outputs or channels" );
			^this;
		});
		if( hadOld, {
			unit.removeDependant( this );
			if( disposeOld && unit.disposed.not, {
				unit.disposeToBundle( bndl );
			});
		});
		unit = argUnit;
		if( unit.notNil, {
			unit.setGroupToBundle( bndl, playGroup2 );
			unit.addDependant( this );
			if( hadOld.not, {
				audioInBusses		= nil ! unit.numAudioInputs;
				madeAudioInBusses	= \no ! unit.numAudioInputs;
				audioOutBusses	= nil ! unit.numAudioOutputs;
				madeAudioOutBusses	= \no ! unit.numAudioOutputs;
			});
		});
	}
	
	crossFade { arg newAttr, fadeTime, fadeType = \eqp, disposeOld = true;
		var bndl;
		
		bndl = this.newBundle;
		this.crossFadeToBundle( bndl, newAttr, fadeTime, fadeType, disposeOld );
		bndl.send;
	}
	
	crossFadeToBundle { arg bndl, newAttr, fadeTime, fadeType = \eqp, disposeOld = true;
		fadeTime = fadeTime ? this.fadeTime;
		
		TypeSafe.checkArgClasses( thisMethod, [ bndl, newAttr, fadeTime, fadeType ],
		                                      [ OSCBundle, Dictionary, Number, Symbol ],
		                                      [ false, true, false, false ]);

		if( this.isFading, {
			if( verbose, { "% saving pending crossFade\n".postf( thisMethod )});
			if( newAttr.isNil.not, { pendingAttr.putAll( newAttr )});
			pendingFadeTime	= fadeTime;
			pendingFadeType	= fadeType;
			pendingDispose	= disposeOld;
			pending			= true;
		}, { if( unit.notNil and: { unit.isPlaying }, {
			this.prCrossPlay( bndl, newAttr, fadeTime, fadeType, disposeOld );
		}, {
			this.applyAttrToBundle( bndl, newAttr );
			if( unit.notNil, { this.playToBundle( bndl, fadeTime, fadeType )});
		})});
	}

	/**
	 *	@warning	will _not_ generate and send an OSCBundle
	 */
	applyAttr { arg newAttr;
		var bndl;
		bndl = this.newBundle;
		this.prApplyAttrToBundle( bndl, unit, newAttr );
		bndl.send;
	}

	applyAttrToBundle { arg bndl, newAttr;
		this.prApplyAttrToBundle( bndl, unit, newAttr );
	}
	
	prApplyAttrToBundle { arg bndl, unit, newAttr;
		var keyStr, unitAttr;

		TypeSafe.checkArgClasses( thisMethod, [ bndl, unit, newAttr ],
		                                      [ OSCBundle, NuagesU, Object ],
		                                      [ false, false, false ]);
		
		if( newAttr.isNil.not, {
			unitAttrMap.putAll( newAttr );
			if( unit.notNil, {
				newAttr.keysValuesDo({ arg key, value;
					unitAttr = unit.attributes.detect({ arg attr; attr.name === key });
					if( unitAttr.notNil, {
//						[ "yes", key, unitAttr.setter ].postln;
						unitAttr.setValueToBundle( bndl, unit, value );
					}, {
						TypeSafe.methodWarn( thisMethod, "attr '" ++ key ++ "' not found" );
					});
				});
			});
		});
	}

	isRunning {
		^group.isRunning;
	}

	isPlaying {
		^group.isPlaying;
	}
	
	play { arg fadeTime, fadeType = \sin;
		var bndl;

		bndl = this.newBundle;
		this.playToBundle( bndl, fadeTime, fadeType );
		bndl.send;
	}
	
	// call inside Routine!
	playToBundle { arg bndl, fadeTime, fadeType = \sin;
		var success;
		if( unit.isPlaying, { ^this });
		
		success = if( unit.isControl, { this.getControlOutputBusToBundle( bndl )}, { this.getAudioOutputBusToBundle( bndl )}).notNil;
		if( success.not, { ^this });
		
		// always resume since group.run info might not yet be updated!
		bndl.add( group.runMsg( true ));
		bndl.add( playGroup1.freeAllMsg );	// smarty killed fades
		bndl.add( playGroup2.freeAllMsg );	// smarty killed fades
		if( unit.isControl, {
			// this.prFadeControlToBundle( bndl, \in, fadeTime, fadeType, 2, false );
		}, {
			this.prFadeAudioToBundle( bndl, \in, fadeTime, fadeType, 2, false );
		});
		unit.playToBundle( bndl );
	}
	
	stop { arg fadeTime, fadeType = \sin;
		var bndl;

		bndl = this.newBundle;
		this.stopToBundle( bndl, fadeTime, fadeType );
		bndl.send;
	}
	
	stopToBundle { arg bndl, fadeTime, fadeType = \sin;
		var wait;
		if( unit.isPlaying, {
			// doneAction 7 = free this synth and all preceeding in the group
			// doneAction 5 = free this synth; if the preceding node is a group then do g_freeAll on it, else free it
			wait = if( unit.isControl, {
				false; // this.prFadeControlToBundle( bndl, \out, fadeTime, fadeType, 5, true );
			}, {
				this.prFadeAudioToBundle( bndl, \out, fadeTime, fadeType, 5, true );
			});
			
			if( wait.not, {
				unit.stopToBundle( bndl );
				bndl.add( playGroup1.freeAllMsg );	// smarty killed fades
				bndl.add( playGroup2.freeAllMsg );	// smarty killed fades
			});
		});
	}
	
	setNeverPausing { arg onOff;
		var bndl;

		bndl = this.newBundle;
		this.setNeverPausingToBundle( bndl, onOff );
		bndl.send;
	}
	
	setNeverPausingToBundle { arg bndl, onOff;
		neverPause = onOff;
		if( neverPause, {
			this.resumeToBundle( bndl );
		});
	}

	isNeverPausing {
		^neverPause;
	}

	pause {
		var bndl;

		bndl = this.newBundle;
		this.pauseToBundle( bndl );
		bndl.send;
	}
	
	pauseToBundle { arg bndl;
		if( neverPause.not, {
			if( verbose, { (thisMethod.asString ++ " : pause "++group).postln; });
			bndl.add( group.runMsg( false ));
			this.tryChanged( \paused );
		});
	}

	resume {
		var bndl;

		if( verbose, { (thisMethod.asString ++ " : resume "++group).postln; });

		bndl = this.newBundle;
		this.resumeToBundle( bndl );
		bndl.send;
	}

	resumeToBundle { arg bndl;
		bndl.add( group.runMsg( true ));
	}

	dispose {
		var bndl;
		bndl = this.newBundle;
		this.disposeToBundle( bndl );
		bndl.send;
	}
	
	disposeToBundle { arg bndl;
		if( unit.notNil, {
			if( unit.disposed.not, { unit.disposeToBundle( bndl )});
			unit = nil;
		});
		this.prKillFadesToBundle( bndl, false );
		this.prKillPending;
		bndl.add( group.freeMsg );	// kills the control map synths as well
		group = nil;

		madeAudioInBusses.do({ arg did, i;
			if( did === \yes, {
				audioInBusses[ i ].free;
			});
		});
		madeAudioOutBusses.do({ arg did, i;
			if( did === \yes, {
				audioOutBusses[ i ].free;	// nil.free allowed!
			});
		});
		madeAudioInBusses	= nil;
		audioInBusses		= nil;
		madeAudioOutBusses	= nil;
		audioOutBusses	= nil;

		if( weMadeProxyControlBus, {
			proxyControlBus.free;
			weMadeProxyControlBus = false;
		});
		proxyControlBus = nil;
		tempControlBus.free;
		tempControlBus = nil;

		this.tryChanged( \disposed );
	}
	
	debugDump {
		("Group "++group).postln;
		("PlayGroup1 "++playGroup1).postln;
		("PlayGroup2 "++playGroup2).postln;
	}
	
	hasMadeAudioOutputBus { arg idx; ^(madeAudioOutBusses[ idx ] !== \no) }
	hasMadeAudioInputBus {  arg idx; ^(madeAudioInBusses[ idx ]  !== \no) }
	
	makeAudioInputBusToBundle { arg bndl, idx, numChannels;
		^this.prMakeAudioBusToBundle( bndl, idx, numChannels,
			\setAudioInputBusToBundle, audioInBusses, madeAudioInBusses,
			\setAudioOutputBusToBundle, audioOutBusses, madeAudioOutBusses );
	}

	makeAudioOutputBusToBundle { arg bndl, idx, numChannels;
		^this.prMakeAudioBusToBundle( bndl, idx, numChannels,
			\setAudioOutputBusToBundle, audioOutBusses, madeAudioOutBusses,
			\setAudioInputBusToBundle, audioInBusses, madeAudioInBusses );
	}

	prMakeAudioBusToBundle { arg bndl, idx, numChannels, primUnitSetter, primAudioBusses, primMadeAudioBusses,
	                                                     secUnitSetter,  secAudioBusses,  secMadeAudioBusses;
		var bus;
	
		TypeSafe.checkArgClasses( thisMethod,	[ bndl, idx, numChannels ],
										[ OSCBundle, Integer, Integer ],
										[ false, false, false ]);

		if( primMadeAudioBusses[ idx ] !== \no, {
			Error( "Trying to create internal bus twice" ).throw;
		});
		bus = Bus.audio( server, numChannels );
[ "GUGUGU 1", primUnitSetter, secUnitSetter ].postln;
		if( bus.notNil, {
[ "GUGUGU 2" ].postln;
			primAudioBusses[ idx ] = bus;
			unit.perform( primUnitSetter, bndl, bus, idx );
			primMadeAudioBusses[ idx ] = \yes;
			if( unit.isAudioInputReadOnly( idx ).not, { // using same bus for input and output?

[ "GUGUGU 3" ].postln;

				if( secMadeAudioBusses[ idx ] !== \no, {
					TypeSafe.methodWarn( "Corresponding internal linked bus already exists" );
				}, {
					secAudioBusses[ idx ] = bus;
					unit.perform( secUnitSetter, bndl, bus, idx );
					secMadeAudioBusses[ idx ] = \alias;
				});
			});
		}, {
			TypeSafe.methodError( thisMethod, "Bus allocator exhausted" );
		});
		// XXX update running synths YYY
		^bus;
	}

	makeInputMixSynthToBundle { arg bndl, srcBus, idx;
		var synth, tgtBus;

		TypeSafe.checkArgClasses( thisMethod,	[ bndl,      srcBus, idx     ],
										[ OSCBundle, Bus,    Integer ],
										[ false,     false,  false   ]);
										
[ "mix1" ].postln;
		tgtBus	= this.getAudioInputBus( idx );
[ "mix2" ].postln;
		synth	= this.prPrepareRouteSynthToBundle( bndl, srcBus.numChannels, tgtBus.numChannels );
[ "mix3" ].postln;
		bndl.add( synth.newMsg( group, [ \busA, srcBus, \busB, tgtBus ], \addToHead ));
		^synth;
	}
	
	getAudioInputBus { arg idx = 0;
		^audioInBusses[ idx ];
	}
	
	/**
	 *	@warning	the semantics have changed. this will not _create_ the
	 *			bus. if the bus does not yet exist, nil is returned
	 */
	getAudioOutputBus { arg idx = 0;
		^audioOutBusses[ idx ];
	}
	
	getAudioOutputBusToBundle { arg bndl, idx = 0;
		var numChannels;

		TypeSafe.checkArgClasses( thisMethod, [ bndl, idx ], [ OSCBundle, Integer ], [ false, false ]);

		if( audioOutBusses[ idx ].isNil and: { unit.notNil }, {
			numChannels = unit.numOutChannels( idx );
			
[ "Juhhhhu", unit.name, numChannels ].postln;
			
			if( numChannels > 0, {
				this.makeAudioOutputBusToBundle( bndl, idx, numChannels );
			}, {
				TypeSafe.methodError( thisMethod, "Unit (%) returns illegal numChannels (%)".format( unit, numChannels ));
			});
		});
		^audioOutBusses[ idx ];
	}
	
//	setAudioOutputBus { arg bus, idx = 0;
//		var bndl;
//		
//		bndl = this.newBundle;
//		this.setAudioOutputBusToBundle( bndl, bus, idx );
//		bndl.send;
//	}
	
	setAudioInputBusToBundle { arg bndl, bus, idx = 0;
		var numChannels, tmp;

		TypeSafe.checkArgClasses( thisMethod, [ bndl, bus, idx ], [ OSCBundle, Bus, Integer ], [ false, false, false ]);

		if( madeAudioInBusses[ idx ] !== \no, {
			this.stopToBundle( bndl );
			tmp = audioInBusses[ idx ];  // freeze
			bndl.addFunction({ tmp.free });
			audioInBusses[ idx ]			= nil;
			if( madeAudioInBusses[ idx ] === \alias, {
				audioOutBusses[ idx ]		= nil;
				madeAudioOutBusses[ idx ]	= \no;
			});
			madeAudioInBusses[ idx ]		= \no;
		});
		if( unit.notNil, {
			numChannels = unit.numInChannels( idx );
			if( numChannels > 0, {
				if( numChannels != bus.numChannels, {
					TypeSafe.methodError( thisMethod, "Cannot change # of channels (% -> %)".format( numChannels, bus.numChannels ));
					^this;
				});
				audioInBusses[ idx ] = bus;
				unit.setAudioInputBusToBundle( bndl, bus, idx );
				if( unit.isAudioInputReadOnly( idx ).not, { // using same bus for input and output?
					audioOutBusses[ idx ] = bus;
					unit.setAudioInputBusToBundle( bndl, bus, idx );
				});
			}, {
				TypeSafe.methodError( thisMethod, "Unit returns illegal numChannels (%)".format( numChannels ));
			});
		}, {
			audioInBusses[ idx ] = bus;
		});
		// XXX update running fade synths YYY
	}
	
	setAudioOutputBusToBundle { arg bndl, bus, idx = 0;
		var numChannels, tmp;

		TypeSafe.checkArgClasses( thisMethod, [ bndl, bus, idx ], [ OSCBundle, Bus, Integer ], [ false, false, false ]);

		if( madeAudioOutBusses[ idx ] !== \no, {
			this.stopToBundle( bndl );
			tmp = audioOutBusses[ idx ];  // freeze
			bndl.addFunction({ tmp.free });
			audioOutBusses[ idx ]			= nil;
			if( madeAudioOutBusses[ idx ] === \alias, {
				audioInBusses[ idx ]		= nil;
				madeAudioInBusses[ idx ]	= \no;
			});
			madeAudioOutBusses[ idx ]		= \no;
		});
		if( unit.notNil, {
			numChannels = unit.numOutChannels( idx );
			if( numChannels > 0, {
				if( numChannels != bus.numChannels, {
					TypeSafe.methodError( thisMethod, "Cannot change # of channels (% -> %)".format( numChannels, bus.numChannels ));
					^this;
				});
				audioInBusses[ idx ] = bus;
				unit.setAudioInputBusToBundle( bndl, bus, idx );
				if( unit.isAudioInputReadOnly( idx ).not, { // using same bus for input and output?
					audioOutBusses[ idx ] = bus;
					unit.setAudioInputBusToBundle( bndl, bus, idx );
				});
			}, {
				TypeSafe.methodError( thisMethod, "Unit returns illegal numChannels (%)".format( numChannels ));
			});
		}, {
			audioInBusses[ idx ] = bus;
		});
		// XXX update running fade synths YYY
	}
	
	getControlOutputBus {
		var bndl, result;
		
		if( proxyControlBus.notNil, { ^proxyControlBus });

		bndl		= this.newBundle;
		result	= this.getControlOutputBusToBundle( bndl );
		bndl.send;
		^result;
	}
	
	getControlOutputBusToBundle { arg bndl;
		if( proxyControlBus.notNil, { ^proxyControlBus });
		
		if( unit.notNil, {
			proxyControlBus	= Bus.control( server );
			tempControlBus	= Bus.control( server );
			if( proxyControlBus.notNil and: { tempControlBus.notNil }, {
				unit.setControlOutputBusToBundle( bndl, proxyControlBus );
				weMadeProxyControlBus = true;
			}, {
				proxyControlBus.free;
				tempControlBus.free;
				proxyControlBus	= nil;
				tempControlBus	= nil;
				TypeSafe.methodError( thisMethod, "Bus allocator exhausted" );
			});
		});
		^proxyControlBus;
	}
	
	// QQQ
	prMakeXFadeDef { arg defName, numChannels;
		^SynthDef( defName, { arg busA, busB, dur = 1, shape = 1, doneAction = 2;
			var envA, envB, envGenA, envGenB, inpA, inpB;
			
			inpA		= In.ar( busA, numChannels );
			inpB		= In.ar( busB, numChannels );
			envA		= Env.new([ 1, 0 ], [ dur ], 1 ).asArray;
			envA[ 6 ]	= shape;
			envB		= Env.new([ 0, 1 ], [ dur ], 1 ).asArray;
			envB[ 6 ]	= shape;
			envGenA	= EnvGen.ar( envA, doneAction: doneAction );
			envGenB	= EnvGen.ar( envB, doneAction: 0 );
			ReplaceOut.ar( busB, (inpA * envGenA) + (inpB * envGenB) );
		});
	}

	prKillPending {
		pendingAttr	= IdentityDictionary.new;
		pending		= false;
	}

	// -------------- private methods --------------

	prCrossPlay { arg bndl, newAttr, fadeTime, fadeType, disposeOld;
		var oldUnit, defName, newUnit, updRec, rec, tmpBusses;
		
		fadeTime = fadeTime ? this.fadeTime;

		oldUnit	= unit;
		newUnit	= unit.duplicate;
		
		// . . . . . . . . . . . . . . . . . . . . . . .
		// tricky shit to inform listeners about
		// attribute updates...
		updRec = UpdateListener.newFor( newUnit, {
			arg upd, u, name ... params;
//			arg upd, u ... params;
			var attr;
			attr	= u.getAttribute( name );
			rec	= rec.add([ name, attr.getValue( u ), attr.getNormalizedValue( u )]);
		}, \attrUpdate );
		this.prApplyAttrToBundle( bndl, newUnit, newAttr );
		this.setUnitToBundle( bndl, newUnit, false );   // handles dependancies
		updRec.remove;
		rec.do({ arg params; this.tryChanged( \unitAttrUpdate, *params )});
		// . . . . . . . . . . . . . . . . . . . . . . .
		
		tmpBusses = audioOutBusses.collect({ arg proxyAudioBus, idx; Bus.audio( server, proxyAudioBus.numChannels )}); // XXX detect failures
		tmpBusses.do({ arg tmpBus, idx; oldUnit.setAudioOutputBusToBundle( bndl, tmpBus, idx )});
		oldUnit.setGroupToBundle( bndl, playGroup1 );
		
		UpdateListener.newFor( oldUnit, { arg upd, obj, what;
			case { what === \unitPlaying }
			{
				upd.removeFrom( obj );
				// YYY needs fork?
				if( disposeOld and: { obj.disposed.notÊ}, {
					if( verbose, { (thisMethod.asString ++ " unitPlaying update : disposing " ++ obj).postln });
					obj.dispose;
				});
				tmpBusses.do( _.free );
				tmpBusses = nil;
			}
			{ what === \unitDisposed }
			{
				upd.removeFrom( obj );
				tmpBusses.do( _.free );
				tmpBusses = nil;
			};
		});
		
		if( fadeTime > 0, {
			fadeSynths = audioOutBusses.collect({ arg proxyAudioBus, idx; var fadeSynth;
				defName   = ("nuages-prXFade" ++ proxyAudioBus.numChannels).asSymbol;
				fadeSynth = Synth.basicNew( defName, server );
				if( defWatcher.isOnline( defName ).not, {
					defWatcher.sendToBundle( bndl, this.prMakeXFadeDef( defName, proxyAudioBus.numChannels ), fadeSynth );
				});
				bndl.add( fadeSynth.newMsg( playGroup2,
					[ \busA, tmpBusses[ idx ], \busB, proxyAudioBus, \dur, fadeTime, \shape,
					  fadeShapes[ fadeType ] ? 1, \doneAction, 2 ], \addAfter ));
				fadeSynth;
			});
			this.prRegisterFadeSynth({ var bndl;
				bndl = this.newBundle;
				oldUnit.stopToBundle( bndl );
				bndl.send;
			});
			newUnit.playToBundle( bndl );
			// crucial: the control mappings need to go after the playToBundle
			// because n_set undoes bus mappings!
			this.prAddControlMapsToBundle( bndl, newUnit );
		}, {
			newUnit.playToBundle( bndl );
			// crucial: the control mappings need to go after the playToBundle
			// because n_set undoes bus mappings!
			this.prAddControlMapsToBundle( bndl, newUnit );
			oldUnit.stopToBundle( bndl );
		});
	}
	
	prRegisterFadeSynth { arg endAction;
		UpdateListener.newFor( fadeSynths.first.register, { arg upd, fadeSynth; var bndl;
			upd.remove;
			forkIfNeeded {
				endAction.value;
				if( fadeSynth == fadeSynths.first, { // only if they were not killed before
					fadeSynths = nil;
					bndl = this.newBundle;
					if( this.prProcessPending( bndl ).not, {
						if( unit.isPlaying.not, {
							this.pauseToBundle( bndl );
//							"HUHU".inform;
						});
					});
					bndl.send;
				});
			};
		}, \n_end );
	}
	
	// returned synth is basic (needs explicit newMsg!); has controls: busA, busB
	prPrepareRouteSynthToBundle { arg bndl, numInChannels, numOutChannels;
		var defName, routSynth;
		defName   = "nuages-pRoute%x%".format( numInChannels, numOutChannels).asSymbol;
		routSynth = Synth.basicNew( defName, server );
[ "prPrepareRouteSynthToBundle", 1, numInChannels, numOutChannels ].postln;
		if( defWatcher.isOnline( defName ).not, {
[ "prPrepareRouteSynthToBundle", 2 ].postln;
			defWatcher.sendToBundle( bndl, SynthDef( defName, { arg busA, busB;
				var inp, outp, fch, ch2, w, off, accum, w2;
				inp = In.ar( busA, numInChannels ).asArray;
				if( numOutChannels == numInChannels, {
					outp = inp;
				}, {
					outp = 0 ! numOutChannels;
//[ "prPrepareRouteSynthToBundle", 2.5, outp, inp ].postln;
					if( numOutChannels > numInChannels, {
						w	= numOutChannels / numInChannels;
						off	= 0;
						numInChannels.do({ arg ch;
							accum = 0;
							while({ accum < w and: { off < numOutChannels }}, {
								w2		= 1 - (off % 1.0);
								ch2		= off.asInteger;
//[ "prPrepareRouteSynthToBundle", 3, ch2, outp.size, ch, inp.size ].postln;
								outp[ ch2 ] = outp[ ch2 ] + (inp[ ch ] * w2);
								off		= ch2 + min( 1, w - accum );
								accum	= accum + w2;
							});
						});
					}, {
//[ prPrepareRouteSynthToBundle, 3, 0, 1, 0, 2, 0 ]
//[ prPrepareRouteSynthToBundle, 3, -1, 1, 1, 2, -1 ]
						numInChannels.do({ arg ch;
							fch = ch.linlin( 0, numInChannels - 1, 0, numOutChannels - 1 );
							ch2 = fch.asInteger;
[ "prPrepareRouteSynthToBundle", 3, ch2, outp.size, ch, inp.size, fch ].postln;
							if( fch == ch2, {
								outp[ ch2 ] = outp[ ch2 ] + inp[ ch ];
							}, {
								w = fch - ch2;
								outp[ ch2 ] = outp[ ch2 ] + (inp[ ch ] * (1 - w));
								outp[ ch2 + 1 ] = outp[ ch2 + 1 ] + (inp[ ch ] * w);
							});
						});
					});
				});
				Out.ar( busB, outp );
			}), routSynth );
		});
		^routSynth;
	}

	prFadeAudioToBundle { arg bndl, dir, fadeTime, fadeType, doneAction, smartKiller;
	
		fadeTime = fadeTime ? this.fadeTime;

		this.prKillFadesToBundle( bndl, smartKiller );

		if( fadeTime > 0, {
			fadeSynths = audioOutBusses.collect({ arg proxyAudioBus, idx; var fadeSynth, routSynth, defName, tmpBus;
				if( neverPause, {
					routSynth = this.prPrepareRouteSynthToBundle( bndl, proxyAudioBus.numChannels );
					tmpBus = Bus.audio( server, proxyAudioBus.channels );
					bndl.add( routSynth.newMsg( playGroup1,
						[ \busA, proxyAudioBus, \busB, tmpBus ], \addBefore ));
					defName   = ("nuages-prXFade" ++ proxyAudioBus.numChannels).asSymbol;
					fadeSynth = Synth.basicNew( defName, server );
					if( defWatcher.isOnline( defName ).not, {
						defWatcher.sendToBundle( bndl, this.prMakeXFadeDef( defName, proxyAudioBus.numChannels ), fadeSynth );
					});
					bndl.add( fadeSynth.newMsg( playGroup2,
						[ \busA, if( dir == \in, tmpBus, proxyAudioBus ),
						  \busB, if( dir == \in, proxyAudioBus, tmpBus ), \dur, fadeTime, \shape,
						  fadeShapes[ fadeType ] ? 1, \doneAction, doneAction ], \addAfter ));
					fadeSynth.onEnd = { tmpBus.free };  // XXX what's with routSynth?
					fadeSynth;
				}, {
					defName   = ("nuages-prFade" ++ proxyAudioBus.numChannels).asSymbol;
					fadeSynth = Synth.basicNew( defName, server );
					if( defWatcher.isOnline( defName ).not, {
						defWatcher.sendToBundle( bndl, SynthDef( defName, {
							arg bus = 0, dur = 1, start = 0, end = 1, shape = 1, doneAction = 2;
							var env, envGen, inp;
							
							inp		= In.ar( bus, proxyAudioBus.numChannels );
							env		= Env.new([ start, end ], [ dur ], 1 ).asArray;
							env[ 6 ]	= shape;
							envGen	= EnvGen.ar( env, levelScale: 0.999, doneAction: doneAction ); // 0.999 = bug fix !!!
							ReplaceOut.ar( bus, inp * envGen );
						}), fadeSynth );
					});
					bndl.add( fadeSynth.newMsg( playGroup2,
						[ \bus, proxyAudioBus, \start, if( dir == \in, 0, 1 ), \end, if( dir == \in, 1, 0 ), \dur, fadeTime, \shape,	
						  fadeShapes[ fadeType ] ? 1, \doneAction, doneAction ], \addAfter ));
					fadeSynth;
				});
			});
			this.prRegisterFadeSynth;
			^true;
		}, {
			^false;
		});
	}

	newBundle { ^NuagesOSCBundle( nuages )}
	
	prKillFades { arg smartKiller = false;
		var bndl;

		bndl = this.newBundle;
		this.prKillFadesToBundle( bndl, smartKiller );
		bndl.send;
	}

	prKillFadesToBundle { arg bndl, smartKiller = false;
		if( this.isFading, {
			fadeSynths.do({ arg fadeSynth;
				bndl.add( if( smartKiller, { fadeSynth.moveToTailMsg( playGroup2 )}, { fadeSynth.freeMsg }));
			});
			fadeSynths = nil;
		});
	}
	
	prProcessPending { arg bndl;
		if( pending, {
			if( unit.notNil, {
				if( unit.isPlaying, {
					this.prCrossPlay( bndl, pendingAttr, pendingFadeTime, pendingFadeType, pendingDispose );
				}, {
					this.applyAttrToBundle( bndl, pendingAttr );
					this.playToBundle( bndl, pendingFadeTime, pendingFadeType );
				});
			}, {
				this.applyAttrToBundle( bndl, pendingAttr );
			});
			this.prKillPending;
			^true;
		}, {
			fadeSynths	= nil;
			^false;
		});
	}
	
	prInitToBundle { arg bndl;
		mapPlayBusToGroup		= IdentityDictionary.new;
		fadeSynths			= nil;
		this.prKillPending;

		group				= Group.basicNew( server ).register;
		playGroup1			= Group.basicNew( server ).register;
		playGroup2			= Group.basicNew( server ).register;

		bndl.add( group.newMsg );
		if( neverPause.not, { bndl.add( group.runMsg( false ))});
		bndl.add( playGroup1.newMsg( group, addAction: \addToTail ));
		bndl.add( playGroup2.newMsg( group, addAction: \addToTail ));
	}

	// -------------- quasi-interface methods --------------

	update { arg obj, what ... params;
		var bndl, bool, name, attr;
	
		switch( what,
		\unitPlaying, {
			if( obj === unit, {
				forkIfNeeded {
					this.tryChanged( \unitPlaying, *params ); // YYY outside fork?
					bndl = this.newBundle;
					bool = true;
					if( this.isFading.not, {
						bool = this.prProcessPending( bndl ).not;
					});
					if( bool && unit.isPlaying.not, {
						this.prKillFadesToBundle( bndl, false );
						this.pauseToBundle( bndl );
					});
					bndl.send;
				};
			});
		},
		\unitRecording, {
			if( obj === unit, {
				this.tryChanged( \unitRecording, *params );
			});
		},
		\attrUpdate, {
			name = params.first;
			attr = obj.getAttribute( name );
			this.tryChanged( \unitAttrUpdate, name, attr.getValue( obj ), attr.getNormalizedValue( obj ));
		});
	}

	// -------------- PlayableToBus interface --------------

	// Note: Monitor class is unusable, looses nodes
	// when triggering play / stop too fast. returning to
	// original concept from SynthProxy

	// has to be called from inside routine
	addPlayBus { arg bus, vol = 1.0;
		var bndl;

		bndl = this.newBundle;
		this.addPlayBusToBundle( bndl, bus, vol );
		bndl.send;
	}
	
	// @deprecated
	addPlayBusToBundle { arg bndl, bus, vol = 1.0, idx = 0;
		var synth, synths, grp, numChannels, def, defName;

		TypeSafe.methodWarn( thisMethod, "Deprecated!" );
		TypeSafe.checkArgClasses( thisMethod, [ bndl, bus, vol ], [ OSCBundle, Bus, Number ], [ false, false, false ]);

		if( mapPlayBusToGroup.includesKey( bus ), {
			TypeSafe.methodWarn( thisMethod, "Bus already playing" );
			^this;
		});
		if( this.getAudioOutputBusToBundle( bndl ).isNil, {
			TypeSafe.methodWarn( thisMethod, "No proxy bus available" );
			^this;
		});
		
		grp 			= Group.basicNew( server ).register;
		mapPlayBusToGroup.put( bus, grp );
		bndl.add( grp.newMsg( group.asTarget, addAction: \addToTail ));

		if( bus.numChannels == audioOutBusses[ idx ].numChannels, {   // use one synth
			numChannels	= bus.numChannels;
			defName		= ("nuages-prPlay" ++ numChannels).asSymbol;
			synth		= Synth.basicNew( defName, server );
			if( defWatcher.isOnline( defName ).not, {
				defWatcher.sendToBundle( bndl, SynthDef( defName, { arg aInBus, aOutBus, vol = 1.0;
					Out.ar( aOutBus, In.ar( aInBus, numChannels ) * Lag.kr( vol ));
				}, [ nil, nil, 0.1 ]), synth );
			});
			bndl.add( synth.newMsg( grp,
				[ \aInBus, audioOutBusses[ idx ].index, \aOutBus, bus.index, \vol, vol ]));
		}, {		// use multiple duplicated or wrapped monos
			defName		= 'nuages-prPlay1';
			numChannels 	= max( bus.numChannels, audioOutBusses[ idx ].numChannels );
			synths		= Array.fill( numChannels, { arg ch;
				synth = Synth.basicNew( defName, server );
				bndl.add( synth.newMsg( grp,
					[ \aInBus, audioOutBusses[ idx ].index + (ch % audioOutBusses[ idx ].numChannels),
					  \aOutBus, bus.index + (ch % bus.numChannels), \vol, vol ]));
				synth;
			});
			if( defWatcher.isOnline( defName ).not, {
				defWatcher.sendToBundle( bndl, SynthDef( defName, { arg aInBus, aOutBus, vol = 1.0;
					Out.ar( aOutBus, In.ar( aInBus, 1 ) * Lag.kr( vol ));
				}), synths.first );
			});
		});
	}
	
	removePlayBus { arg bus;
		var bndl;
		bndl = this.newBundle;
		this.removePlayBusToBundle( bndl, bus );
		bndl.send;
	}
	
	removePlayBusToBundle { arg bndl, bus;
		var grp, defName, synth;

		TypeSafe.checkArgClasses( thisMethod, [ bndl, bus ], [ OSCBundle, Bus ], [ false, false ]);
		
		grp = mapPlayBusToGroup.removeAt( bus );
		if( grp.isNil, {
			TypeSafe.methodWarn( thisMethod, "Bus was not registered" );
		}, {
			bndl.add( grp.setMsg( \vol, 0.0 ));	// i.e. 100ms lagged fade-out
			defName	= 'nuages-prKillGroup';
			synth	= Synth.basicNew( defName, server );
			if( defWatcher.isOnline( defName ).not, {
				defWatcher.sendToBundle( bndl, SynthDef( defName, { arg i_dur = 0.1;
					Line.kr( 0, 0, i_dur, doneAction: 14 );
				}), synth );
			});
			bndl.add( synth.newMsg( grp ));
		});
	}

	setPlayBusVolume { arg bus, vol;
		var grp;
		
		if( bus.isNil, {
			mapPlayBusToGroup.keysDo({ arg bus; this.setPlayBusVolume( bus, vol )});
			^this;
		});
		grp = mapPlayBusToGroup.at( bus );
		if( grp.isNil, {
			TypeSafe.methodError( thisMethod, "Unknown bus" );
		}, {
			if( verbose, { (thisMethod.asString ++ " : group = " ++ grp.nodeID ++
						  ", vol = " ++ vol).postln; });
			grp.set( \vol, vol );
		});
	}
}