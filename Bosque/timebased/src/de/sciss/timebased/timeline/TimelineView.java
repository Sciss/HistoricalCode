/*
 *  TimelineView.java
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
 *		17-Jul-08	created
 */

package de.sciss.timebased.timeline;

import javax.swing.undo.UndoableEdit;

import de.sciss.app.BasicEvent;
import de.sciss.app.BasicUndoableEdit;
import de.sciss.app.PerformableEdit;
import de.sciss.io.Span;
import de.sciss.util.Disposable;

public interface TimelineView
extends Disposable
{
	public Timeline getTimeline();
	public TimelineCursor getCursor();
	public TimelineSelection getSelection();
	
	/**
	 *	@return	the view span, that is the visible span inside the whole timeline's span.
	 */
	public Span getSpan();
	public void setSpan( Object source, Span span );

	public void addListener( Listener listener );
	public void removeListener( Listener listener );

	public interface Listener
	{
		/**
		 *  Notifies the listener that
		 *  the basic timeline properties were modified
		 *  (e.g. the length or rate changed).
		 *
		 *  @param  e   the event describing
		 *				the timeline modification
		 */
		public void timelinePositioned( Event e );
		/**
		 *  Notifies the listener that
		 *  the basic timeline properties were modified
		 *  (e.g. the length or rate changed).
		 *
		 *  @param  e   the event describing
		 *				the timeline modification
		 */
		public void timelineSelected( Event e );
		/**
		 *  Notifies the listener that
		 *  the basic timeline properties were modified
		 *  (e.g. the length or rate changed).
		 *
		 *  @param  e   the event describing
		 *				the timeline modification
		 */
		public void timelineChanged( Event e );
		/**
		 *  Notifies the listener that
		 *  a the view of the timeline frame was scrolled
		 *  to a new position (or zoomed).
		 *
		 *  @param  e   the event describing
		 *				the timeline scrolling
		 *				(<code>getActionObj</code> will
		 *				return the new visible span)
		 */
		public void timelineScrolled( Event e );
	}

	public static class Event
	extends BasicEvent
	{
	// --- ID values ---
		/**
		 *  returned by getID() : a portion of the timeline
		 *  has been selected or deselected
		 */
		public static final int SELECTED	= 0;
		/**
		 *  returned by getID() : the basic properties of
		 *  the timeline, rate or length, have been modified.
	     *  <code>actionObj</code> is a (potentially empty)
	     *  <code>Span</code> object
		 */
		public static final int CHANGED		= 1;
		/**
		 *  returned by getID() : the 'playback head' of
		 *  the timelime has been moved
		 */
		public static final int POSITIONED	= 2;
		/**
		 *  returned by getID() : the visible portion of
		 *  the timelime has been changed.
	     *  <code>actionObj</code> is a (potentially empty)
	     *  <code>Span</code> object
		 */
		public static final int SCROLLED	= 3;
		
		private final TimelineView view;

		/**
		 *  Constructs a new <code>TimelineEvent</code>
		 *
		 *  @param  source		who originated the action
		 *  @param  id			one of <code>CHANGED</code>
		 *  @param  when		system time when the event occured
		 */
		public Event( Object source, int id, long when, TimelineView view )
		{
			super( source, id, when );
			this.view = view;
		}
		
		public static Event convert( Timeline.Event e, TimelineView view )
		{
			if( e.getID() != Timeline.Event.CHANGED ) throw new IllegalArgumentException();
			return new Event( e.getSource(), Event.CHANGED, e.getWhen(), view );
		}
		
		public static Event convert( TimelineCursor.Event e, TimelineView view )
		{
			if( e.getID() != TimelineCursor.Event.CHANGED ) throw new IllegalArgumentException();
			return new Event( e.getSource(), Event.POSITIONED, e.getWhen(), view );
		}

		public static Event convert( TimelineSelection.Event e, TimelineView view )
		{
			if( e.getID() != TimelineCursor.Event.CHANGED ) throw new IllegalArgumentException();
			return new Event( e.getSource(), Event.SELECTED, e.getWhen(), view );
		}

		public TimelineView getView()
		{
			return view;
		}

		public boolean incorporate( BasicEvent oldEvent )
		{
			if( oldEvent instanceof Event &&
				this.getSource() == oldEvent.getSource() &&
				this.getID() == oldEvent.getID() &&
				this.view == ((Event) oldEvent).view ) {
				
				// XXX beware, when the actionID and actionObj
				// are used, we have to deal with them here
				
				return true;

			} else return false;
		}
	}
	
// --------------------- Editor interface ---------------------
	
	public interface Editor
	extends de.sciss.timebased.Editor
	{
		public void editPosition( int id, long newPos );
		public void editScroll( int id, Span newSpan );
		public void editSelect( int id, Span newSpan );
	}
	
	// --------------------------------------------------
	
	public static class Edit
	extends BasicUndoableEdit
	{
		private final TimelineView	view;
		
		private Object				source;
		private long				oldPos, newPos;
		private Span				oldVisi, newVisi, oldSel, newSel;

		private int					actionMask;
		
		private static final int	ACTION_POSITION	= 0x01;
		private static final int	ACTION_SCROLL	= 0x02;
		private static final int	ACTION_SELECT	= 0x04;

		/*
		 *  Create and perform the edit. This method
		 *  invokes the <code>Timeline.setSelectionSpan</code> method,
		 *  thus dispatching a <code>TimelineEvent</code>.
		 *
		 *  @param  source		who originated the edit. the source is
		 *						passed to the <code>Timeline.setSelectionSpan</code> method.
		 *  @param  doc			session into whose <code>Timeline</code> is
		 *						to be selected / deselected.
		 *  @param  span		the new timeline selection span.
		 *  @synchronization	waitExclusive on DOOR_TIME
		 */
		private Edit( Object source, TimelineView view )
		{
			super();
			this.source		= source;
			this.view		= view;
			actionMask		= 0;
		}
		
		public static Edit position( Object source, TimelineView view, long pos )
		{
			final Edit tve = new Edit( source, view );
			tve.actionMask	= ACTION_POSITION;
			
			tve.oldPos		= view.getCursor().getPosition();
			tve.newPos		= pos;
			return tve;
		}

		public static Edit scroll( Object source, TimelineView view, Span newVisi )
		{
			final Edit tve = new Edit( source, view );
			tve.actionMask	= ACTION_SCROLL;
			
			tve.oldVisi		= view.getSpan();
			tve.newVisi		= newVisi;
			return tve;
		}

		public static Edit select( Object source, TimelineView view, Span newSel )
		{
			final Edit tve = new Edit( source, view );
			tve.actionMask	= ACTION_SELECT;
			
			tve.oldSel		= view.getSelection().getSpan();
			tve.newSel		= newSel;
			return tve;
		}
		
		public PerformableEdit perform()
		{
			if( (actionMask & ACTION_POSITION) != 0 ) {
				view.getCursor().setPosition( source, newPos );
			}
			if( (actionMask & ACTION_SCROLL) != 0 ) {
				view.setSpan( source, newVisi );
			}
			if( (actionMask & ACTION_SELECT) != 0 ) {
				view.getSelection().setSpan( source, newSel );
			}
			source	= this;
			return this;
		}

		/**
		 *  @return		false to tell the UndoManager it should not feature
		 *				the edit as a single undoable step in the history.
		 *				which is especially important since <code>TimelineAxis</code>
		 *				will generate lots of edits when the user drags
		 *				the timeline selection.
		 */
		public boolean isSignificant()
		{
			return false;
		}

		/**
		 *  Undo the edit
		 *  by calling the <code>Timeline.setSelectionSpan</code>,
		 *  method, thus dispatching a <code>TimelineEvent</code>.
		 *
		 *  @synchronization	waitExlusive on DOOR_TIME.
		 */
		public void undo()
		{
			super.undo();
			if( (actionMask & ACTION_POSITION) != 0 ) {
				view.getCursor().setPosition( source, oldPos );
			}
			if( (actionMask & ACTION_SCROLL) != 0 ) {
				view.setSpan( source, oldVisi );
			}
			if( (actionMask & ACTION_SELECT) != 0 ) {
				view.getSelection().setSpan( source, oldSel );
			}
		}
		
		/**
		 *  Redo the edit. The original source is discarded
		 *  which means, that, since a new <code>TimelineEvent</code>
		 *  is dispatched, even the original object
		 *  causing the edit will not know the details
		 *  of the action, hence thoroughly look
		 *  and adapt itself to the new edit.
		 *
		 *  @synchronization	waitExlusive on DOOR_TIME.
		 */
		public void redo()
		{
			super.redo();
			perform();
		}
		
		/**
		 *  Collapse multiple successive EditSetReceiverBounds edit
		 *  into one single edit. The new edit is sucked off by
		 *  the old one.
		 */
		public boolean addEdit( UndoableEdit anEdit )
		{
			if( anEdit instanceof Edit ) {
				final Edit tve = (Edit) anEdit;
				if( view != tve.view ) return false;

				if( (tve.actionMask & ACTION_POSITION) != 0 ) {
					newPos		= tve.newPos;
					if( (actionMask & ACTION_POSITION) == 0 ) {
						oldPos = tve.oldPos;
					}
				}
				if( (tve.actionMask & ACTION_SCROLL) != 0 ) {
					newVisi	= tve.newVisi;
					if( (actionMask & ACTION_SCROLL) == 0 ) {
						oldVisi = tve.oldVisi;
					}
				}
				if( (tve.actionMask & ACTION_SELECT) != 0 ) {
					newSel		= tve.newSel;
					if( (actionMask & ACTION_SELECT) == 0 ) {
						oldSel = tve.oldSel;
					}
				}
				actionMask |= tve.actionMask;
				anEdit.die();
				return true;
			} else {
				return false;
			}
		}

		/**
		 *  Collapse multiple successive edits
		 *  into one single edit. The old edit is sucked off by
		 *  the new one.
		 */
		public boolean replaceEdit( UndoableEdit anEdit )
		{
			if( anEdit instanceof Edit ) {
				final Edit tve = (Edit) anEdit;
				if( view != tve.view ) return false;
				
				if( (tve.actionMask & ACTION_POSITION) != 0 ) {
					oldPos		= tve.oldPos;
					if( (actionMask & ACTION_POSITION) == 0 ) {
						newPos	= tve.newPos;
					}
				}
				if( (tve.actionMask & ACTION_SCROLL) != 0 ) {
					oldVisi	= tve.oldVisi;
					if( (actionMask & ACTION_SCROLL) == 0 ) {
						newVisi = tve.newVisi;
					}
				}
				if( (tve.actionMask & ACTION_SELECT) != 0 ) {
					oldSel		= tve.oldSel;
					if( (actionMask & ACTION_SELECT) == 0 ) {
						newSel = tve.newSel;
					}
				}
				actionMask |= tve.actionMask;
				anEdit.die();
				return true;
			} else {
				return false;
			}
		}

		public String getPresentationName()
		{
			return getResourceString( "editSetTimelineView" );
		}
	}
}
