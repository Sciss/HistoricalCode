/*
 *  AbstractEditor.java
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
 *		19-Jul-08	created
 */

package de.sciss.timebased;

import java.awt.EventQueue;
import java.util.HashMap;
import java.util.Map;

import javax.swing.undo.UndoManager;

import de.sciss.common.BasicCompoundEdit;

public abstract class AbstractEditor
implements Editor
{
	private UndoManager					undoMgr		= null;
	private final Map<Integer,Client>	map			= new HashMap<Integer,Client>();
	private static int					uniqueID	= 0;
	
	protected AbstractEditor( UndoManager undoMgr )
	{
		this();
		setUndoManager( undoMgr );
	}
	
	protected AbstractEditor()
	{
		// nada
	}
	
	public void setUndoManager( UndoManager undoMgr )
	{
		this.undoMgr = undoMgr;
	}
	
	protected UndoManager getUndoManager()
	{
		return undoMgr;
	}
	
	public int editBegin( Object source, String name )
	{
		if( !EventQueue.isDispatchThread() ) throw new IllegalMonitorStateException();
		
		final Client	c	= new Client( source, name );
		final int		id	= uniqueID++;
		map.put( id, c );
		return id;
	}
	
	public void editEnd( int id )
	{
		if( !EventQueue.isDispatchThread() ) throw new IllegalMonitorStateException();
		
		final Client c = map.remove( id );
		if( c == null ) throw new IllegalStateException( String.valueOf( id ));
		c.edit.perform();
		c.edit.end();
		if( undoMgr != null ) undoMgr.addEdit( c.edit );
	}
	
	public void editCancel( int id )
	{
		if( !EventQueue.isDispatchThread() ) throw new IllegalMonitorStateException();
		
		final Client c = map.remove( id );
		if( c == null ) throw new IllegalStateException( String.valueOf( id ));
		c.edit.cancel();
	}
	
	protected Client getClient( int id )
	{
		final Client c = map.get( id );
		if( c == null ) throw new IllegalStateException( String.valueOf( id ));
		
		return c;
	}
	
	protected static class Client
	{
		public final Object				source;
		public final BasicCompoundEdit	edit;
		
		protected Client( Object source, String name )
		{
			this.source	= source;
			edit		= new BasicCompoundEdit( name );
		}
	}
}