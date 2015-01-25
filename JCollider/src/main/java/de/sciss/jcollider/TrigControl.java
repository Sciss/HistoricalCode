/*
 *  TrigControl.java
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
 *	Class for the TrigControl UGen.
 *	This class is recognized by SynthDef
 *	in the building process.
 *	For the sake of clarity, trigger control
 *	names should begin with a small 't'
 *	letter, though this is not obligatory.
 *	<p>
 *	For details, refer to the <code>Control</code> class.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.25, 15-Oct-05
 *
 *	@see		Control
 */
public class TrigControl
extends Control
{
	private TrigControl( String[] names, Object rate, float[] values )
	{
		super( "TrigControl", rate, names.length, new UGenInput[0], 0 );
	
		for( int i = 0; i < names.length; i++ ) {
			descs[ i ] = new ControlDesc( names[ i ], rate, values[ i ], 0.0f );
		}
	}
	
	public static GraphElem kr( String name )
	{
		return kr( name, 0.0f );
	}

	public static Control kr( String name, float defaultValue )
	{
		return new TrigControl( new String[] { name }, kControlRate, new float[] { defaultValue });
	}

	public static Control kr( String[] names, float[] values )
	{
		return new TrigControl( names, kControlRate, values );
	}
}
