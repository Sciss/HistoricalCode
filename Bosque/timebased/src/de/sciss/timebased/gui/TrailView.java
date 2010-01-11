/*
 *  TrailView.java
 *  de.sciss.timebased.gui package
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
 */
package de.sciss.timebased.gui;

import de.sciss.app.BasicEvent;
import de.sciss.app.EventManager;
import de.sciss.gui.ComponentHost;
import de.sciss.io.Span;
import de.sciss.timebased.Stake;
import de.sciss.timebased.Trail;
import de.sciss.timebased.session.Track;
import de.sciss.timebased.session.TrackBased;
import de.sciss.timebased.timeline.TimelineView;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;

/**
 *  @author		Hanns Holger Rutz
 *  @version	0.11, 20-Jul-08
 *  
 *  @todo		drag shape painting should be in the toppainter
 *  			otherwise complex graphics like waveform displays
 *  			need to be constantly redrawn!
 */
public class TrailView
extends JComponent
implements Trail.Listener, TimelineView.Listener, EventManager.Processor,
		   MouseInputListener
{
	private Trail				trail;
	private Span				timelineVis		= new Span();
	private StakeRenderer		stakeRenderer	= new DefaultStakeRenderer();
	
	private final ComponentHost	host;
	
	private final Set			setSelected		= new HashSet();
	
	private boolean				paintDrag		= false;
	private int					dragDeltaX, dragDeltaY, dragDeltaWidth, dragDeltaHeight;
	
	private static final Color	colrDrag		= Color.black;
	private static final Stroke	strkDrag		= new BasicStroke( 2f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 1f, new float[] { 4f, 4f }, 0f );
	
	private final GeneralPath	shpDrag			= new GeneralPath();

//	private final List			tracks			= new ArrayList();
//	private float				trackVScale		= 1f;
	
//	private final TimelineView	tlv;
	
	private TracksTable			tracksTable		= null;
	private final Rectangle		stakeBounds		= new Rectangle();
	private final Insets		stakeInsets		= new Insets( 0, 0, 0, 0 );
	private EventManager		elm				= null;
	
	public TrailView( ComponentHost host, TimelineView tlv )
	{
		super();
		this.host	= host;
//		this.tlv	= tlv;
		
		tlv.addListener( this );
		addMouseListener( this );
		addMouseMotionListener( this );
	}

	public TrailView( TimelineView tlv )
	{
		this( null, tlv );
	}
	
	public void setStakeRenderer( StakeRenderer sr )
	{
		stakeRenderer = sr;
	}
	
	public StakeRenderer getStakeRenderer()
	{
		return stakeRenderer;
	}
	
	public void setTrail( Trail t )
	{
		if( this.trail != null ) {
			this.trail.removeListener( this );
		}
		this.trail = t;
		if( t != null ) {
			t.addListener( this );
		}
		triggerRedisplay();
	}
	
	public Trail getTrail() { return trail; }
	
	public void setTracksTable( TracksTable tt )
	{
		tracksTable = tt;
		triggerRedisplay();
	}
	
	public TracksTable getTracksTable()
	{
		return tracksTable;
	}
	
//	public void setVisibleSpan( Span span )
//	{
//		timelineVis = span;
//		triggerRedisplay();
//	}
	
	public Span getVisibleSpan()
	{
		return timelineVis;
	}
	
	public void setDrag( int deltaX, int deltaY, int deltaWidth, int deltaHeight )
	{
		dragDeltaX		= deltaX;
		dragDeltaY		= deltaY;
		dragDeltaWidth	= deltaWidth;
		dragDeltaHeight	= deltaHeight;
		if( paintDrag ) triggerRedisplay();
	}
	
	public void setDragPainted( boolean b )
	{
		if( b != paintDrag ) {
			paintDrag = b;
			triggerRedisplay();
		}
	}
	
	public void addToSelection( List stakes )
	{
		boolean update = false;
		Stake stake;
		
		for( Iterator iter = stakes.iterator(); iter.hasNext(); ) {
			stake = (Stake) iter.next();
			update |= setSelected.add( stake ) && stake.getSpan().touches( timelineVis );
		}
		if( update ) triggerRedisplay();
	}
	
	public void addToSelection( Stake stake )
	{
		if( setSelected.add( stake ) && stake.getSpan().touches( timelineVis )) triggerRedisplay();
	}
	
	public void removeFromSelection( List stakes )
	{
		boolean update = false;
		Stake stake;
		
		for( Iterator iter = stakes.iterator(); iter.hasNext(); ) {
			stake = (Stake) iter.next();
			update |= setSelected.remove( stake ) && stake.getSpan().touches( timelineVis );
		}
		if( update ) triggerRedisplay();
	}
	
	public void removeFromSelection( Stake stake )
	{
		if( setSelected.remove( stake ) && stake.getSpan().touches( timelineVis )) triggerRedisplay();
	}
	
	public void paintComponent( Graphics g )
	{
		super.paintComponent( g );

		if( timelineVis.isEmpty() || (trail == null) ) return;

		final List				collVisi	= trail.getRange( timelineVis, true );
		final int				w			= getWidth();
//		final int				h			= getHeight();
		final double			hscale		= (double) w / timelineVis.getLength();
//		final float				vscale		= h; // trackVScale * h;
		Stake					s;
		Component				c;
		boolean					selected;	 // , muted = false, frozen = false;	// not yet used
//		AtomRect				ar			= null;
		Span					stakeSpan;
		int 					offx;

//		collAtomRects.clear();

//		g2.setFont( fntAtom );
//		System.out.println( "collVisi.size() = " + collVisi.size() );
		
		shpDrag.reset();

		for( int i = 0; i < collVisi.size(); i++ ) {
			s		= (Stake) collVisi.get( i );
//			System.out.println( "stake #" + i + " = " + s );
			selected	= setSelected.contains( s );
			if( (tracksTable != null) && (s instanceof TrackBased) ) {
				tracksTable.getTrackBounds( ((TrackBased) s).getTrack(), stakeBounds );
			} else {
				stakeBounds.y		= 0;
				stakeBounds.height	= getHeight();
			}
			stakeSpan		= s.getSpan();
			offx			= (int) ((stakeSpan.start - timelineVis.start) * hscale + 0.5);
			stakeBounds.x  += offx;
			stakeBounds.width = (int) ((stakeSpan.stop - timelineVis.start) * hscale + 0.5) - offx;
			c				= stakeRenderer.getStakeRendererComponent( this, s, selected, hscale, stakeBounds );
//			System.out.println( "stake #" + i + " = " + s );
			c.paint( g );
			
			if( paintDrag && selected ) {
				stakeBounds.x      += dragDeltaX;
				stakeBounds.y      += dragDeltaY;
				stakeBounds.width  += dragDeltaWidth;
				stakeBounds.height += dragDeltaHeight;
				shpDrag.append( stakeBounds, false );
			}
		}
		
		if( paintDrag ) {
			final Graphics2D g2 = (Graphics2D) g;
			g2.setColor( colrDrag );
			g2.setStroke( strkDrag );
			g2.draw( shpDrag );
//			for( int i = 0; i < collVisi.size(); i++ ) {
//				s			= (Stake) collVisi.get( i );
//				selected	= setSelected.contains( s );
//				if( selected ) {
//					g2.drawRect( 
//							(int) ((s.getSpan().start - timelineVis.start) * hscale + 0.5) + dragDeltaX,
//							dragDeltaY,
//							(int) (s.getSpan().getLength() * hscale + 0.5) + dragDeltaWidth,
//							h + dragDeltaHeight );
//				}
//			}
		}
//		
//		g2.setStroke( strkOrig );
	}

	public void triggerRedisplay()
	{
//		doRecalc	= true;
		if( host != null ) {
			host.update( this );
		} else if( isVisible() ) {
			repaint();
		}
	}
	
	public void addListener( Listener l )
	{
		if( elm == null ) elm = new EventManager( this );
		elm.addListener( l );
	}

	public void removeListener( Listener l )
	{
		elm.removeListener( l );
	}
	
	private void dispatch( MouseEvent me )
	{
		if( elm == null ) return;
		elm.dispatchEvent( new Event( me ));
	}

	// ----------- MouseInputListener interface -----------
	
	public void mousePressed( MouseEvent e )  { dispatch( e );}
	public void mouseReleased( MouseEvent e ) { dispatch( e );}
	public void mouseClicked( MouseEvent e )  { dispatch( e );}
	public void mouseEntered( MouseEvent e )  { dispatch( e );}
	public void mouseExited( MouseEvent e )   { dispatch( e );}
	public void mouseMoved( MouseEvent e )    { dispatch( e );}
	public void mouseDragged( MouseEvent e )  { dispatch( e );}

	// ----------- TimelineView.Listener interface -----------
	
	public void timelineChanged( TimelineView.Event e ) { /* nothing */ }
	public void timelinePositioned( TimelineView.Event e ) { /* nothing */ }
	public void timelineSelected( TimelineView.Event e ) { /* nothing */ }
	
	public void timelineScrolled( TimelineView.Event e )
	{
		timelineVis = e.getView().getSpan();
		triggerRedisplay();
	}
	
	// ----------- Trail.Listener interface -----------
	
	public void trailModified( Trail.Event e )
	{
		if( e.getAffectedSpan().touches( timelineVis )) {
			triggerRedisplay(); // XXX could be a more efficient update rectangle
		}
	}

	// ----------- EventManager.Processor interface -----------
	
	public void processEvent( BasicEvent be )
	{
		final Event		e		= (Event) be;
		final double	hscale	= (double) getWidth() / timelineVis.getLength();
		final long		frame	= (long) (e.me.getX() / hscale + 0.5) + timelineVis.start;
		final float		level;
		final float		innerLevel;
		Track			track	= null;
		Stake			stake	= null;
		int				hitIdx	= -1;
	
findTrack:
		if( tracksTable != null ) {
			for( int i = 0; i < tracksTable.getNumTracks(); i++ ) {
				track = tracksTable.getTrack( i );
				tracksTable.getTrackBounds( track, stakeBounds );
				if( stakeBounds.contains( e.me.getPoint() )) break findTrack;
			}
			track = null;
		}
findStake:
		if( trail != null ) {
			final List collHit = trail.getRange( new Span( frame, frame + 1), true );
			for( int i = 0; i < collHit.size(); i++ ) {
				stake = (Stake) collHit.get( i );
				if( !(stake instanceof TrackBased) ) break findStake; // assuming it covers the whole vertical span
				if( ((TrackBased) stake).getTrack() == track ) break findStake;
			}
			stake = null;
		}
		
		if( track != null ) {
			level	= 1.0f - ((float) (e.me.getY() - stakeBounds.y) / (Math.max( 1, stakeBounds.height - 1)) );
			if( stake != null ) {
				stakeRenderer.getInsets( stakeInsets, this, stake );
				final int offx	   = (int) ((stake.getSpan().start - timelineVis.start) * hscale + 0.5);
				stakeBounds.x     += offx + stakeInsets.left;
				stakeBounds.width  = (int) ((stake.getSpan().stop - timelineVis.start) * hscale + 0.5) - offx - (stakeInsets.left + stakeInsets.right);
				stakeBounds.y      += stakeInsets.top;
				stakeBounds.height -= stakeInsets.top + stakeInsets.bottom;
				innerLevel	= 1.0f - ((float) (e.me.getY() - stakeBounds.y) /
					(Math.max( 1, stakeBounds.height - 1)) );
				hitIdx = stakeRenderer.getHitIndex( stake, frame, innerLevel, stakeBounds, e.me );
			} else {
				innerLevel	= level;
			}
			
		} else {
			level	= 1.0f - ((float) e.me.getY() / (Math.max( 1, e.me.getComponent().getHeight() - 1)) );
			if( stake != null ) {
				stakeRenderer.getInsets( stakeInsets, this, stake );
				innerLevel	= 1.0f - ((float) (e.me.getY() - stakeInsets.top) /
					(Math.max( 1, e.me.getComponent().getHeight() - (stakeInsets.top + stakeInsets.bottom)- 1)) );
			} else {
				innerLevel	= level;
			}
		}
				
		for( int i = 0; i < elm.countListeners(); i++ ) {
			final Listener l = (Listener) elm.getListener( i );
			l.mouseAction( e.me, frame, level, innerLevel, hitIdx, track, stake );
		}
	}

	// ----------- inner Interfaces -----------
	
	public interface Listener
	{
		public void mouseAction( MouseEvent e, long frame, float level, float innerLevel,
								 int hitIdx, Track t, Stake s );
	}
	
	private static class Event
	extends BasicEvent
	{
		protected final MouseEvent me;
		
		protected Event( MouseEvent me )
		{
			super( me.getSource(), me.getID(), me.getWhen() );
			this.me	= me;
		}
		
		public boolean incorporate( BasicEvent be )
		{
			if( be instanceof Event ) {
				final Event e = (Event) be;
				return( (me.getSource() == e.me.getSource()) &&
						(me.getID() == e.me.getID()) &&
						(me.getComponent() == e.me.getComponent()) );
			}
			return false;
		}
	}
	
}