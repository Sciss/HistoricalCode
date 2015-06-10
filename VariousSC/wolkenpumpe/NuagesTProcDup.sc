/**
 *	NuagesTProcDup
 *	(Wolkenpumpe)
 *
 *	(C)opyright 2005-2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.11, 26-Aug-09
 *	@author	Hanns Holger Rutz
 *
 *	@todo	inBus and outBus are not properly handled for recall
 */
NuagesTProcDup : NuagesT {
	// arguments to the transaction
	var orig, pred, metaData;
	
	// the result
	var <succ, <>pSuccID;

	*new {Êarg nuages, orig, pred, metaData;
		^super.new( nuages ).prInitProcTrns( orig, pred, metaData );
	}
	
	prInitProcTrns { arg argOrig, argPred, argMetaData;
		orig		= argOrig;
		pred		= argPred;
		metaData	= argMetaData;

		TypeSafe.checkArgClasses( thisMethod, [ orig,       pred ],
		                                      [ NuagesProc, NuagesProc ],
		                                      [ false,      true ]);
	}
	
	usesRandom { ^true }
	
	storeArgs { ^[ nuages, orig, pred, metaData ]}
	
	protPerform { arg bndl, recall;
		var bus;
		
//		if( recall, {
//			orig = nuages.persistGet( pOrigID );
//			pred = if( pPredID.notNil, { nuages.persistGet( pPredID )});
//		});
		
		succ = orig.duplicate;
		if( pred.notNil, {
			bus = pred.getAudioOutputBusToBundle( bndl );
			succ.unit.setAudioInputBusToBundle( bndl, bus );
			succ.setAudioOutputBusToBundle( bndl, bus );
			bndl.add( succ.group.moveAfterMsg( pred.group ));
			succ.setNeverPausingToBundle( bndl, true );
		}, {
			bus = succ.getAudioOutputBusToBundle( bndl );
		});
		if( recall, {
			nuages.persistPut( pSuccID, succ );
//			succ.pID = pSuccID;
"NuagesTProcDup : RECALL NOT WORKING : NEED TO SWITCH TO VERTEX".error;
		});
		nuages.tryChanged( \procAdded, succ, metaData );
		if( pred.notNil, {
			pred.addTarget( target: succ ); // XXX
		});
		succ.playToBundle( bndl, 0.0 );
		^true;
	}
	
	protPersist {
		pSuccID	= nuages.persistNextID;
		nuages.persistPut( pSuccID, succ );
//		succ.pID = pSuccID;
"NuagesTProcDup : RECORD NOT WORKING : NEED TO SWITCH TO VERTEX".error;
//		pOrigID	= nuages.persistGetID( orig );
//		pPredID	= if( pred.notNil, { nuages.persistGetID( pred )});
	}

	storeModifiersOn { arg stream;
		super.storeModifiersOn( stream );
		if( pSuccID.notNil, {
			stream << ".pSuccID_(" <<< pSuccID << ")";
		});
	}
}