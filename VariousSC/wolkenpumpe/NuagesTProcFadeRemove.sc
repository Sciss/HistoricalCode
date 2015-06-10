/**
 *	NuagesTProcFadeRemove
 *	(Wolkenpumpe)
 *
 *	(C)opyright 2005-2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.11, 26-Aug-09
 *	@author	Hanns Holger Rutz
 */
NuagesTProcFadeRemove : NuagesT {
	// arguments to the transaction
	var vertex, fdt;
	
//	// the result
//	var pVertexID;

	*new {Êarg nuages, vertex, fdt;
		^super.new( nuages ).prInitProcTrns( vertex, fdt );
	}
	
	prInitProcTrns { arg argVertex, argFdT;
		vertex		= argVertex;
		fdt			= argFdT;

		TypeSafe.checkArgClasses( thisMethod, [ vertex,       fdt ],
		                                      [ NuagesV, SimpleNumber ],
		                                      [ false,        false ]);
	}

	storeArgs { ^[ nuages, vertex, fdt ]}
	
	protPerform { arg bndl, recall;
		var upd;
		
//		if( recall, {
//			vertex = nuages.persistGet( pVertexID );
//		});
//
		vertex.proc.tryChanged( \dying );
		vertex.proc.stopToBundle( bndl, fdt );
		upd = UpdateListener.newFor( vertex.proc, { arg upd, proc, what;
			switch( what,
			\paused, {
				upd.remove;
				// note: in recall mode we have already recorded the
				// corresponding taskRemoveProc invocation, so don't
				// try to call it twice!
				if( recall.not, { nuages.taskSched( nil, \taskRemoveProc, vertex )});
			},
			\disposed, {
				upd.remove;
			});
		});
		^true;
	}
	
//	protPersist {
//		pVertexID	= nuages.persistGetID( vertex );
//	}
}