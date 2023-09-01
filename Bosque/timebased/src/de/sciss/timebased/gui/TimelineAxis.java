/*
 *  TimelineAxis.java
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
 *		12-May-05	re-created from de.sciss.meloncillo.timeline.TimelineAxis
 *		16-Jul-05	allows to switch between time and samples units
 *		13-Jul-08	copied back from EisK
 *		18-Jul-08	copied from Cillo
 */

package de.sciss.timebased.gui;

import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

import de.sciss.gui.ComponentHost;
import de.sciss.gui.VectorSpace;

import de.sciss.app.AbstractApplication;
import de.sciss.app.Application;
import de.sciss.app.DynamicAncestorAdapter;
import de.sciss.app.DynamicListening;
import de.sciss.app.GraphicsHandler;

import de.sciss.io.Span;
import de.sciss.timebased.timeline.TimelineView;

/**
 *  A GUI element for displaying
 *  the timeline's axis (ruler)
 *  which is used to display the
 *  time indices and to allow the
 *  user to position and select the
 *  timeline.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.70, 09-Jan-10
 */
public class TimelineAxis
extends Axis
implements	TimelineView.Listener,
			DynamicListening
{
    private final TimelineView			view;
	
	private boolean						isListening		= false;
	protected TimelineView.Editor		editor			= null;
	
	private final MouseInputListener	mil;
    
	public TimelineAxis( TimelineView view )
	{
		this( view, null );
	}
	
	/**
	 *  Constructs a new object for
	 *  displaying the timeline ruler
	 *
	 *  @param  root	application root
	 *  @param  doc		session Session
	 */
	public TimelineAxis( final TimelineView view, ComponentHost host )
	{
		super( HORIZONTAL, TIMEFORMAT, host );
        
        this.view = view;
        
 		final Application app = AbstractApplication.getApplication();
 		if( app != null ) setFont( app.getGraphicsHandler().getFont( GraphicsHandler.FONT_SYSTEM | GraphicsHandler.FONT_MINI ));
		
		// --- Listener ---
        mil = new MouseInputAdapter() {
        	// when the user begins a selection by shift+clicking, the
        	// initially fixed selection bound is saved to selectionStart.
        	private long				selectionStart  = -1;
        	private boolean				shiftDrag, altDrag;
        	
        	public void mousePressed( MouseEvent e )
        	{
        		shiftDrag		= e.isShiftDown();
        		altDrag			= e.isAltDown();
        		selectionStart  = -1;
        		dragTimelinePosition( e );
        	}

        	public void mouseDragged( MouseEvent e )
        	{
        		dragTimelinePosition( e );
        	}
        	
        	private void dragTimelinePosition( MouseEvent e )
        	{
        		if( editor == null ) return;
        		
        		final int		x   = e.getX();
        		Span			span, span2;
        		long			position;

        		// translate into a valid time offset
                span        = view.getSpan();
                position    = span.getStart() + (long) ((double) x / (double) getWidth() *
                                                        span.getLength());
                position    = view.getTimeline().getSpan().clip( position );
                
                final int id = editor.editBegin( TimelineAxis.this, getResourceString( "editTimelineView" ));
                
                if( shiftDrag ) {
        			span2	= view.getSelection().getSpan();
        			if( altDrag || span2.isEmpty() ) {
        				selectionStart = view.getCursor().getPosition();
        				altDrag = false;
        			} else if( selectionStart == -1 ) {
        				selectionStart = Math.abs( span2.getStart() - position ) >
        								 Math.abs( span2.getStop() - position ) ?
        								 span2.getStart() : span2.getStop();
        			}
        			span	= new Span( Math.min( position, selectionStart ),
        								Math.max( position, selectionStart ));
//System.out.println( "sel span " + span2 + "; pos " + position + "; selectionStart " + selectionStart + "; new span " + span );
//        			edit	= TimelineView.Edit.select( this, view, span ).perform();
        			editor.editSelect( id, span );
                } else {
        			if( altDrag ) {
//        				edit	= new CompoundEdit();
//        				edit.addEdit( TimelineView.Edit.select( this, view, new Span() ).perform() );
//        				edit.addEdit( TimelineView.Edit.position( this, view, position ).perform() );
//        				((CompoundEdit) edit).end();
        				editor.editSelect( id, new Span() );
        				editor.editPosition( id, position );
        				altDrag = false;
        			} else {
//        				edit	= TimelineView.Edit.position( this, view, position ).perform();
        				editor.editPosition( id, position );
        			}
                }
//                if( undoMgr != null ) undoMgr.addEdit( edit );
                editor.editEnd( id );
        	}
        };
        
        new DynamicAncestorAdapter( this ).addTo( this );
//        new DynamicAncestorAdapter( new DynamicPrefChangeManager(
//			AbstractApplication.getApplication().getUserPrefs(), new String[] { PrefsUtil.KEY_TIMEUNITS }, this
//		)).addTo( this );
        
//        recalcSpace();
	}
	
	public void setEditor( TimelineView.Editor newEditor )
	{
		if( editor != newEditor ) {
			if( (editor != null) && (newEditor == null) ) {
				this.removeMouseListener( mil );
				this.removeMouseMotionListener( mil );
			} else if( (editor == null) && (newEditor != null) ) {
				this.addMouseListener( mil );
				this.addMouseMotionListener( mil );
			}
			editor = newEditor;
		}
	}

	private void recalcSpace()
	{
		final Span			visibleSpan;
		final double		d1;
		final VectorSpace	space;
	
		visibleSpan = view.getSpan();
		if( (getFlags() & TIMEFORMAT) == 0 ) {
			space	= VectorSpace.createLinSpace( visibleSpan.getStart(),
												  visibleSpan.getStop(),
												  0.0, 1.0, null, null, null, null );
		} else {
			d1		= 1.0 / view.getTimeline().getRate();
			space	= VectorSpace.createLinSpace( visibleSpan.getStart() * d1,
												  visibleSpan.getStop() * d1,
												  0.0, 1.0, null, null, null, null );
		}
		setSpace( space );
	}
	
// ---------------- PreferenceChangeListener interface ---------------- 
//
//		public void preferenceChange( PreferenceChangeEvent e )
//		{
//			final String key = e.getKey();
//			
//			if( key.equals( PrefsUtil.KEY_TIMEUNITS )) {
//				int timeUnits = e.getNode().getInt( key, 0 );
//				if( timeUnits == 0 ) {
//					setFlags( INTEGERS );
//				} else {
//					setFlags( TIMEFORMAT );
//				}
//				recalcSpace();
//			}
//		}
//
// ---------------- DynamicListening interface ---------------- 

    public void startListening()
    {
    	if( !isListening ) {
//System.out.println( "TLA start" );
    		isListening = true;
    		view.addListener( this );
    		recalcSpace();
    	}
    }

    public void stopListening()
    {
    	if( isListening ) {
//System.out.println( "TLA stop" );
    		isListening = false;
    		view.removeListener( this );
    	}
    }

	protected static String getResourceString( String key )
	{
		return( AbstractApplication.getApplication().getResourceString( key ));
	}

	// -------------- Disposable interface --------------

	public void dispose()
	{
		stopListening();
		setEditor( null );
//		undoMgr = null;
		super.dispose();
	}

// ---------------- TimelineListener interface ---------------- 
  
   	public void timelineSelected( TimelineView.Event e ) { /* ignore */ }
	public void timelinePositioned( TimelineView.Event e ) { /* ignore */ }

	public void timelineChanged( TimelineView.Event e )
	{
		recalcSpace();
	}

   	public void timelineScrolled( TimelineView.Event e )
    {
		recalcSpace();
    }
}