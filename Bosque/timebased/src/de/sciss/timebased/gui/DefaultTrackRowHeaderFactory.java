package de.sciss.timebased.gui;

import de.sciss.timebased.session.Track;

public class DefaultTrackRowHeaderFactory
implements TrackRowHeaderFactory
{
	public DefaultTrackRowHeaderFactory() { /* nada */ }
	
	public TrackRowHeader createRowHeader( Track t ) { return new TrackRowHeader(); }
}
