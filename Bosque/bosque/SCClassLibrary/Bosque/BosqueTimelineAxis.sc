/**
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.11, 11-Aug-07
 */
BosqueTimelineAxis {
	var <view;
	var <doc;
	var selectionStart  = -1;
	var shiftDrag, altDrag;
  	
	*new { arg doc, parent, bounds;
		^super.new.prInit( doc, parent, bounds );
	}
	
	prInit { arg argDoc, parent, bounds;
		var jTimelineAxis, forest, fntSmall, updTimeline;
		
		doc = argDoc;
		forest = doc.forest;
		fntSmall = JFont( "Helvetica", 10 );
		jTimelineAxis = JavaObject( 'de.sciss.timebased.gui.Axis', forest.swing, 0, 4 );
		view = JSCPlugView( parent, bounds, jTimelineAxis ).resize_( 2 );
		jTimelineAxis.setFont( fntSmall );
		updTimeline = UpdateListener.newFor( doc.timeline, { arg upd, timeline, what ... params;
			if( (what === \visible) or: { what === \rate }, {
				jTimelineAxis.server.sendMsg( '/method', jTimelineAxis.id, \setSpace,
					'[', '/method', 'de.sciss.gui.VectorSpace', \createLinSpace,
						timeline.visibleSpan.start / timeline.rate, timeline.visibleSpan.stop / timeline.rate, 0.0, 1.0, "", "", "", "",
					']' );
			});
		});
		view.onClose = { updTimeline.remove };
//		view.mouseUpAction		= { arg ... args; this.mouseReleased( Bosque.createMouseEvent( *args ))};
		view.mouseDownAction	= { arg ... args; this.mousePressed( Bosque.createMouseEvent( *args ))};
		view.mouseMoveAction	= { arg ... args; this.mouseDragged( Bosque.createMouseEvent( *args ))};
	}
	
	mousePressed { arg e;
		shiftDrag		= e.isShiftDown;
		altDrag		= e.isAltDown;
		selectionStart	= -1;
		this.prDragTimelinePosition( e );
    }

	mouseDragged { arg e;
		this.prDragTimelinePosition( e );
	}

	prDragTimelinePosition { arg e;
		var	x   = e.x;
		var	span, span2;
		var	position;
		var	edit;
	   
		// translate into a valid time offset
//		if( !doc.bird.attemptExclusive( Session.DOOR_TIME, 200 )) return;
//		try {
            span        = doc.timeline.visibleSpan;
            position    = span.start + (x / view.bounds.width * span.length).asInteger;
            position    = max( 0, min( doc.timeline.length, position ));
            
            if( shiftDrag, {
			span2	= doc.timeline.selectionSpan;
			if( altDrag or: { span2.isEmpty }, {
				selectionStart = doc.timeline.position;
				altDrag = false;
			}, { if( selectionStart == -1, {
				selectionStart = if( abs( span2.start - position ) > abs( span2.stop - position ), span2.start, span2.stop );
			})});
			span	= Span( min( position, selectionStart ), max( position, selectionStart ));
			edit	= BosqueTimelineVisualEdit.select( this, doc, span ).performEdit;
            }, {
			if( altDrag, {
				edit	= JCompoundEdit.new;
				edit.addEdit( BosqueTimelineVisualEdit.select( this, doc, Span.new ).performEdit );
				edit.addEdit( BosqueTimelineVisualEdit.position( this, doc, position ).performEdit );
				edit.end;
				altDrag = false;
			}, {
				edit	= BosqueTimelineVisualEdit.position( this, doc, position ).performEdit;
			});
            });
		doc.undoManager.addEdit( edit );
//		}
//		finally {
//			doc.bird.releaseExclusive( Session.DOOR_TIME );
//		}
	}
}
