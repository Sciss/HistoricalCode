package de.sciss.timebased.bosque;

import de.sciss.timebased.Trail;
import de.sciss.timebased.session.Track;

public class BosqueTrack
extends Track
{
	private int			id;
	private final Trail	trail;

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
}
