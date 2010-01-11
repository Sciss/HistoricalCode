/*
 *  BasicTimelineView.java
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

import java.awt.EventQueue;

import de.sciss.app.BasicEvent;
import de.sciss.app.EventManager;
import de.sciss.io.Span;
import de.sciss.timebased.AbstractEditor;

public class BasicTimelineView
implements TimelineView, EventManager.Processor
{
	private final Timeline						tl;
	protected Span								span;
	private final TimelineCursor				cursor;
	private final TimelineSelection				sel;
	
	private final Timeline.Listener				tll;
	private final TimelineCursor.Listener		tlcl;
	private final TimelineSelection.Listener	tlsl;
	
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
		tll = new Timeline.Listener() {
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
		};
		
		tlcl = new TimelineCursor.Listener() {
			public void timelinePositioned( TimelineCursor.Event e )
			{
				elm.dispatchEvent( Event.convert( e, BasicTimelineView.this ));
			}
		};
		
		tlsl = new TimelineSelection.Listener() {
			public void timelineSelected( TimelineSelection.Event e )
			{
				elm.dispatchEvent( Event.convert( e, BasicTimelineView.this ));
			}
		};
		
		tl.addListener( tll );
		cursor.addListener( tlcl );
		sel.addListener( tlsl );
	}
	
	public void dispose()
	{
		tl.removeListener( tll );
		cursor.removeListener( tlcl );
		sel.removeListener( tlsl );
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

// ------------------- inner classes -------------------
	
	public static class Editor
	extends AbstractEditor
	implements TimelineView.Editor
	{
		private final TimelineView view;
		
		public Editor( TimelineView view )
		{
			super();
			this.view	= view;
		}
		
		public void editPosition( int id, long newPos )
		{
			final Client c = getClient( id );
			c.edit.addPerform( TimelineView.Edit.position( c.source, view, newPos ));
		}
		
		public void editScroll( int id, Span newSpan )
		{
			final Client c = getClient( id );
			c.edit.addPerform( TimelineView.Edit.scroll( c.source, view, newSpan ));
		}
		
		public void editSelect( int id, Span newSpan )
		{
			final Client c = getClient( id );
			c.edit.addPerform( TimelineView.Edit.select( c.source, view, newSpan ));
		}
	}
}