/*
 *  ForestEnvRegionStake.java
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
import java.io.IOException;

import de.sciss.io.Span;
import de.sciss.timebased.BasicTrail;
import de.sciss.timebased.RegionTrail;
import de.sciss.timebased.Stake;
import de.sciss.timebased.Trail;

/**
 *  @version	0.10, 06-Jul-08
 *	@author		Hanns Holger Rutz
 */
public class EnvRegionStake
extends BosqueRegionStake
implements Trail.Listener
{
	private final BasicTrail env;  // actually BasicTrail<EnvSegmentStake>
	private RegionTrail t;
	
	public EnvRegionStake( Span span, String name, BosqueTrack track, Color colr, Fade fadeIn, Fade fadeOut, float gain, BasicTrail env )
	{
		super( span, name, track, colr, fadeIn, fadeOut, gain );
		this.env	= env;
		
		env.addListener( this );
	}

	public void trailModified( Trail.Event e )
	{
//		System.out.println( " YOOOO " + t + "; "+ e.getSource() + "; " + e.getAffectedSpan().shift( span.start ));
		if( t != null ) t.modified( e.getSource(), e.getAffectedSpan().shift( span.start ));
	}
	
	public void dispose()
	{
		env.removeListener( this );
		t = null;
		super.dispose();
	}

	public void setRegionTrail( RegionTrail t )
	{
		super.setTrail( t );
		this.t = t;
	}

	public EnvRegionStake( EnvRegionStake orig )
	{
		this( orig.span, orig.name, orig.track, orig.colr, orig.fadeIn, orig.fadeOut, orig.gain, orig.getEnvCopy() );
	}
	
	public BasicTrail getEnv()
	{
		return env;
	}
	
	private BasicTrail getEnvCopy()
	{
		final BasicTrail envCopy = env.createEmptyCopy();
		try {
			envCopy.addAll( null, env.getAll( true ));
		}
		catch( IOException e1 ) {	// never happens
			e1.printStackTrace();
		}
		return envCopy;
	}

	public Stake duplicate()
	{
		return new EnvRegionStake( this );
	}

//	public void dispose()
//	{
//		t	= null;
//	}
	
	public Stake replaceStart( long newStart )
	{
		final long			delta	= newStart - span.start;
		final Span			spanNew	= new Span( newStart, span.stop );
		final BasicTrail	envNew	= env.createEmptyCopy();
		try {
			envNew.addAll( null, env.getCuttedRange( new Span( delta, env.getSpan().stop ), true, Trail.TOUCH_SPLIT, -delta ));
		}
		catch( IOException e1 ) {	// never happens
			e1.printStackTrace();
		}
		return new EnvRegionStake( spanNew, name, track, colr, fadeIn, fadeOut, gain, envNew );
	}
	
	public Stake replaceStop( long newStop )
	{
		final Span			spanNew	= new Span( span.start, newStop );
		final BasicTrail	envNew	= env.createEmptyCopy();
		try {
			envNew.addAll( null, env.getCuttedRange( new Span( 0, spanNew.getLength() ), true, Trail.TOUCH_SPLIT, 0 ));
		}
		catch( IOException e1 ) {	// never happens
			e1.printStackTrace();
		}
		return new EnvRegionStake( spanNew, name, track, colr, fadeIn, fadeOut, gain, envNew );
	}
	
	public Stake shiftVirtual( long delta )
	{
		return new EnvRegionStake( span.shift( delta ), name, track, colr, fadeIn, fadeOut, gain, getEnvCopy() );
	}
}
