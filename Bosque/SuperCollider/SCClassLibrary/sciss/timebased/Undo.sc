/**
 *	(C)opyright 2006-2008 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Class dependancies:
 *
 *	SuperCollider implementation of the java class
 *	javax.swing.undo.AbstractUndoableEdit
 *
 *	@version	0.13, 26-Oct-08
 *	@author	Hanns Holger Rutz
 */
JAbstractUndoableEdit {	// abstract
	var hasBeenDone	= true;
	var alive			= true;

	*new {
		^super.new;
	}

	die {
		alive	= false;
	}

	/**
	 *	@throws	CannotUndoException
	 */
	undo {
		if( this.canUndo.not, {
			MethodError( "Cannot Undo " ++ this.presentationName, thisMethod ).throw;
		});
		hasBeenDone	= false;
	}

	canUndo {
		^(alive && hasBeenDone);
	}

	/**
	 *	@throws	CannotRedoException
	 */
	redo {
		if( this.canRedo.not, {
			MethodError( "Cannot Redo " ++ this.presentationName, thisMethod ).throw;
		});
		hasBeenDone	= true;
	}

	canRedo {
		^(alive && hasBeenDone.not);
	}

	addEdit { arg anEdit;
		^false;
	}
	
	replaceEdit { arg anEdit;
		^false;
	}

	isSignificant {
		^true;
	}

	presentationName {
		^"";
	}
    
	undoPresentationName {
		var pName;
		
		pName = this.presentationName;
		^if( pName == "", "Undo", { "Undo " ++ pName });
	}

	redoPresentationName {
		var pName;
		
		pName = this.presentationName;
		^if( pName == "", "Redo", { "Redo " ++ pName });
	}
}

JCompoundEdit : JAbstractUndoableEdit {
	var inProgress = true;
	var edits;
	
	*new {
		^super.new;
	}
	
	/**
	 *	Sends undo to all contained UndoableEdits in the reverse of the order in which they were added
	 */
	undo {
		super.undo();
		edits.reverseDo( _.undo );
	}
	
	/**
	 *	Sends redo to all contained UndoableEdits in the order in which they were added
	 */
	redo {
		super.redo();
		edits.do( _.redo );
	}
	
	/**
	 *	Returns the last UndoableEdit in edits, or nil if edits is empty
	 */
	protLastEdit { ^if( edits.size > 0, { edits.last })}
	
	/**
	 *	Sends die to each subedit, in the reverse of the order that they were added
	 */
	die {
		edits.reverseDo( _.die );
		super.die;
	}
	
	/**
	 *	If this edit is inProgress, accepts anEdit and returns true.
	 *	The last edit added to this JCompoundEdit is given a chance to addEdit(anEdit).
	 *	If it refuses (returns false), anEdit is given a
	 *	chance to replaceEdit the last edit. If anEdit returns false here, it is added to edits. 
	 */
	addEdit { arg anEdit;
		var last;
		
		if( inProgress.not, { ^false });
		
		last = this.protLastEdit;
		if( last.isNil, {
			edits = edits.add( anEdit )
		}, { if( last.addEdit( anEdit ).not, {
			if( anEdit.replaceEdit( last ).not, {
				edits = edits.add( anEdit );
			}, {
				edits[ edits.size - 1 ] = anEdit;  // replaced
			});
		})});
		^true;
	}
	
	/**
	 *	Sets inProgress to false
	 */
	end { inProgress = false }
	
	/**
	 *	Returns false if isInProgress or if super returns false.
	 */
	canUndo {
		^(this.isInProgress.not and: { super.canUndo });
	}
	
	/**
	 *	Returns false if isInProgress or if super returns false.
	 */
	canRedo {
		^(this.isInProgress.not and: { super.canRedo });
	}
	
	/**
	 *	Returns true if this edit is in progress--that is, it has not received end.
	 *	This generally means that edits are still being added to it.
	 */
	isInProgress { ^inProgress }
	
	/**
	 *	Returns true if any of the UndoableEdits in edits do. Returns false if they all return false
	 */
	isSignificant {
		edits.do({ arg edit;
			if( edit.isSignificant, { ^true })});
		^false;
	}
	
	/**
	 *	Returns presentationName from the last UndoableEdit added to edits. If edits is empty, calls super.
	 */
	presentationName {
		var last = this.protLastEdit;
		^if( last.notNil, { last.presentationName }, { super.presentationName });
	}
	
	/**
	 *	Returns undoPresentationName from the last UndoableEdit  added to edits. If edits is empty, calls super
	 */
	undoPresentationName {
		var last = this.protLastEdit;
		^if( last.notNil, { last.undoPresentationName }, { super.undoPresentationName });
	}

	/**
	 *	Returns redoPresentationName from the last UndoableEdit  added to edits. If edits is empty, calls super
	 */
	redoPresentationName {
		var last = this.protLastEdit;
		^if( last.notNil, { edits.last.redoPresentationName }, { super.redoPresentationName });
	}
}

JUndoManager : JCompoundEdit {
	var indexOfNextAdd = 0;
	var <limit = 100;
	
	*new {
		^super.new;
	}
	
	discardAllEdits {
		edits.do( _.die );
		edits = nil;
		indexOfNextAdd = 0;
	}
	
	protTrimForLimit {
		var size, halfLimit, keepFrom, keepTo, delta;
		
		size = edits.size;
		if( (limit < 0) or: { size <= limit }, { ^this });
		
		halfLimit	= limit.div(2);
		keepFrom	= indexOfNextAdd - 1 - halfLimit;
		keepTo	= indexOfNextAdd - 1 + halfLimit;
		
		if( (keepTo - keepFrom + 1) > limit, { keepFrom = keepFrom + 1 });
		if( keepFrom < 0, { keepTo = keepTo - keepFrom; keepFrom = 0 });
		if( keepTo >= size, { delta = size - keepTo - 1; keepTo = keepTo + delta; keepFrom = keepFrom + delta });
		
		this.protTrimEdits( keepTo + 1, size - 1 );
		this.protTrimEdits( 0, keepFrom - 1 );
	}
	
	protTrimEdits { arg from, to;
		var i;

//		[ \protTrimEdits, from, to ].postln;
		
		if( from > to, { ^this });
		
		i = to;
		while({ i >= from }, {
//			[ "die:", edits.at( i )].postln;
			edits.removeAt( i ).die;
			i = i - 1;
		});
		if( indexOfNextAdd > to, {
			indexOfNextAdd = indexOfNextAdd - (to - from + 1);
		}, { if( indexOfNextAdd >= from, {
			indexOfNextAdd = from;
		})});
	}
	
	limit_ { arg l;
		if( inProgress.not, { MethodError( "Attempt to call UndoManager.setLimit() after UndoManager.end() has been called", thisMethod ).throw });
		
		limit = l;
		this.protTrimForLimit;
	}
	
    protEditToBeUndone {
    	var i, edit;
    	
    	i = indexOfNextAdd;
        while({ i > 0 }, {
        	i = i - 1;
            edit = edits[ i ];
            if( edit.isSignificant, { ^edit });
        });
        ^nil;
    }

    protEditToBeRedone {
    	var count, i, edit;
    	
        count	= edits.size;
        i		= indexOfNextAdd;
        while({ i < count }, {
            edit = edits[ i ];
            i = i + 1;
            if( edit.isSignificant, { ^edit });
        });
        ^nil;
    }

   	protUndoTo { arg edit;
   		var done, next;
   		
   		done = false;
        while({ done.not }, {
        	indexOfNextAdd = indexOfNextAdd - 1;
            next = edits[ indexOfNextAdd ];
            next.undo;
            done = next == edit;
        });
    }

    protRedoTo { arg edit;
    	var done, next;
    	
        done = false;
        while({ done.not }, {
            next = edits[ indexOfNextAdd ];
            indexOfNextAdd = indexOfNextAdd + 1;
            next.redo;
            done = next == edit;
        });
    }

    undoOrRedo {
        if( indexOfNextAdd == edits.size, {
            this.undo;
        }, {
            this.redo;
        });
    }

    canUndoOrRedo {
        if( indexOfNextAdd == edits.size, {
			^this.canUndo;
        }, {
            ^this.canRedo;
        });
    }

    undo {
    	var edit;
        if( inProgress, {
            edit = this.protEditToBeUndone;
            if( edit.isNil, {
				MethodError( "Cannot Undo", thisMethod ).throw;
			});
            this.protUndoTo( edit );
        }, {
            super.undo;
        });
    }

	canUndo {
		var edit;
        if( inProgress, {
            edit = this.protEditToBeUndone;
            ^(edit.notNil and: { edit.canUndo });
        }, {
            ^super.canUndo;
        });
    }

   	redo {
   		var edit;
        if( inProgress, {
            edit = this.protEditToBeRedone;
            if( edit.isNil, {
				MethodError( "Cannot Reo", thisMethod ).throw;
            });
            this.protRedoTo( edit );
        }, {
            super.redo;
        });
    }

    canRedo {
    	var edit;
        if( inProgress, {
            edit = this.protEditToBeRedone;
            ^(edit.notNil and: { edit.canRedo });
        }, {
           ^super.canRedo;
        });
    }

    addEdit { arg anEdit;
		var retVal;

		this.protTrimEdits( indexOfNextAdd, edits.size - 1 );

		retVal = super.addEdit( anEdit );
		if( inProgress, {
			retVal = true;
		});

		indexOfNextAdd = edits.size;

		this.protTrimForLimit;

		^retVal;
	}

	end {
		super.end;
		this.protTrimEdits( indexOfNextAdd, edits.size - 1 );
	}

	undoOrRedoPresentationName {
		if( indexOfNextAdd == edits.size, {
			^this.undoPresentationName;
		}, {
			^this.redoPresentationName;
		});
	}

	getUndoPresentationName {
		if( inProgress, {
			if( this.canUndo, {
				^this.protEditToBeUndone.undoPresentationName;
			}, {
				^"Undo"; // UIManager.getString("AbstractUndoableEdit.undoText");
			});
		}, {
			^super.undoPresentationName;
		});
	}

	redoPresentationName {
		if( inProgress, {
			if( this.canRedo, {
				^this.protEditToBeRedone.redoPresentationName;
			}, {
				^"Redo"; // UIManager.getString("AbstractUndoableEdit.redoText");
			});
		}, {
			^super.redoPresentationName;
		});
	}

//    public void undoableEditHappened(UndoableEditEvent e) {
//        addEdit(e.getEdit());
//    }
}


JBasicUndoableEdit : JAbstractUndoableEdit	// abstract
// implements PerformableEdit
{
	var <>onDeath;

//	perform
	performEdit { ^this.subclassResponsibility( thisMethod )}

	die {
//		[ "JBasicUndoableEdit.die", onDeath ].postln;
		super.die;
		onDeath.value( this );
	}

//	public void debugDump( int nest );
}

ScissUndoManager : JUndoManager
{

	/*
	 *  An <code>Action</code> object
	 *  suitable for attaching to a
	 *  <code>JMenuItem</code> in a edit
	 *  menu. The action will cause the
	 *  <code>UndoManager</code> to undo
	 *  the last edit in its history.
	 */
//	var undoAction;
	/*
	 *  An <code>Action</code> object
	 *  suitable for attaching to a
	 *  <code>JMenuItem</code> in a edit
	 *  menu. The action will cause the
	 *  <code>UndoManager</code> to redo
	 *  the edit that was undone recently.
	 */
//	var redoAction;
//
//	var debugAction;

//	private final de.sciss.app.Document		doc;

//	var undoText, redoText;

	/*
	 *	The concept of pendingEdits is that
	 *	insignificant (visual only) edits
	 *	should not destroy the redo tree
	 *	(which they do in the standard undo manager).
	 *	Imagine the user places a marker on the timeline,
	 *	hits undo, then accidentally or by intention
	 *	moves the timeline position. This will render
	 *	the redo of the marker add impossible. Now the
	 *	timeline position is insignificant and hence
	 *	will be placed on the pendingEdits stack.
	 *	This is a &quot;lazy&quot; thing because these
	 *	pendingEdits will only be added to the real
	 *	undo history when the next significant edit comes in.
	 */
	var pendingEdits;
	var pendingEditCount = 0;	// fucking JCompoundEdit hasn't got getter methods

	/**
	 *  Instantiate a new UndoManager.
	 *
	 *  @param  doc		the document whose edits are monitored
	 */
	*new {
		^super.new.prInit;
	}
	
	prInit {
		this.limit = 1000;
		
//		this.doc	= doc;
//		undoText	= "Undo"; // doc.getApplication().getResourceString( "menuUndo" );
//		redoText	= "Reso"; // doc.getApplication().getResourceString( "menuRedo" );
//		undoAction	= actionUndoClass.new;
//		redoAction	= actionRedoClass.new;
		pendingEdits= JCompoundEdit.new;
		this.prUpdateStates;
	}

	
	/**
	 *  Add a new edit to the undo history.
	 *  This behaves just like the normal
	 *  UndoManager, i.e. it tries to replace
	 *  the previous edit if possible. When
	 *  the edits <code>isSignificant()</code>
	 *  method returns true, the main application
	 *  is informed about this edit by calling
	 *  the <code>setModified</code> method.
	 *  Also the undo and redo action's enabled
	 *  / disabled states are updated.
	 *	<p>
	 *	Insignificant edits are saved in a pending
	 *	compound edit that gets added with the
	 *	next significant edit to allow redos as
	 *	long as possible.
	 *
	 *  @see	de.sciss.app.Document#setDirty( boolean )
	 *  @see	javax.swing.undo.UndoableEdit#isSignificant()
	 *  @see	javax.swing.Action#setEnabled( boolean )
	 */

	addEdit { arg anEdit;
		var result;
		if( anEdit.isSignificant, {
//			synchronized( pendingEdits ) {
				if( pendingEditCount > 0, {
					pendingEdits.end;
					super.addEdit( pendingEdits );
					pendingEdits = JCompoundEdit.new;
					pendingEditCount = 0;
				});
//			}
			result = super.addEdit( anEdit );
			this.prUpdateStates;
	//		if( anEdit.isSignificant() ) doc.setDirty( true );
			^result;
		}, {
//			synchronized( pendingEdits ) {
				pendingEditCount = pendingEditCount + 1;
				^pendingEdits.addEdit( anEdit );
//			}
		});
	}
			
	redo {
//	throws CannotRedoException
		protect {
			this.prUndoPending;
			super.redo;
		} { this.prUpdateStates };
	}

	undo {
//	throws CannotUndoException
		protect {
			this.prUndoPending;
			super.undo;
		} { this.prUpdateStates };
	}
	
	prUndoPending {
//		synchronized( pendingEdits ) {
			if( pendingEditCount > 0, {
				pendingEdits.end;
				pendingEdits.undo;
				pendingEdits = JCompoundEdit.new;
				pendingEditCount = 0;
			});
//		}
	}
	
	/**
	 *	Empty the undo manager, sending each edit a die message in the process.
	 *
	 *  Purge the undo history and
	 *  update the undo / redo actions enabled / disabled state.
	 *
	 *  @see	de.sciss.app.Document#setDirty( boolean )
	 */
	discardAllEdits {
//		synchronized( pendingEdits ) {
			pendingEdits.die;
			pendingEdits = JCompoundEdit.new;
			pendingEditCount = 0;
//		}

		super.discardAllEdits();
		this.prUpdateStates;
	}

	prUpdateStates {
		this.tryChanged( \state );
//		var text;
//
//		if( undoAction.isEnabled() != canUndo() ) {
//			undoAction.setEnabled( canUndo() );
//			doc.setDirty( canUndo() );
//		}
//		if( redoAction.isEnabled() != canRedo() ) redoAction.setEnabled( canRedo() );
//		
////		text = canUndo() ? undoText + " " + undoPresentationName() : undoText;
//		text = undoPresentationName();
//		if( !text.equals( undoAction.getValue( Action.NAME ))) undoAction.putValue( Action.NAME, text );
//		
////		text = canRedo() ? redoText + " " + redoPresentationName() : redoText;
//		text = redoPresentationName();
//		if( !text.equals( redoAction.getValue( Action.NAME ))) redoAction.putValue( Action.NAME, text );
	}
}

JSyncCompoundEdit : JCompoundEdit
//implements PerformableEdit
{
	var presentationName;
	var collToPerform;

//	/**
//	 *  The LockManager to use
//	 *  for locking doors
//	 */
//	protected final LockManager lm;
//	/**
//	 *  The doors to lock exclusively
//	 *  in undo / redo operation
//	 */
//	protected final int doors;

	/**
	 *  Creates a <code>CompountEdit</code> object, whose Undo/Redo
	 *  actions are synchronized.
	 *
	 *  @param  lm		the <code>LockManager</code> to use in synchronization
	 *  @param  doors   the doors to lock exclusively using the provided <code>LockManager</code>
	 */
	 *new { arg presentationName;
	 	^super.new.prInitSync( presentationName );
	 }

	/**
	 *  Creates a <code>CompountEdit</code> object with a given name, whose Undo/Redo
	 *  actions are synchronized.
	 *
	 *  @param  lm					the <code>LockManager</code> to use in synchronization
	 *  @param  doors				the doors to lock exclusively using the provided <code>LockManager</code>
	 *	@param	presentationName	text describing the compound edit
	 */
	prInitSync { arg argPresentationName;
//		this.lm			= lm;
//		this.doors		= doors;
		presentationName	= argPresentationName;
	}
	
	/**
	 *  Performs undo on all compound sub edits within
	 *  a synchronization block.
	 *
	 *  @synchronization	waitExclusive on the given LockManager and doors
	 */
	undo {
//		protected {
//			lm.waitExclusive( doors );
			super.undo;
			this.protUndoDone;
//		} {
//			lm.releaseExclusive( doors );
//		}
	}

	/**
	 *  Performs redo on all compound sub edits within
	 *  a synchronization block.
	 *
	 *  @synchronization	waitExclusive on the given LockManager and doors
	 */
	redo {
//		protected {
//			lm.waitExclusive( doors );
			super.redo;
			this.protRedoDone;
//		} {
//			lm.releaseExclusive( doors );
//		}
	}

	/**
	 *  Cancels the compound edit and undos all sub edits
	 *  made so far.
	 *
	 *  @synchronization	waitExclusive on the given LockManager and doors.
	 *						<strong>however, the caller must
	 *						block all <code>addEdit</code>
	 *						and the <code>end</code> or <code>cancel</code> call
	 *						into a sync block itself to prevent confusion
	 *						by intermitting calls to the locked objects.</strong>
	 */
	cancel {
//		protected {
//			lm.waitExclusive( doors );
			collToPerform.do({ arg edit;
				edit.die;
			});
			collToPerform = nil;
			this.end;
			super.undo;
			super.die;
			this.protCancelDone;
//		} {
//			lm.releaseExclusive( doors );
//		}
	}

	/**
	 *  This gets called after the undo
	 *  operation but still inside the
	 *  sync block. Subclasses can use this
	 *  to fire any notification events.
	 */
	protUndoDone {}

	/**
	 *  This gets called after the redo
	 *  operation but still inside the
	 *  sync block. Subclasses can use this
	 *  to fire any notification events.
	 */
	protRedoDone {}

	/**
	 *  This gets called after the cancel
	 *  operation but still inside the
	 *  sync block. Subclasses can use this
	 *  to fire any notification events.
	 */
	protCancelDone {}

	presentationName {
		^if( presentationName.notNil, presentationName, { super.presentationName });
	}

	undoPresentationName {
		if( presentationName.notNil, {
			^("Undo " ++ presentationName);
		}, {
			^super.undoPresentationName;
		});
	}

	redoPresentationName {
		if( presentationName.notNil, {
			^("Redo " ++ presentationName);
		}, {
			^super.redoPresentationName;
		});
	}

//	protected String getResourceString( String key )
//	{
//		return AbstractApplication.getApplication().getResourceString( key );
//	}

	addPerform { arg edit;
	
//("ADD PERFORM " ++ edit).postln;
	
		collToPerform = collToPerform.add( edit );
	}
		
	performEdit {
		collToPerform.do({ arg edit;
			edit.performEdit;
			this.addEdit( edit );
		});
		collToPerform = nil;
//		this.end;
	}
	
	performAndEnd {
		^this.performEdit.end;
	}

//	public void debugDump( int nest )
//	{
//		final StringBuffer strBuf = new StringBuffer( nest << 1 );
//		for( int i = 0; i < nest; i++ ) strBuf.append( "  " );
//		final String pre = strBuf.toString();
//		PerformableEdit edit;
//		
//		nest++;
//	
//		System.err.println( pre + "Edits : "+edits.size() );
//		for( int i = 0; i < edits.size(); i++ ) {
//			edit = (PerformableEdit) edits.get( i );
//			System.err.print( pre + " edit #"+i+" = " );
//			edit.debugDump( nest );
//		}
//		System.err.println( pre + "To perform : "+(collToPerform != null ? collToPerform.size() : 0 ));
//		if( collToPerform != null ) {
//			for( int i = 0; i < collToPerform.size(); i++ ) {
//				edit = (PerformableEdit) collToPerform.get( i );
//				System.err.print( pre + " perf #"+i+" = " );
//				edit.debugDump( nest );
//			}
//		}
//	}
}