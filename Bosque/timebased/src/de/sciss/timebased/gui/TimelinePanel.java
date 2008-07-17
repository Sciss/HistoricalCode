/*
 *  TimelinePanel.java
 *  de.sciss.timebased.gui package
 *
 *  Copyright (c) 2007 Hanns Holger Rutz. All rights reserved.
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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.Timer;

import de.sciss.gui.ComponentHost;
import de.sciss.gui.TopPainter;
import de.sciss.io.Span;

/**
 *  @author		Hanns Holger Rutz
 *  @version	0.11, 23-Aug-07
 */
public class TimelinePanel
extends ComponentHost
implements TopPainter
{
	private final Color colrSelection			= new Color( 0x00, 0x00, 0xFF, 0x2F ); // GraphicsUtil.colrSelection;
	private final Color colrPosition			= new Color( 0xFF, 0x00, 0x00, 0x7F );

	private Rectangle   vpRecentRect			= new Rectangle();
	private int			vpPosition				= -1;
	private Rectangle   vpPositionRect			= new Rectangle();
	private final ArrayList	vpSelections		= new ArrayList();
	private final ArrayList	vpSelectionColors	= new ArrayList();
	private Rectangle	vpSelectionRect			= new Rectangle();
	
	private Rectangle   vpUpdateRect			= new Rectangle();
//	private Rectangle   vpZoomRect				= null;
//	private float[]		vpDash					= { 3.0f, 5.0f };
	private float		vpScale;
	
	private Span		timelineVis				= new Span();
	private Span		timelineSel				= new Span();
	protected long		timelinePos				= 0;

	private final Timer	playTimer;
	
	// !!! for some crazy reason, these need to be volatile because otherwise
	// the playTimer's actionPerformed body might use a cached value !!!
	// how can this happen when javax.swing.Timer is playing on the event thread?!
	protected double		timelineRate			= 44100.0;
	protected double		playRate				= 1.0;
	protected long			playStartPos			= 0;
	protected long			playStartTime;
	protected boolean		isPlaying				= false;

	public TimelinePanel()
	{
		addTopPainter( this );
		setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ));

		playTimer = new Timer( 33, new ActionListener() {
			public void actionPerformed( ActionEvent e )
			{
				// the swing timer doesn't have a cancel method,
				// hence events already scheduled will be delivered
				// even if stop is called between firing and delivery(?)
				if( !isPlaying ) return;
				
				timelinePos = (long) ((System.currentTimeMillis() - playStartTime) * timelineRate * playRate / 1000 + playStartPos);
//System.out.println( "playTimer : " + timelinePos );
				updatePositionAndRepaint();
//				scroll.setPosition( timelinePos, 50, TimelineScroll.TYPE_TRANSPORT );
			}
		});
	}

	public void paintOnTop( Graphics2D g2 )
	{
		Rectangle r;

		r = new Rectangle( 0, 0, getWidth(), getHeight() ); // getViewRect();
		if( !vpRecentRect.equals( r )) {
			recalcTransforms( r );
		}

		for( int i = 0; i < vpSelections.size(); i++ ) {
			r = (Rectangle) vpSelections.get( i );
			g2.setColor( (Color) vpSelectionColors.get( i ));
			g2.fillRect( vpSelectionRect.x, r.y - vpRecentRect.y, vpSelectionRect.width, r.height );
		}
		
//		if( markVisible ) {
//			markAxis.paintFlagSticks( g2, vpRecentRect );
//		}
		
		g2.setColor( colrPosition );
		g2.drawLine( vpPosition, 0, vpPosition, vpRecentRect.height );

//		if( vpZoomRect != null ) {
//			g2.setColor( colrZoom );
//			g2.setStroke( vpZoomStroke[ vpZoomStrokeIdx ]);
//			g2.drawRect( vpZoomRect.x, vpZoomRect.y, vpZoomRect.width, vpZoomRect.height );
//		}
	}

	public void setVisibleSpan( long start, long stop )
	{
		setVisibleSpan( new Span( start, stop ));
	}

	public void setVisibleSpan( Span span )
	{
		timelineVis	= span;

//		updateOverviews( false, true );
		updateTransformsAndRepaint( false );
	}
	
	public void setSelectionSpan( long start, long stop )
	{
		setSelectionSpan( new Span( start, stop ));
	}

	public void setSelectionSpan( Span span )
	{
//		final boolean	wasEmpty = timelineSel.isEmpty();
//		final boolean	isEmpty;
	
		timelineSel	= span;

		updateSelectionAndRepaint();
//		isEmpty	= timelineSel.isEmpty();
//		if( wasEmpty != isEmpty ) {
//			updateEditEnabled( !isEmpty );
//		}
    }
	
	public void setPosition( long pos )
	{
		timelinePos		= pos;
		playStartPos	= pos;
		playStartTime	= System.currentTimeMillis();
//System.out.println( "setPosition : " + timelinePos );
		
		updatePositionAndRepaint();
//		scroll.setPosition( timelinePos, 0, pointerTool.validDrag ?
//			TimelineScroll.TYPE_DRAG : TimelineScroll.TYPE_UNKNOWN );
	}

	public void setRate( double rate )
	{
		timelineRate				= rate;
//		timelineLen					= doc.timeline.getLength();
		playTimer.setDelay( Math.min( (int) (1000 / (vpScale * timelineRate * playRate)), 33 ));
//		updateAFDGadget();
//		updateOverviews( false, true );
	}
	
	public void play( double rate )
	{
//System.out.println( "play : " + playStartPos );
		playRate		= rate;
		playStartTime	= System.currentTimeMillis();
		playTimer.setDelay( Math.min( (int) (1000 / (vpScale * timelineRate * playRate)), 33 ));
		isPlaying		= true;
		playTimer.restart();
	}
	
	public void stop()
	{
		isPlaying = false;
		playTimer.stop();
	}

	public void dispose()
	{
		this.stop();
		super.dispose();
	}

	private void recalcTransforms( Rectangle newRect )
	{
		int x, w;
		
		vpRecentRect = newRect; // getViewRect();
	
		if( !timelineVis.isEmpty() ) {
			vpScale			= (float) vpRecentRect.width / (float) Math.max( 1, timelineVis.getLength() ); // - 1;
			playTimer.setDelay( Math.min( (int) (1000 / (vpScale * timelineRate * playRate)), 33 ));
			vpPosition		= (int) ((timelinePos - timelineVis.getStart()) * vpScale + 0.5f);
			vpPositionRect.setBounds( vpPosition, 0, 1, vpRecentRect.height );
			if( !timelineSel.isEmpty() ) {
				x			= (int) ((timelineSel.getStart() - timelineVis.getStart()) * vpScale + 0.5f) + vpRecentRect.x;
				w			= Math.max( 1, (int) ((timelineSel.getStop() - timelineVis.getStart()) * vpScale + 0.5f) - x );
				vpSelectionRect.setBounds( x, 0, w, vpRecentRect.height );
			} else {
				vpSelectionRect.setBounds( 0, 0, 0, 0 );
			}
		} else {
			vpScale			= 0.0f;
			vpPosition		= -1;
			vpPositionRect.setBounds( 0, 0, 0, 0 );
			vpSelectionRect.setBounds( 0, 0, 0, 0 );
		}
	}

	protected void updatePositionAndRepaint()
	{
		boolean pEmpty, cEmpty;
		int		x, x2;
		
		pEmpty = (vpPositionRect.x + vpPositionRect.width < 0) || (vpPositionRect.x > vpRecentRect.width);
		if( !pEmpty ) vpUpdateRect.setBounds( vpPositionRect );

//			recalcTransforms();
		if( vpScale > 0f ) {
			vpPosition	= (int) ((timelinePos - timelineVis.getStart()) * vpScale + 0.5f);
//				positionRect.setBounds( position, 0, 1, recentRect.height );
			// choose update rect such that even a paint manager delay of 200 milliseconds
			// will still catch the (then advanced) position so we don't see flickering!
			// XXX this should take playback rate into account, though
			vpPositionRect.setBounds( vpPosition, 0, Math.max( 1, (int) (vpScale * timelineRate * 0.2f) ), vpRecentRect.height );
		} else {
			vpPosition	= -1;
			vpPositionRect.setBounds( 0, 0, 0, 0 );
		}

		cEmpty = (vpPositionRect.x + vpPositionRect.width <= 0) || (vpPositionRect.x > vpRecentRect.width);
		if( pEmpty ) {
			if( cEmpty ) return;
			x   = Math.max( 0, vpPositionRect.x );
			x2  = Math.min( vpRecentRect.width, vpPositionRect.x + vpPositionRect.width );
			vpUpdateRect.setBounds( x, vpPositionRect.y, x2 - x, vpPositionRect.height );
		} else {
			if( cEmpty ) {
				x   = Math.max( 0, vpUpdateRect.x );
				x2  = Math.min( vpRecentRect.width, vpUpdateRect.x + vpUpdateRect.width );
				vpUpdateRect.setBounds( x, vpUpdateRect.y, x2 - x, vpUpdateRect.height );
			} else {
				x   = Math.max( 0, Math.min( vpUpdateRect.x, vpPositionRect.x ));
				x2  = Math.min( vpRecentRect.width, Math.max( vpUpdateRect.x + vpUpdateRect.width,
															vpPositionRect.x + vpPositionRect.width ));
				vpUpdateRect.setBounds( x, vpUpdateRect.y, x2 - x, vpUpdateRect.height );
			}
		}
		if( !vpUpdateRect.isEmpty() ) {
			repaint( vpUpdateRect );
//ggTrackPanel.repaint( updateRect );
		}
//			if( !updateRect.isEmpty() ) paintImmediately( updateRect );
//			Graphics g = getGraphics();
//			if( g != null ) {
//				paintDirty( g, updateRect );
//				g.dispose();
//			}
	}

	private void updateSelectionAndRepaint()
	{
		final Rectangle r = new Rectangle( 0, 0, getWidth(), getHeight() );
	
		vpUpdateRect.setBounds( vpSelectionRect );
		recalcTransforms( r );
//			try {
//				doc.bird.waitShared( Session.DOOR_TIMETRNS | Session.DOOR_GRP );
			updateSelection();
//			}
//			finally {
//				doc.bird.releaseShared( Session.DOOR_TIMETRNS | Session.DOOR_GRP );
//			}
		if( vpUpdateRect.isEmpty() ) {
			vpUpdateRect.setBounds( vpSelectionRect );
		} else if( !vpSelectionRect.isEmpty() ) {
			vpUpdateRect = vpUpdateRect.union( vpSelectionRect );
		}
		vpUpdateRect = vpUpdateRect.intersection( new Rectangle( 0, 0, getWidth(), getHeight() ));
		if( !vpUpdateRect.isEmpty() ) {
			repaint( vpUpdateRect );
//ggTrackPanel.repaint( updateRect );
		}
//			if( !updateRect.isEmpty() ) {
//				Graphics g = getGraphics();
//				if( g != null ) {
//					paintDirty( g, updateRect );
//				}
//				g.dispose();
//			}
	}

	private void updateTransformsAndRepaint( boolean verticalSelection )
	{
		final Rectangle r = new Rectangle( 0, 0, getWidth(), getHeight() );

		vpUpdateRect = vpSelectionRect.union( vpPositionRect );
		recalcTransforms( r );
		if( verticalSelection ) updateSelection();
		vpUpdateRect = vpUpdateRect.union( vpPositionRect ).union( vpSelectionRect ).intersection( r );
		if( !vpUpdateRect.isEmpty() ) {
			repaint( vpUpdateRect );	// XXX ??
//ggTrackPanel.repaint( updateRect );
		}
	}

	private void updateSelection()
	{
//		Rectangle	r;
//		Track		t;
//		int			x, y;

		vpSelections.clear();
		vpSelectionColors.clear();
		if( !timelineSel.isEmpty() ) {
vpSelections.add( new Rectangle( 0, 0, getWidth(), getHeight() ));
vpSelectionColors.add( colrSelection );
//			x			= waveView.getX();
//			y			= waveView.getY();
//			vpSelections.add( timeAxis.getBounds() );
//			vpSelectionColors.add( colrSelection );
//			t			= doc.markerTrack;
//			vpSelections.add( markAxis.getBounds() );
//			vpSelectionColors.add( doc.selectedTracks.contains( t ) ? colrSelection : colrSelection2 );
//			for( int ch = 0; ch < waveView.getNumChannels(); ch++ ) {
//				r		= new Rectangle( waveView.rectForChannel( ch ));
//				r.translate( x, y );
//				t		= (Track) doc.audioTracks.get( ch );
//				vpSelections.add( r );
//				vpSelectionColors.add( doc.selectedTracks.contains( t ) ? colrSelection : colrSelection2 );
//			}
		}
	}
}
