/*
 *  TrackPanel.java
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
 */

package de.sciss.timebased.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;

import de.sciss.gui.GUIUtil;
import de.sciss.gui.GradientPanel;
import de.sciss.gui.StretchedGridLayout;
import de.sciss.timebased.session.MutableSessionCollection;
import de.sciss.timebased.session.SessionCollection;
import de.sciss.timebased.session.Track;

/**
 *	@author		Hanns Holger Rutz
 * 	@version	0.11, 18-Aug-08	
 */
public class TrackPanel
extends JPanel
implements TracksTable
{
	private final TimelineScroll		scroll;
	private final JPanel				allHeaderPanel;
	private final JPanel				trackHeaderPanel;
    protected final TrackRowHeader		markTrackHeader;
    private TrackRowHeaderFactory		trhf				= new DefaultTrackRowHeaderFactory();
    
    private SessionCollection			activeTracks		= null;
    private MutableSessionCollection	selectedTracks		= null;
    private Track						markerTrack			= null;
    
    private final TimelinePanel			tlp;
	private final List<TrackRowHeader> collTrackHeaders		= new ArrayList<TrackRowHeader>();
	private final Map<Track,TrackRowHeader>	mapTrackHeaders	= new HashMap<Track,TrackRowHeader>();
	
	private final SessionCollection.Listener	activeTracksListener;
	
	private MutableSessionCollection.Editor tracksEditor	= null;
	private JComponent						mainView		= null;
	
//	private int							recentWidth			= -1;
//	private int							recentHeight			= -1;

	public TrackPanel( TimelinePanel tlp )
	{
		super( new BorderLayout() );
		
		this.tlp	= tlp;
		
		final Box			topBox;
		final GradientPanel	gp;
		final MarkerAxis	markAxis	= tlp.getMarkerAxis();
		
        scroll				= new TimelineScroll( tlp.getTimelineView() );
        allHeaderPanel		= new JPanel( new BorderLayout() );
        trackHeaderPanel	= new JPanel( new StretchedGridLayout( 0, 1, 1, 1 ));
		markTrackHeader		= new TrackRowHeader(); //  doc.markerTrack, doc.getTracks(), doc.getMutableSelectedTracks() );
		markTrackHeader.setPreferredSize( new Dimension( 63, markAxis.getPreferredSize().height ));	// XXX
		markTrackHeader.setMaximumSize( new Dimension( 128, markAxis.getMaximumSize().height ));		// XXX
		
		if( !markAxis.isVisible() ) markTrackHeader.setVisible( false );
//			markAxis.startListening();
//		} else {
//			markAxis.setVisible( false );
//			markAxisHeader.setVisible( false );
//		}
        topBox				= Box.createVerticalBox();
        gp					= GUIUtil.createGradientPanel();
        gp.setBottomBorder( true );
        gp.setLayout( null );
        gp.setPreferredSize( new Dimension( 0, tlp.getTimelineAxis().getPreferredSize().height ));
        topBox.add( gp );
        topBox.add( markTrackHeader );
        allHeaderPanel.add( topBox, BorderLayout.NORTH );
//      trackHeaderPanel.add( flagsPanel );
//    	trackHeaderPanel.add( metersPanel );
//     	trackHeaderPanel.add( rulersPanel );
        allHeaderPanel.add( trackHeaderPanel, BorderLayout.CENTER );
        
        add( tlp, BorderLayout.CENTER );
		add( allHeaderPanel, BorderLayout.WEST );
		add( scroll, BorderLayout.SOUTH );
		
		activeTracksListener = new SessionCollection.Listener() {
			public void sessionCollectionChanged( SessionCollection.Event e )
			{
				documentUpdate();
			}

			public void sessionObjectMapChanged( SessionCollection.Event e ) { /* ignored */ }

			public void sessionObjectChanged( SessionCollection.Event e )
			{
				// nothing
			}
		};
		
		tlp.setTracksTable( this );
	}
	
	public void setTracks( SessionCollection activeTracks, MutableSessionCollection selectedTracks )
	{
		if( this.activeTracks != null ) {
			this.activeTracks.removeListener( activeTracksListener );
		}
		this.activeTracks	= activeTracks;
		this.selectedTracks	= selectedTracks;
		checkSetMarkTrack();
		if( this.activeTracks != null ) {
			this.activeTracks.addListener( activeTracksListener );
		}
		tlp.setTracks( activeTracks, selectedTracks );
	}
	
	public void setTrackRowHeaderFactory( TrackRowHeaderFactory trhf )
	{
		this.trhf = trhf;
	}
	
	public void dispose()
	{
		setTracks( null, null );
	}
	
	private void checkSetMarkTrack()
	{
		if( (activeTracks != null) && (markerTrack != null) ) {
			markTrackHeader.setTrack( markerTrack, activeTracks, selectedTracks );
			// this should not be a special case XXX
			collTrackHeaders.add( markTrackHeader );
			mapTrackHeaders.put( markerTrack, markTrackHeader );
		} else {
			markTrackHeader.setTrack( null, null, null );
			collTrackHeaders.remove( markTrackHeader );
			mapTrackHeaders.remove( markerTrack );
		}
	}
	
	protected void documentUpdate()
	{
		int					oldNumWaveTracks, newNumWaveTracks;
//		final List			collChannelMeters;
//		PeakMeter[]			meters;
		TrackRowHeader		trackRowHead;
		Track				t;
//		Axis				chanRuler;
//		PeakMeter			chanMeter;
		boolean				revalidate	= false;

		newNumWaveTracks	= activeTracks.size(); // EEE doc.getDisplayDescr().channels;
		oldNumWaveTracks	= collTrackHeaders.size();
		
//		meters				= channelMeters;
//		collChannelMeters	= new ArrayList( meters.length );
//		for( int ch = 0; ch < meters.length; ch++ ) {
//			collChannelMeters.add( meters[ ch ]);
//		}
	
		// first kick out editors whose tracks have been removed
		for( int ch = 0; ch < oldNumWaveTracks; ch++ ) {
			trackRowHead	= collTrackHeaders.get( ch );
			t			= trackRowHead.getTrack();

			if( !activeTracks.contains( t )) {
				trackRowHead	= collTrackHeaders.remove( ch );
				mapTrackHeaders.remove( t );
//				chanMeter	= (PeakMeter) collChannelMeters.remove( ch );
//				chanRuler	= (Axis) collChannelRulers.remove( ch );
				oldNumWaveTracks--;
				// XXX : dispose trnsEdit (e.g. free vectors, remove listeners!!)
				trackHeaderPanel.remove( trackRowHead );
//				flagsPanel.remove( chanHead );
//				metersPanel.remove( chanMeter );
//				rulersPanel.remove( chanRuler );
				ch--;
				trackRowHead.dispose();
//				chanMeter.dispose();
//				chanRuler.dispose();
				revalidate = true;
			}
		}
		// next look for newly added transmitters and create editors for them

//System.out.println( "now oldNumWaveTracks = " + oldNumWaveTracks + "; collTrackHeaders.size = " + collTrackHeaders.size() + "; newNumWaveTracks = " + newNumWaveTracks );
		
// EEE
newLp:	for( int ch = 0; ch < newNumWaveTracks; ch++ ) {
		
			t = (Track) activeTracks.get( ch );
//System.out.println( "checking track " + t + " (" + t.getName() + ")" );
			for( int ch2 = 0; ch2 < oldNumWaveTracks; ch2++ ) {
				trackRowHead = collTrackHeaders.get( ch2 );
				if( trackRowHead.getTrack() == t ) continue newLp;
			}
//System.out.println( "...gonna add row header" );
			
//			chanHead = new TransmitterRowHeader( t, doc.getTracks(), doc.getMutableSelectedTracks(), doc.getUndoManager() );
//			trackRowHead = new TrackRowHeader();
			trackRowHead = trhf.createRowHeader( t );
			trackRowHead.setTrack( t, activeTracks, selectedTracks );
			trackRowHead.setEditor( tracksEditor );
			collTrackHeaders.add( trackRowHead );
			mapTrackHeaders.put( t, trackRowHead );
			trackHeaderPanel.add( trackRowHead, ch - 1 ); // XXX tricky! -1 because of marker track
			
			revalidate = true;

//			chanMeter = new PeakMeter();
//			collChannelMeters.add( chanMeter );
//			metersPanel.add( chanMeter, ch );

//			chanRuler = new Axis( Axis.VERTICAL, Axis.FIXEDBOUNDS );
//			collChannelRulers.add( chanRuler );
//			rulersPanel.add( chanRuler, ch );
		}

		if( revalidate ) {
//System.out.println( "revalidate" );
			revalidate();
			repaint();
		}
		updateOverviews( /* false, */ true );
	}
	
	public void setTracksEditor( MutableSessionCollection.Editor editor )
	{
		if( tracksEditor != editor ) {
			tracksEditor = editor;
			for( int i = 0; i < collTrackHeaders.size(); i++ ) {
				final TrackRowHeader trh = collTrackHeaders.get( i );
				trh.setEditor( tracksEditor );
			}
			markTrackHeader.setEditor( tracksEditor );
		}
	}

	// sync: attempts exclusive on MTE and shared on TIME!
	private void updateOverviews( boolean allTracks )
	{
//		waveView.update( timelineVis );
		if( allTracks ) tlp.updateAll();
	}

	public void setMarkerTrack( Track t )
	{
		markerTrack = t;
		checkSetMarkTrack();
		tlp.setMarkerTrack( markerTrack );
	}

	public void setMainView( JComponent view )
	{
		if( mainView != null ) {
			tlp.remove( mainView );
		}
		mainView = view;
		if( view != null ) {
			tlp.add( mainView );
		}
	}
	
	public TimelinePanel getTimelinePanel() { return tlp; }

// ------------------ TracksTable interface ------------------
	
	public JComponent getMainView() { return mainView; }
	
	public TrackRowHeader getRowHeader( Track t ) { return mapTrackHeaders.get( t );}
	
	public int getNumTracks() { return activeTracks.size(); }

	public Track getTrack( int i ) { return (Track) activeTracks.get( i );}
	
	public int indexOf( Track t ) { return activeTracks.indexOf( t );}
	
	public Rectangle getTrackBounds( Track t, Rectangle r )
	{
		if( r == null ) r = new Rectangle();
		
		TrackRowHeader trh = mapTrackHeaders.get( t );
		if( trh == null ) {
			documentUpdate();
			trh = mapTrackHeaders.get( t );
			if( trh == null ) throw new IllegalArgumentException( t.toString() );
		}
		
		trh.getBounds( r );
		r.x = 0;
		if( mainView != null ) {
			r.width = mainView.getWidth();
			if( t == markerTrack ) {	// XXX stupid special handling
				r.y -= mainView.getY();
			}
		} else {
			r.width = 0;
		}
		
//		r.x    += r.width - (mainView != null ? mainView.getX() : 0);
//		r.width = getWidth() - r.x;
//		r.y	   += trackHeaderPanel.getY();
		
		return r;
	}
}
