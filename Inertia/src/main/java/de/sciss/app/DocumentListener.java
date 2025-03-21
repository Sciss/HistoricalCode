/*
 *  DocumentListener.java
 *  de.sciss.app package
 *
 *  Copyright (c) 2004-2005 Hanns Holger Rutz. All rights reserved.
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
 *		02-Aug-05	created
 */

package de.sciss.app;

import java.util.EventListener;

/**
 *  Interface for listening
 *  the changes of the list of documents
 *  of an application. They are
 *	usually generated by the <code>DocumentHandler</code>
 *	of the application.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.13, 02-Aug-05
 *
 *  @see	DocumentHandler
 */
public interface DocumentListener
extends EventListener
{
	/**
	 *  Notifies the listener that
	 *  a document has been created and added
	 *	to the list of open documents.
	 *
	 *  @param  e   the event describing
	 *				the change
	 */
	public void documentAdded( DocumentEvent e );

	/**
	 *  Notifies the listener that
	 *  a document has been removed from
	 *	the list of open documents and
	 *	was destroyed.
	 *
	 *  @param  e   the event describing
	 *				the change
	 */
	public void documentRemoved( DocumentEvent e );

	/**
	 *  Notifies the listener that
	 *  a the active document has been switched.
	 *	<code>e.getDocument</code> will return
	 *	the newly active document (or <code>null</code>,
	 *	if no document is active any more).
	 *
	 *  @param  e   the event describing
	 *				the change
	 */
	public void documentFocussed( DocumentEvent e );
}