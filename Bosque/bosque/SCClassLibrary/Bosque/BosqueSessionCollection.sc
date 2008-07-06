/**
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.15, 28-Aug-07
 */
BosqueSessionCollection : Object {
//	var <forest;
	var coll;
	
	var debug = false;
	
	*new {
		^super.new.prInit;
	}
	
	prInit {
//		forest	= Bosque.default;
		coll		= List.new;
	}

	storeModifiersOn { arg stream;
		stream << ".addAll(this,";
		stream.nl; stream.tab;
		coll.asArray.storeOn( stream );
		stream << ")";
	}
	
	clear { arg source;
		var objects = coll;
		coll = List.new;
		this.changed( \remove, *objects );
	}
	
	add { arg source, object;
		if( debug, { [ \add, this.hash, source, object ].postln });
		coll.add( object );
		this.changed( \add, object );
	}
	
	addAll { arg source, objects;
		if( debug, {[ \addAll, this.hash, source, objects ].postln });
		coll.addAll( objects );
		this.changed( \add, *objects );
	}

	remove { arg source, object;
		if( debug, {[ \remove, this.hash, source, object ].postln });
		if( coll.remove( object ).notNil, {
			this.changed( \remove, object );
		});
	}

	removeAll { arg source, objects;
		if( debug, {[ \removeAll, this.hash, source, objects ].postln });
		coll.removeAll( objects );
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
