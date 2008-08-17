/*
 *	AnyMap
 *
 *	Copyright (c) 2008 Hanns Holger Rutz. All rights reserved.
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
 *	@version	0.10, 17-Aug-07
 */
AnyMap {
	var <dict, nilRef;
	
	*new {
		^super.new.prInit;
	}
	
	prInit {
		dict		= IdentityDictionary.new;
		nilRef	= Object.new;
	}
	
	put {Êarg key, value;
		dict.put( key, value ? nilRef );
	}
	
	at { arg key;
		var value;
		value = dict.at( key );
		if( value.isNil, {
			DoesNotUnderstandError( this, key ).throw;
		});
		^if( value !== nilRef, value );
	}
	
	doesNotUnderstand { arg selector ... args;
		var func;

		if( selector.isSetter, {
			selector = selector.asGetter;
			this.put( selector, args[ 0 ]);
			^this;
		}, {
			^this.at( selector ).functionPerformList( \value, this, args );
		});
	}
	
	// ---- now override a couple of methods in Object that ----
	// ---- might produce name conflicts  ----
		
	size { arg ... args; ^this.doesNotUnderstand( \size, *args ); }
	do { arg ... args; ^this.doesNotUnderstand( \do, *args ); }
	generate { arg ... args; ^this.doesNotUnderstand( \generate, *args ); }
	copy { arg ... args; ^this.doesNotUnderstand( \copy, *args ); }
	dup { arg ... args; ^this.doesNotUnderstand( \dup, *args ); }
	poll { arg ... args; ^this.doesNotUnderstand( \poll, *args ); }
	value { arg ... args; ^this.doesNotUnderstand( \value, *args ); }
	next { arg ... args; ^this.doesNotUnderstand( \next, *args ); }
	reset { arg ... args; ^this.doesNotUnderstand( \reset, *args ); }
	first { arg ... args; ^this.doesNotUnderstand( \first, *args ); }
	iter { arg ... args; ^this.doesNotUnderstand( \iter, *args ); }
	stop { arg ... args; ^this.doesNotUnderstand( \stop, *args ); }
	free { arg ... args; ^this.doesNotUnderstand( \free, *args ); }
	repeat { arg ... args; ^this.doesNotUnderstand( \repeat, *args ); }
	loop { arg ... args; ^this.doesNotUnderstand( \loop, *args ); }
	throw { arg ... args; ^this.doesNotUnderstand( \throw, *args ); }
	rank { arg ... args; ^this.doesNotUnderstand( \rank, *args ); }
	slice { arg ... args; ^this.doesNotUnderstand( \slice, *args ); }
	shape { arg ... args; ^this.doesNotUnderstand( \shape, *args ); }
	obtain { arg ... args; ^this.doesNotUnderstand( \obtain, *args ); }
	switch { arg ... args; ^this.doesNotUnderstand( \switch, *args ); }
	yield { arg ... args; ^this.doesNotUnderstand( \yield, *args ); }
	release { arg ... args; ^this.doesNotUnderstand( \release, *args ); }
	update { arg ... args; ^this.doesNotUnderstand( \update, *args ); }
	layout { arg ... args; ^this.doesNotUnderstand( \layout, *args ); }
	inspect { arg ... args; ^this.doesNotUnderstand( \inspect, *args ); }
	crash { arg ... args; ^this.doesNotUnderstand( \crash, *args ); }
	freeze { arg ... args; ^this.doesNotUnderstand( \freeze, *args ); }
	blend { arg ... args; ^this.doesNotUnderstand( \blend, *args ); }
	pair { arg ... args; ^this.doesNotUnderstand( \pair, *args ); }
	source { arg ... args; ^this.doesNotUnderstand( \source, *args ); }
	clear { arg ... args; ^this.doesNotUnderstand( \clear, *args ); }
}