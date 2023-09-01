/*
 *  BasicTimeline.java
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
 *		13-Jun-04   using a null selection span is not allowed any more
 *		12-Aug-04   commented
 *		17-Jul-08	copied from Cillo
 */

package de.sciss.timebased.timeline;

import java.awt.EventQueue;

import de.sciss.app.BasicEvent;
import de.sciss.app.EventManager;
import de.sciss.io.Span;

/**
 *  This class describes the document's timeline
 *  properties, e.g. length, selection, visible span.
 *  It contains an event dispatcher for TimelineEvents
 *  which get fired when methods like setPosition are
 *  called.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.75, 10-Jun-08
 *
 *	@todo		a session length greater than approx.
 *				16 1/2 minutes produces deadlocks when
 *				select all is called on the timeline frame!!
 */
public class BasicTimeline
implements Timeline, EventManager.Processor
{
	private double	rate;		// frames per second
	private Span	span;

	// --- event handling ---

	private final EventManager	elm		= new EventManager( this );

	/**
	 *  Creates a new empty timeline
	 */
	public BasicTimeline()
	{
		this( 1000.0, new Span() );
	}
	
	public BasicTimeline( double rate )
	{
		this( rate, new Span() );
	}
	
	public BasicTimeline( double rate, Span span )
	{
		this.rate	= rate;
		this.span	= span;
	}
	
	public void dispose()
	{
		elm.dispose();
	}
	
	/**
	 *  Pauses the event dispatching. No
	 *  events are destroyed, only execution
	 *  is deferred until resumeDispatcher is
	 *  called.
	 */
	public void pauseDispatcher()
	{
		elm.pause();
	}

	/**
	 *  Resumes the event dispatching. Pending
	 *  events will be diffused during the
	 *  next run of the event thread.
	 */
	public void resumeDispatcher()
	{
		elm.resume();
	}

	/**
	 *  Queries the timeline sample rate
	 *
	 *  @return the rate of timeline data (trajectories etc.)
	 *			in frames per second
	 */
	public double getRate()
	{
		if( !EventQueue.isDispatchThread() ) throw new IllegalMonitorStateException();
		return rate;
	}

	/**
	 *  Queries the timeline's span
	 *
	 *  @return the timeline span
	 */
	public Span getSpan()
	{
		if( !EventQueue.isDispatchThread() ) throw new IllegalMonitorStateException();
		return span;
	}

	/**
	 *  Changes the timeline sample rate. This
	 *  fires a <code>TimelineEvent</code> (<code>CHANGED</code>). Note
	 *  that there's no corresponding undoable edit.
	 *
	 *  @param  source  the source of the <code>TimelineEvent</code>
	 *  @param  rate	the new rate of timeline data (trajectories etc.)
	 *					in frames per second
	 *
	 *  @see	TimelineEvent#CHANGED
	 */
    public void setRate( Object source, double rate )
    {
		if( !EventQueue.isDispatchThread() ) throw new IllegalMonitorStateException();
		if( this.rate != rate ) {
			this.rate = rate;
			if( source != null ) dispatchChange( source );
		}
    }

	/**
	 *  Changes the timeline's starting frame.
	 *  This fires a <code>Timeline.Event</code> (<code>CHANGED</code>).
	 *
	 *  @param  source  the source of the <code>TimelineEvent</code>
	 *  @param  newStop  the new timeline stopping frame
	 */
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
				l.timelineChanged( (Event) e );
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