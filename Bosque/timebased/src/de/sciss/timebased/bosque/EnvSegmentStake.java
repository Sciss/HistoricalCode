/*
 *  EnvSegmentStake.java
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
 *		06-Jul-08	created
 */

package de.sciss.timebased.bosque;

import de.sciss.io.Span;
import de.sciss.timebased.BasicStake;
import de.sciss.timebased.Stake;

/**
 * 	Segment of an envelope (breakpoint function). Note that
 * 	we decided to use regions over markers for each break-"point",
 * 	since this way painting and cutting operations can be performed
 * 	on the bottom level which makes this thing much more elegant
 * 	despite the little redundancy.
 * 
 *	@version	0.10, 06-Jul-08
 *	@author		Hanns Holger Rutz
 */
public class EnvSegmentStake
extends BasicStake
{
	private final float	startLevel;
	private final float	stopLevel;
	private final int	shape;
	private final float	curve;
		
	public EnvSegmentStake( Span span, float startLevel, float stopLevel, int shape, float curve )
	{
		super( span );
		this.startLevel	= startLevel;
		this.stopLevel	= stopLevel;
		this.shape		= shape;
		this.curve		= curve;
	}
	
	public EnvSegmentStake( EnvSegmentStake orig )
	{
		this( orig.span, orig.startLevel, orig.stopLevel, orig.shape, orig.curve );
	}

	public float getStartLevel()
	{
		return startLevel;
	}
	
	public float getStopLevel()
	{
		return stopLevel;
	}
	
	public int getShape()
	{
		return shape;
	}
	
	public float getCurve()
	{
		return curve;
	}
	
	public Stake duplicate()
	{
		return new EnvSegmentStake( this );
	}

	public Span	getSpan()
	{
		return span;
	}
	
	// XXX shape
	public float levelAt( long pos )
	{
		final double w = (double) (pos - span.start) / span.getLength();
		return (float) (startLevel + (stopLevel - startLevel) * w);
	}
	
	public Stake replaceStart( long newStart )
	{
		final float newStartLevel = levelAt( newStart );
		return new EnvSegmentStake( new Span( newStart, span.stop ), newStartLevel, stopLevel, shape, curve );
	}
	
	public Stake replaceStop( long newStop )
	{
		final float newStopLevel = levelAt( newStop );
		return new EnvSegmentStake( new Span( span.start, newStop ), startLevel, newStopLevel, shape, curve );
	}
	
	public Stake shiftVirtual( long delta )
	{
		return new EnvSegmentStake( span.shift( delta ), startLevel, stopLevel, shape, curve );
	}
}