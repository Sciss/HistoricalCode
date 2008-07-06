/**
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.12, 14-Aug-07
 */
BosqueTimelineVisualEdit : JBasicUndoableEdit {
	var	doc;
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
	*new { arg source, doc;
		^super.new.prInitTimelineVisualEdit( source, doc );
	}
	
	prInitTimelineVisualEdit { arg argSource, argDoc;
		source		= argSource;
		doc			= argDoc;
		actionMask	= 0;
	}
	
	*position { arg source, doc, pos;
		var tve = this.new( source, doc );
		tve.actionMask	= kActionPosition;
		
		tve.oldPos		= doc.timeline.position;
		tve.newPos		= pos;
		^tve;
	}

	*scroll { arg source, doc, newVisi;
		var tve = this.new( source, doc );
		tve.actionMask	= kActionScroll;
		
		tve.oldVisi		= doc.timeline.visibleSpan;
		tve.newVisi		= newVisi;
		^tve;
	}

	*select { arg source, doc, newSel;
		var tve = this.new( source, doc );
		tve.actionMask	= kActionSelect;
		
		tve.oldSel		= doc.timeline.selectionSpan;
		tve.newSel		= newSel;
		^tve;
	}
	
	performEdit {
		if( (actionMask & kActionPosition) != 0, {
			doc.timeline.position = newPos; // setPosition( source, newPos );
		});
		if( (actionMask & kActionScroll) != 0, {
			doc.timeline.visibleSpan = newVisi; // setVisibleSpan( source, newVisi );
		});
		if( (actionMask & kActionSelect) != 0, {
			doc.timeline.selectionSpan = newSel; // setSelectionSpan( source, newSel );
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
			doc.timeline.position = oldPos; // setPosition( source, oldPos );
		});
		if( (actionMask & kActionScroll) != 0, {
			doc.timeline.visibleSpan = oldVisi; // setVisibleSpan( source, oldVisi );
		});
		if( (actionMask & kActionSelect) != 0, {
			doc.timeline.selectionSpan = oldSel; // setSelectionSpan( source, oldSel );
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
//		if( dontMerge ) return false;
	
		if( anEdit.isKindOf( this.class ), {
			tve = anEdit;
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
//		if( dontMerge ) return false;

		if( anEdit.isKindOf( this.class ), {
			tve = anEdit;
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

BosqueEditAddSessionObjects : JBasicUndoableEdit {
	var	collSessionObjects;
	var	source;
	var	quoi;
	var	signi;

	/**
	 *  Create and perform this edit. This
	 *  invokes the <code>SessionObjectCollection.addAll</code> method,
	 *  thus dispatching a <code>SessionCollection.Event</code>.
	 *
	 *  @param  source			who initiated the action
	 *  @param  doc				session object to which the
	 *							receivers are added
	 *  @param  collSessionObjects   collection of receivers to
	 *							add to the session.
	 *  @see	de.sciss.meloncillo.session.SessionCollection
	 *  @see	de.sciss.meloncillo.session.SessionCollection.Event
	 *  @synchronization		waitExclusive on doors
	 */
	*new { arg source, sc, objects, significant = true;
		^super.new.prInitEditAdd( source, sc, objects, significant );
	}
	
	prInitEditAdd { arg argSource, sc, objects, significant;
		source			= argSource;
		quoi				= sc;
		collSessionObjects	= objects.copy;
		signi			= significant;
	}

	performEdit {
//		[ quoi, \addAll, source, collSessionObjects, collSessionObjects.size ].postln;
		quoi.addAll( source, collSessionObjects );
	}
	
	isSignificant { ^signi }

	/**
	 *  Undo the edit
	 *  by calling the <code>SessionObjectCollection.removeAll</code>,
	 *  method, thus dispatching a <code>SessionCollection.Event</code>.
	 *
	 *  @synchronization	waitExlusive on doors.
	 */
	undo {
		super.undo;
		quoi.removeAll( source, collSessionObjects );
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
	redo {
		super.redo;
		this.performEdit;
	}
	
	presentationName {
		^"Add Session Objects"; // getResourceString( "editAddSessionObjects" );
	}
}

// XXX should make common superclass with BosqueEditAddSessionObjects
BosqueEditRemoveSessionObjects : JBasicUndoableEdit {
	var	collSessionObjects;
	var	source;
	var	quoi;
	var	signi;

	/**
	 *  Create and perform this edit. This
	 *  invokes the <code>SessionObjectCollection.addAll</code> method,
	 *  thus dispatching a <code>SessionCollection.Event</code>.
	 *
	 *  @param  source				who initiated the action
	 *  @param  doc					session object to which the
	 *								receivers are added
	 *  @param  collSessionObjects  collection of objects to
	 *								remove from the session.
	 *  @see	de.sciss.meloncillo.session.SessionCollection
	 *  @see	de.sciss.meloncillo.session.SessionCollection.Event
	 *  @synchronization		waitExclusive on doors
	 */
	*new { arg source, sc, objects, significant = true;
		^super.new.prInitEditRemove( source, sc, objects, significant );
	}
	
	prInitEditRemove { arg argSource, sc, objects, significant;
		source				= argSource;
		collSessionObjects		= objects.copy;
		quoi					= sc;
		signi				= significant;
	}

	performEdit {
		quoi.removeAll( source, collSessionObjects );
	}

	isSignificant { ^signi }

	/**
	 *  Undo the edit
	 *  by calling the <code>SessionObjectCollection.addAll</code> method,
	 *  thus dispatching a <code>SessionCollection.Event</code>
	 *
	 *  @synchronization	waitExclusive on doors
	 */
	undo {
		super.undo;
		quoi.addAll( source, collSessionObjects );
	}
	
	/**
	 *  Redo the add operation.
	 *  The original source is discarded
	 *  which means, that, since a new <code>SessionCollection.Event</code>
	 *  is dispatched, even the original object
	 *  causing the edit will not know the details
	 *  of the action, hence thorougly look
	 *  and adapt itself to the new edit.
	 *
	 *  @synchronization	waitExclusive on doors
	 */
	redo {
		super.redo;
		this.performEdit;
	}

	presentationName {
		^"Remove Session Objects"; // getResourceString( "editRemoveSessionObjects" );
	}
}

BosqueTimelineEdit : JBasicUndoableEdit {
	var	doc;
	var	source;
	var	<>oldValue, <>newValue, <>setter, <>getter;

	*new {�arg source, doc, value, setter, getter;
		^super.new.prInitTimelineEdit( source, doc, value, setter, getter );
	}
	
	prInitTimelineEdit { arg argSource, argDoc, value, argSetter, argGetter;
		source		= argSource;
		doc			= argDoc;
		setter		= argSetter;
		getter		= argGetter;
		oldValue		= doc.timeline.perform( getter );
		newValue		= value;
	}
	
	*rate { arg source, doc, newRate;
		^this.new( source, doc, newRate, 'rate_', 'rate' );
	}

	*length { arg source, doc, newLength;
		^this.new( source, doc, newLength, 'length_', 'length' );
	}
	
	performEdit {
		doc.timeline.perform( setter, newValue );
	}

	undo	{
		super.undo;
		doc.timeline.perform( setter, oldValue );
	}
	
	redo {
		super.redo;
		this.performEdit;
	}
	
	addEdit { arg anEdit;
		if( anEdit.isKindOf( this.class ) and: { (anEdit.getter === this.getter) and:
			{ anEdit.setter === this.setter }}, {
			
			setter	= anEdit.setter;
			getter	= anEdit.getter;
			newValue	= anEdit.newValue;
			anEdit.die;
			^true;
		}, {
			^false;
		});
	}

	presentationName {
		^"Change Timeline";
	}
}

BosqueFunctionEdit : JBasicUndoableEdit {
	var	repName, signi, doFunction, undoFunction;

	*new { arg doFunction, undoFunction, name, significant = true;
		^super.new.prInitFuncEdit( doFunction, undoFunction, name, significant );
	}
	
	prInitFuncEdit { arg doFunc, undoFunc, name, significant;
		doFunction	= doFunc;
		undoFunction	= undoFunc;
		repName		= name;
		signi		= significant;
	}

	performEdit {
		doFunction.value;
	}

	isSignificant { ^signi }

	undo {
		super.undo;
		undoFunction.value;
	}
	
	redo {
		super.redo;
		this.performEdit;
	}

	presentationName {
		^repName;
	}
}