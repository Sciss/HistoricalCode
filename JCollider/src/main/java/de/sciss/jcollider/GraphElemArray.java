/*
 *  GraphElemArray.java
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
 *	elements under one brand  which
 *	can be used in UGen graph generation.
 *	The <code>GraphElemArray</code> is
 *	used in automatic multichannel expansion.
 *
 *	@see	UGen#array( GraphElem, GraphElem )
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.2, 11-Sep-05 (some countries are still at war)
 */
public class GraphElemArray
implements GraphElem
{
	private final GraphElem[] elements;

	public GraphElemArray( GraphElem[] elements )
	{
		this.elements = elements;
	}
	
	public static GraphElemArray asArray( GraphElem g )
	{
		if( g instanceof GraphElemArray ) {
			return (GraphElemArray) g;
		} else {
			return new GraphElemArray( new GraphElem[] { g });
		}
	}
	
	public GraphElem getElement( int idx )
	{
		return elements[ idx ];
	}

	public int getNumElements()
	{
		return elements.length;
	}

// ----------- GraphElem interface -----------

	public int getNumOutputs()
	{
		return elements.length;
	}
	
	public GraphElem getOutput( int idx )
	{
		return elements[ idx ];
	}
	
	public UGenInput[] asUGenInputs()
	{
		switch( getNumOutputs() ) {
		case 0:
			return new UGenInput[0];
			
		case 1:
			return getOutput( 0 ).asUGenInputs();
			
		default:
			final UGenInput[][] result = new UGenInput[ getNumOutputs() ][];
		
			for( int i = 0; i < getNumOutputs(); i++ ) {
				result[ i ] = getOutput( i ).asUGenInputs();
			}
			return flatten( result );
		}
	}

	public static UGenInput[] flatten( UGenInput[][] ins )
	{
		int numCh = 0;
		for( int i = 0; i < ins.length; i++ ) numCh += ins[ i ].length;
		
		final UGenInput[] flat = new UGenInput[ numCh ];
		
		for( int i = 0, j = 0; i < ins.length; i++ ) {
			System.arraycopy( ins[ i ], 0, flat, j, ins[ i ].length );
			j += ins[ i ].length;
		}
		
		return flat;
	}
}