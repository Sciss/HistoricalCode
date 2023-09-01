/*
 *	BosqueTimelineAxis
 *	(Bosque)
 *
 *	Copyright (c) 2007-2008 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation; either
 *	version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	This software is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public
 *	License (gpl.txt) along with this software; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 *
 *
 *	Changelog:
 */

/**
 *	@author	Hanns Holger Rutz
 *	@version	0.13, 23-Jul-08
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
		var bosque, fntSmall;
		
		doc = argDoc;
		panel = argPanel;
		java = argJava;
		bosque = doc.bosque;
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
