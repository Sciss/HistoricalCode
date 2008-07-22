package de.sciss.timebased.bosque;

import de.sciss.timebased.gui.DefaultTrackRowHeaderFactory;
import de.sciss.timebased.gui.TrackRowHeader;
import de.sciss.timebased.session.Track;

public class BosqueTrackRowHeaderFactory
extends DefaultTrackRowHeaderFactory
{
	public TrackRowHeader createRowHeader( Track t )
	{
		if( t instanceof BosqueTrack ) return new BosqueTrackRowHeader();
		return super.createRowHeader( t );
	}
}
