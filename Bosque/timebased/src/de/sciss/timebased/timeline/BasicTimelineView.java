package de.sciss.timebased.timeline;

import java.awt.EventQueue;

import de.sciss.app.BasicEvent;
import de.sciss.app.EventManager;
import de.sciss.io.Span;

public class BasicTimelineView
implements TimelineView, EventManager.Processor
{
	private final Timeline			tl;
	protected Span					span;
	private final TimelineCursor	cursor;
	private final TimelineSelection	sel;
	
	// --- event handling ---

	protected final EventManager	elm		= new EventManager( this );
	
	public BasicTimelineView( Timeline tl )
	{
		this( tl, tl.getSpan() );
	}
	
	public BasicTimelineView( Timeline tl, Span span )
	{
		this( tl, span, new BasicTimelineCursor( tl ), new BasicTimelineSelection( tl ));
	}
	
	public BasicTimelineView( Timeline tl, Span span, TimelineCursor cursor, TimelineSelection sel )
	{
		this.span	= span;
		this.tl		= tl;
		this.cursor	= cursor;
		this.sel	= sel;
		tl.addListener( new Timeline.Listener() {
			public void timelineChanged( Timeline.Event e )
			{
				final Span tlSpan = e.getTimeline().getSpan();
				if( !tlSpan.contains( BasicTimelineView.this.span )) {
					if( tlSpan.start > BasicTimelineView.this.span.start ) {
						BasicTimelineView.this.span = new Span( tlSpan.start,
						    Math.min( tlSpan.stop, tlSpan.start + BasicTimelineView.this.span.getLength() ));
					} else {
						BasicTimelineView.this.span = new Span(
						    Math.max( tlSpan.start, tlSpan.stop - BasicTimelineView.this.span.getLength() ),
						    tlSpan.stop );
					}
					elm.dispatchEvent( Event.convert( e, BasicTimelineView.this ));
				}
				elm.dispatchEvent( Event.convert( e, BasicTimelineView.this ));
			}
		});
		cursor.addListener( new TimelineCursor.Listener() {
			public void timelinePositioned( TimelineCursor.Event e )
			{
				elm.dispatchEvent( Event.convert( e, BasicTimelineView.this ));
			}
		});
		sel.addListener( new TimelineSelection.Listener() {
			public void timelineSelected( TimelineSelection.Event e )
			{
				elm.dispatchEvent( Event.convert( e, BasicTimelineView.this ));
			}
		});
	}
	
	public Timeline getTimeline()
	{
		return tl;
	}
	
	public TimelineCursor getCursor()
	{
		return cursor;
	}
	
	public TimelineSelection getSelection()
	{
		return sel;
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
			if( source != null ) dispatchScroll( source );
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
			case Event.POSITIONED:
				l.timelinePositioned( (Event) e );
				break;
			case Event.SELECTED:
				l.timelineSelected( (Event) e );
				break;
			case Event.SCROLLED:
				l.timelineScrolled( (Event) e );
				break;
			case Event.CHANGED:
				l.timelineChanged( (Event) e );
				break;
			default:
				assert false : e.getID();
			}
		}
	}
	
	// utility function to create and dispatch a TimelineEvent
	protected void dispatchScroll( Object source )
	{
		elm.dispatchEvent( new Event( source, Event.SCROLLED,
		                              System.currentTimeMillis(), this ));
	}
}