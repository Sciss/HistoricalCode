package de.sciss.timebased.net;

import java.awt.EventQueue;

import de.sciss.timebased.session.MutableSessionCollection;
import de.sciss.timebased.session.SessionObject;

public class NetSessionCollectionEditor
extends NetEditor
implements MutableSessionCollection.Editor
{
	public NetSessionCollectionEditor( Master master )
	{
		super( master, "/coll" );
	}
	
	public void editAdd( int editID, SessionObject... objects )
	{
		editPerform( editID, "add", objects );
	}
	
	public void editRemove( int editID, SessionObject... objects )
	{
		editPerform( editID, "rem", objects );
	}

	private void editPerform( int editID, String cmd, SessionObject... objects )
	{
		if( !EventQueue.isDispatchThread() ) throw new IllegalMonitorStateException();
		
		final Client c = getClient( editID );
		for( int i = 0; i < objects.length; ) {
			// if an ID is a 32-bit integer, approx. 13000 should
			// fit into a 64K OSC-message
			final int num = Math.min( 10000, objects.length - i );
			final Object[] args = new Object[ num + 1 ];
			args[ 0 ] = cmd;
			for( int j = 1, k = i; j < args.length; j++, k++ ) {
				args[ j ] = objects[ k ].getMap().getValue( "oscID" );
			}
			c.add( args );
			i += num;
		}
	}
}
