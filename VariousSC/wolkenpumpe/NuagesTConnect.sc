/**
 *	NuagesTConnect
 *	(Wolkenpumpe)
 *
 *	(C)opyright 2005-2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.10, 02-Aug-09
 *	@author	Hanns Holger Rutz
 */
NuagesTConnect : NuagesT {
	// arguments to the transaction
	var sourceVertex, outletIndex, targetVertex, inletIndex;
	
	// the result
	var <edge, <>pEdgeID;

	*new {Êarg nuages, sourceVertex, outletIndex, targetVertex, inletIndex;
		^super.new( nuages ).prInitProcTrns( sourceVertex, outletIndex, targetVertex, inletIndex );
	}
	
	prInitProcTrns { arg argSourceVertex, argOutletIndex, argTargetVertex, argInletIndex;
		sourceVertex		= argSourceVertex;
		outletIndex		= argOutletIndex;
		targetVertex		= argTargetVertex;
		inletIndex		= argInletIndex;

		TypeSafe.checkArgClasses( thisMethod, [ sourceVertex, outletIndex, targetVertex, inletIndex ],
		                                      [ NuagesV,      Integer,     NuagesV,      Integer    ],
		                                      [ false,        false,       false,        false      ]);
	}

	storeArgs { ^[ nuages, sourceVertex, outletIndex, targetVertex, inletIndex ]}
	
	protPerform { arg bndl, recall;
		var outlet, inlet;

		outlet	= sourceVertex.audioOutlets[ outletIndex ];
		inlet	= targetVertex.audioInlets[ inletIndex ];
		edge		= nuages.graph.addAudioEdgeToBundle( bndl, outlet, inlet );
		if( edge.isNil, { ^false });

		if( recall, {
			nuages.persistPut( pEdgeID, edge );
			edge.pID = pEdgeID;
		});

		nuages.tryChanged( \edgeAdded, edge );
		^true;
	}
	
	protPersist {
		pEdgeID 	= nuages.persistNextID;
		nuages.persistPut( pEdgeID, edge );
		edge.pID = pEdgeID;
	}

	storeModifiersOn { arg stream;
		super.storeModifiersOn( stream );
		if( pEdgeID.notNil, {
			stream << ".pEdgeID_(" <<< pEdgeID << ")";
		});
	}
}