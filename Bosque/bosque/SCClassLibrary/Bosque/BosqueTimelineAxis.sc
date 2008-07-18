/**
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.12, 18-Jul-08
 */
BosqueTimelineAxis {
//	var <view;
	var <doc;
	var selectionStart  = -1;
	var shiftDrag, altDrag;
	var java, panel;
  	
	*new { arg doc, panel, java;
		^super.new.prInit( doc, panel, java );
	}
	
	prInit { arg argDoc, argPanel, argJava;
		var forest, fntSmall;
		
		doc = argDoc;
		panel = argPanel;
		java = argJava;
		forest = doc.forest;
		fntSmall = JFont( "Helvetica", 10 );
//		view = JSCPlugView( parent, bounds, java ).resize_( 2 );
		java.setFont( fntSmall );
//		updTimeline = UpdateListener.newFor( doc.timelineView, { arg upd, view, what ... params;
//			if( (what === \scrolled) or: { what === \changed }, {
//				java.server.sendMsg( '/method', java.id, \setSpace,
//					'[', '/method', 'de.sciss.gui.VectorSpace', \createLinSpace,
//						view.span.start / view.timeline.rate, view.span.stop / view.timeline.rate, 0.0, 1.0, "", "", "", "",
//					']' );
//			});
//		});
//		view.onClose = { updTimeline.remove };
//		view.mouseUpAction		= { arg ... args; this.mouseReleased( Bosque.createMouseEvent( *args ))};

// XXX
//		view.mouseDownAction	= { arg ... args; this.mousePressed( Bosque.createMouseEvent( *args ))};
//		view.mouseMoveAction	= { arg ... args; this.mouseDragged( Bosque.createMouseEvent( *args ))};
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
		var	edit, width;

// XXX		
//		width = view.bounds.width;
		width = panel.view.bounds.width;
	   
		// translate into a valid time offset
//		if( !doc.bird.attemptExclusive( Session.DOOR_TIME, 200 )) return;
//		try {
            span        = doc.timelineView.span;
            position    = span.start + (x / width * span.length).asInteger;
            position    = doc.timeline.span.clip( position );
            
            if( shiftDrag, {
			span2	= doc.timelineView.selection.span;
			if( altDrag or: { span2.isEmpty }, {
				selectionStart = doc.timelineView.cursor.position;
				altDrag = false;
			}, { if( selectionStart == -1, {
				selectionStart = if( abs( span2.start - position ) > abs( span2.stop - position ), span2.start, span2.stop );
			})});
			span	= Span( min( position, selectionStart ), max( position, selectionStart ));
			edit	= BosqueTimelineViewEdit.select( this, doc.timelineView, span ).performEdit;
            }, {
			if( altDrag, {
				edit	= JCompoundEdit.new;
				edit.addEdit( BosqueTimelineViewEdit.select( this, doc.timelineView, Span.new ).performEdit );
				edit.addEdit( BosqueTimelineViewEdit.position( this, doc.timelineView, position ).performEdit );
				edit.end;
				altDrag = false;
			}, {
				edit	= BosqueTimelineViewEdit.position( this, doc.timelineView, position ).performEdit;
			});
            });
		doc.undoManager.addEdit( edit );
//		}
//		finally {
//			doc.bird.releaseExclusive( Session.DOOR_TIME );
//		}
	}
}
