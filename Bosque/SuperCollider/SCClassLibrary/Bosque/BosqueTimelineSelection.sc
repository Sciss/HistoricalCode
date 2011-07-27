/*
 *	BosqueTimelineSelection
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
 *	@version	0.11, 26-Oct-08
 */
BosqueTimelineSelection : Object {
	var <timeline;
	var <span;
	var upd, <java;

	*new { arg timeline, span;
		^super.new.prInit( timeline, span );
	}
	
	prInit { arg argTimeline, argSpan;
		var swing;
		
		timeline		= argTimeline;
		span			= argSpan ?? { Span( timeline.span.start, timeline.span.start )};
		swing		= Bosque.default.swing;
		java			= JavaObject( "de.sciss.timebased.timeline.BasicTimelineSelection",
			swing, timeline, span );
		
		upd		= UpdateListener.newFor( timeline, { arg upd, tl;
			var tlSpan = tl.span;
			if( tlSpan.contains( span ).not, {
				this.span = span.intersection( tlSpan );
			});
		}, \changed );
	}
	
	storeModifiersOn { arg stream;
		stream << ".span_(";
		span.storeOn( stream );
		stream << ")";
	}
	
	span_ { arg newSpan;
		if( newSpan.equals( span ).not, {
			span = newSpan;
			this.tryChanged( \changed, span );
			java.setSpan( java, span );
		});
	}
	
	dispose {
		upd.remove;
		java.dispose; java.destroy; java = nil;
	}
	
	asSwingArg {
		^java.asSwingArg;
	}
}