/**
 *	@version	0.1, 03-May-06
 *	@author	Hanns Holger Rutz
 */
MethodMapper {
	var map;
	var objDep = nil;
	var cmdP = false;
	
	*new {
		^super.new.prInitMethodMapper;
	}
	
	prInitMethodMapper {
		map = IdentityDictionary.new;
	}

	map { arg methodName, func;
		map.put( methodName.asSymbol, func );
	}
	
	unmap { arg methodName;
		map.removeAt( methodName.asSymbol );
	}
	
	clear {
		if( objDep.notNil, {
			this.removeUpdate;
		});
		if( cmdP, {
			this.removeCmdPeriod;
		});
		map.clear;
	}
	
	addUpdate { arg obj, func;
		if( objDep.notNil, {
			TypeSafe.methodWarn( thisMethod, "Cannot add more than one updater!" );
		}, {
			this.map( \update, func );
			obj.addDependant( this );
			objDep = obj;
		});
	}
	
	removeUpdate {
		if( objDep.notNil, {
			this.unmap( \update );
			objDep.removeDependant( this );
			objDep = nil;
		});
	}
	
	addCmdPeriod { arg func;
		if( cmdP, {
			TypeSafe.methodWarn( thisMethod, "Cannot add more than one cmd period!" );
		}, {
			this.map( \cmdPeriod, func );
			CmdPeriod.add( this );
			cmdP = true;
		});
	}
	
	removeCmdPeriod {
		if( cmdP, {
			this.unmap( \cmdPeriod );
			CmdPeriod.remove( this );
			cmdP = false;
		});
	}
	
	update { arg ... args;
		^map[ \update ].valueArray( args );
	}
	
	doesNotUnderstand { arg selector ... args;
		^map[ selector ].valueArray( args );
	}
}