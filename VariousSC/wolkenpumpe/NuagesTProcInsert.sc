/**
 *	NuagesTProcInsert
 *	(Wolkenpumpe)
 *
 *	(C)opyright 2005-2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.12, 31-Aug-09
 *	@author	Hanns Holger Rutz
 *
 *	@todo	argument should be a NuagesE instead of two vertices
 */
NuagesTProcInsert : NuagesT {
	// arguments to the transaction
	var vertex, edge, notify, metaData;

	// the result
	var <inEdge, <outEdge, <>pInEdgeID, <>pOutEdgeID;
	
	*new {Êarg nuages, vertex, edge, notify, metaData;
		^super.new( nuages ).prInitProcTrns( vertex, edge, notify, metaData );
	}
	
	prInitProcTrns { arg argVertex, argEdge, argNotify, argMetaData;
		vertex		= argVertex;
		edge			= argEdge;
		notify		= argNotify;
		metaData		= argMetaData;

		TypeSafe.checkArgClasses( thisMethod, [ vertex,  edge,    notify ],
		                                      [ NuagesV, NuagesE, Boolean ],
		                                      [ false,   false,   false ]);
	}
	
	storeArgs { ^[ nuages, vertex, edge, notify, metaData ]}
	
	protPerform { arg bndl, recall;
		var sourceOutlet, targetInlet, inlet, outlet;
				
//		if( recall, {
//			vertex	= nuages.persistGet( pVertexID );
//			edge		= nuages.persistGet( pEdgeID );
//		});
//

[ "ins1" ].postln;
		
		sourceOutlet	= edge.source;  // save these before calling removeEdgeToBundle!
		targetInlet	= edge.target;
		if( nuages.graph.removeEdgeToBundle( bndl, edge ).not, { ^false });
		outlet		= vertex.audioOutlets.first;
		inlet		= vertex.audioInlets.first;
		
		if( vertex.proc.unit.shouldConnectOutputFirst, {
[ "ins2" ].postln;
			outEdge		= nuages.graph.addAudioEdgeToBundle( bndl, outlet, targetInlet );
			if( outEdge.isNil, { ^false });
			inEdge		= nuages.graph.addAudioEdgeToBundle( bndl, sourceOutlet, inlet );
			if( inEdge.isNil, { ^false });
		}, {
[ "ins3" ].postln;
			inEdge		= nuages.graph.addAudioEdgeToBundle( bndl, sourceOutlet, inlet );
			if( inEdge.isNil, { ^false });
			outEdge		= nuages.graph.addAudioEdgeToBundle( bndl, outlet, targetInlet );
			if( outEdge.isNil, { ^false });
		});

[ "ins4" ].postln;

		// Now feed in silence to unconnected inputs
		// XXX this could also be more smart
		(vertex.proc.unit.numAudioInputs - 1).do({ arg i; var idx = i + 1;
			if( vertex.proc.getAudioInputBus( idx ).isNil, {
				vertex.proc.setAudioInputBusToBundle( bndl, nuages.getSilentBus( vertex.proc.unit.numInChannels( idx )), idx );
			});
		});

		if( recall, {
			nuages.persistPut( pInEdgeID, inEdge );
			inEdge.pID = pInEdgeID;
			nuages.persistPut( pOutEdgeID, outEdge );
			outEdge.pID = pOutEdgeID;
		});
				
		vertex.proc.setNeverPausingToBundle( bndl, true );
		vertex.proc.playToBundle( bndl, 0.0 ); // XXX --> NuagesLinkedGraph

		if( notify, { nuages.tryChanged( \edgeRemoved, edge )});
		if( notify, { nuages.tryChanged( \vertexAdded, vertex, metaData )});
		if( notify, { nuages.tryChanged( \edgeAdded, inEdge )});
		if( notify, { nuages.tryChanged( \edgeAdded, outEdge )});

		^true;
	}
	
	protPersist {
//		pVertexID		= nuages.persistGetID( vertex );
//		pEdgeID		= nuages.persistGetID( edge );

		pInEdgeID 	= nuages.persistNextID;
		nuages.persistPut( pInEdgeID, inEdge );
		inEdge.pID	= pInEdgeID;
		pOutEdgeID 	= nuages.persistNextID;
		nuages.persistPut( pOutEdgeID, outEdge );
		outEdge.pID	= pOutEdgeID;
	}

	storeModifiersOn { arg stream;
		super.storeModifiersOn( stream );
		if( pInEdgeID.notNil, {
			stream << ".pInEdgeID_(" <<< pInEdgeID << ")";
		});
		if( pOutEdgeID.notNil, {
			stream << ".pOutEdgeID_(" <<< pOutEdgeID << ")";
		});
	}
}