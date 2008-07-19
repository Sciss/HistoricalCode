/*
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 */
 
/** 
 *	@version	0.16, 19-Jul-08
 */
BosqueSessionCollection : Object {
//	var <forest;
	var coll;
	
	var debug = false;
	
	var <java, master;
	
	*new {
		^super.new.prInit;
	}
	
	prInit {
		var forest;
		
		forest		= Bosque.default;
		coll			= List.new;
		master		= forest.master;
		java			= JavaObject( "de.sciss.timebased.session.BasicSessionCollection", forest.swing );
	}
	
	dispose {
//		upd.remove;
//		javaResp.remove; javaResp = nil;
//		javaNet.dispose; javaNet.destroy; javaNet = nil;
		java.dispose; java.destroy; java = nil;
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
		java.clear( master );
		this.changed( \remove, *objects );
	}
	
	add { arg source, object;
		if( debug, { [ \add, this.hash, source, object ].postln });
		coll.add( object );
		java.add( master, object );
		this.changed( \add, object );
	}
	
	addAll { arg source, objects;
		if( debug, {[ \addAll, this.hash, source, objects ].postln });
		coll.addAll( objects );
		java.addAll( master, objects.asList );
		this.changed( \add, *objects );
	}

	remove { arg source, object;
		if( debug, {[ \remove, this.hash, source, object ].postln });
		if( coll.remove( object ).notNil, {
			java.remove( master, object );
			this.changed( \remove, object );
		});
	}

	removeAll { arg source, objects;
		if( debug, {[ \removeAll, this.hash, source, objects ].postln });
		coll.removeAll( objects );
		java.removeAll( master, objects.asList );
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
}
