/*
 *  Timeline.java
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
 *		17-Jul-08	created
 */

package de.sciss.timebased.timeline;

import de.sciss.app.BasicEvent;
import de.sciss.io.Span;
import de.sciss.util.Disposable;

public interface Timeline
extends Disposable
{
	public double getRate();
	public void setRate( Object source, double newRate );
	public Span getSpan();
	public void setSpan( Object source, Span newSpan );
	public void addListener( Listener listener );
	public void removeListener( Listener listener );

	public interface Listener
	{
		/**
		 *  Notifies the listener that
		 *  the basic timeline properties were modified
		 *  (e.g. the length or rate changed).
		 *
		 *  @param  e   the event describing
		 *				the timeline modification
		 */
		public void timelineChanged( Event e );
	}

	public static class Event
	extends BasicEvent
	{
	// --- ID values ---
		/**
		 *  returned by getID() : the basic properties of
		 *  the timeline, rate or length, have been modified.
	     *  <code>actionObj</code> is a (potentially empty)
	     *  <code>Span</code> object
		 */
		public static final int CHANGED		= 1;
		
		private final Timeline tl;

		/**
		 *  Constructs a new <code>TimelineEvent</code>
		 *
		 *  @param  source		who originated the action
		 *  @param  id			one of <code>CHANGED</code>, <code>SELECTED</code>,
		 *						<code>POSITIONED</code> and <code>SCROLLED</code>
		 *  @param  when		system time when the event occured
		 *  @param  actionID	currently unused - thus use zero
		 *  @param  actionObj   for <code>SELECTED</code> and <code>SCROLLED</code>
		 *						this is a <code>Span</code> describing the new
		 *						visible or selected span.
		 */
		public Event( Object source, int id, long when, Timeline tl )
		{
			super( source, id, when );
			this.tl = tl;
		}
		
		public Timeline getTimeline()
		{
			return tl;
		}

		public boolean incorporate( BasicEvent oldEvent )
		{
			if( oldEvent instanceof Event &&
				this.getSource() == oldEvent.getSource() &&
				this.getID() == oldEvent.getID() ) {
				
				// XXX beware, when the actionID and actionObj
				// are used, we have to deal with them here
				
				return true;

			} else return false;
		}
	}
}
