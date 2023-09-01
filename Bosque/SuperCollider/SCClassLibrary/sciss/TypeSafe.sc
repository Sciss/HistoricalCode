/**
 *	(C)opyright 2006-2008 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies: (none)
 *
 *	@version	0.12, 29-Oct-08
 *	@author	Hanns Holger Rutz
 */
TypeSafe {
	classvar <>enabled = true;

	*methodInform { arg method, message;
		(method.ownerClass.name ++ ":" ++ method.name ++ " : " ++ message).inform;
	}

	*methodWarn { arg method, message;
		(method.ownerClass.name ++ ":" ++ method.name ++ " : " ++ message).warn;
	}

	*methodError { arg method, message;
		(method.ownerClass.name ++ ":" ++ method.name ++ " failed: " ++ message).error;
	}

	*checkArgResp { arg method, obj ... selectors;
		if( enabled.not, { ^true; });
		
		selectors.do({ arg selector;
			if( obj.respondsTo( selector ).not, {
				(method.ownerClass.name ++ ":" ++ method.name ++ " : Argument type mismatch : " ++
				obj.class ++ " does not respond to '" ++ selector ++ "'").error;
				^false;
			});
		});
		^true;
	}
	
	*checkInterface { arg method, obj, interf, nilAllowed;
		if( enabled.not, { ^true; });

		if( obj.isNil, {
			if( nilAllowed.not, {
				(method.ownerClass.name ++ ":" ++ method.name ++
					" : Argument type mismatch : nil not allowed").error;
				^false;
			});
		}, { if( obj.isKindOf( interf ).not, {
			^this.checkArgResp( method, obj, *interf.names );
		})});
		^true;
	}

	*checkAnyArgClass { arg method, obj ... classes;
		if( enabled.not, { ^true; });
		
		classes.do({ arg class;
			if( obj.isKindOf( class ), { ^true; });
		});
		
		(method.ownerClass.name ++ ":" ++ method.name ++
			" : Argument type mismatch").error;
		^false;
	}

	*checkArgClass { arg method, obj, kind, nilAllowed = true;
		if( enabled.not, { ^true; });
		
		if( nilAllowed.not && obj.isNil, {
			(method.ownerClass.name ++ ":" ++ method.name ++
				" : Argument type mismatch : nil not allowed").error;
			^false;
		}, { if( obj.isNil.not && obj.isKindOf( kind ).not, {
			(method.ownerClass.name ++ ":" ++ method.name ++ " : Argument type mismatch : " ++
				obj.class ++ " is not a kind of " ++ kind).error;
			^false;
		})});
		^true;
	}

	*checkArgClasses { arg method, args, classes, nilAllowed;
		var success = true;
		
		if( enabled.not, { ^true; });
	
//		method.argNames.postln;
		
		args.do({ arg agga, idx;
			if( nilAllowed[ idx ].not && agga.isNil, {
				(method.ownerClass.name ++ ":" ++ method.name ++
					" : Argument type mismatch (" ++ method.argNames[ idx + 1 ] ++
					") : nil not allowed").error;
				success = false;
			}, { if( agga.isNil.not && agga.isKindOf( classes[ idx ]).not, {
				(method.ownerClass.name ++ ":" ++ method.name ++
					" : Argument type mismatch (" ++ method.argNames[ idx + 1 ] ++
					") : " ++ agga.class ++ " is not a kind of " ++ classes[ idx ]).error;
				success = false;
			})});
		});
		^success;
	}
}