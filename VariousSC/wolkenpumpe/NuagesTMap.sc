/**
 *	NuagesTMap
 *	(Wolkenpumpe)
 *
 *	(C)opyright 2005-2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.10, 28-Aug-09
 *	@author	Hanns Holger Rutz
 */
NuagesTMap : NuagesT {
	// arguments to the transaction
	var sourceVertex, outletIndex, targetVertex, attrIndex, fdt;

	// the result
	var <edge, <>pEdgeID;
	
	*new {Êarg nuages, sourceVertex, outletIndex, targetVertex, attrIndex, fdt;
		^super.new( nuages ).prInitProcTrns( sourceVertex, outletIndex, targetVertex, attrIndex, fdt );
	}
	
	prInitProcTrns { arg argSourceVertex, argOutletIndex, argTargetVertex, argAttrIndex, argFdT;
		sourceVertex	= argSourceVertex;
		outletIndex	= argOutletIndex;
		targetVertex	= argTargetVertex;
		attrIndex		= argAttrIndex;
		fdt			= argFdT;

		TypeSafe.checkArgClasses( thisMethod, [ sourceVertex, outletIndex, targetVertex, attrIndex, fdt          ],
		                                      [ NuagesV,      Integer,     NuagesV,      Integer,   SimpleNumber ],
		                                      [ false,        false,       false,        false,     false        ]);
	}
	
	storeArgs { ^[ nuages, sourceVertex, outletIndex, targetVertex, attrIndex, fdt ]}
	
	protPerform { arg bndl, recall;
		var attr, unit, outlet, sidelet;

		unit		= targetVertex.proc.unit;
		if( unit.isNil, { ^false });
		attr		= unit.attributes[ attrIndex ];
		if( attr.isNil, { ^false });
		// mix 0 means proc is paused --> set it so something slightly above 0
		// so that it unpauses before being mapped
		if( (attr.name === \mix) and: { attr.getValue( unit ) == 0 }, {
			attr.setValueToBundle( bndl, unit, 1.0e-6 );
		});
		targetVertex.proc.addControlMapToBundle( bndl, attr, sourceVertex.proc );

		outlet	= sourceVertex.controlOutlets[ outletIndex ];
		sidelet	= targetVertex.controlSidelets[ attrIndex ];
		edge		= nuages.graph.addControlEdgeToBundle( bndl, outlet, sidelet );
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