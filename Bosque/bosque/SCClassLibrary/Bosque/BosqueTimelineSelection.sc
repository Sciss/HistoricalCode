/**
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.10, 18-Jul-08
 */
BosqueTimelineSelection : Object {
	var <timeline;
	var <span;
	var upd, javaBackEnd, javaNet, javaResp;

	*new { arg timeline, span;
		^super.new.prInit( timeline, span );
	}
	
	prInit { arg argTimeline, argSpan;
		var swing;
		
		timeline		= argTimeline;
		span			= argSpan ?? { Span( timeline.span.start, timeline.span.start )};
		swing		= Bosque.default.swing;
		javaBackEnd	= JavaObject( "de.sciss.timebased.timeline.BasicTimelineSelection",
			swing, timeline.backend, span );
		javaNet		= JavaObject( "de.sciss.timebased.net.NetTimelineSelection",
			swing, timeline.net, javaBackEnd );
		
		upd		= UpdateListener.newFor( timeline, { arg upd, tl;
			var tlSpan = tl.span;
			if( tlSpan.contains( span ).not, {
				this.span = span.intersection( tlSpan );
			});
		}, \changed );

		javaNet.setID( javaNet.id );
		javaResp = ScissOSCPathResponder( swing.addr, [ '/timeline', javaNet.id, \select ], { arg time, resp, msg;
			this.span = Span( msg[ 3 ], msg[ 4 ]);
		}).add;
	}
	
	span_ {Êarg newSpan;
		if( newSpan.equals( span ).not, {
			span = newSpan;
			this.changed( \changed, span );
			javaBackEnd.setSpan( javaBackEnd, span );
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