package de.sciss.timebased.timeline;

import de.sciss.app.BasicEvent;

public interface TimelineCursor
{
	public Timeline getTimeline();
	public long getPosition();
	public void setPosition( Object source, long newPos );

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
		public void timelinePositioned( Event e );
	}

	public static class Event
	extends BasicEvent
	{
	// --- ID values ---
		/**
		 *  returned by getID() : the 'playback head' of
		 *  the timelime has been moved
		 */
		public static final int CHANGED	= 0;
		
		private final TimelineCursor cursor;

		/**
		 *  Constructs a new <code>TimelineEvent</code>
		 *
		 *  @param  source		who originated the action
		 *  @param  id			one of <code>CHANGED</code>
		 *  @param  when		system time when the event occured
		 */
		public Event( Object source, int id, long when, TimelineCursor cursor )
		{
			super( source, id, when );
			this.cursor	= cursor;
		}
		
		public TimelineCursor getCursor()
		{
			return cursor;
		}

		public boolean incorporate( BasicEvent oldEvent )
		{
			if( oldEvent instanceof Event &&
				this.getSource() == oldEvent.getSource() &&
				this.getID() == oldEvent.getID() &&
				this.cursor == ((Event) oldEvent).cursor ) {
				
				// XXX beware, when the actionID and actionObj
				// are used, we have to deal with them here
				
				return true;

			} else return false;
		}
	}
}
