/*
 *  ServerListener.java
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

/**
 *	Objects which wish to be informed about server status
 *	changes must implement this interface and register using
 *	<code>Server.addListener</code>.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.23, 02-Oct-05
 *
 *	@see	Server#addListener( ServerListener )
 */
public interface ServerListener
{
	/**
	 *	Called when the server status changed.
	 *
	 *	@param	e	the event describing what <strong>REALLY</strong> happened
	 *
	 *	@synchronization	this method is invoked
	 *						in the java VM event queue thread. it is
	 *						therefore safe to call swing methods, for example
	 */
	public void serverAction( ServerEvent e );
}