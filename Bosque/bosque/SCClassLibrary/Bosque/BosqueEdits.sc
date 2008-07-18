/**
 *	(C)opyright 2007-2008 by Hanns Holger Rutz. All rights reserved.
 *
 *	@version	0.12, 14-Aug-07
 */
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

	*new { arg source, doc, value, setter, getter;
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

	*span { arg source, doc, newSpan;
		^this.new( source, doc, newSpan, 'span_', 'span' );
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