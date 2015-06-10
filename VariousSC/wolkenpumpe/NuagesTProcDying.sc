/**
 *	NuagesTProcDying
 *	(Wolkenpumpe)
 *
 *	(C)opyright 2005-2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.11, 26-Aug-09
 *	@author	Hanns Holger Rutz
 */
NuagesTProcDying : NuagesT {
	// arguments to the transaction
	var vertex;
	
//	// the result
//	var pVertexID;

	*new {Êarg nuages, vertex;
		^super.new( nuages ).prInitProcTrns( vertex );
	}
	
	prInitProcTrns { arg argVertex, argMode, argFdT;
		vertex		= argVertex;
		TypeSafe.checkArgClass( thisMethod, vertex, NuagesV, false );
	}
	
	storeArgs { ^[ nuages, vertex ]}
	
	protPerform { arg bndl, recall;
//		if( recall, {
//			vertex = nuages.persistGet( pVertexID );
//		});
		vertex.proc.tryChanged( \dying );
		^true;
	}
	
//	protPersist {
//		pVertexID	= nuages.persistGetID( vertex );
//	}
}