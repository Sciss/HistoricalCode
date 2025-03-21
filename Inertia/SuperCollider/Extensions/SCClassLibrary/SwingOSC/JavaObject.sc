/**
 *	Based on the fact that Object.sc "catches" all
 *	calls to unknown methods in "doesNotUnderstand",
 *	we exploit this behaviour to create an easy wrapper
 *	class for Java object control in SwingOSC.
 *
 *	@version	0.14, 06-Jan-07
 *	@author	Hanns Holger Rutz
 */
JavaObject {
	classvar allObjects;
	var <server, <id;

	*initClass {
		UI.registerForShutdown({ this.destroyAll; });
	}

	*new { arg className, server ... args;
		^super.new.prInitJavaObject( className, server, args );
	}
	
	*getClass { arg className, server;
		^super.new.prInitJavaClass( className, server );
	}

	*newFrom { arg javaObject, selector ... args;
		^super.new.prInitJavaResult( javaObject, selector, args );
	}
	
	*basicNew {�arg id, server;
		^super.newCopyArgs( server, id );
	}
	
	prInitJavaObject { arg className, argServer, args;
		var msg;
		
		server		= argServer ?? SwingOSC.default;
		allObjects	= allObjects.add( this );	// array grows
		id			= server.nextNodeID;
		
		msg			= List[ '/local', id, '[', '/new', className ];
		this.prAddArgs( msg, args );
		msg.add( ']' );
		
//		server.sendBundle( nil, [ '/local', id, '[', '/new', className ] ++ args ++ [ ']' ]);
//msg.postln;
		server.listSendMsg( msg );
	}
	
	prInitJavaClass { arg className, argServer;
		server		= argServer ?? SwingOSC.default;
		allObjects	= allObjects.add( this );	// array grows
		id			= server.nextNodeID;
		server.sendMsg( '/local', id, '[', '/method', 'java.lang.Class', \forName, className, ']' );
	}

	prInitJavaResult { arg javaObject, selector, args;
		var msg;
		
		server		= javaObject.server;
		allObjects	= allObjects.add( this );	// array grows
		id			= server.nextNodeID;
		msg			= List[ '/local', id, '[' ];
		javaObject.prMethodCall( msg, selector, args );
		msg.add( ']' );
		server.listSendMsg( msg );
	}
		
	destroy {
		server.sendMsg( '/free', id );
		allObjects.remove( this );
	}
	
	*destroyAll {
		var list;
		list = allObjects.copy;
		allObjects = Array.new( 8 );
		list.do({ arg obj; obj.destroy; });
	}

	doesNotUnderstand { arg selector ... args;
		var selStr;
		
		selStr = selector.asString;
		if( selStr.last === $_, {
			if( thisThread.isKindOf( Routine ), {
				^this.prMethodCallAsync( selStr.copyFromStart( selStr.size - 2 ), args );
			}, {
				"JavaObject : asynchronous call outside routine".warn;
				{ ("RESULT: " ++ this.prMethodCallAsync( selStr.copyFromStart( selStr.size - 2 ), args )).postln; }.fork( SwingOSC.clock );
			});
		}, {
			server.listSendMsg( this.prMethodCall( nil, selector, args ));
		});
	}
	
	prMethodCallAsync {�arg selector, args;
		var id, msg;
		id	= UniqueID.next;
		msg	= List[ '/query', id, '[' ];
		this.prMethodCall( msg, selector, args );
		msg.add( ']' );
		msg	= server.sendMsgSync( msg, [ '/info', id ], nil );
		^if( msg.notNil, { msg[ 2 ]}, nil );
	}
	
	prAddArgs { arg list, args;
		args.do({ arg x;
			if( x.respondsTo( \id ), {
				list.addAll([ '[', '/ref', x.id, ']' ]);
			}, {
				list.addAll( x.asSwingArg );
			});
		});
	}

	prMethodCall { arg list, selector, args;
		list = list ?? { List.new; };
		list.add( '/method' );
		list.add( id );
		list.add( selector );
		this.prAddArgs( list, args );
		^list;
	}
	
	// ---- now override a couple of methods in Object that ----
	// ---- might produce name conflicts with java methods  ----
		
	size {�arg ... args; this.doesNotUnderstand( \size, *args ); }
	do {�arg ... args; this.doesNotUnderstand( \do, *args ); }
	generate {�arg ... args; this.doesNotUnderstand( \generate, *args ); }
	copy {�arg ... args; this.doesNotUnderstand( \copy, *args ); }
	dup {�arg ... args; this.doesNotUnderstand( \dup, *args ); }
	poll {�arg ... args; this.doesNotUnderstand( \poll, *args ); }
	value {�arg ... args; this.doesNotUnderstand( \value, *args ); }
	next {�arg ... args; this.doesNotUnderstand( \next, *args ); }
	reset {�arg ... args; this.doesNotUnderstand( \reset, *args ); }
	first {�arg ... args; this.doesNotUnderstand( \first, *args ); }
	iter {�arg ... args; this.doesNotUnderstand( \iter, *args ); }
	stop {�arg ... args; this.doesNotUnderstand( \stop, *args ); }
	free {�arg ... args; this.doesNotUnderstand( \free, *args ); }
	repeat {�arg ... args; this.doesNotUnderstand( \repeat, *args ); }
	loop {�arg ... args; this.doesNotUnderstand( \loop, *args ); }
	throw {�arg ... args; this.doesNotUnderstand( \throw, *args ); }
	rank {�arg ... args; this.doesNotUnderstand( \rank, *args ); }
	slice {�arg ... args; this.doesNotUnderstand( \slice, *args ); }
	shape {�arg ... args; this.doesNotUnderstand( \shape, *args ); }
	obtain {�arg ... args; this.doesNotUnderstand( \obtain, *args ); }
	switch {�arg ... args; this.doesNotUnderstand( \switch, *args ); }
	yield {�arg ... args; this.doesNotUnderstand( \yield, *args ); }
	release {�arg ... args; this.doesNotUnderstand( \release, *args ); }
	update {�arg ... args; this.doesNotUnderstand( \update, *args ); }
	layout {�arg ... args; this.doesNotUnderstand( \layout, *args ); }
	inspect {�arg ... args; this.doesNotUnderstand( \inspect, *args ); }
	crash {�arg ... args; this.doesNotUnderstand( \crash, *args ); }
	freeze {�arg ... args; this.doesNotUnderstand( \freeze, *args ); }
	blend {�arg ... args; this.doesNotUnderstand( \blend, *args ); }
	pair {�arg ... args; this.doesNotUnderstand( \pair, *args ); }
	source {�arg ... args; this.doesNotUnderstand( \source, *args ); }
	clear {�arg ... args; this.doesNotUnderstand( \clear, *args ); }
}