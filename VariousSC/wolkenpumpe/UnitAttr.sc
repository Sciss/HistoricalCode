/**
 *	(C)opyright 2006-2009 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: TypeSafe
 *
 *	@author	Hanns Holger Rutz
 *	@version	0.18, 28-Aug-09
 */
UnitAttr : Object {
//	var <unit;
	var <name;
	var <spec;
	var <type;
	var <getter;
	var <setter;
	var <updates;
	var <shouldFade;
	var <canMap;

	*make { arg name, spec, type, getter, setter, updates, shouldFade = false, canMap = true;
		^this.new( name, spec, type ?? {Êif( spec.minval + spec.maxval == 0, \pan, \normal )}, shouldFade: shouldFade, canMap: canMap );
	}
	
	*new { arg name, spec, type, getter, setter, updates, shouldFade = true, canMap;
		^super.new.prInitUnitAttr( name, spec, type, getter, setter, updates, shouldFade, canMap ? shouldFade.not );
	}
	
	prInitUnitAttr { arg argName, argSpec, argType, argGetter, argSetter, argUpdates, argShouldFade, argCanMap;
//		var str;

		TypeSafe.checkArgClasses( thisMethod, [ argName, argSpec,     argType, argGetter, argSetter, argUpdates, argShouldFade, argCanMap ],
		                                      [ Symbol,  ControlSpec, Symbol,  Symbol,    Symbol,    Set,        Boolean,       Boolean ],
		                                      [ false,   false,       false,   true,      true,      true,       false,         false ]);

//		unit		= argUnit;
		name		= argName;
		spec		= argSpec;
		type		= argType;
		getter	= argGetter;
		setter	= argSetter;
		updates	= argUpdates;
		shouldFade = argShouldFade;
		canMap	= argCanMap;
	}
	
	kr { arg default, lag;
//		^NamedControl.kr( name, default ?? { spec.default }, lag ).asArray.flatten.at( 0 ); // XXX stupidity, it always returns an array, sometimes nested!!
		^if( lag.isNil, {
			Control.names( name ).kr( default ?? { spec.default })
		}, {
			LagControl.names( name ).kr( default ?? { spec.default }, lag )
		}).asArray.first;  // note that stupid LagControl also behaves different
	}

	ir { arg default;
//		^NamedControl.ir( name, default ?? { spec.default }).asArray.flatten.at( 0 ); // XXX stupidity, it always returns an array, sometimes nested!!
		^Control.names( name ).ir([ default ?? { spec.default }]);
	}
	
	getValue { arg unit;
		^if( getter.isNil, {
			unit.getAttrValue( this );
		}, {
			unit.perform( getter );
		});
	}
	
//	setValue { arg unit, value;
//		unit.perform( setter, value );
//	}
	
	setValueToBundle { arg bndl, unit, value;
		if( setter.isNil, {
			unit.setAttrValueToBundle( bndl, this, value );
		}, {
			unit.perform( (setter ++ "ToBundle").asSymbol, bndl, value );
		});
	}

	getNormalizedValue { arg unit;
		^spec.unmap( this.getValue( unit ));
	}

//	setNormalizedValue { arg unit, value;
//		this.setValue( unit, spec.map( value ));
//	}
}