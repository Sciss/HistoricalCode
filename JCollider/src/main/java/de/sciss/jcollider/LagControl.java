/*
 *  LagControl.java
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
 *	Class for the LagControl UGen.
 *	This class is recognized by SynthDef
 *	in the building process.
 *	<p>
 *	For details, refer to the <code>Control</code> class.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.25, 15-Oct-05
 *
 *	@see		Control
 */
public class LagControl
extends Control
{
	private LagControl( String[] names, Object rate, float[] values, Constant[] lags )
	{
		super( "LagControl", rate, names.length, lags, 0 );
	
		if( (names.length != values.length) || (values.length != lags.length) ) {
			throw new IllegalArgumentException( "LagControl: # of names / values / lags must be equal" );
		}
	
		for( int i = 0; i < names.length; i++ ) {
			descs[ i ] = new ControlDesc( names[ i ], rate, values[ i ], lags[ i ].getValue() );
		}
	}
	
	private static Constant[] createLagInputs( float[] lags )
	{
		final Constant[] ins = new Constant[ lags.length ];
		
		for( int i = 0; i < ins.length; i++ ) {
			ins[ i ] = new Constant( lags[ i ]);
		}
		
		return ins;
	}
	
//	public static GraphElem kr( String name )
//	{
//		return kr( name, 0.0f );
//	}

	public static Control kr( String name, float defaultValue, float lag )
	{
		return new LagControl( new String[] { name }, kControlRate,
							   new float[] { defaultValue }, new Constant[] { new Constant( lag )});
	}

	public static Control kr( String[] names, float[] values, float[] lags )
	{
		return new LagControl( names, kControlRate, values, createLagInputs( lags ));
	}
}
