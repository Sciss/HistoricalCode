/*
 *  Constant.java
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
 *	A <code>Constant</code> is a wrapper
 *	for a constant in a UGen graph. It
 *	implements the <code>UGenInput</code>
 *	interface and therefore also the
 *	<code>GraphElem</code> interface which
 *	describes heterogeneous elements of
 *	a UGen graph.
 *
 *	@see	UGen#ir( float )
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.31, 08-Oct-07
 */
public class Constant
implements UGenInput, Constants
{
	private final float value;

	public Constant( float value )
	{
		this.value	= value;
	}
	
	public float getValue()
	{
		return value;
	}

// --------- UGenInput interface ---------

	public Object getRate()
	{
		return kScalarRate;
	}

	public String dumpName()
	{
		return( String.valueOf( value ));
	}

	public UGenInput[] asUGenInputs()
	{
		return new UGenInput[] { this };
	}
	
	public int getNumOutputs()
	{
		return 1;
	}
	
	public GraphElem getOutput( int idx )
	{
		return this;
	}
}
