package de.sciss.timebased.bosque;

import java.awt.Color;
import java.awt.Font;

import de.sciss.timebased.gui.DefaultTrackRowHeaderFactory;
import de.sciss.timebased.gui.TrackRowHeader;
import de.sciss.timebased.session.Track;

public class BosqueTrackRowHeaderFactory
extends DefaultTrackRowHeaderFactory
{
	private Font	fntLevel	= null;
	private Color	colrLevel	= null;
	
	public TrackRowHeader createRowHeader( Track t )
	{
		if( t instanceof BosqueTrack ) {
			final BosqueTrackRowHeader btrh = new BosqueTrackRowHeader();
			if( fntLevel != null ) btrh.setLevelFont( fntLevel );
			if( colrLevel != null ) btrh.setLevelColor(  colrLevel );
			return btrh;
		}
		return super.createRowHeader( t );
	}

	public void setLevelFont( Font fnt )
	{
		fntLevel = fnt;
	}
	
	public void setLevelColor( Color c )
	{
		colrLevel	= c;
	}
}
