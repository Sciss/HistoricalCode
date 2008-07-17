package de.sciss.timebased.timeline;

import de.sciss.app.BasicEvent;
import de.sciss.io.Span;

public interface TimelineView
{
	public Timeline getTimeline();
	public TimelineCursor getCursor();
	public TimelineSelection getSelection();
	
	/**
	 *	@return	the view span, that is the visible span inside the whole timeline's span.
	 */
	public Span getSpan();
	public void setSpan( Object source, Span span );

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
		/**
		 *  Notifies the listener that
		 *  the basic timeline properties were modified
		 *  (e.g. the length or rate changed).
		 *
		 *  @param  e   the event describing
		 *				the timeline modification
		 */
		public void timelineSelected( Event e );
		/**
		 *  Notifies the listener that
		 *  the basic timeline properties were modified
		 *  (e.g. the length or rate changed).
		 *
		 *  @param  e   the event describing
		 *				the timeline modification
		 */
		public void timelineChanged( Event e );
		/**
		 *  Notifies the listener that
		 *  a the view of the timeline frame was scrolled
		 *  to a new position (or zoomed).
		 *
		 *  @param  e   the event describing
		 *				the timeline scrolling
		 *				(<code>getActionObj</code> will
		 *				return the new visible span)
		 */
		public void timelineScrolled( Event e );
	}

	public static class Event
	extends BasicEvent
	{
	// --- ID values ---
		/**
		 *  returned by getID() : a portion of the timeline
		 *  has been selected or deselected
		 */
		public static final int SELECTED	= 0;
		/**
		 *  returned by getID() : the basic properties of
		 *  the timeline, rate or length, have been modified.
	     *  <code>actionObj</code> is a (potentially empty)
	     *  <code>Span</code> object
		 */
		public static final int CHANGED		= 1;
		/**
		 *  returned by getID() : the 'playback head' of
		 *  the timelime has been moved
		 */
		public static final int POSITIONED	= 2;
		/**
		 *  returned by getID() : the visible portion of
		 *  the timelime has been changed.
	     *  <code>actionObj</code> is a (potentially empty)
	     *  <code>Span</code> object
		 */
		public static final int SCROLLED	= 3;
		
		private final TimelineView view;

		/**
		 *  Constructs a new <code>TimelineEvent</code>
		 *
		 *  @param  source		who originated the action
		 *  @param  id			one of <code>CHANGED</code>
		 *  @param  when		system time when the event occured
		 */
		public Event( Object source, int id, long when, TimelineView view )
		{
			super( source, id, when );
			this.view = view;
		}
		
		public static Event convert( Timeline.Event e, TimelineView view )
		{
			if( e.getID() != Timeline.Event.CHANGED ) throw new IllegalArgumentException();
			return new Event( e.getSource(), Event.CHANGED, e.getWhen(), view );
		}
		
		public static Event convert( TimelineCursor.Event e, TimelineView view )
		{
			if( e.getID() != TimelineCursor.Event.CHANGED ) throw new IllegalArgumentException();
			return new Event( e.getSource(), Event.POSITIONED, e.getWhen(), view );
		}

		public static Event convert( TimelineSelection.Event e, TimelineView view )
		{
			if( e.getID() != TimelineCursor.Event.CHANGED ) throw new IllegalArgumentException();
			return new Event( e.getSource(), Event.SELECTED, e.getWhen(), view );
		}

		public TimelineView getView()
		{
			return view;
		}

		public boolean incorporate( BasicEvent oldEvent )
		{
			if( oldEvent instanceof Event &&
				this.getSource() == oldEvent.getSource() &&
				this.getID() == oldEvent.getID() &&
				this.view == ((Event) oldEvent).view ) {
				
				// XXX beware, when the actionID and actionObj
				// are used, we have to deal with them here
				
				return true;

			} else return false;
		}
	}
}
