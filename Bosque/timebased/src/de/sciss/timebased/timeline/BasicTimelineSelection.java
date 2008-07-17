package de.sciss.timebased.timeline;

import java.awt.EventQueue;

import de.sciss.app.BasicEvent;
import de.sciss.app.EventManager;
import de.sciss.io.Span;

public class BasicTimelineSelection
implements TimelineSelection, EventManager.Processor
{
	private final Timeline	tl;
	protected Span			span;
	
	// --- event handling ---

	protected final EventManager	elm		= new EventManager( this );
	
	public BasicTimelineSelection( Timeline tl )
	{
		this( tl, new Span( tl.getSpan().start, tl.getSpan().start ));
	}
	
	public BasicTimelineSelection( Timeline tl, Span span )
	{
		this.span	= span;
		this.tl		= tl;
		tl.addListener( new Timeline.Listener() {
			public void timelineChanged( Timeline.Event e )
			{
				final Span tlSpan = e.getTimeline().getSpan();
				if( !tlSpan.contains( BasicTimelineSelection.this.span )) {
					BasicTimelineSelection.this.span = BasicTimelineSelection.this.span.intersection( tlSpan );
					elm.dispatchEvent( new Event( e.getSource(), Event.CHANGED, e.getWhen(), BasicTimelineSelection.this ));
				}
			}
		});
	}
	
	public Timeline getTimeline()
	{
		return tl;
	}
	
	public Span getSpan()
	{
		if( !EventQueue.isDispatchThread() ) throw new IllegalMonitorStateException();
		return span;
	}

	public void setSpan( Object source, Span newSpan )
	{
		if( !EventQueue.isDispatchThread() ) throw new IllegalMonitorStateException();
		if( !span.equals( newSpan )) {
			span = newSpan;
			if( source != null ) dispatchChange( source );
		}
	}
	
	/**
	 *  Register a <code>TimelineListener</code>
	 *  which will be informed about changes of
	 *  the timeline (i.e. changes in rate and length,
	 *  scrolling in the timeline frame and selection
	 *  of timeline portions by the user).
	 *
	 *  @param  listener	the <code>TimelineListener</code> to register
	 *  @see	de.sciss.app.EventManager#addListener( Object )
	 */
	public void addListener( Listener listener )
	{
		elm.addListener( listener );
	}

	/**
	 *  Unregister a <code>TimelineListener</code>
	 *  from receiving timeline events.
	 *
	 *  @param  listener	the <code>TimelineListener</code> to unregister
	 *  @see	de.sciss.app.EventManager#removeListener( Object )
	 */
	public void removeListener( Listener listener )
	{
		elm.removeListener( listener );
	}
	
	/**
	 *  This is called by the EventManager
	 *  if new events are to be processed
	 */
	public void processEvent( BasicEvent e )
	{
		for( int i = 0; i < elm.countListeners(); i++ ) {
			final Listener l = (Listener) elm.getListener( i );
			switch( e.getID() ) {
			case Event.CHANGED:
				l.timelineSelected( (Event) e );
				break;
			default:
				assert false : e.getID();
			}
		}
	}
	
	// utility function to create and dispatch a TimelineEvent
	protected void dispatchChange( Object source )
	{
		elm.dispatchEvent( new Event( source, Event.CHANGED,
		                              System.currentTimeMillis(), this ));
	}
}