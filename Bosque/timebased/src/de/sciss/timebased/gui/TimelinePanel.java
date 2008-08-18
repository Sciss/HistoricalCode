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
import javax.swing.JComponent;
import javax.swing.Timer;

import de.sciss.gui.ComponentHost;
import de.sciss.gui.TopPainter;
import de.sciss.io.Span;
import de.sciss.timebased.Trail;
import de.sciss.timebased.session.MutableSessionCollection;
import de.sciss.timebased.session.SessionCollection;
import de.sciss.timebased.session.Track;
import de.sciss.timebased.timeline.BasicTimeline;
import de.sciss.timebased.timeline.BasicTimelineView;
import de.sciss.timebased.timeline.Timeline;
import de.sciss.timebased.timeline.TimelineView;

/**
 *  @author		Hanns Holger Rutz
 *  @version	0.13, 18-Aug-08
 */
public class TimelinePanel
extends ComponentHost
implements TopPainter, TimelineView.Listener
{
	private final Color 						colrSelection			= new Color( 0x00, 0x00, 0xFF, 0x2F ); // GraphicsUtil.colrSelection;
	private final Color 						colrPosition			= new Color( 0xFF, 0x00, 0x00, 0x7F );
	private final Color							colrSelection2			= new Color( 0x00, 0x00, 0x00, 0x20 );  // selected timeline span over unselected trns

	private Rectangle  							vpRecentRect			= new Rectangle();
	private int									vpPosition				= -1;
	private Rectangle  							vpPositionRect			= new Rectangle();
	private final ArrayList						vpSelections			= new ArrayList();
	private final ArrayList						vpSelectionColors		= new ArrayList();
	private Rectangle							vpSelectionRect			= new Rectangle();
	
	private Rectangle   						vpUpdateRect			= new Rectangle();
//	private Rectangle   						vpZoomRect				= null;
//	private float[]								vpDash					= { 3.0f, 5.0f };
	private float								vpScale;
	
	private Span								timelineVis;
	private Span								timelineSel;
	protected long								timelinePos;
//	private Span								timelineSpan;
	protected double							timelineRate;

	private final Timer							playTimer;
	
	// !!! for some crazy reason, these need to be volatile because otherwise
	// the playTimer's actionPerformed body might use a cached value !!!
	// how can this happen when javax.swing.Timer is playing on the event thread?!
	protected double							playRate				= 1.0;
	protected long								playStartPos			= 0;
	protected long								playStartTime;
	protected boolean							isPlaying				= false;

	private final TimelineAxis					timeAxis;
	private final MarkerAxis					markAxis;
//	private Track								markerTrack				= null;
	private Trail								markerTrail				= null;
	private final Trail.Listener				markerListener;

	private final TimelineView					tlv;
	
	protected boolean							markVisible;
	
	private SessionCollection					activeTracks			= null;
	private MutableSessionCollection			selectedTracks			= null;
	private final SessionCollection.Listener	activeTracksListener;
	private final SessionCollection.Listener	selectedTracksListener;
	
	private TracksTable							tracksTable;

	public TimelinePanel()
	{
		this( new BasicTimeline() );
	}
	
	public TimelinePanel( Timeline tl )
	{
		this( new BasicTimelineView( tl ));
	}

	public TimelinePanel( TimelineView tlv )
	{
		super();
		
		this.tlv = tlv;

		timelineVis		= tlv.getSpan();
		timelineSel		= tlv.getSelection().getSpan();
		timelinePos		= tlv.getCursor().getPosition();
//		timelineSpan	= tlv.getTimeline().getSpan();
		timelineRate	= tlv.getTimeline().getRate();
		markVisible		= true;

		addTopPainter( this );
		setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ));
		
		timeAxis			= new TimelineAxis( tlv, this );
		markAxis			= new MarkerAxis( tlv, this );

		add( timeAxis );
		add( markAxis );
//		wavePanel.add( waveView );

		playTimer = new Timer( 33, new ActionListener() {
			public void actionPerformed( ActionEvent e )
			{
				// the swing timer doesn't have a cancel method,
				// hence events already scheduled will be delivered
				// even if stop is called between firing and delivery(?)
				if( !isPlaying ) return;
				
				timelinePos = (long) ((System.currentTimeMillis() - playStartTime) * timelineRate * playRate / 1000 + playStartPos);
				updatePositionAndRepaint();
			}
		});
		
		// ---------- Listeners ----------
		activeTracksListener = new SessionCollection.Listener() {
			public void sessionCollectionChanged( SessionCollection.Event e )
			{
//				documentUpdate();
				updateSelectionAndRepaint();
//				final List coll = e.getCollection();
				switch( e.getModificationType() ) {
				case SessionCollection.Event.ACTION_ADDED:
//					for( int i = 0; i < coll.size(); i++ ) {
//						((Transmitter) coll.get( i )).getAudioTrail().addListener( waveTrailListener );
//					}
					break;
					
				case SessionCollection.Event.ACTION_REMOVED:
//					for( int i = 0; i < coll.size(); i++ ) {
//						((Transmitter) coll.get( i )).getAudioTrail().removeListener( waveTrailListener );
//					}
					break;
				
				default:
					break;
				}
			}

			public void sessionObjectMapChanged( SessionCollection.Event e ) { /* ignored */ }

			public void sessionObjectChanged( SessionCollection.Event e )
			{
				// nothing
			}
		};
		
		selectedTracksListener = new SessionCollection.Listener() {
			public void sessionCollectionChanged( SessionCollection.Event e )
			{
				updateSelectionAndRepaint();
			}

			public void sessionObjectMapChanged( SessionCollection.Event e ) { /* ignore */ }
			public void sessionObjectChanged( SessionCollection.Event e ) { /* ignore */ }
		};

		markerListener = new Trail.Listener() {
			public void trailModified( Trail.Event e )
			{
//				System.out.println( "HUHUHUHU " + e.getAffectedSpan() );
				repaintMarkers( e.getAffectedSpan() );
			}
		};
		tlv.addListener( this );
		markAxis.addListener( new MarkerAxis.Listener() {
			public void markerDragStarted( MarkerAxis.Event e )
			{
				addCatchBypass();
			}
			
			public void markerDragStopped( MarkerAxis.Event e )
			{
				removeCatchBypass();
			}

			public void markerDragAdjusted( MarkerAxis.Event e )
			{
				repaintMarkers( e.getModificatioSpan() );
			}
		});
	}
	
	public void setTracksTable( TracksTable tt )
	{
		this.tracksTable = tt;
	}
	
	public void setTracks( SessionCollection activeTracks, MutableSessionCollection selectedTracks )
	{
		if( this.activeTracks != null ) {
			this.activeTracks.removeListener( activeTracksListener );
			this.selectedTracks.removeListener( selectedTracksListener );
		}
		this.activeTracks	= activeTracks;
		this.selectedTracks	= selectedTracks;
		if( this.activeTracks != null ) {
			this.activeTracks.addListener( activeTracksListener );
			this.selectedTracks.addListener( selectedTracksListener );
		}
	}

	public TimelineView getTimelineView() { return tlv; }
	
	public void setMarkerTrack( Track t )
	{
		final Trail mt = (t != null) ? t.getTrail() : null;
//		markerTrack	= t;
		if( markerTrail != null ) {
			markerTrail.removeListener( markerListener );
		}
		markerTrail = mt;
		if( markerTrail != null ) {
			markerTrail.addListener( markerListener );
		}
		markAxis.setTrail( markerTrail );
	}

	public void addCatchBypass() { /* scroll.addCatchBypass(); XXX*/ }
	public void removeCatchBypass() { /* scroll.removeCatchBypass(); XXX*/ }

//	// sync: attempts exclusive on MTE and shared on TIME!
//	protected void updateOverviews( boolean allTracks )
//	{
////		waveView.update( timelineVis );
//		if( allTracks ) updateAll();
//	}

	protected void repaintMarkers( Span affectedSpan )
	{
		if( !markVisible || !affectedSpan.touches( timelineVis )) return;
	
		final Span span	 = affectedSpan.shift( -timelineVis.start );
		final Rectangle updateRect = new Rectangle(
			(int) (span.start * vpScale), 0,
			(int) (span.getLength() * vpScale) + 2, getHeight() ).
				intersection( new Rectangle( 0, 0, getWidth(), getHeight() ));
		if( !updateRect.isEmpty() ) {
			// update markAxis in any case, even if it's invisible
			// coz otherwise the flag stakes are not updated!
//			update( markAxis );
			repaint( updateRect );
		}
	}

	public TimelineAxis getTimelineAxis()
	{
		return timeAxis;
	}

	public MarkerAxis getMarkerAxis()
	{
		return markAxis;
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
		
		if( markVisible ) {
			markAxis.paintFlagSticks( g2, vpRecentRect );
		}
		
		g2.setColor( colrPosition );
		g2.drawLine( vpPosition, 0, vpPosition, vpRecentRect.height );

//		if( vpZoomRect != null ) {
//			g2.setColor( colrZoom );
//			g2.setStroke( vpZoomStroke[ vpZoomStrokeIdx ]);
//			g2.drawRect( vpZoomRect.x, vpZoomRect.y, vpZoomRect.width, vpZoomRect.height );
//		}
	}

//	public void setVisibleSpan( long start, long stop )
//	{
//		setVisibleSpan( new Span( start, stop ));
//	}
//
//	public void setVisibleSpan( Span span )
//	{
//		timelineVis	= span;
//
////		updateOverviews( false, true );
//		updateTransformsAndRepaint( false );
//	}
//	
//	public void setSelectionSpan( long start, long stop )
//	{
//		setSelectionSpan( new Span( start, stop ));
//	}
//
//	public void setSelectionSpan( Span span )
//	{
////		final boolean	wasEmpty = timelineSel.isEmpty();
////		final boolean	isEmpty;
//	
//		timelineSel	= span;
//
//		updateSelectionAndRepaint();
////		isEmpty	= timelineSel.isEmpty();
////		if( wasEmpty != isEmpty ) {
////			updateEditEnabled( !isEmpty );
////		}
//    }
//	
//	public void setPosition( long pos )
//	{
//		timelinePos		= pos;
//		playStartPos	= pos;
//		playStartTime	= System.currentTimeMillis();
////System.out.println( "setPosition : " + timelinePos );
//		
//		updatePositionAndRepaint();
////		scroll.setPosition( timelinePos, 0, pointerTool.validDrag ?
////			TimelineScroll.TYPE_DRAG : TimelineScroll.TYPE_UNKNOWN );
//	}
//
//	public void setRate( double rate )
//	{
//		timelineRate				= rate;
////		timelineLen					= doc.timeline.getLength();
//		playTimer.setDelay( Math.min( (int) (1000 / (vpScale * timelineRate * playRate)), 33 ));
////		updateAFDGadget();
////		updateOverviews( false, true );
//	}
	
	public void play( long startPos, double rate )
	{
		playStartPos	= startPos; // timelinePos;
		//System.out.println( "play : " + playStartPos );
		playRate		= rate;
		playStartTime	= System.currentTimeMillis();
		playTimer.setDelay( Math.min( (int) (1000 / (vpScale * timelineRate * Math.abs( playRate ))), 33 ));
		isPlaying		= true;
		playTimer.restart();
	}
	
	public void setPlayRate( long startPos, double rate )
	{
		if( !isPlaying ) return;
		
		playStartPos	= startPos; // timelinePos;
		//System.out.println( "rate : " + playStartPos );
		playRate		= rate;
		playStartTime	= System.currentTimeMillis();
		playTimer.setDelay( Math.min( (int) (1000 / (vpScale * timelineRate * Math.abs( playRate ))), 33 ));
//		isPlaying		= true;
//		playTimer.restart();
	}
	
	public void stop()
	{
		isPlaying = false;
		playTimer.stop();
	}

	public void dispose()
	{
		tlv.removeListener( this );
		setMarkerTrack( null );
		this.stop();
		super.dispose();
	}

	private void recalcTransforms( Rectangle newRect )
	{
		int x, w;
		
		vpRecentRect = newRect; // getViewRect();
	
		if( !timelineVis.isEmpty() ) {
			vpScale			= (float) vpRecentRect.width / (float) Math.max( 1, timelineVis.getLength() ); // - 1;
			playTimer.setDelay( Math.min( (int) (1000 / (vpScale * timelineRate * Math.abs( playRate ))), 33 ));
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

//		recalcTransforms();
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

	/**
	 *  Only call in the Swing thread!
	 */
	protected void updateSelectionAndRepaint()
	{
		final Rectangle r = new Rectangle( 0, 0, getWidth(), getHeight() );
	
		vpUpdateRect.setBounds( vpSelectionRect );
		recalcTransforms( r );
		updateSelection();
		if( vpUpdateRect.isEmpty() ) {
			vpUpdateRect.setBounds( vpSelectionRect );
		} else if( !vpSelectionRect.isEmpty() ) {
			vpUpdateRect = vpUpdateRect.union( vpSelectionRect );
		}
		vpUpdateRect = vpUpdateRect.intersection( new Rectangle( 0, 0, getWidth(), getHeight() ));
		if( !vpUpdateRect.isEmpty() ) {
			repaint( vpUpdateRect );
		}
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

	// sync: caller must sync on timeline + grp + tc
	private void updateSelection()
	{
		final JComponent	tracksMainView;
		final int			x, y;
		Track				t;
		Rectangle			r;

		vpSelections.clear();
		vpSelectionColors.clear();
		
		if( (tracksTable == null) || timelineSel.isEmpty() ) return;
		tracksMainView	= tracksTable.getMainView();
		if( tracksMainView == null ) return;
		
		x			= tracksMainView.getX();
		y			= tracksMainView.getY();
		vpSelections.add( timeAxis.getBounds() );
		vpSelectionColors.add( colrSelection );
		if( selectedTracks == null ) return;
		
//		if( markerTrack != null ) {
//			vpSelections.add( markAxis.getBounds() );
//			vpSelectionColors.add( selectedTracks.contains( markerTrack ) ? colrSelection : colrSelection2 );
//		}

		for( int i = 0; i < activeTracks.size(); i++ ) {
			t	= (Track) activeTracks.get( i );
			r	= tracksTable.getTrackBounds( t, null );
			r.translate( x, y );
			vpSelections.add( r );
			vpSelectionColors.add( selectedTracks.contains( t ) ? colrSelection : colrSelection2 );
		}
	}

	// ---------------- TimelineListener interface ---------------- 

	public void timelineSelected( TimelineView.Event e )
    {
		final boolean	wasEmpty = timelineSel.isEmpty();
		final boolean	isEmpty;
	
		timelineSel	= e.getView().getSelection().getSpan();

		updateSelectionAndRepaint();
		isEmpty	= timelineSel.isEmpty();
		if( wasEmpty != isEmpty ) {
//			updateEditEnabled( !isEmpty );
		}
    }

	// warning : don't call doc.setAudioFileDescr, it will restore the old markers!
	public void timelineChanged( TimelineView.Event e )
    {
		final Timeline tl = e.getView().getTimeline();
		timelineRate				= tl.getRate();
//		timelineSpan				= tl.getSpan();
		playTimer.setDelay( Math.min( (int) (1000 / (vpScale * timelineRate * Math.abs( playRate ))), 33 ));
// EEE
//		updateAFDGadget();
//		updateOverviews( false, true );
    }

	public void timelinePositioned( TimelineView.Event e )
	{
		timelinePos = e.getView().getCursor().getPosition();
		
		updatePositionAndRepaint();
//		scroll.setPosition( timelinePos, 0, pointerTool.validDrag ?
//			TimelineScroll.TYPE_DRAG : TimelineScroll.TYPE_UNKNOWN );
	}

    public void timelineScrolled( TimelineView.Event e )
    {
    	timelineVis	= e.getView().getSpan();

//		updateOverviews( false, true );
		updateTransformsAndRepaint( false );
    }
}
