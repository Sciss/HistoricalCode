/*
 *	BosqueTimelineView
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
 *	@version	0.14, 26-Oct-08
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
				this.tryChanged( \scrolled, span );
			});
			this.tryChanged( \changed, *rest );
		});
		
		updTLCL = UpdateListener.newFor( cursor, { arg upd, csr ... rest;
			this.tryChanged( \positioned, *rest );
		}, \changed );
		
		updTLSL = UpdateListener.newFor( selection, { arg upd, sel ... rest;
			this.tryChanged( \selected, *rest );
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

	span_ { arg newSpan;
		if( newSpan.equals( span ).not, {
			span = newSpan;
			this.tryChanged( \scrolled, span );
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