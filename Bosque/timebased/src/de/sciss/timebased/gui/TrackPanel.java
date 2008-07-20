package de.sciss.timebased.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JPanel;

import de.sciss.gui.GUIUtil;
import de.sciss.gui.GradientPanel;
import de.sciss.gui.StretchedGridLayout;
import de.sciss.timebased.session.MutableSessionCollection;
import de.sciss.timebased.session.SessionCollection;
import de.sciss.timebased.session.Track;

public class TrackPanel
extends JPanel
{
	private final TimelineScroll		scroll;
	private final JPanel				allHeaderPanel;
	private final JPanel				trackHeaderPanel;
    protected final TrackRowHeader		markTrackHeader;
    
    private SessionCollection			activeTracks		= null;
    private MutableSessionCollection	selectedTracks		= null;
    private Track						markerTrack			= null;
    
    private final TimelinePanel tlp;
	private final List collTrackHeaders		= new ArrayList();
	
	private final SessionCollection.Listener	activeTracksListener;
	
	private MutableSessionCollection.Editor tracksEditor	= null;

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
	
	public void dispose()
	{
		setTracks( null, null );
	}
	
	private void checkSetMarkTrack()
	{
		if( (activeTracks != null) && (markerTrack != null) ) {
			markTrackHeader.setTrack( markerTrack, activeTracks, selectedTracks );
		} else {
			markTrackHeader.setTrack( null, null, null );
		}
	}
	
	protected void documentUpdate()
	{
		int					oldNumWaveTracks, newNumWaveTracks;
//		final List			collChannelMeters;
//		PeakMeter[]			meters;
		TrackRowHeader		chanHead;
		Track				t;
//		Axis				chanRuler;
//		PeakMeter			chanMeter;

		newNumWaveTracks	= activeTracks.size(); // EEE doc.getDisplayDescr().channels;
		oldNumWaveTracks	= collTrackHeaders.size();
		
//		meters				= channelMeters;
//		collChannelMeters	= new ArrayList( meters.length );
//		for( int ch = 0; ch < meters.length; ch++ ) {
//			collChannelMeters.add( meters[ ch ]);
//		}
	
		// first kick out editors whose tracks have been removed
		for( int ch = 0; ch < oldNumWaveTracks; ch++ ) {
			chanHead	= (TrackRowHeader) collTrackHeaders.get( ch );
			t			= chanHead.getTrack();

			if( !activeTracks.contains( t )) {
//System.out.println( "removing " + t );
				chanHead	= (TrackRowHeader) collTrackHeaders.remove( ch );
//				chanMeter	= (PeakMeter) collChannelMeters.remove( ch );
//				chanRuler	= (Axis) collChannelRulers.remove( ch );
				oldNumWaveTracks--;
				// XXX : dispose trnsEdit (e.g. free vectors, remove listeners!!)
				trackHeaderPanel.remove( chanHead );
//				flagsPanel.remove( chanHead );
//				metersPanel.remove( chanMeter );
//				rulersPanel.remove( chanRuler );
				ch--;
				chanHead.dispose();
//				chanMeter.dispose();
//				chanRuler.dispose();
			}
		}
		// next look for newly added transmitters and create editors for them

//		System.out.println( "now oldNumWaveTracks = " + oldNumWaveTracks + "; collChannelHeaders.size = " + collChannelHeaders.size() );
		
// EEE
newLp:	for( int ch = 0; ch < newNumWaveTracks; ch++ ) {
			t = (Track) activeTracks.get( ch );
			for( int ch2 = 0; ch2 < oldNumWaveTracks; ch2++ ) {
				chanHead = (TrackRowHeader) collTrackHeaders.get( ch2 );
				if( chanHead.getTrack() == t ) continue newLp;
			}
			
//			chanHead = new TransmitterRowHeader( t, doc.getTracks(), doc.getMutableSelectedTracks(), doc.getUndoManager() );
			chanHead = new TrackRowHeader();
			chanHead.setTrack( t, activeTracks, selectedTracks );
			chanHead.setEditor( tracksEditor );
			collTrackHeaders.add( chanHead );
			trackHeaderPanel.add( chanHead, ch );

//			chanMeter = new PeakMeter();
//			collChannelMeters.add( chanMeter );
//			metersPanel.add( chanMeter, ch );

//			chanRuler = new Axis( Axis.VERTICAL, Axis.FIXEDBOUNDS );
//			collChannelRulers.add( chanRuler );
//			rulersPanel.add( chanRuler, ch );
		}

		updateOverviews( /* false, */ true );
	}
	
	public void setTracksEditor( MutableSessionCollection.Editor editor )
	{
		if( tracksEditor != editor ) {
			tracksEditor = editor;
			for( int i = 0; i < collTrackHeaders.size(); i++ ) {
				final TrackRowHeader trh = (TrackRowHeader) collTrackHeaders.get( i );
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
		tlp.setMarkerTrail( (markerTrack != null) ? markerTrack.getTrail() : null );
	}
}
