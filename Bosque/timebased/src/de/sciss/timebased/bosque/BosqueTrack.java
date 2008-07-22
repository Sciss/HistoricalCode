package de.sciss.timebased;

import de.sciss.timebased.session.Track;

public class ForestTrack
extends Track
{
	private int			id;
	private final Trail	trail;

	public ForestTrack( Trail trail, int id )
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
