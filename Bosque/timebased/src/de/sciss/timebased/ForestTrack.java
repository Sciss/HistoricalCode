package de.sciss.timebased;

import de.sciss.timebased.session.Track;

public class ForestTrack
extends Track
{
	private final int	id;
	private final Trail	trail;

	public ForestTrack( int id, Trail trail )
	{
		this.id 	= id;
		this.trail	= trail;
	}
	
	public int getID() { return id; }
	public Trail getTrail() { return trail; }
}
