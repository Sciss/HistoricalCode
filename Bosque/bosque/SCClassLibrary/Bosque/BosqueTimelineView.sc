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
	
	storeModifiersOn { arg stream;
		stream << ".span_(";
		span.storeOn( stream );
		stream << ")";
		stream << ".doCursor({ arg csr; csr";
		cursor.storeModifiersOn( stream );
		stream << "}).doSelection({ arg sel; sel";
		selection.storeModifiersOn( stream );
		stream << "})";
	}
	
	doCursor { arg func; func.value( cursor )}
	doSelection { arg func; func.value( selection )}

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