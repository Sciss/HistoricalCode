/*
 *  ForestRegionStake.java
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

package de.sciss.timebased.bosque;

import java.awt.Color;

import de.sciss.io.Span;
import de.sciss.timebased.RegionStake;
import de.sciss.timebased.Stake;
import de.sciss.timebased.session.SessionObject;
import de.sciss.timebased.session.Track;
import de.sciss.timebased.session.TrackBased;
import de.sciss.util.MapManager;

public class BosqueRegionStake
extends RegionStake
implements SessionObject, TrackBased
{
	protected final BosqueTrack	track;
	public final Color	 		colr;
	public final Fade			fadeIn;
	public final Fade			fadeOut;
	public final float			gain;
	
	private final MapManager	map;
	
	public BosqueRegionStake( Span span, String name, BosqueTrack track, Color colr, Fade fadeIn, Fade fadeOut, float gain )
	{
		super( span, name );
		
//System.out.println( "Track = " + track );
		this.track		= track;
		this.colr		= colr;
		this.fadeIn		= fadeIn;
		this.fadeOut	= fadeOut;
		this.gain		= gain;
		
		// shit this doesn't make senses, a stake
		// is considered immutable, a session object
		// not...
		map				= new MapManager( this );
	}
	
	public BosqueRegionStake( BosqueRegionStake orig )
	{
		this( orig.span, orig.name, orig.track, orig.colr, orig.fadeIn, orig.fadeOut, orig.gain );
	}
	
	public Track getTrack() { return track; }

	public Stake duplicate()
	{
		return new BosqueRegionStake( this );
	}

//	public void dispose()
//	{
//		t	= null;
//	}
	
	public String getName()
	{
		return name;
	}
	
	public MapManager getMap()
	{
		return map;
//		return null; // XXX woooo, not so good
	}
	
	public Stake replaceStart( long newStart )
	{
		return new BosqueRegionStake( new Span( newStart, span.stop ), name, track, colr, fadeIn, fadeOut, gain );
	}
	
	public Stake replaceStop( long newStop )
	{
		return new BosqueRegionStake( new Span( span.start, newStop ), name, track, colr, fadeIn, fadeOut, gain );
	}
	
	public Stake shiftVirtual( long delta )
	{
		return new BosqueRegionStake( span.shift( delta ), name, track, colr, fadeIn, fadeOut, gain );
	}
}
