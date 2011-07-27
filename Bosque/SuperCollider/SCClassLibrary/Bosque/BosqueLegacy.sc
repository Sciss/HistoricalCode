BosqueLegacityTimeline {
	var doc;
	
	*new { arg doc;
		^super.new.prInit( doc )
	}
	
	prInit { arg argDoc;
		doc = argDoc;
	}
		
	length_ { arg len;
		doc.timeline.span_( Span( 0, len ));
	}
	
	position_ { arg pos;
		doc.timelineView.cursor.position_( pos );
	}
	
	visibleSpan_ { arg span;
		doc.timelineView.span = span;
	}
	
	selectionSpan_ { arg span;
		doc.timelineView.selection.span_( span );
	}
	
	// valid current ones
	span_ { arg span;
		doc.timeline.span_( span );
	}
	
	rate_ { arg rate;
		doc.timeline.rate_( rate );
	}
}