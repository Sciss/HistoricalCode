package de.sciss.timebased.bosque;

import de.sciss.timebased.Trail;
import de.sciss.timebased.session.Track;

public class BosqueTrack
extends Track
{
	public static final int	OWNER_LEVEL	= 0x0001;
	
	private int			id;
	private final Trail	trail;
	private String		levelString;
//	private Trail		liveTrail	= null;

	public BosqueTrack( Trail trail, int id )
	{
		this.trail	= trail;
		setID( id );
	}
	
	public void setID( int id )
	{
		this.id	= id;
		getMap().putValue( null, "oscID", id );
	}
	
	public int getID() { return id; }
	public Trail getTrail() { return trail; }
	
	public void setLevelString( /* Object source,*/ String text )
	{
		levelString = text;
		getMap().dispatchOwnerModification( this, OWNER_LEVEL, text );
	}
	
	public String getLevelString() { return levelString; }
	
//	public void setLiveTrail( Trail t ) { liveTrail = t; }
//	
//	public Trail getLiveTrail() { return liveTrail; }
}
