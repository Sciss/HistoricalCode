package de.sciss.timebased.bosque;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import de.sciss.timebased.gui.TrackRowHeader;
import de.sciss.util.MapManager;

public class BosqueTrackRowHeader
extends TrackRowHeader
{
	private final Font fnt = new Font( "Courier", Font.PLAIN, 8 );
	
	public BosqueTrackRowHeader()
	{
		super();
	}

	protected void trackChanged( MapManager.Event e )
	{
		if( e.getOwnerModType() == BosqueTrack.OWNER_LEVEL ) repaint();
	}
	
	public void paintComponent( Graphics g )
	{
		super.paintComponent( g );
		
		if( t == null ) return;
		final String lvlStr = ((BosqueTrack) t).getLevelString();
		if( lvlStr == null ) return;
		g.setColor( Color.black );
		g.setFont( fnt );
		g.drawString( lvlStr, 8, getHeight() - 20 );
	}
}
