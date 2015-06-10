/**
 *	NuagesTProcRemove
 *	(Wolkenpumpe)
 *
 *	(C)opyright 2005-2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.11, 26-Aug-09
 *	@author	Hanns Holger Rutz
 */
NuagesTProcRemove : NuagesT {
	// arguments to the transaction
	var vertex, force;
	
//	// the result
//	var pVertexID;

	*new {Êarg nuages, vertex, force;
		^super.new( nuages ).prInitProcTrns( vertex, force );
	}
	
	prInitProcTrns { arg argVertex, argForce;
		vertex		= argVertex;
		force		= argForce;

		TypeSafe.checkArgClasses( thisMethod, [ vertex,       force ],
		                                      [ NuagesV, Boolean ],
		                                      [ false,        false ]);
	}
	
	storeArgs { ^[ nuages, vertex, force ]}
	
	protPerform { arg bndl, recall;
//		if( recall, {
//			vertex = nuages.persistGet( pVertexID );
//		});

//		proc.tryChanged( \dying );
		this.prRecurse( bndl, vertex, force );
		^true;
	}
	
	prRecurse { arg bndl, vertex, force;
		var sources, targets, changed, unit, reconnects, e, e2, inlet, outlet;
		
		unit = vertex.proc.unit;
		
//[ "prRecurve", unit ].postln;
		
		if( force or: { unit.numVisibleAudioOutputs > 0 }, { // not allowed for OutputUnit !
			nuages.stopLines( vertex );
		
//			targets = proc.targets.copy; // XXX deep?
			if( force or: { unit.isKindOf( NuagesUGen )}, {  // it's a generator
				// recurse and delete targets whose
				// only input we are
				changed = true;
				while({ changed }, {
					changed = block { arg break;
						vertex.audioOutlets.do({ arg outlet;
							outlet.edges.do({ arg edge;
//[ "checkin", edge.target.vertex.proc.unit, edge.target.vertex.audioInlets.collect({ arg inlet; inlet.edges.size })].postln;
								if( edge.target.vertex.audioInlets.collect({ arg inlet; inlet.edges.size }).sum == 1, { // we are the only source
									this.prRecurse( bndl, edge.target.vertex, true );
									break.( true );
								});
							});
						});
						false;
					};
				});
				// second loop
			});
			// figure out which edges to re-connect
			vertex.audioInlets.do({ arg inlet, idx;
//[ "1", idx, inlet.readOnly ].postln;
				if( inlet.readOnly.not, {
					inlet.edges.do({ arg inEdge;
//[ "2", inEdge, inEdge.source.vertex.proc.unit ].postln;
						vertex.audioOutlets[ idx ].do({ arg outlet;
							outlet.edges.do({ arg outEdge;
//[ "reconnect", inEdge.source.vertex.proc.unit, outEdge.target.vertex.proc.unit ].postln;
								reconnects = reconnects.add( inEdge.source -> outEdge.target );
							});
						});
					});
				});
			});
			// remove remaining edges
			[ vertex.audioInlets, vertex.audioOutlets ].do({ arg ports; ports.do({ arg port;
				port.edges.do({ arg edge;
//[ "edge", edge.source.vertex.proc.unit, edge.target.vertex.proc.unit ].postln;
					nuages.graph.removeEdgeToBundle( bndl, edge );
					nuages.tryChanged( \edgeRemoved, edge );
				});
			})});
			// perform reconnects
			reconnects.do({ arg tuple;
				outlet = tuple.key; inlet = tuple.value;
				e2 = outlet.edges.detect({ arg e; e.target == inlet });
				if( e2.isNil, { // avoid double connects!
					e = nuages.graph.addAudioEdgeToBundle( bndl, outlet, inlet );
//					if( e.isNil, { ^false }); // XXX
					nuages.tryChanged( \edgeAdded, e );
				}, {
// XXX : otherwise insert gain 0 dB !
				});
			});
			
			// XXX control outlets

			vertex.proc.tryChanged( \dying );
			vertex.proc.disposeToBundle( bndl );
		});
	}
	
	protPersist {
//		pVertexID	= nuages.persistGetID( vertex );
"NuagesTProvRemove : RECORD NOT YET IMPLEMETED : NEED TO STORE RECONNECT EDGES".error;
	}
}