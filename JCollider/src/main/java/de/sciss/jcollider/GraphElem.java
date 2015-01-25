/*
 *  GraphElem.java
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
 *	This interface does the dirty
 *	job of putting all kinds of different
 *	elements under one brand which
 *	can be used in UGen graph generation.
 *	Kind of a wurmfortsatz of quickly translating
 *	an untyped polymorph language into java.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.2, 11-Sep-05 (some countries are still at war)
 */
public interface GraphElem
{
	public int			getNumOutputs();
	public GraphElem	getOutput( int idx );
	public UGenInput[]	asUGenInputs();
}
