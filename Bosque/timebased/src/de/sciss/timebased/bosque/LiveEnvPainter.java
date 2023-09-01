package de.sciss.timebased.bosque;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;

import de.sciss.gui.ComponentHost;
import de.sciss.gui.TopPainter;
import de.sciss.io.Span;
import de.sciss.timebased.Trail;
import de.sciss.timebased.gui.TracksTable;
import de.sciss.timebased.session.Track;
import de.sciss.timebased.timeline.TimelineView;

public class LiveEnvPainter
implements TopPainter, Trail.Listener
{
	private final TracksTable 		tt;
	private final TimelineView		view;
	private final Map<Track,EnvRegionStake>	map		= new HashMap<Track,EnvRegionStake>();
	private final ComponentHost		host;
	private final Rectangle			bounds			= new Rectangle();
	
	private static final Color		colrLiveRect	= new Color( 0xFF, 0x00, 0x00, 0x7F );
	// for env renderer
	private Span					viewSpan;
	private Span					stakeSpan;
	private Trail					env;
	private final Line2D			envLine			= new Line2D.Double();
	private final Rectangle2D		envKnob			= new Rectangle2D.Double();
	private static final int		envKnobExtent	= 3;
	private static final int		hndlExtent		= 13; // pntHndlGradientPixels.length;
	private double					hscale;
	
	public LiveEnvPainter( TracksTable tt,
						   ComponentHost host,
						   TimelineView view )
	{
		this.tt		= tt;
		this.host	= host;
		this.view	= view;
		
		host.addTopPainter( this );
	}
	
	public void addTrack( Track track, EnvRegionStake ers )
	{
		map.put( track, ers );
		ers.getEnv().addListener( this );
		host.repaint();	// XXX could be a more efficient update rectangle
	}
	
	public void removeTrack( Track track )
	{
		final EnvRegionStake ers = map.remove( track );
		ers.getEnv().removeListener( this );
		host.repaint();	// XXX could be a more efficient update rectangle
	}
	
	public void paintOnTop( Graphics2D g2 )
	{
		if( map.isEmpty() ) return;
		final JComponent cmp = tt.getMainView();
		if( cmp == null ) return;
		
		final AffineTransform atOrig = g2.getTransform();
		
		viewSpan	= view.getSpan();
		hscale		= (double) cmp.getWidth() / viewSpan.getLength();
		
		for( int i = 0; i < tt.getNumTracks(); i++ ) {
			final Track t = tt.getTrack( i );
			final EnvRegionStake ers = map.get( t ); 
			if( ers == null ) continue;
			env = ers.getEnv();
			tt.getTrackBounds( t, bounds );
//			bounds.translate( cmp.getX(), cmp.getY() );
			stakeSpan = ers.getSpan();
			final int offx = (int) ((stakeSpan.start - viewSpan.start) * hscale + 0.5);
			g2.translate( bounds.x + cmp.getX() + offx, bounds.y + cmp.getY() );
			g2.setColor( colrLiveRect );
			g2.fillRect( 0, hndlExtent, bounds.width, bounds.height - hndlExtent );
			g2.setColor( Color.yellow );
			paintEnv( g2 );
			g2.setTransform( atOrig );
		}
	}

	private void paintEnv( Graphics2D g2 )
	{
		final Span	insideSpan	= viewSpan.intersection( stakeSpan ).shift( -stakeSpan.start );
		final int	numStakes	= env.getNumStakes();
		final int	envNegRad	= -envKnobExtent / 2;
		boolean		drawStart	= true;
		int idx = env.indexOf( insideSpan.start, true );
		if( idx < 0 ) idx = Math.max( 0, -(idx + 2) );
				
		for( ; idx < numStakes; idx++ ) {
			final EnvSegmentStake	segm		= (EnvSegmentStake) env.get( idx, true );
			final Span				segmSpan	= segm.getSpan();
			final int				vscale2		= bounds.height - hndlExtent;
			if( segmSpan.start >= insideSpan.stop ) return;
			
			// XXX shape
			envLine.setLine( hscale * segmSpan.start,
			                 vscale2 * (1f - segm.getStartLevel() ) + hndlExtent,
			                 hscale * segmSpan.stop,
			                 vscale2 * (1f - segm.getStopLevel() ) + hndlExtent );
			g2.draw( envLine );
			if( drawStart ) {
				envKnob.setRect( envLine.getX1() + envNegRad, envLine.getY1() + envNegRad, envKnobExtent, envKnobExtent );
				g2.fill( envKnob );
				drawStart = false;
			}
			envKnob.setRect( envLine.getX2() + envNegRad, envLine.getY2() + envNegRad, envKnobExtent, envKnobExtent );
			g2.fill( envKnob );
		}
	}

	// ----------- Trail.Listener interface -----------
	
	public void trailModified( Trail.Event e )
	{
		if( e.getAffectedSpan().touches( view.getSpan() )) {
			host.repaint(); // XXX could be a more efficient update rectangle
		}
	}

}