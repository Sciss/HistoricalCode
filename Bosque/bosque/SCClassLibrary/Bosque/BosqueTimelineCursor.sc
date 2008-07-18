/**
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.10, 18-Jul-08
 */
BosqueTimelineCursor : Object {
	var <timeline;
	var <position;
	var javaBackEnd, javaNet, upd, javaResp;

	*new { arg timeline, pos;
		^super.new.prInit( timeline, pos );
	}
	
	prInit { arg argTimeline, argPos;
		var swing;
		
		timeline		= argTimeline;
		position		= argPos ?? { timeline.span.start };
		swing		= Bosque.default.swing;
		javaBackEnd	= JavaObject( "de.sciss.timebased.timeline.BasicTimelineCursor", swing, timeline.backend, position );
		javaNet		= JavaObject( "de.sciss.timebased.net.NetTimelineCursor", swing, timeline.net, javaBackEnd );
		
		upd		= UpdateListener.newFor( timeline, { arg upd, tl;
			var tlSpan = tl.span;
			if( tlSpan.containsPos( position ).not, {
				this.position = tlSpan.clip( position );
			});
		}, \changed );
		
		javaNet.setID( javaNet.id );
		javaResp = ScissOSCPathResponder( swing.addr, [ '/timeline', javaNet.id, \position ], { arg time, resp, msg;
			this.position = msg[ 3 ];
		}).add;
	}
	
	position_ {Êarg newPos;
		if( newPos != position, {
			position = newPos;
			this.changed( \changed, position );
			javaBackEnd.setPosition( javaBackEnd, position );
		});
	}
	
	dispose {
		upd.remove;
		javaResp.remove; javaResp = nil;
		javaNet.dispose; javaNet.destroy; javaNet = nil;
		javaBackEnd.dispose; javaBackEnd.destroy; javaBackEnd = nil;
	}

//	asSwingArg {
//		^javaBackEnd.asSwingArg;
//	}

	backend { ^javaBackEnd }
	net { ^javaNet }
}