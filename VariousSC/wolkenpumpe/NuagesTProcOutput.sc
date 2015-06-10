/**
 *	NuagesTProcOutput
 *	(Wolkenpumpe)
 *
 *	(C)opyright 2005-2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.12, 31-Aug-09
 *	@author	Hanns Holger Rutz
 */
NuagesTProcOutput : NuagesT {
	// arguments to the transaction
	var vertex, initialGain, metaData;
	
	// the result
	var <oVertex, <edge, <>pOVertexID, <>pEdgeID;
//	var pChoice, pAttrs;

	*new {Êarg nuages, vertex, initialGain, metaData;
		^super.new( nuages ).prInitProcTrns( vertex, initialGain, metaData );
	}
	
	prInitProcTrns { arg argVertex, argInitialGain, argMetaData;
		vertex		= argVertex;
		initialGain	= argInitialGain;
		metaData		= argMetaData;

		TypeSafe.checkArgClasses( thisMethod, [ vertex,  initialGain  ],
		                                      [ NuagesV, SimpleNumber ],
		                                      [ false,   true         ]);
	}
	
	usesRandom { ^true }
	
	storeArgs { ^[ nuages, vertex, initialGain, metaData ]}
		
	protPerform { arg bndl, recall;
		var unit, oProc, unitAttr, playBus;
		
//		if( recall, {
//			vertex	= nuages.persistGet( pVertexID );
////		}, {
////			pChoice	= nuages.uf.audioOutputs.size.rand;
//		});
//		pChoice	= nuages.uf.audioOutputs.size.rand;
//		unit = nuages.uf.makeUnit( nuages.uf.audioOutputs[ pChoice ], metaData );
		unit = nuages.uf.makeUnit( nuages.uf.preferredAudioOutput, metaData );
//unit = uf.makeUnit( NuagesPOutputUnit, metaData ); // XXX
//unit.setPreferredNumOutChannels( Wolkenpumpe.masterNumChannels );

//[ "Aqui", unit.getAudioOutputBus, nuages.masterBus, unit.isAudioInputReadOnly( 0 )].postln;
unit.setPreferredNumInChannels( vertex.proc.unit.numOutChannels );
//[ "GAIN", initialGain ].postln;
		if( initialGain.notNil, {
			unitAttr = unit.attributes.detect({ arg attr; attr.name === \volume });
			if( unitAttr.notNil, {
				unitAttr.setValueToBundle( bndl, unit, initialGain.dbamp );
			}, {
				TypeSafe.methodWarn( thisMethod, "Unit does not have standard gain attribute" );
			});
		});
"::::1".postln;
//		unit.setAudioOutputBusToBundle( bndl, nuages.masterBus );
		
		oProc = NuagesProc.newToBundle( bndl, nuages );
		oProc.setUnitToBundle( bndl, unit );
		
"::::2".postln;
//		if( unit.hasPreferredNumInChannels, {
//"::::3".postln;
//			oProc.makeAudioInputBusToBundle( bndl, 0, unit.protPreferredNumInChannels );
//		});
"::::4".postln;
	
//		oProc.setAudioOutputBusToBundle( bndl, nuages.masterBus );
//[ "Aqui2", unit.getAudioOutputBus, oProc.getAudioOutputBus ].postln;
//"::::3".postln;
		oVertex = NuagesV( oProc );
		nuages.graph.addAfterToBundle( bndl, oVertex, vertex );

//		proc.addTarget( target: oProc );
		edge = nuages.graph.addAudioEdgeToBundle( bndl, vertex.audioOutlets.first, oVertex.audioInlets.first );
		if( edge.isNil, { ^false });
//[ "Aqui3", unit.getAudioOutputBus, oProc.getAudioOutputBus ].postln;

		if( unit.respondsTo( \preferredPlayBus ), {
			playBus = unit.preferredPlayBus;
		}, {
			playBus = nuages.masterBus;
		});
		oProc.addPlayBusToBundle( bndl, playBus ); // XXX --> NuagesLinkedGraph ?
//		oProc.setAudioOutputBusToBundle( bndl, nuages.masterBus );
		oProc.playToBundle( bndl ); // XXX --> NuagesLinkedGraph ?
//[ "Aqui4", unit.getAudioOutputBus, oProc.getAudioOutputBus ].postln;

		if( recall, {
			nuages.persistPut( pOVertexID, oVertex );
			oVertex.pID = pOVertexID;
			nuages.persistPut( pEdgeID, edge );
			edge.pID = pEdgeID;
		});
		
		nuages.tryChanged( \vertexAdded, oVertex );
		nuages.tryChanged( \edgeAdded, edge );
		
		^true;
	}

	protPersist {
//		pAttrs = oProc.unit.attributes
//			.select({ arg attr; attr.getValue( unit ) != attr.spec.default })
//			.collect({ arg attr; (attr.name -> attr.getValue( unit ))}).as( IdentitySet );
//		pVertexID		= nuages.persistGetID( vertex );
		pOVertexID	= nuages.persistNextID;
		nuages.persistPut( pOVertexID, oVertex );
		oVertex.pID	= pOVertexID;
		pEdgeID		= nuages.persistNextID;
		nuages.persistPut( pEdgeID, edge );
		edge.pID		= pEdgeID;
	}

	storeModifiersOn { arg stream;
		super.storeModifiersOn( stream );
		if( pOVertexID.notNil, {
			stream << ".pOVertexID_(" <<< pOVertexID << ")";
		});
		if( pEdgeID.notNil, {
			stream << ".pEdgeID_(" <<< pEdgeID << ")";
		});
	}
}