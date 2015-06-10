/**
 *	NuagesTProcCreate
 *	(Wolkenpumpe)
 *
 *	(C)opyright 2005-2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.12, 26-Aug-09
 *	@author	Hanns Holger Rutz
 *
 *	@todo	inBus and outBus are not properly handled for recall
 */
NuagesTProcCreate : NuagesT {
	// arguments to the transaction
	var name, notify, metaData;
	
	// the result
	var <vertex, <>pVertexID;

	*new {Êarg nuages, name, notify, metaData;
		^super.new( nuages ).prInitProcTrns( name, notify, metaData );
	}
	
	prInitProcTrns { arg argName, argNotify, argMetaData;
		name		= argName;
		notify	= argNotify;
		metaData	= argMetaData;

		TypeSafe.checkArgClasses( thisMethod, [ name,   notify ],
		                                      [ Symbol, Boolean ],
		                                      [ false,  false ]);
	}
	
	usesRandom { ^true }
	
	storeArgs { ^[ nuages, name, notify, metaData ]}
	
	protPerform { arg bndl, recall;
//		nuages.createProcToBundle( bndl, name, inBus, outBus, metaData );
		var clazz, unit, proc;
				
		clazz = name.asClass;
		if( clazz.isNil, {
			(thisMethod.asString ++ " - no generator class '" ++ name ++ "'").error;
			^nil;
		});
// UNCOMMENT THIS AS SOON AS THE GENERATOR UNITS HAVE NEW ATTR SUPPORT
		if( recall, {
//			unit = clazz.new( nuages.server );
//			pAttrs.keysValuesDo({ arg name, value; unit.getAttribute( name ).setValueToBundle( bndl, unit, value )});
unit = nuages.uf.makeUnit( clazz );
			
		}, {
			unit = nuages.uf.makeUnit( clazz );
		});
		proc = NuagesProc.newToBundle( bndl, nuages );
		proc.setUnitToBundle( bndl, unit );
//		if( recall, {
//			if( pInBusID.notNil, {
//				inBus = nuages.persistGet( pInBusID );
//			});
//			if( pOutBusID.notNil, {
//				outBus = nuages.persistGet( pOutBusID );
//			});
//		});
//		if( inBus.notNil, {
//			unit.setAudioInputBusToBundle( bndl, inBus );
//		});
//		if( outBus.notNil, {
//			proc.setAudioOutputBusToBundle( bndl, outBus );
//		});
		vertex = NuagesV( proc );
		if( recall, {
			nuages.persistPut( pVertexID, vertex );
			vertex.pID = pVertexID;
		});
		nuages.graph.addFirst( vertex );
		if( notify, { nuages.tryChanged( \vertexAdded, vertex, metaData )});
		^true;
	}
	
	protPersist {
//		vertex.persist;
		pVertexID = nuages.persistNextID;
		nuages.persistPut( pVertexID, vertex );
		vertex.pID = pVertexID;
	}
	
	storeModifiersOn { arg stream;
		super.storeModifiersOn( stream );
		if( pVertexID.notNil, {
			stream << ".pVertexID_(" <<< pVertexID << ")";
		});
	}
}