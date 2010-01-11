/*
 *  RegionTrail.java
 *  de.sciss.timebased package
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
 */
package de.sciss.timebased;

import java.io.IOException;
import java.util.Arrays;

import de.sciss.io.Span;

public class RegionTrail
extends BasicTrail
{
//	private final Object oscSource = new Object();
	
	public int getDefaultTouchMode()
	{
		return TOUCH_SPLIT;
	}
	
	public BasicTrail createEmptyCopy()
	{
		return new RegionTrail();
	}
	
	public BasicTrail duplicate()
	{
		final BasicTrail dup = createEmptyCopy();
		dup.editGetCollByStart( null ).addAll( getAll( true ));
		dup.editGetCollByStop( null ).addAll( getAll( false ));
		return dup;
	}
	
	public void modified( Object source, Span span )
	{
		dispatchModification( source, span );
	}

//	public void modified( Span span )
//	{
//		dispatchModification( oscSource, span );
//	}

	public void addAll( Object source, Object[] stakes )
	throws IOException
	{
//		System.out.println( "HERE ");
//		try {
			addAll( source, Arrays.asList( stakes ));
//		}
//		catch( Exception e1 ) {
//			e1.printStackTrace( System.out );
//		}
//		System.out.println( "JOJO ");
	}

	public void removeAll( Object source, Object[] stakes )
	throws IOException
	{
		removeAll( source, Arrays.asList( stakes ));
	}
}
