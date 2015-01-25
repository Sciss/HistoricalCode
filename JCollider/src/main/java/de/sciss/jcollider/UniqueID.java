/*
 *  UniqueID.java
 *  (JCollider)
 *
 *  Copyright (c) 2004-2015 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.jcollider;

/**
 *	Utility class for
 *	outputting unique incremental
 *	numbers.
 *
 *	@todo	this should be simply put in
 *			the JCollider class
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.2, 11-Sep-05 (some countries are still at war)
 *
 *	@synchronization	this class is thread safe
 */
public abstract class UniqueID
{
	private static int id	= 1000;
	
	/**
	 *	Returns the next unique ID
	 */
	public static synchronized int next()
	{
		return id++;
	}
}
