/*
 *  ServerEvent.java
 *  (JCollider)
 *
 *  Copyright (c) 2004-2015 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.jcollider;

import de.sciss.app.BasicEvent;

/**
 *	These kind of events get delivered by a 
 *	server represenation to inform listeners about
 *	server status changes
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.33, 19-Mar-08
 */
public class ServerEvent
extends BasicEvent
{
// --- ID values ---
	/**
	 *  returned by getID() : the server started running
	 */
	public static final int RUNNING		= 0;

	/**
	 *  returned by getID() : the server stopped running
	 */
	public static final int STOPPED		= 1;

	/**
	 *  returned by getID() : the server status has been updated
	 */
	public static final int COUNTS		= 2;

	private final Server	server;

	/**
	 *	@param	source	who fired the event
	 *	@param	ID		the type of status change, e.g. <code>RUNNING</code>
	 *	@param	when	timestamp of the event (e.g. <code>System.currentTimeMillis()</code>)
	 *	@param	server	the representation of the server whose status changed
	 */
	protected ServerEvent( Object source, int ID, long when, Server server )
	{
		super( source, ID, when );
	
		this.server		= server;
	}
	
	/**
	 *	@return	the representation of the server whose status changed
	 */
	public Server getServer()
	{
		return server;
	}

	/**
	 *	Used by the <code>EventManager</code> to
	 *	fuse successive events together when they queue.
	 *	Do not call this method.
	 */
	public boolean incorporate( BasicEvent oldEvent )
	{
		if( (oldEvent instanceof ServerEvent) &&
			(this.getSource() == oldEvent.getSource()) &&
			(this.getID() == oldEvent.getID()) ) {
			
			// XXX beware, when the actionID and actionObj
			// are used, we have to deal with them here
			
			return true;

		} else return false;
	}
}