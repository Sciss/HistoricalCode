/**
 *	NuagesLinkedGraph
 *	(Wolkenpumpe)
 *
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: TypeSafe, NuagesProc, NuagesUnit
 *
 *	Changelog:
 *
 *	@version	0.11, 26-Aug-09
 *	@author	Hanns Holger Rutz
 */
NuagesE {
	var <source, <target, <synth;
	
	// persistence
	var <>pID;
	
	*new { arg source, target;
		^super.newCopyArgs( source, target ).prInit;
	}
	
	*get { arg pID; ^Wolkenpumpe.default.persistGet( pID )}
	
	prInit {
		source.protAddEdge( this );
		target.protAddEdge( this );
	}
	
	protRemoveToBundle { arg bndl;
		if( synth.notNil, {
			bndl.add( synth.freeMsg );
			synth = nil;
		});
		source.protRemoveEdge( this );
		target.protRemoveEdge( this );
		source = nil; target = nil;
		^true;
	}
	
//	protSetSynth { arg aSynth;
//		if( synth.notNil and: { aSynth.notNil }, {
//			Error( "Mix synth already existed" ).throw;
//		});
//		synth = aSynth;
//	}
	
	protMakeMixSynthToBundle { arg bndl;
		if( synth.notNil, {
			Error( "Mix synth already existed" ).throw;
		});
		synth = target.vertex.proc.makeInputMixSynthToBundle( bndl, source.bus, target.index );
		^synth;
	}

	storeParamsOn { arg stream;
		if( pID.isNil, {
			^super.storeParamsOn( stream );
		});
		stream << ".get(%)".format( pID );
	}
}

NuagesPort {
	var <vertex, <index, <name, <rate, <visible;
	
	var <edges;
	
	*new { arg vertex, index, name, rate, visible;
		^super.new.prInit( vertex, index, name, rate, visible );
	}
	
	prInit { arg argVertex, argIndex, argName, argRate, argVisible;
		TypeSafe.checkArgClasses( thisMethod, [ argVertex, argIndex, argName, argRate, argVisible ],
		                                      [ NuagesV,   Integer,  Symbol,  Symbol,  Boolean    ],
		                                      [ false,     false,    true,    false,   false      ]);
		                                      
		vertex	= argVertex;
		index	= argIndex;
		name		= argName;
		rate		= argRate;
		visible	= argVisible;
	}
	
	type { ^this.subclassResponsibility( thisMethod )}
	bus { ^this.subclassResponsibility( thisMethod )}
	
	protAddEdge { arg e; edges = edges.add( e )}
	protRemoveEdge { arg e; edges.remove( e )}
}

NuagesAudioInlet : NuagesPort {
	var <readOnly;
	
	*new { arg vertex, index, name, visible, readOnly;
		^super.new( vertex, index, name, \audio, visible ).prInitInlet( readOnly );
	}

	prInitInlet { arg argReadOnly;
		TypeSafe.checkArgClass( thisMethod, argReadOnly, Boolean, false );
		readOnly = argReadOnly;
	}
	
	type { ^\in }
	bus { ^vertex.proc.unit.getAudioInputBus( index )}
}

NuagesAudioOutlet : NuagesPort {
	*new { arg vertex, index, name, visible;
		^super.new( vertex, index, name, \audio, visible );
	}
	
	type { ^\out }
	bus { ^vertex.proc.getAudioOutputBus( index )}
}

NuagesControlInlet : NuagesPort {
	var <readOnly;
	
	*new { arg vertex, index, name, visible, readOnly;
		^super.new( vertex, index, name, \control, visible ).prInitInlet( readOnly );
	}

	prInitInlet { arg argReadOnly;
		TypeSafe.checkArgClass( thisMethod, argReadOnly, Boolean, false );
		readOnly = argReadOnly;
	}
	
	type { ^\in }
	bus { ^vertex.proc.unit.getControlInputBus( index )}
}

NuagesControlOutlet : NuagesPort {
	*new { arg vertex, index, name, visible;
		^super.new( vertex, index, name, \control, visible );
	}
	
	type { ^\out }
	bus { ^vertex.proc.getControlOutputBus( index )}
}

NuagesControlSidelet : NuagesControlInlet {
	*new { arg vertex, index, name, visible;
		^super.new( vertex, index, name, visible, true );
	}
	
	type { ^\sid }
//	bus { ^vertex.proc.unit.attributes[ index ]}
	attr { ^vertex.proc.unit.attributes[ index ]}
}

NuagesV { 
	// rep
	var <proc;

	// linked list org
	var <pred, <succ;
	
	// nuages org
	var <audioInlets, <audioOutlets, <controlInlets, <controlOutlets, <controlSidelets;
	
	// persistence
	var <>pID;
	
	*new { arg proc;
		^super.newCopyArgs( proc ).prInit;
	}

	*get { arg pID; ^Wolkenpumpe.default.persistGet( pID )}
	
	prInit {
		var unit;
		
		TypeSafe.checkArgClass( thisMethod, proc, NuagesProc, false );
		unit = proc.unit;
		if( unit.isNil, {
			Error( "proc.unit cannot be nil here" ).throw;
		});
		
		// XXX eventually, the Inlet and Outlet instances could
		// be directly created in NuagesUnit ! (without reference to NuagesV though)
		
		audioInlets = Array.fill( unit.numAudioInputs, { arg i;
			if( i < unit.numVisibleAudioInputs, {
				NuagesAudioInlet( this, i, unit.getAudioInputName( i ), true, unit.isAudioInputReadOnly( i ));
			}, {
				NuagesAudioInlet( this, i, nil, false, unit.isAudioInputReadOnly( i ));
			});
		});
		audioOutlets = Array.fill( unit.numAudioOutputs, { arg i;
			if( i < unit.numVisibleAudioOutputs, {
				NuagesAudioOutlet( this, i, unit.getAudioOutputName( i ), true );
			}, {
				NuagesAudioOutlet( this, i, nil, false );
			});
		});
		controlInlets = Array.fill( unit.numControlInputs, { arg i;
			if( i < unit.numVisibleControlInputs, {
				NuagesControlInlet( this, i, unit.getControlInputName( i ), true, true ); // XXX readOnly
			}, {
				NuagesControlInlet( this, i, nil, false, true );
			});
		});
		controlOutlets = Array.fill( unit.numControlOutputs, { arg i;
			if( i < unit.numVisibleControlOutputs, {
				NuagesControlOutlet( this, i, unit.getControlOutputName( i ), true );
			}, {
				NuagesControlOutlet( this, i, nil, false );
			});
		});
		controlSidelets = unit.attributes.collect({ arg attr, i;
			if( attr.canMap, {
				NuagesControlSidelet( this, i, nil, true );
			});
		}).reject( _.isNil );
	}
	
//	persist { arg nuages;
//		pID = nuages.persistNextID;
//		nuages.persistPut( pID, this );
//	}
	
	protRemove {
		if( pred.notNil, { pred.protSetSucc( succ )});
		if( succ.notNil, { succ.protSetPred( pred )});
		succ = pred = nil;
	}
		
	copy { this.shouldNotImplement( thisMethod )}
	
	isLinked {
		^(pred.notNil ||Êsucc.notNil);
	}
	
	printOn { arg stream;
		stream << this.class.name.asString << "( ";
		proc.printOn( stream );
		stream << " )";
//		this.storeOn( stream );
	}
	
	storeParamsOn { arg stream;
		if( pID.isNil, {
			^super.storeParamsOn( stream );
		});
		stream << ".get(%)".format( pID );
	}
	
	storeArgs { ^[ proc ]}
	
	protSetPred { arg newPred; pred = newPred }
	protSetSucc { arg newSucc; succ = newSucc }
}

NuagesLinkedGraph : SequenceableCollection {
	var <nuages;
	
	var head, tail, <size = 0;
	
	*new { arg graph;
		^super.newCopyArgs( graph );
	}
	
	copy { this.shouldNotImplement( thisMethod )}
	
	species { ^this.class }
	
	do { arg function;
		var i      = 0;
		var vertex = head;
		while({ vertex.notNil }, {
			function.value( vertex, i );
			vertex = vertex.succ;
			i = i + 1;
		});
	}
	
	reverseDo { arg function;
		var i      = size - 1;
		var vertex = tail;
		while({ vertex.notNil },{
			function.value( vertex, i );
			vertex = vertex.pred;
			i = i - 1;
		});
	}
	
	removeEdgeToBundle { arg bndl, edge;
		var inlet, tgtProc;
		
		inlet	= edge.target;
		tgtProc	= inlet.vertex.proc;
		if( tgtProc.hasMadeAudioInputBus( inlet.index ).not, {
			tgtProc.setAudioInputBusToBundle( bndl, nuages.getSilentBus( inlet.bus.numChannels ), inlet.index );
		});
		^edge.protRemoveToBundle( bndl );
	}
	
	/**
	 *	@returns	the edge if successful, nil on failure
	 */
	addAudioEdgeToBundle { arg bndl, outlet, inlet;
		var e, srcBus, tgtBus, srcProc, tgtProc;
	
		TypeSafe.checkArgClasses( thisMethod, [ bndl,      outlet,       inlet       ],
		                                      [ OSCBundle, NuagesAudioOutlet, NuagesAudioInlet ],
		                                      [ false,     false,        false       ]);
		                                      
// implied by the class
//		if( outlet.rate != inlet.rate, {
//			Error( "Can only connect outlet to an inlet of the same rate" ).throw;
//		});

		if( outlet.visible != inlet.visible, {
			"Should not connect visible to invisible ports".warn;
		});
		
		e  = NuagesE( outlet, inlet );  // this automatically adds the edge

// implied by the class
//		// graph order is only enforced for audio rate connections
//		// as control busses are not shared and node order is relaxed there
//		if( outlet.rate != \audio, { ^e }); // XXX establish ctrl bus connection
		
		if( this.prCheckAddedEdge( bndl, e ).not, { ^nil });

		// establish audio bus connection
		srcProc	= outlet.vertex.proc;
		tgtProc	= inlet.vertex.proc;
//		bus		= srcProc.getAudioOutputBusToBundle( bndl, outlet.index );
		tgtBus	= inlet.bus;
		if( tgtBus.isNil and: {Êinlet.vertex.proc.unit.hasPreferredNumInChannels }, {
[ "LULU0", inlet.vertex.proc.unit.protPreferredNumInChannels ].postln;
			tgtBus = inlet.vertex.proc.makeAudioInputBusToBundle( bndl, 0, inlet.vertex.proc.unit.protPreferredNumInChannels );
		});

		if( srcProc.getAudioOutputBus( outlet.index ).isNil and: { tgtBus.notNil and: { srcProc.unit.respondsTo( \setPreferredNumOutChannels )}}, { // tricky disco XXX
			srcProc.unit.setPreferredNumOutChannels( tgtBus.numChannels );
		});
		srcBus	= srcProc.getAudioOutputBusToBundle( bndl, outlet.index );  // other than outlet.getBus, this creates the bus if necessary!

[ "LULU0.5", srcBus, tgtBus, tgtProc.getAudioOutputBus ].postln;
		if( inlet.readOnly and: { tgtBus.isNil or: {Ênuages.isSilentBus( tgtBus ) && (tgtBus.numChannels == srcBus.numChannels) }}, {
[ "LULU1" ].postln;
			// plug source's bus directly into the inlet
			tgtProc.setAudioInputBusToBundle( bndl, srcBus, inlet.index );
//[ "LULU2", tgtProc.getAudioOutputBus ].postln;
		}, {
			if( tgtProc.hasMadeAudioInputBus( outlet.index ).not, {
[ "LULU2" ].postln;
				// create internal target bus and pre-mix group
				// ; we assume the target is using the bus
				// on the outlet with the same index!
				tgtBus = tgtProc.makeAudioInputBusToBundle( bndl, inlet.index, srcBus.numChannels );
//				// XXX this should be known by NuagesProc I guess
//				tgtProc.setAudioInputBusToBundle( bndl, tgtBus, inlet.index );
				inlet.edges.do({ arg e2;
					e2.protMakeMixSynthToBundle( bndl );
				});
//[ "LULU3", tgtProc.getAudioOutputBus ].postln;
			}, {
[ "LULU3" ].postln;
				// i.e. tgt is already using internal bus
				e.protMakeMixSynthToBundle( bndl );
//[ "LULU4", tgtProc.getAudioOutputBus ].postln;
			});
		});
[ "LULU4" ].postln;
		^e;
	}
	
	/**
	 *	@returns	the edge if successful, nil on failure
	 */
	addControlEdgeToBundle { arg bndl, outlet, inlet;
		var e, srcBus, tgtBus, srcProc, tgtProc;
	
		TypeSafe.checkArgClasses( thisMethod, [ bndl,      outlet,              inlet       ],
		                                      [ OSCBundle, NuagesControlOutlet, NuagesControlInlet ],
		                                      [ false,     false,               false       ]);
		                                      
		if( outlet.visible != inlet.visible, {
			"Should not connect visible to invisible ports".warn;
		});
		
		e  = NuagesE( outlet, inlet );  // this automatically adds the edge
		
		// establish control bus connection
		^e;
	}
	
	addFirst { arg vertex;
		this.prCheckVertexLinked( vertex, thisMethod );
		
		if( head.notNil, {
			vertex.protSetSucc( head );
			head.protSetPred( vertex );
		});
		head = vertex;
		if( tail.isNil, {
			tail = vertex;
		});
		size = size + 1;
	}
	
	add { arg vertex;
		this.prCheckVertexLinked( vertex, thisMethod );

		if( tail.notNil, {
			vertex.protSetPred( tail );
			tail.protSetSucc( vertex );
		});
		tail = vertex;
		if( head.isNil, {
			head = vertex;
		});
		size = size + 1;
	}
	
	addAfter { arg vertex, pred;
		this.prCheckVertexLinked( vertex, thisMethod );

		if( pred.notNil, {
			vertex.protSetPred( pred );
			vertex.protSetSucc( pred.succ );
			pred.protSetSucc( vertex );
			if( pred.succ.notNil, {
				pred.succ.protSetPred( vertex );
			}, {
				tail = vertex;
			});
		}, {
// implied by prCheckVertexLinked
//			vertex.protSetPred( nil );
//			vertex.protSetSucc( nil );
			tail = vertex;
			head = vertex;
		});
		size = size + 1;
	}
	
	addAfterToBundle { arg bndl, vertex, pred;
		this.addAfter( vertex, pred );
		bndl.add( vertex.proc.group.moveAfterMsg( pred.proc.group ));
	}
	
	moveAfterToBundle { arg bndl, vertex, pred;
		this.moveAfter( vertex, pred );
		bndl.add( vertex.proc.group.moveAfterMsg( pred.proc.group ));
	}
	
	moveAfter { arg vertex, pred;
		this.remove( vertex );
		this.addAfter( vertex, pred );
	}
	
	remove { arg vertex;
		if( head == vertex, { head = vertex.succ });
		if( tail == vertex, { tail = vertex.pred });
		vertex.protRemove;
		size = size - 1;
	}
	
	pop { 
		var vertex;
		^if( tail.notNil, {
			vertex = tail;
			this.remove( tail );
			vertex;
		}, {
			nil;
		});
		
	}
	
	popFirst { 
		var vertex;
		^if( head.notNil, {
			vertex = head;
			this.remove( head );
			vertex;
		}, {
			nil;
		});
	}
	
	first { ^head }
	last  { ^tail }
	
	at { arg index;
		var i = 0, vertex;
		if( index < 0 or: { index >= size }, { ^nil });
		
		if( index < (size - index), {
			vertex = head;
			while({ i != index }, {
				vertex = vertex.succ;
				i = i + 1;
			});
		}, {
			i = size - 1;
			vertex = tail;
			while({ i != index }, {
				vertex = vertex.pred;
				i = i - 1;
			});
		});
		^vertex;
	}
	
	put { arg index, vertex;
		var oldVertex;

		this.prCheckVertexLinked( vertex, thisMethod );
		
		oldVertex = this.at( index );
		if( oldVertex.isNil, {
			Error( "Index % out of bounds".format( index )).throw;
		});
		
		vertex
			.protSetPred( oldVertex.pred )
			.protSetSucc( oldVertex.succ );
			
		if( oldVertex.pred.notNil, {
			oldVertex.pred.protSetSucc( vertex );
		}, {
			head = vertex;
		});
		if( oldVertex.succ.notNil, {
			oldVertex.succ.protSetPred( vertex );
		}, {
			tail = vertex;
		});
		oldVertex
			.protSetSucc( nil )
			.protSetPred( nil );
			
//		vertex.protSetGraph( this );
	}
	
	removeAt { arg index;
		var vertex = this.at( index );
		if( vertex.notNil, {
			this.remove( vertex );
		});
		^vertex;
	}
	
	// Algorithm based loosely on Marchetti-Spaccamela / Nanni / Rohnert,
	// as shown in PK fig. 3
	prCheckAddedEdge { arg bndl, e;
		var visited, loBound, upBound, source, target;
		
		source	= e.source.vertex;
		target	= e.target.vertex;
		loBound	= this.indexOf( target );
		upBound	= this.indexOf( source );
		if( loBound <= upBound, {
			visited = IdentitySet.new;
			if( (loBound == upBound) or: {Êthis.prDiscovery( visited, target, upBound ).not }, {
				// Cycle --> Abort
				e.protRemove;
				^false;
			});
			this.prShiftToBundle( bndl, visited, target, source );
		});
		^true;
	}

	// note: assumes audio rate
	prDiscovery { arg visited, vertex, upBound;
		var s, sOrd;
		visited.add( vertex );
		vertex.audioOutlets.do({ arg outlet;
			outlet.edges.do({ arg e;
				s	= e.target.vertex;
				sOrd	= this.indexOf( s );
				if( sOrd == upBound, { ^false });  // cycle detected
				// visit s if it was not not already visited
				// and if it is in affected region 
				if( visited.includes( s ).not and: { sOrd < upBound }, {
					this.prDiscovery( visited, s, upBound );
				});
			});
		});
		^true;
	}

	prShiftToBundle { arg bndl, visited, target, source;
		var l, shift = 0, w, succ, pred;
		// shift vertices in affected region down ord
		w	= target;
		pred	= source;
		while({ w != source }, {
			succ = w.succ;
			if( visited.includes( w ), {
				// move w to the tail
				visited.remove( w );
				this.moveAfterToBundle( bndl, w, pred );
				pred	= w;
			});
			w = succ;
		});
	}
	
	prCheckVertexLinked { arg vertex, method;
//		if( vertex.isLinked, { ... })
		if( vertex.isLinked or: {Êhead == vertex }, {
			Error( "%:% failed: Vertex % already linked".format( method.ownerClass.name, method.name, vertex )).throw;
		});
	}
}
