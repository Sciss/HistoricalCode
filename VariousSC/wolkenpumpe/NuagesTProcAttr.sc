/**
 *	NuagesTProcAttr
 *	(Wolkenpumpe)
 *
 *	(C)opyright 2005-2009 Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.11, 26-Aug-09
 *	@author	Hanns Holger Rutz
 */
NuagesTProcAttr : NuagesT {
	// arguments to the transaction
	var vertex, attrName, newNormVal, fdt, stopLine;
	
	// the result
	var pVertexID;

	*new {Êarg nuages, vertex, attrName, newNormVal, fdt, stopLine;
		^super.new( nuages ).prInitProcTrns( vertex, attrName, newNormVal, fdt, stopLine );
	}
	
	prInitProcTrns { arg argVertex, argAttrName, argNewNormVal, argFdT, argStopLine;
		vertex		= argVertex;
		attrName		= argAttrName;
		newNormVal	= argNewNormVal;
		fdt			= argFdT;
		stopLine		= argStopLine;

		TypeSafe.checkArgClasses( thisMethod, [ vertex,       attrName, newNormVal,   fdt,          stopLine ],
		                                      [ NuagesV, Symbol,   SimpleNumber, SimpleNumber, Boolean ],
		                                      [ false,        false,    false,        false,        false ]);
	}
	
	storeArgs { ^[ nuages, vertex, attrName, newNormVal, fdt, stopLine ]}
	
	protPerform { arg bndl, recall;
		var unit, oldVal, newVal, map, attr;
		
		unit		= vertex.proc.unit;
		if( unit.isNil, { ^false });
		attr		= unit.getAttribute( attrName );
		if( attr.isNil, { ^false });
		newVal	= attr.spec.map( newNormVal );
		oldVal	= attr.getValue( unit );
		if( oldVal == newVal, { ^false });

		if( stopLine, { nuages.stopLine( vertex, attr )});

		map	= IdentityDictionary[ attr.name -> newVal ];
		if( attr.shouldFade, {
			vertex.proc.crossFadeToBundle( bndl, map, fdt );
		}, {
			vertex.proc.applyAttrToBundle( bndl, map );
		});
		^true;
	}
}