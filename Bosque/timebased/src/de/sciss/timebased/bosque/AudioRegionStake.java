/*
 *  ForestAudioRegionStake.java
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
import de.sciss.swingosc.SoundFileView;
import de.sciss.timebased.Stake;

/**
 *  @version	0.12, 13-Aug-07
 *	@author		Hanns Holger Rutz
 */
public class AudioRegionStake
extends BosqueRegionStake
{
	private final SoundFileView	sfv;
	private final long			fileStartFrame;
	
	public AudioRegionStake( Span span, String name, BosqueTrack track, Color colr, Fade fadeIn, Fade fadeOut, float gain, long fileStartFrame, SoundFileView sfv )
	{
		super( span, name, track, colr, fadeIn, fadeOut, gain );
		this.sfv			= sfv;
		this.fileStartFrame	= fileStartFrame;
	}
	
	public SoundFileView getSoundFileView()
	{
		return sfv;
	}
	
	public long getFileStartFrame()
	{
		return fileStartFrame;
	}

	public AudioRegionStake( AudioRegionStake orig )
	{
		this( orig.span, orig.name, orig.track, orig.colr, orig.fadeIn, orig.fadeOut, orig.gain, orig.fileStartFrame, orig.sfv );
	}

	public Stake duplicate()
	{
		return new AudioRegionStake( this );
	}

//	public void dispose()
//	{
//		t	= null;
//	}
	
	public Stake replaceStart( long newStart )
	{
		return new AudioRegionStake( new Span( newStart, span.stop ), name, track, colr, fadeIn, fadeOut, gain, fileStartFrame + (newStart - span.start), sfv );
	}
	
	public Stake replaceStop( long newStop )
	{
		return new AudioRegionStake( new Span( span.start, newStop ), name, track, colr, fadeIn, fadeOut, gain, fileStartFrame, sfv );
	}
	
	public Stake shiftVirtual( long delta )
	{
		return new AudioRegionStake( span.shift( delta ), name, track, colr, fadeIn, fadeOut, gain, fileStartFrame, sfv );
	}
}
