/*
 *  TrailView.java
 *  de.sciss.timebased.gui package
 *
 *  Copyright (c) 2007-2008 Hanns Holger Rutz. All rights reserved.
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

import de.sciss.gui.ComponentHost;
import de.sciss.io.Span;
import de.sciss.timebased.Stake;
import de.sciss.timebased.ForestTrack;
import de.sciss.timebased.Trail;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;

/**
 *  @author		Hanns Holger Rutz
 *  @version	0.10, 14-Aug-07
 */
public class TrailView
extends JComponent
implements Trail.Listener
{
	private Trail				t;
	private Span				timelineVis		= new Span();
	private StakeRenderer		stakeRenderer	= new DefaultStakeRenderer();
	
	private final ComponentHost	host;
	
	private final Set			setSelected		= new HashSet();
	
	private boolean				paintDrag		= false;
	private int					dragDeltaX, dragDeltaY, dragDeltaWidth, dragDeltaHeight;
	
	private static final Color	colrDrag		= Color.black;
	private static final Stroke	strkDrag		= new BasicStroke( 2f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 1f, new float[] { 4f, 4f }, 0f );

	private final List			tracks			= new ArrayList();
	private float				trackVScale		= 1f;
	
	public TrailView( ComponentHost host )
	{
		super();
		this.host	= host;
	}

	public TrailView()
	{
		this( null );
	}
	
	public StakeRenderer getStakeRenderer()
	{
		return stakeRenderer;
	}
	
	public void addTrack( ForestTrack ft )
	{
		tracks.add( ft );
		recalcTrackBounds();
		triggerRedisplay();
	}
	
	public void removeTrack( ForestTrack ft )
	{
		tracks.remove( ft );
		recalcTrackBounds();
		triggerRedisplay();
	}
	
	private void recalcTrackBounds()
	{
		float y = 0f;
		ForestTrack ft;
		for( int i = 0; i < tracks.size(); i++ ) {
			ft = (ForestTrack) tracks.get( i );
			ft.y = y;
			y += ft.height;
		}
		trackVScale = y == 0f ? 1f : (1f / y);
	}
	
	public void setTrail( Trail t )
	{
		if( this.t != null ) {
			this.t.removeListener( this );
		}
		this.t = t;
		if( t != null ) {
			t.addListener( this );
		}
		triggerRedisplay();
	}
	
	public void setVisibleSpan( Span span )
	{
		timelineVis = span;
		triggerRedisplay();
	}
	
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

		if( timelineVis.isEmpty() || (t == null) ) return;

		final List				collVisi	= t.getRange( timelineVis, true );
		final int				w			= getWidth();
		final int				h			= getHeight();
		final double			hscale		= (double) w / timelineVis.getLength();
		final float				vscale		= trackVScale * h;
		Stake					s;
		Component				c;
		boolean					selected;	 // , muted = false, frozen = false;	// not yet used
//		AtomRect				ar			= null;

//		collAtomRects.clear();

//		g2.setFont( fntAtom );
//		System.out.println( "collVisi.size() = " + collVisi.size() );

		for( int i = 0; i < collVisi.size(); i++ ) {
			s		= (Stake) collVisi.get( i );
//			System.out.println( "stake #" + i + " = " + s );
			selected	= setSelected.contains( s );
			c			= stakeRenderer.getStakeRendererComponent( this, s, selected, hscale, vscale );
//			System.out.println( "stake #" + i + " = " + s );
			c.paint( g );
		}
		
		if( paintDrag ) {
			final Graphics2D g2 = (Graphics2D) g;
			g2.setColor( colrDrag );
			g2.setStroke( strkDrag );
			for( int i = 0; i < collVisi.size(); i++ ) {
				s			= (Stake) collVisi.get( i );
				selected	= setSelected.contains( s );
				if( selected ) {
					g2.drawRect( 
							(int) ((s.getSpan().start - timelineVis.start) * hscale + 0.5) + dragDeltaX,
							dragDeltaY,
							(int) (s.getSpan().getLength() * hscale + 0.5) + dragDeltaWidth,
							h + dragDeltaHeight );
				}
			}
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

	// ----------- Trail.Listener interface -----------
	
	public void trailModified( Trail.Event e )
	{
		if( e.getAffectedSpan().touches( timelineVis )) {
			triggerRedisplay();
		}
	}
}