/*
 *  SessionObject.java
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
 *		15-Jan-05	created
 *		02-Feb-05	moved to package 'session'
 *		18-Jul-08	copied from Cillo
 */

package de.sciss.timebased.session;

import de.sciss.util.Disposable;
import de.sciss.util.MapManager;

/**
 *  @author		Hanns Holger Rutz
 *  @version	0.75, 13-Jul-08
 */
public interface SessionObject
extends Disposable
{
	/**
	 *	Code for <code>MapManager.Event.getOwnerModType()</code>:
	 *	the object has been renamed
	 *
	 *	@see	de.sciss.meloncillo.util.MapManager.Event#getOwnerModType()
	 */
	public static final int OWNER_RENAMED		=	0x1000;

	/**
	 *	Code for <code>MapManager.Event.getOwnerModType()</code>:
	 *	the object has been visually changed
	 *
	 *	@see	de.sciss.meloncillo.util.MapManager.Event#getOwnerModType()
	 */
	public static final int OWNER_VISUAL		=	MapManager.OWNER_VISUAL;

	public static final String	MAP_KEY_FLAGS	= "flags";
	
	public static final int FLAGS_SOLO			= 0x01;
	public static final int FLAGS_MUTE			= 0x02;
	public static final int FLAGS_SOLOSAFE		= 0x04;
	public static final int FLAGS_VIRTUALMUTE	= 0x08;

	public static final String XML_ATTR_NAME		= "name";
	public static final String XML_ATTR_CLASS		= "class";
	public static final String XML_ELEM_OBJECT		= "object";
	public static final String XML_ELEM_COLL		= "coll";
	public static final String XML_ELEM_MAP			= "map";

	/**
	 *  Retrieves the property map manager of the session
	 *	object. This manager may be used to read and
	 *	write properties and register listeners.
	 *
	 *	@return	the property map manager that stores
	 *			all the properties of this session object
	 */
	public MapManager getMap();

	/**
	 *  Queries the object's logical name.
	 *  This name is used for displaying on the GUI.
	 *
	 *  @return		current object's name.
	 */
	public String getName();
}