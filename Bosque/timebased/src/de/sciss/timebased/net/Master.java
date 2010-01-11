/*
 *  Master.java
 *  TimeBased
 *
 *  Copyright (c) 2004-2010 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation; either
 *	version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	This software is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public
 *	License (gpl.txt) along with this software; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 *
 *
 *  Changelog:
 *		18-Jul-08	created
 */

package de.sciss.timebased.net;

import java.io.IOException;

import de.sciss.net.OSCMessage;
import de.sciss.net.OSCPacket;
import de.sciss.swingosc.SwingClient;
import de.sciss.swingosc.SwingOSC;

public class Master
{
	private final SwingClient client;
	
	public Master()
	{
		this( SwingOSC.getInstance().getCurrentClient() );
	}
	
	public Master( SwingClient client )
	{
		this.client	= client;
	}
	
	public SwingClient getClient() { return client; }
	
	public void reply( OSCPacket p )
	{
		try {
			client.reply( p );
		}
		catch( IOException e1 ) {
			e1.printStackTrace();
		}
	}
	
	public void reply( String cmd, Object... args )
	{
		reply( new OSCMessage( cmd, args ));
	}
}
