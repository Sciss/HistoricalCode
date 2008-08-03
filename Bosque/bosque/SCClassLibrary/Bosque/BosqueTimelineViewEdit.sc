/*
 *	BosqueTimelineViewEdit
 *	(Bosque)
 *
 *	Copyright (c) 2007-2008 Hanns Holger Rutz. All rights reserved.
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
 *	Changelog:
 */

/**
 *	@author	Hanns Holger Rutz
 *	@version	0.10, 18-Jul-08
 */
BosqueTimelineViewEdit : JBasicUndoableEdit {
	var	<view;
	var	source;
	var	<>oldPos, <>newPos;
	var	<>oldVisi, <>newVisi, <>oldSel, <>newSel;

	var	<>actionMask;
		
	classvar kActionPosition	= 0x01;
	classvar kActionScroll		= 0x02;
	classvar kActionSelect		= 0x04;

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
	*new { arg source, view;
		^super.new.prInitTVE( source, view );
	}
	
	prInitTVE { arg argSource, argView;
		source		= argSource;
		view			= argView;
		actionMask	= 0;
	}
	
	*position { arg source, view, pos;
		var tve = this.new( source, view );
		tve.actionMask	= kActionPosition;
		
		tve.oldPos		= view.cursor.position;
		tve.newPos		= pos;
		^tve;
	}

	*scroll { arg source, view, newVisi;
		var tve = this.new( source, view );
		tve.actionMask	= kActionScroll;
		
		tve.oldVisi		= view.span;
		tve.newVisi		= newVisi;
		^tve;
	}

	*select { arg source, view, newSel;
		var tve = this.new( source, view );
		tve.actionMask	= kActionSelect;
		
		tve.oldSel		= view.selection.span;
		tve.newSel		= newSel;
		^tve;
	}
	
	performEdit {
		if( (actionMask & kActionPosition) != 0, {
			view.cursor.position = newPos; // setPosition( source, newPos );
		});
		if( (actionMask & kActionScroll) != 0, {
			view.span = newVisi; // setVisibleSpan( source, newVisi );
		});
		if( (actionMask & kActionSelect) != 0, {
			view.selection.span = newSel; // setSelectionSpan( source, newSel );
		});
		source	= this;
	}

	/**
	 *  @return		false to tell the UndoManager it should not feature
	 *				the edit as a single undoable step in the history.
	 *				which is especially important since <code>TimelineAxis</code>
	 *				will generate lots of edits when the user drags
	 *				the timeline selection.
	 */
	isSignificant { ^false }

	/**
	 *  Undo the edit
	 *  by calling the <code>Timeline.setSelectionSpan</code>,
	 *  method, thus dispatching a <code>TimelineEvent</code>.
	 *
	 *  @synchronization	waitExlusive on DOOR_TIME.
	 */
	undo	{
		super.undo;
		if( (actionMask & kActionPosition) != 0, {
			view.cursor.position = oldPos; // setPosition( source, oldPos );
		});
		if( (actionMask & kActionScroll) != 0, {
			view.span = oldVisi; // setVisibleSpan( source, oldVisi );
		});
		if( (actionMask & kActionSelect) != 0, {
			view.selection.span = oldSel; // setSelectionSpan( source, oldSel );
		});
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
	redo {
		super.redo;
		this.performEdit;
	}
	
	/**
	 *  Collapse multiple successive EditSetReceiverBounds edit
	 *  into one single edit. The new edit is sucked off by
	 *  the old one.
	 */
	addEdit { arg anEdit;
		var tve;

		if( anEdit.isKindOf( this.class ), {
			tve = anEdit;
			if( view != tve.view, { ^false });
			
			if( (tve.actionMask & kActionPosition) != 0, {
				newPos		= tve.newPos;
				if( (actionMask & kActionPosition) == 0, {
					oldPos = tve.oldPos;
				});
			});
			if( (tve.actionMask & kActionScroll) != 0, {
				newVisi	= tve.newVisi;
				if( (actionMask & kActionScroll) == 0, {
					oldVisi = tve.oldVisi;
				});
			});
			if( (tve.actionMask & kActionSelect) != 0, {
				newSel		= tve.newSel;
				if( (actionMask & kActionSelect) == 0, {
					oldSel = tve.oldSel;
				});
			});
			actionMask = actionMask | tve.actionMask;
			anEdit.die;
			^true;
		}, {
			^false;
		});
	}

	/**
	 *  Collapse multiple successive edits
	 *  into one single edit. The old edit is sucked off by
	 *  the new one.
	 */
	replaceEdit { arg anEdit;
		var tve;

		if( anEdit.isKindOf( this.class ), {
			tve = anEdit;
			if( view != tve.view, { ^false });

			if( (tve.actionMask & kActionPosition) != 0, {
				oldPos		= tve.oldPos;
				if( (actionMask & kActionPosition) == 0, {
					newPos	= tve.newPos;
				});
			});
			if( (tve.actionMask & kActionScroll) != 0, {
				oldVisi	= tve.oldVisi;
				if( (actionMask & kActionScroll) == 0, {
					newVisi = tve.newVisi;
				});
			});
			if( (tve.actionMask & kActionSelect) != 0, {
				oldSel		= tve.oldSel;
				if( (actionMask & kActionSelect) == 0, {
					newSel = tve.newSel;
				});
			});
			actionMask = actionMask | tve.actionMask;
			anEdit.die;
			^true;
		}, {
			^false;
		});
	}

	presentationName {
		^"Set Timeline View"; // getResourceString( "editSetTimelineView" );
	}
}

