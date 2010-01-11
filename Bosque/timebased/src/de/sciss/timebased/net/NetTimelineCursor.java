/*
 *  SlaveTimelineCursor.java
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

import de.sciss.timebased.timeline.BasicTimelineCursor;
import de.sciss.timebased.timeline.Timeline;
import de.sciss.timebased.timeline.TimelineCursor;

public class NetTimelineCursor
implements TimelineCursor
{
	private final Master					master;
	private final NetTimeline				timeline;
	private final TimelineCursor			backend;
	private int								id	= -1;
	
	public NetTimelineCursor( NetTimeline timeline )
	{
		this( timeline, new BasicTimelineCursor( timeline ));
	}
	
	public NetTimelineCursor( NetTimeline timeline, TimelineCursor backend )
	{
System.out.println( getClass().getName() + " : THIS CLASS SHOULD NOT BE USED ANYMORE!!" );		
		this.timeline	= timeline;
		this.backend	= backend;
		master			= timeline.getMaster();
	}
	
	public Master getMaster()
	{
		return master;
	}

	public void setID( int id )
	{
		this.id = id;
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
	
	public Timeline getTimeline()
	{
		return timeline;
	}

	public long getPosition()
	{
		return backend.getPosition();
	}
	
	public void setPosition( Object source, long newPos )
	{
//		if( source == master ) {
//			backend.setPosition( source, newPos );
//		} else {
			if( newPos != backend.getPosition() ) {
				master.reply( "/timeline", id, "position", newPos );
			}
//		}
	}
}