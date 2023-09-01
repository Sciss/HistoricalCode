/*
 *  SlaveTimelineView.java
 *  TimeBased
 *
 *  Copyright (c) 2004-2010 Hanns Holger Rutz. All rights reserved.
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
 *  Changelog:
 *		18-Jul-08	created
 */

package de.sciss.timebased.net;

import de.sciss.io.Span;
import de.sciss.timebased.timeline.Timeline;
import de.sciss.timebased.timeline.TimelineCursor;
import de.sciss.timebased.timeline.TimelineSelection;
import de.sciss.timebased.timeline.TimelineView;

public class NetTimelineView
implements TimelineView
{
	private final Master				master;
	private final TimelineView			backend;
	private final NetTimeline			timeline;
	private final NetTimelineCursor		cursor;
	private final NetTimelineSelection	sel;
	private int							id	= -1;
	
//	public NetTimelineView( TimelineView backend )
//	{
//		this( new Master( SwingOSC.getInstance().getCurrentClient() ), backend );
//	}
//	
	public NetTimelineView( TimelineView backend, NetTimeline timeline,
							NetTimelineCursor cursor, NetTimelineSelection sel )
	{
System.out.println( getClass().getName() + " : THIS CLASS SHOULD NOT BE USED ANYMORE!!" );		
		this.backend	= backend;
		this.timeline	= timeline;
		this.master		= timeline.getMaster();
//		this.timeline	= new NetTimeline( master, backend.getTimeline() );
		this.cursor		= cursor;
		this.sel		= sel;
	}
	
	public Master getMaster()
	{
		return master;
	}
	
	public void setID( int id )
	{
		this.id = id;
//		timeline.setID( id );
//		cursor.setID( id );
//		sel.setID( id );
	}
	
	public void dispose()
	{
//		backend.dispose();
	}
	
	public void addListener( Listener l )
	{
		backend.addListener( l );
	}
	
	public void removeListener( Listener l )
	{
		backend.removeListener( l );
	}
	
	public TimelineCursor getCursor()
	{
		return cursor;
	}
	
	public TimelineSelection getSelection()
	{
		return sel;
	}
	
	public Timeline getTimeline()
	{
		return timeline;
	}
	
	public Span getSpan()
	{
		return backend.getSpan();
	}

	public void setSpan( Object source, Span newSpan )
	{
//		if( source == master ) {
//			backend.setSpan( source, newSpan );
//		} else {
			if( !newSpan.equals( backend.getSpan() )) {
				master.reply( "/timeline", id, "scroll", newSpan.start, newSpan.stop );
			}
//		}
	}
}
