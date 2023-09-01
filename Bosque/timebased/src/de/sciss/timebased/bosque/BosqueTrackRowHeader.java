package de.sciss.timebased.bosque;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import de.sciss.timebased.gui.TrackRowHeader;
import de.sciss.util.MapManager;

public class BosqueTrackRowHeader
extends TrackRowHeader
{
	private Font	fntLevel	= new Font( "Courier", Font.PLAIN, 9 );
	private Color	colrLevel	= Color.black;
	
	public BosqueTrackRowHeader()
	{
		super();
	}
	
	public void setLevelFont( Font fnt )
	{
		fntLevel = fnt;
		repaint();
	}
	
	public void setLevelColor( Color c )
	{
		colrLevel	= c;
		repaint();
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
	
		g.setFont( fntLevel );
		final FontMetrics fm = g.getFontMetrics();
		
		g.setColor( colrLevel );
//System.out.println( "g.drawString( " + lvlStr + ", " + (getWidth() - 8 - fm.stringWidth( lvlStr )) +
//                    ", " + (getHeight() - 16 - fm.getDescent()) + " )" );
		g.drawString( lvlStr, getWidth() - 8 - fm.stringWidth( lvlStr ),
		              getHeight() - 16 - fm.getDescent() );
	}
}
