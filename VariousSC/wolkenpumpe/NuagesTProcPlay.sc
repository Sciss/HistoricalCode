/**
 *	NuagesTProcPlay
 *	(Wolkenpumpe)
 *
 *	(C)opyright 2005-2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.11, 26-Aug-09
 *	@author	Hanns Holger Rutz
 */
NuagesTProcPlay : NuagesT {
	// arguments to the transaction
	var vertex, mode, fdt;
	
//	// the result
//	var pVertexID;

	*new {Êarg nuages, vertex, mode, fdt;
		^super.new( nuages ).prInitProcTrns( vertex, mode, fdt );
	}
	
	prInitProcTrns { arg argVertex, argMode, argFdT;
		vertex	= argVertex;
		mode		= argMode;
		fdt		= argFdT;

		TypeSafe.checkArgClasses( thisMethod, [ vertex, mode, fdt ],
		                                      [ NuagesV, Symbol, SimpleNumber ],
		                                      [ false, false, true ]);
	}
	
	storeArgs { ^[ nuages, vertex, mode, fdt ]}
	
	protPerform { arg bndl, recall;
//		if( recall, {
//			vertex = nuages.persistGet( pVertexID );
//		});
		switch( mode, \play, {
			vertex.proc.playToBundle( bndl, fdt );
		}, \stop, {
			vertex.proc.stopToBundle( bndl, fdt );
		}, {
			TypeSafe.methodError( thisMethod, "Illegal mode '%'".format( mode ));
			^false;
		});
		^true;
	}
	
//	protPersist {
//		pVertexID	= nuages.persistGetID( vertex );
//	}
}