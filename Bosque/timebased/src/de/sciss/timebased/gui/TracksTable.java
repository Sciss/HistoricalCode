package de.sciss.timebased.gui;

import java.awt.Rectangle;

import javax.swing.JComponent;

import de.sciss.timebased.session.Track;

public interface TracksTable
{
	public JComponent getMainView();
	public TrackRowHeader getRowHeader( Track t );
	public Rectangle getTrackBounds( Track t, Rectangle r );
	public int getNumTracks();
	public Track getTrack( int i );
	public int indexOf( Track t );
}
