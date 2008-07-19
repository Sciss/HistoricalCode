/**
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.13, 18-Jul-08
 */
BosqueTimelineView : Object {
	var <timeline;
	var <cursor;
	var <selection;
	var <span;
	var <java;
	var updTLL, updTLCL, updTLSL;

	*new { arg timeline, span /*, cursor, sel */;
		^super.new.prInit( timeline, span /*, cursor, sel */);
	}
	
	prInit { arg argTimeline, argSpan /*, argCursor, argSel */;
		var swing;
		
		timeline		= argTimeline;
		span			= argSpan ?? { timeline.span };
		cursor		= BosqueTimelineCursor( timeline ); // argCursor ?? { BosqueTimelineCursor( timeline )};
		selection		= BosqueTimelineSelection( timeline ); // argSel ?? { BosqueTimelineSelection( timeline )};
		swing		= Bosque.default.swing;
		java			= JavaObject( "de.sciss.timebased.timeline.BasicTimelineView",
			swing, timeline, span, cursor, selection );
		
		updTLL = UpdateListener.newFor( timeline, { arg upd, tl ... rest;
			var tlSpan;
			
			tlSpan = timeline.span;
			if( tlSpan.contains( span ).not, {
				span = if( tlSpan.start > span.start, {
					Span( tlSpan.start, min( tlSpan.stop, tlSpan.start + span.length ));
				}, {
					Span( max( tlSpan.start, tlSpan.stop - span.length ), tlSpan. stop );
				});
				java.setSpan( java, span );
				this.changed( \scrolled, span );
			});
			this.changed( \changed, *rest );
		});
		
		updTLCL = UpdateListener.newFor( cursor, { arg upd, csr ... rest;
			this.changed( \positioned, *rest );
		}, \changed );
		
		updTLSL = UpdateListener.newFor( selection, { arg upd, sel ... rest;
			this.changed( \selected, *rest );
		}, \changed );
	}
	
//	storeModifiersOn { arg stream;
//		stream << ".rate_(";
//		rate.storeOn( stream );
//		stream << ")";
//		stream << ".length_(";
//		length.storeOn( stream );
//		stream << ")";
//		stream << ".position_(";
//		position.storeOn( stream );
//		stream << ")";
//		stream << ".visibleSpan_(";
//		visibleSpan.storeOn( stream );
//		stream << ")";
//		stream << ".selectionSpan_(";
//		selectionSpan.storeOn( stream );
//		stream << ")";
//	}
	
	span_ {Êarg newSpan;
		if( newSpan.equals( span ).not, {
			span = newSpan;
			this.changed( \scrolled, span );
			java.setSpan( java, span );
		});
	}

	dispose {
		updTLL.remove; updTLCL.remove; updTLSL.remove;
		java.destroy; java = nil;
	}

	asSwingArg {
		^java.asSwingArg;
	}
//
//	backend { ^java }
//	net { ^javaNet }
}