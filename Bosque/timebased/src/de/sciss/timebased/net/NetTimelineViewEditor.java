package de.sciss.timebased.net;

import java.awt.EventQueue;

import de.sciss.io.Span;
import de.sciss.timebased.timeline.TimelineView;

public class NetTimelineViewEditor
extends NetEditor
implements TimelineView.Editor
{
	public NetTimelineViewEditor( Master master )
	{
		super( master, "/time" );
	}

	public void editPosition( int id, long newPos )
	{
		if( !EventQueue.isDispatchThread() ) throw new IllegalMonitorStateException();
		
		final Client c = getClient( id );
		c.add( "pos", newPos );
	}
	
	public void editScroll( int id, Span newSpan )
	{
		if( !EventQueue.isDispatchThread() ) throw new IllegalMonitorStateException();
		
		final Client c = getClient( id );
		c.add( "scr", newSpan.start, newSpan.stop );
	}
	
	public void editSelect( int id, Span newSpan )
	{
		if( !EventQueue.isDispatchThread() ) throw new IllegalMonitorStateException();
		
		final Client c = getClient( id );
		c.add( "sel", newSpan.start, newSpan.stop );
	}
}
