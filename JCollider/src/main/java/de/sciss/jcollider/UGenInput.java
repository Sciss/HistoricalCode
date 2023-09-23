/*
 *  UGenInput.java
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
 *	A subinterface of <code>GraphElem</code>
 *	that represents elements in a graph
 *	that can directly be used as inputs to a ugen.
 *	So there are two implementing classes,
 *	<code>UGenChannel</code> and <code>Constant</code>.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.31, 08-Oct-07
 */
public interface UGenInput
extends GraphElem
{
	/**
	 *	A UGen input is naturally single-channelled.
	 *	So this returns the rate of that single channel
	 *	or <code>kScalarRate</code> in the case of a <code>Constant</code>
	 */
	public Object getRate();
	
	public String dumpName();
}
