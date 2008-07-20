package de.sciss.timebased.net;

import java.awt.EventQueue;
import java.io.IOException;

import de.sciss.timebased.Stake;
import de.sciss.timebased.Trail;

public abstract class NetTrailEditor
extends NetEditor
implements Trail.Editor
{
	protected NetTrailEditor( Master master )
	{
		super( master, "/trail" );
	}
	
	public void editAdd( int editID, Stake... stakes )
	throws IOException
	{
		editPerform( editID, "add", stakes );
	}
	
	public void editRemove( int editID, Stake... stakes )
	throws IOException
	{
		editPerform( editID, "rem", stakes );
	}
	
	private void editPerform( int editID, String cmd, Stake... stakes )
	throws IOException
	{
		if( !EventQueue.isDispatchThread() ) throw new IllegalMonitorStateException();
		
		final Client c = getClient( editID );
		for( Stake s : stakes ) {
			final Object[] osc	= oscRepresentation( s );
			final Object[] args = new Object[ osc.length + 1 ];
			args[ 0 ] = cmd;
			System.arraycopy( osc, 0, args, 1, osc.length );
			c.add( args );
		}
	}

	protected abstract Object[] oscRepresentation( Stake s ) throws IOException;
}
