package de.sciss.timebased.net;

import java.io.IOException;

import de.sciss.io.Marker;
import de.sciss.timebased.Stake;

public class NetMarkerTrailEditor
extends NetTrailEditor
{
	public NetMarkerTrailEditor( Master master )
	{
		super( master );
	}
	
	protected Object[] oscRepresentation( Stake s )
	throws IOException
	{
		if( !(s instanceof Marker) ) throw new IOException( s.toString() );
		
		final Marker m = (Marker) s;
		return new Object[] { m.pos, m.name };
	}
}
