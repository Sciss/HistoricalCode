/*
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 */
 
/** 
 *	@version	0.16, 21-Jul-08
 */
BosqueSessionCollection : Object {
//	var <forest;
	var coll;
	
	var debug = false;
	
	var <java, master;
	var <isSignificant;
	var mapNames;
	
	*new { arg significant = true, hasJava = true, uniqueNames = false;
		^super.new.prInit( significant, hasJava, uniqueNames );
	}
	
	prInit { arg significant, hasJava, uniqueNames;
		var forest;
		
		forest		= Bosque.default;
		isSignificant	= significant;
		coll			= List.new;
		master		= forest.master;
		if( hasJava, {
			java		= JavaObject( "de.sciss.timebased.session.BasicSessionCollection", forest.swing, significant );
		});
		if( uniqueNames, { mapNames = IdentityDictionary.new });
	}

	*createUniqueName { arg ptrn, count, theseNot;
		var name;
		
		ptrn = ptrn.asString;
		while({ name = (ptrn.format( count )).asSymbol; this.find( theseNot, name ).notNil }, {
			count = count + 1;
		});
		^name;
	}

	createUniqueName { arg ptrn, count;
		var name;
		
		ptrn = ptrn.asString;
		while({ name = (ptrn.format( count )).asSymbol; this.find( name ).notNil }, {
			count = count + 1;
		});
		^name;
	}
	
	*find { arg coll, name;
		^coll.detect({ arg object; object.name === name });
	}
	
	dispose {
//		upd.remove;
//		javaResp.remove; javaResp = nil;
//		javaNet.dispose; javaNet.destroy; javaNet = nil;
		if( java.notNil, { java.dispose; java.destroy; java = nil });
	}
	
	asSwingArg { ^java.asSwingArg }

	storeModifiersOn { arg stream;
		stream << ".addAll(this,";
		stream.nl; stream.tab;
		coll.asArray.storeOn( stream );
		stream << ")";
	}
	
	clear { arg source;
		var objects = coll;
		coll = List.new;
		if( java.notNil, { java.clear( master )});
		if( mapNames.notNil, { mapNames.clear });
		this.changed( \remove, *objects );
	}
	
	add { arg source, object;
		if( mapNames.notNil, {
			if( mapNames.includesKey( object.name ), {
				MethodError( "Already includes object named " ++ object.name.asCompileString, thisMethod ).throw;
			});
		});
		if( debug, {[ \add, this.hash, source, object ].postln });
		coll.add( object );
		if( mapNames.notNil, { mapNames.put( object.name, object )});
		if( java.notNil, { java.add( master, object )});
		this.changed( \add, object );
	}
	
	addAll { arg source, objects;
		if( debug, {[ \addAll, this.hash, source, objects ].postln });
		if( mapNames.notNil, {
			objects.do({ arg object; if( mapNames.includesKey( object.name ), {
				MethodError( "Already includes object named " ++ object.name.asCompileString, thisMethod ).throw;
			})});
		});
		coll.addAll( objects );
		if( mapNames.notNil, { objects.do({ arg object; mapNames.put( object.name, object )})});
		if( java.notNil, { java.addAll( master, objects.asList )});
		this.changed( \add, *objects );
	}

	remove { arg source, object;
		if( debug, {[ \remove, this.hash, source, object ].postln });
		if( coll.remove( object ).notNil, {
			if( mapNames.notNil, { mapNames.removeAt( object.name )});
			if( java.notNil, { java.remove( master, object )});
			this.changed( \remove, object );
		});
	}

	removeAll { arg source, objects;
		if( debug, {[ \removeAll, this.hash, source, objects ].postln });
		coll.removeAll( objects );
		if( mapNames.notNil, { objects.do({ arg object; mapNames.removeAt( object.name )})});
		if( java.notNil, { java.removeAll( master, objects.asList )});
		this.changed( \remove, *objects );
	}
		
	size { ^coll.size }
	at { arg index; ^coll[ index ]}
	getAll { ^coll.copy }
	
	isEmpty { ^coll.isEmpty }
	notEmpty { ^coll.notEmpty }
	
	collect { arg function;
		^coll.collect( function );
	}

	select { arg function;
		^coll.select( function );
	}
	
	indexOf { arg object;
		^coll.indexOf( object );
	}
	
	includes { arg object;
		^coll.indexOf( object ).notNil;
	}
	
	do { arg function; coll.do( function )}
	
	detect { arg function; ^coll.detect( function )}
	
	findÊ{Êarg name;
		name = name.asSymbol;
		if( mapNames.notNil, {
			^mapNames.at( name );
		}, {
			^coll.detect({ arg object; object.name === name });
		});
	}
}