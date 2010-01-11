/*
 *  NetTrail.java
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

package de.sciss.timebased.net;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.TreeNode;

import de.sciss.app.AbstractCompoundEdit;
import de.sciss.io.Span;
import de.sciss.timebased.Stake;
import de.sciss.timebased.Trail;

/**
 *	@author		Hanns Holger Rutz
 *	@version	0.10, 19-Jul-08
 */
public class NetTrail
implements Trail
{
	private final Master	master;
	private final Trail		backend;
	private int				id		= -1;
	
	public NetTrail( Master master, Trail backend )
	{
System.out.println( getClass().getName() + " : THIS CLASS SHOULD NOT BE USED ANYMORE!!" );		
		this.master		= master;
		this.backend	= backend;
	}
	
	public Master getMaster()
	{
		return master;
	}

	public void setID( int id )
	{
		this.id = id;
	}

	public void clear( Object source )
	{
		// XXX
		System.out.println( id );
	}
	
	public void editInsert( Object source, Span span, int touchMode, AbstractCompoundEdit ce )
	{
		if( ce != null ) throw new UnsupportedOperationException();
		// XXX
		System.out.println( id );
	}

	public void editRemove( Object source, Span span, int touchMode, AbstractCompoundEdit ce )
	{
		if( ce != null ) throw new UnsupportedOperationException();
		// XXX
		System.out.println( id );
	}
	
	public void editClear( Object source, Span span, int touchMode, AbstractCompoundEdit ce )
	{
		if( ce != null ) throw new UnsupportedOperationException();
		// XXX
		System.out.println( id );
	}
	
	public void editAddAll( Object source, List stakes, AbstractCompoundEdit ce )
	throws IOException
	{
		if( ce != null ) throw new UnsupportedOperationException();
		// XXX
		System.out.println( id );
	}
	
	public void editRemoveAll( Object source, List stakes, AbstractCompoundEdit ce )
	throws IOException
	{
		if( ce != null ) throw new UnsupportedOperationException();
		// XXX
		System.out.println( id );
	}
	
	public void dispose()
	{
		// nada
	}
	
	// ----------- these only forward to any of the above methods -----------
	
	public void insert( Object source, Span span )
	{
		editInsert( source, span, getDefaultTouchMode(), null );
	}
	
	public void insert( Object source, Span span, int touchMode )
	{
		editInsert( source, span, touchMode, null );
	}
	
	public void editInsert( Object source, Span span, AbstractCompoundEdit ce )
	{
		editInsert( source, span, getDefaultTouchMode(), ce );
	}

	public void remove( Object source, Span span )
	{
		editRemove( source, span, getDefaultTouchMode(), null );
	}
	
	public void remove( Object source, Span span, int touchMode )
	{
		editRemove( source, span, touchMode, null );
	}
	
	public void editRemove( Object source, Span span, AbstractCompoundEdit ce )
	{
		editRemove( source, span, getDefaultTouchMode(), ce );
	}
	
	public void clear( Object source, Span span )
	{
		editClear( source, span, getDefaultTouchMode(), null );
	}
	
	public void clear( Object source, Span span, int touchMode )
	{
		editClear( source, span, touchMode, null );
	}
	
	public void editClear( Object source, Span span, AbstractCompoundEdit ce )
	{
		editClear( source, span, getDefaultTouchMode(), ce );
	}
	
	public void add( Object source, Stake stake )
	throws IOException
	{
		editAddAll( source, Collections.singletonList( stake ), null );
	}
	
	public void editAdd( Object source, Stake stake, AbstractCompoundEdit ce )
	throws IOException
	{
		editAddAll( source, Collections.singletonList( stake ), ce );
	}
	
	public void addAll( Object source, List stakes )
	throws IOException
	{
		editAddAll( source, stakes, null );
	}
	
	public void remove( Object source, Stake stake )
	throws IOException
	{
		editRemoveAll( source, Collections.singletonList( stake ), null );
	}
	
	public void editRemove( Object source, Stake stake, AbstractCompoundEdit ce )
	throws IOException
	{
		editRemoveAll( source, Collections.singletonList( stake ), ce );
	}
	
	public void removeAll( Object source, List stakes ) throws IOException
	{
		editRemoveAll( source, stakes, null );
	}

	// ----------- these forward to the backend -----------

	public int getDefaultTouchMode()
	{
		return backend.getDefaultTouchMode();
	}
	
	public List getRange( Span span, boolean byStart )
	{
		return backend.getRange( span, byStart );
	}
	
	public List getCuttedRange( Span span, boolean byStart, int touchMode, long shiftVirtual )
	{
		return backend.getCuttedRange( span, byStart, touchMode, shiftVirtual );
	}
	
	public Trail getCuttedTrail( Span span, int touchMode, long shiftVirtual )
	{
		return backend.getCuttedTrail( span, touchMode, shiftVirtual );
	}
	
	public List getAll( boolean byStart )
	{
		return backend.getAll( byStart );
	}
	
	public Stake get( int idx, boolean byStart )
	{
		return backend.get( idx, byStart );
	}
	
	public int getNumStakes()
	{
		return backend.getNumStakes();
	}

	public Span getSpan()
	{
		return backend.getSpan();
	}
	
	public double getRate()
	{
		return backend.getRate();
	}
	
	public int indexOf( Stake stake, boolean byStart )
	{
		return backend.indexOf( stake, byStart );
	}
	
	public int indexOf( long pos, boolean byStart )
	{
		return backend.indexOf( pos, byStart );
	}
	
	public boolean contains( Stake stake )
	{
		return backend.contains( stake );
	}

	public boolean isEmpty()
	{
		return backend.isEmpty();
	}

	public void addListener( Trail.Listener l )
	{
		backend.addListener( l );
	}
	
	public void removeListener( Trail.Listener l )
	{
		backend.removeListener( l );
	}
	
	// ----------- these are tricky XXX -----------

	public void editBegin( AbstractCompoundEdit ce )
	{
		// XXX
	}
	
	public void editEnd( AbstractCompoundEdit ce )
	{
		// XXX
	}

	// ----------- TreeNode interface -----------
	
	public TreeNode getChildAt( int childIndex ) { return backend.getChildAt( childIndex );}
	public int getChildCount() { return backend.getChildCount(); }
	public TreeNode getParent() { return backend.getParent(); }
	public int getIndex( TreeNode node ) { return backend.getIndex( node );}
	public boolean getAllowsChildren() { return backend.getAllowsChildren(); }
	public boolean isLeaf() { return backend.isLeaf(); }
	public Enumeration children() { return backend.children(); }
}
