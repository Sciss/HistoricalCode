/**
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.10, 18-Jul-08
 */
BosqueTimelineSelection : Object {
	var <timeline;
	var <span;
	var upd, <java;

	*new { arg timeline, span;
		^super.new.prInit( timeline, span );
	}
	
	prInit { arg argTimeline, argSpan;
		var swing;
		
		timeline		= argTimeline;
		span			= argSpan ?? { Span( timeline.span.start, timeline.span.start )};
		swing		= Bosque.default.swing;
		java			= JavaObject( "de.sciss.timebased.timeline.BasicTimelineSelection",
			swing, timeline, span );
		
		upd		= UpdateListener.newFor( timeline, { arg upd, tl;
			var tlSpan = tl.span;
			if( tlSpan.contains( span ).not, {
				this.span = span.intersection( tlSpan );
			});
		}, \changed );
	}
	
	span_ {Êarg newSpan;
		if( newSpan.equals( span ).not, {
			span = newSpan;
			this.changed( \changed, span );
			java.setSpan( java, span );
		});
	}
	
	dispose {
		upd.remove;
		java.dispose; java.destroy; java = nil;
	}
	
	asSwingArg {
		^java.asSwingArg;
	}
}