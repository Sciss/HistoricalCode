BosqueLegacityTimeline {
	var doc;
	
	*new { arg doc;
		^super.new.prInit( doc )
	}
	
	prInit { arg argDoc;
		doc = argDoc;
	}
	
	rate_ { arg rate;
	
	}
	
	length_ { arg len;
	
	}
	
	position_ { arg pos;
	
	}
	
	visibleSpan_ { arg span;
		doc.timelineView.span = span;
	}
	
	selectionSpan_ { arg span;
	
	}
}