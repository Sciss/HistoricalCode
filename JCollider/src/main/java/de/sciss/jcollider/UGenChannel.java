/*
 *  UGenChannel.java
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
 *	Represents one output channel of a (potentially
 *	multi-ouput) UGen.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.31, 08-Oct-07
 */
public class UGenChannel
implements UGenInput
{
	private final UGen	ugen;
	private final int	channel;

	/**
	 *	You do not directly create <code>UGenChannel</code>s
	 *	but retrieve them from a <code>UGen</code> by
	 *	calling its <code>getChannel</code> method
	 *
	 *	@see	UGen#getChannel( int )
	 */
	protected UGenChannel( UGen ugen, int channel )
	{
		this.ugen		= ugen;
		this.channel	= channel;
	}
	
	/**
	 *	Returns the <code>UGen</code> whose
	 *	output this object represents
	 */
	public UGen getUGen()
	{
		return ugen;
	}
	
	/**
	 *	Returns the index in the array of
	 *	outputs of the corresponding <code>UGen</code>
	 *	(beginning at zero).
	 */
	public int getChannel()
	{
		return channel;
	}

// -------- UGenInput interface --------

	public Object getRate()
	{
		return ugen.getOutputRate( channel );
	}

	public String dumpName()
	{
		if( ugen.getNumOutputs() <= 1 ) {
			return ugen.dumpName();
		} else {
			return( ugen.dumpName() + "[ch:" + channel + ']' );
		}
	
//		return( ugen.getSynthIndex() + "_" + ugen.getName() +
//			(ugen.getNumOutputs() > 1 ? String.valueOf( channel ) : "") );
	}
	
	/**
	 *	Returns <code>this</code> as an array
	 */
	public UGenInput[] asUGenInputs()
	{
		return new UGenInput[] { this };
	}
	
	/**
	 *	Returns <code>1</code> naturally
	 */
	public int getNumOutputs()
	{
		return 1;
	}
	
	/**
	 *	Returns <code>this</code> naturally
	 */
	public GraphElem getOutput( int idx )
	{
		return this;
	}
}