/*
 *	BosqueSessionCollection
 *	(Bosque)
 *
 *	Copyright (c) 2007-2008 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation; either
 *	version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	This software is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public
 *	License (gpl.txt) along with this software; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 *
 *
 *	Changelog:
 */

/**
 *	@author	Hanns Holger Rutz
 *	@version	0.18, 26-Oct-08
 */
BosqueSessionCollection : Object {
//	var <bosque;
	var coll;
	
	var debug = false;
	
	var <java, master;
	var <isSignificant;
	var mapNames;
	
	*new { arg significant = true, hasJava = true, uniqueNames = false;
		^super.new.prInit( significant, hasJava, uniqueNames );
	}
	
	prInit { arg significant, hasJava, uniqueNames;
		var bosque;
		
		bosque		= Bosque.default;
		isSignificant	= significant;
		coll			= List.new;
		master		= bosque.master;
		if( hasJava, {
			java		= JavaObject( "de.sciss.timebased.session.BasicSessionCollection", bosque.swing, significant );
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
		this.tryChanged( \remove, *objects );
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
		this.tryChanged( \add, object );
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
		this.tryChanged( \add, *objects );
	}

	remove { arg source, object;
		if( debug, {[ \remove, this.hash, source, object ].postln });
		if( coll.remove( object ).notNil, {
			if( mapNames.notNil, { mapNames.removeAt( object.name )});
			if( java.notNil, { java.remove( master, object )});
			this.tryChanged( \remove, object );
		});
	}

	removeAll { arg source, objects;
		if( debug, {[ \removeAll, this.hash, source, objects ].postln });
		coll.removeAll( objects );
		if( mapNames.notNil, { objects.do({ arg object; mapNames.removeAt( object.name )})});
		if( java.notNil, { java.removeAll( master, objects.asList )});
		this.tryChanged( \remove, *objects );
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
	
	find { arg name;
		name = name.asSymbol;
		if( mapNames.notNil, {
			^mapNames.at( name );
		}, {
			^coll.detect({ arg object; object.name === name });
		});
	}
}