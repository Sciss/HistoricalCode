/*
 *  MutableSessionCollection.java
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
 *		18-Jul-08	copied from Cillo
 */

package de.sciss.timebased.session;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.sciss.app.BasicUndoableEdit;
import de.sciss.app.PerformableEdit;

public interface MutableSessionCollection
extends SessionCollection
{
	public boolean addAll( Object source, List c );
	public boolean removeAll( Object source, List c );
	public void clear( Object source );
	
	// -------------------------- Editor interface --------------------------
	
	public interface Editor
	extends de.sciss.timebased.Editor
	{
		public void editAdd( int id, SessionObject... objects );
		public void editRemove( int id, SessionObject... objects );
	}
	
	// -------------------------- Edit class --------------------------
	
	public class Edit
	extends BasicUndoableEdit
	{
		private final String					name;
		private final List<SessionObject>		collToAdd;
		private final List<SessionObject>		collToRemove;
		private Object							source;
		private final MutableSessionCollection	quoi;

		/**
		 *  Create and perform this edit. This
		 *  invokes the <code>SessionObjectCollection.addAll</code> method,
		 *  thus dispatching a <code>SessionCollection.Event</code>.
		 *
		 *  @param  source			who initiated the action
		 *  @param  doc				session object to which the
		 *							receivers are added
		 *  @param  collToAdd   collection of receivers to
		 *							add to the session.
		 *  @see	de.sciss.meloncillo.session.SessionCollection
		 *  @see	de.sciss.meloncillo.session.SessionCollection.Event
		 *  @synchronization		waitExclusive on doors
		 */
		private Edit( String name, Object source, MutableSessionCollection quoi,
					  List<SessionObject> collToAdd, List<SessionObject> collToRemove )
		{
			super();
			this.name				= name;
			this.source				= source;
			this.quoi				= quoi;
			this.collToAdd			= collToAdd;
			this.collToRemove		= collToRemove;
//			perform();
//			this.source				= this;
		}

		public static Edit add( Object source, MutableSessionCollection msc,
								SessionObject... collToAdd )
		{
			final Edit e = new Edit( "editAddSessionObjects",
			                         source, msc, Arrays.asList( collToAdd ),
			                         Collections.EMPTY_LIST );
			return e;
		}
		
		public static Edit remove( Object source, MutableSessionCollection msc,
								   SessionObject... collToRemove )
		{
			final Edit e = new Edit( "editRemoveSessionObjects",
			                         source, msc, Collections.EMPTY_LIST,
			                         Arrays.asList( collToRemove ));
			return e;
		}

		public PerformableEdit perform()
		{
			if( !collToRemove.isEmpty() ) {
				quoi.removeAll( source, collToAdd );
			}
			if( !collToAdd.isEmpty() ) {
				quoi.addAll( source, collToAdd );
			}
			this.source = this;
			return this;
		}

		/**
		 *  Undo the edit
		 *  by calling the <code>SessionObjectCollection.removeAll</code>,
		 *  method, thus dispatching a <code>SessionCollection.Event</code>.
		 *
		 *  @synchronization	waitExlusive on doors.
		 */
		public void undo()
		{
			super.undo();
			if( !collToAdd.isEmpty() ) {
				quoi.removeAll( source, collToAdd );
			}
			if( !collToRemove.isEmpty() ) {
				quoi.addAll( source, collToAdd );
			}
		}
		
		/**
		 *  Redo the add operation.
		 *  The original source is discarded
		 *  which means, that, since a new <code>SessionCollection.Event</code>
		 *  is dispatched, even the original object
		 *  causing the edit will not know the details
		 *  of the action, hence thoroughly look
		 *  and adapt itself to the new edit.
		 *
		 *  @synchronization	waitExlusive on doors.
		 */
		public void redo()
		{
			super.redo();
			perform();
		}

		public String getPresentationName()
		{
//			return name; // return getResourceString( "editAddSessionObjects" );
			return getResourceString( name );
		}
	}
}
