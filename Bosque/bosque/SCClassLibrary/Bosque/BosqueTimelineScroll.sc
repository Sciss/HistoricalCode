/**
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.11, 11-Aug-07
 */
BosqueTimelineScroll {
	var <view;
	var <doc;
  	
	*new { arg doc, parent, bounds;
		^super.new.prInit( doc, parent, bounds );
	}
	
	prInit { arg argDoc, parent, bounds;
		var updTimeline;
		
		doc	= argDoc;
		view = JSCScrollBar( parent, bounds ).resize_( 8 );
		updTimeline = UpdateListener.newFor( doc.timeline, { arg upd, timeline, what ... params;
			if( (what === \visible) or: { what === \length }, {
				view.setSpan( timeline.visibleSpan.start / timeline.length, timeline.visibleSpan.stop / timeline.length );
			});
		});
		view.action = { arg b;
		doc.timeline.editScroll( this, Span( (b.value * doc.timeline.length).asInteger, ((b.value + b.extent) * doc.timeline.length).asInteger ));
		};
		view.onClose = { updTimeline.remove };
	}
}
