package de.sciss.timebased.gui;

import de.sciss.timebased.session.Track;

public interface TrackRowHeaderFactory
{
	public TrackRowHeader createRowHeader( Track t );
}
