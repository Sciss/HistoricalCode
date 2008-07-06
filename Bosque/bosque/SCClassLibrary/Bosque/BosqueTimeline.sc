/**
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.12, 13-Aug-07
 */
BosqueTimeline : Object {
	var <doc;
	var <position	= 0;
	var <length	= 0;
	var <rate		= 44100;
	var <visibleSpan;
	var <selectionSpan;

	*new { arg doc;
		^super.new.prInit( doc );
	}
	
	prInit { arg argDoc;
		doc			= argDoc ?? { Bosque.default.session };
		visibleSpan	= Span.new;
		selectionSpan	= Span.new;
	}
	
	storeModifiersOn { arg stream;
		stream << ".rate_(";
		rate.storeOn( stream );
		stream << ")";
		stream << ".length_(";
		length.storeOn( stream );
		stream << ")";
		stream << ".position_(";
		position.storeOn( stream );
		stream << ")";
		stream << ".visibleSpan_(";
		visibleSpan.storeOn( stream );
		stream << ")";
		stream << ".selectionSpan_(";
		selectionSpan.storeOn( stream );
		stream << ")";
	}
	
	clear {
		this.position_( 0 ).selectionSpan_( Span.new ).visibleSpan_( Span.new ).length_( 0 );
	}

	position_ { arg val;
		position = val;
		this.changed( \position, position );
	}
	
	length_ { arg val;
		length = val;
		this.changed( \length, length );
	}

	rate_ { arg val;
		rate = val;
		this.changed( \rate, rate );
	}
		
	visibleSpan_ { arg span;
		visibleSpan = span;
		this.changed( \visible, visibleSpan );
	}

	selectionSpan_ { arg span;
		selectionSpan = span;
		this.changed( \selection, selectionSpan );
	}

	editPosition { arg source, pos;
		doc.undoManager.addEdit( BosqueTimelineVisualEdit.position( source, doc, pos ).performEdit );	
	}

	editScroll { arg source, span;
		doc.undoManager.addEdit( BosqueTimelineVisualEdit.scroll( source, doc, span ).performEdit );	
	}

	editSelect { arg source, span;
		doc.undoManager.addEdit( BosqueTimelineVisualEdit.select( source, doc, span ).performEdit );
	}
}
