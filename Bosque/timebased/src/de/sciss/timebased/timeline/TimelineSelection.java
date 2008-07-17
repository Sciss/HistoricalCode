package de.sciss.timebased.timeline;

import de.sciss.app.BasicEvent;
import de.sciss.io.Span;

public interface TimelineSelection
{
	public Timeline getTimeline();
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
		public void timelineSelected( Event e );
	}

	public static class Event
	extends BasicEvent
	{
	// --- ID values ---
		/**
		 *  returned by getID() : a portion of the timeline
		 *  has been selected or deselected
		 */
		public static final int CHANGED	= 0;
		
		private final TimelineSelection sel;

		/**
		 *  Constructs a new <code>TimelineEvent</code>
		 *
		 *  @param  source		who originated the action
		 *  @param  id			one of <code>CHANGED</code>
		 *  @param  when		system time when the event occured
		 */
		public Event( Object source, int id, long when, TimelineSelection sel )
		{
			super( source, id, when );
			this.sel = sel;
		}
		
		public TimelineSelection getSelection()
		{
			return sel;
		}

		public boolean incorporate( BasicEvent oldEvent )
		{
			if( oldEvent instanceof Event &&
				this.getSource() == oldEvent.getSource() &&
				this.getID() == oldEvent.getID() &&
				this.sel == ((Event) oldEvent).sel ) {
				
				// XXX beware, when the actionID and actionObj
				// are used, we have to deal with them here
				
				return true;

			} else return false;
		}
	}
}
