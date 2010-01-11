/*
 *  DefaultStakeRenderer.java
 *  de.sciss.timebased.gui package
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

import de.sciss.io.Span;
import de.sciss.swingosc.SoundFileView;
import de.sciss.timebased.BasicTrail;
import de.sciss.timebased.Stake;
import de.sciss.timebased.Trail;
import de.sciss.timebased.gui.DefaultStakeRenderer;
import de.sciss.timebased.gui.TrailView;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

/**
 *  @version	0.14, 09-Sep-07
 *	@author		Hanns Holger Rutz
 */
public class BosqueStakeRenderer
extends DefaultStakeRenderer
{
	private static final Color			colrWave				= Color.black;
	private static final Color			colrWaveS				= Color.white;

	private boolean						fadeIn, fadeOut;
	private final Line2D				lineFadeIn				= new Line2D.Float();
	private final Line2D				lineFadeOut				= new Line2D.Float();
	private final GeneralPath			shpFillFadeIn			= new GeneralPath(); 
	private final GeneralPath			shpFillFadeOut			= new GeneralPath();
	private Shape						shpDrawFadeIn;
	private Shape						shpDrawFadeOut;
	
	private static final int[]			pntFadePixels	= {
		0xFF05AF3A, 0x00000000, 0x00000000, 0x00000000,
		0x00000000, 0x00000000, 0xFF05AF3A, 0x00000000
	};
	private static final Paint			pntFade;
	private static final Color			colrFade				= new Color( 0x05, 0xAF, 0x3A );
	
	private SoundFileView				sfv;
	private BasicTrail					env;

	private float						yZoom					= 1f;

	// env
	private final Line2D				envLine					= new Line2D.Double();
	private final Rectangle2D			envKnob					= new Rectangle2D.Double();
	private static final int			envKnobExtent			= 5;
	
	static {
		final BufferedImage img;
		img		= new BufferedImage( 4, 2, BufferedImage.TYPE_INT_ARGB );
		img.setRGB( 0, 0, 4, 2, pntFadePixels, 0, 4 );
		pntFade	= new TexturePaint( img, new Rectangle( 0, 0, 4, 2 ));
	}

	public BosqueStakeRenderer()
	{
		super();
	}
	
	public void setYZoom( float val )
	{
		yZoom = val;
	}
	
	public int getHitIndex( Stake s, long frame, float innerLevel,
		 Rectangle stakeBounds, MouseEvent me )
	{
		if( !((me.getID() == MouseEvent.MOUSE_PRESSED) &&
		      s instanceof EnvRegionStake) ) {
			return super.getHitIndex( s, frame, innerLevel, stakeBounds, me );
		}
		
		final EnvRegionStake ers = (EnvRegionStake) s;
		final Trail		t		= ers.getEnv();
		frame -= ers.getSpan().start;
		final double	hScal	= (double) ers.getSpan().getLength() / stakeBounds.width;
//		final Span		checka	= new Span( frame - (long) (2 * hScal), frame + (long) (2 * hScal) );
//		final List		checkaL	= t.getRange( checka, true );
		int				startIdx = t.indexOf( frame - (long) (3 * hScal), true );
		if( startIdx < 0 ) startIdx = Math.max( 0, -(startIdx + 2) );
		int				stopIdx	=  t.indexOf( frame + (long) (3 * hScal), true );
		if( stopIdx < 0 ) stopIdx = -(stopIdx + 2);
		final float		vScal	= 1.0f / stakeBounds.height;
//System.out.println( "checkin points " + startIdx + " thru " + stopIdx + "; hScal " + hScal + " (since length = " + ers.getSpan().getLength() + "; and width = " + stakeBounds.width + ") ; vScal " + vScal );
		
		double d1, startCost, stopCost = 0.0;
		double bestCost = 20; // bit more than 3*3 + 3*3
		float f1;
		int hitIdx = -1;

//System.out.println( "frame is " + frame + "; innerLevel is " + innerLevel );
		                    
		for( int i = startIdx; i <= stopIdx; i++ ) {
			final EnvSegmentStake ess = (EnvSegmentStake) t.get( i, true );
//System.out.println( "index " + i + "; ess.span = " + ess.getSpan() );
			if( i == startIdx ) {
				d1 = (ess.getSpan().start - frame) / hScal;
				f1 = (ess.getStartLevel() - innerLevel) / vScal;
//System.out.println( "  start d1 = " + d1 + "; f1 = " + f1 );
				startCost = d1 * d1 + f1 * f1;
			} else {
				startCost = stopCost;
			}
			d1 = (ess.getSpan().stop - frame) / hScal;
			f1 = (ess.getStopLevel() - innerLevel) / vScal;
//System.out.println( "  stop d1 = " + d1 + "; f1 = " + f1 );
			stopCost = d1 * d1 + f1 * f1;
			if( startCost < stopCost ) {
				if( startCost < bestCost ) {
//System.out.println( " => startCost = " + startCost );
					hitIdx = i;
					bestCost = startCost;
				}
			} else {
				if( stopCost < bestCost ) {
//System.out.println( " => stopCost = " + stopCost );
					hitIdx = i + 1;
					stopCost = startCost;
				}
			}
		}

		return hitIdx;
	}
	
	protected void customizeRendererComponent( TrailView tv, Stake value )
	{
		sfv		= null;
		env		= null;
		
		if( value instanceof BosqueRegionStake ) {
			final BosqueRegionStake bsr = (BosqueRegionStake) value;
			
			colr = (bsr.colr != null) ? bsr.colr : colrAtomB;
			fadeIn = bsr.fadeIn != null;
			if( fadeIn ) {
				shpFillFadeIn.reset();
				final float px = (float) (bsr.fadeIn.numFrames * hscale);
				if( bsr.fadeIn.type == Fade.TYPE_LINEAR ) {
					lineFadeIn.setLine( 0, bounds.height - 1, px, hndlExtent );
					shpFillFadeIn.append( lineFadeIn, false );
					shpFillFadeIn.lineTo( 0, hndlExtent );
					shpDrawFadeIn = lineFadeIn;
				}
			}
			fadeOut = bsr.fadeOut != null;
			if( fadeOut ) {
				shpFillFadeOut.reset();
				final float px = (float) (bsr.fadeOut.numFrames * hscale);
				if( bsr.fadeOut.type == Fade.TYPE_LINEAR ) {
					lineFadeOut.setLine( bounds.width - 1, bounds.height - 1, bounds.width - 1 - px, hndlExtent );
					shpFillFadeOut.append( lineFadeOut, false );
					shpFillFadeOut.lineTo( bounds.width - 1, hndlExtent );
					shpDrawFadeOut = lineFadeOut;
				}
			}
			
whichRegion:
			if( bsr instanceof EnvRegionStake ) {
				env	= ((EnvRegionStake) bsr).getEnv();
				
			} else if( bsr instanceof AudioRegionStake ) {
				final AudioRegionStake fars = (AudioRegionStake) value;
				final Span spanClip = stakeSpan.intersection( viewSpan );
				final Span sfvSpanV = spanClip.shift( -viewSpan.start );
				final Span sfvSpanF = spanClip.shift( fars.getFileStartFrame() - stakeSpan.start );
				final int sfvw = (int) (sfvSpanV.getLength() * hscale + 0.5) ;
				final int sfvh = bounds.height - hndlExtent - 1;
				if( (sfvw > 0) && (sfvh > 0) ) {	// currently error with 0 size in SoundFileView !
					sfv = fars.getSoundFileView();
					if( sfv == null ) break whichRegion;
					final Insets in = sfv.getInsets();
					sfv.setBounds( (int) (sfvSpanV.start * hscale + 0.5) - in.left - bounds.x,
								   hndlExtent - in.top,
								   sfvw + (in.left + in.right),
								   sfvh + (in.top + in.bottom ));
					sfv.setViewSpan( sfvSpanF );
					sfv.setBackground( selected ? colrAtomBgS : colrAtomBg );
					sfv.setWaveColor( selected ? colrWaveS : colrWave );
					sfv.setYZoom( fars.gain * yZoom );
				}
			}
//		} else {
//			colr = colrAtomB;
		}
		
		pntHandle = (Paint) mapColrToPaint.get( colr );
		if( pntHandle == null ) {
			final BufferedImage img2;
			final int[]			pntHndlGradientPixels;
			float				add;
			pntHndlGradientPixels = new int[ hndlExtent ];
			for( int i = 0; i < hndlExtent; i++ ) {
				add = i * hndlAdd;
				pntHndlGradientPixels[ i ] = 0xFF000000 |
					(Math.min( 0xFF, (int) (colr.getRed() + add)) << 16) |
					(Math.min( 0xFF, (int) (colr.getGreen() + add)) << 8) |
					Math.min( 0xFF, (int) (colr.getBlue() + add));
			}
			img2		= new BufferedImage( 1, hndlExtent, BufferedImage.TYPE_INT_ARGB );
			img2.setRGB( 0, 0, 1, hndlExtent, pntHndlGradientPixels, 0, 1 );
			pntHandle	= new TexturePaint( img2, new Rectangle( 0, 0, 1, hndlExtent ));
			mapColrToPaint.put( colr, pntHandle );
		}
	}
	
	protected void paintInnerPart( Graphics2D g2 )
	{
		if( sfv == null ) {
			g2.setColor( selected ? colrAtomBgS : colrAtomBg );
			g2.fillRect( 1, 1 + hndlExtent, bounds.width - 2, bounds.height - hndlExtent - 2 );
			if( env != null ) {
				g2.setColor( selected ? Color.white : Color.black );
				paintEnv( g2 );
			}
		} else {
			g2.translate( sfv.getX(), sfv.getY() );
			sfv.paintComponent( g2 );
			g2.translate( -sfv.getX(), -sfv.getY() );
		}
		if( fadeIn ) {
			g2.setPaint( pntFade );
			g2.fill( shpFillFadeIn );
			g2.setColor( colrFade );
			g2.draw( shpDrawFadeIn );
		}
		if( fadeOut ) {
			g2.setPaint( pntFade );
			g2.fill( shpFillFadeOut );
			g2.setColor( colrFade );
			g2.draw( shpDrawFadeOut );
		}
	}
	
	private void paintEnv( Graphics2D g2 )
	{
		final Span	insideSpan	= viewSpan.intersection( stakeSpan ).shift( -stakeSpan.start );
		final int	numStakes	= env.getNumStakes();
		final int	envNegRad	= -envKnobExtent / 2;
		boolean		drawStart	= true;
		int idx = env.indexOf( insideSpan.start, true );
		if( idx < 0 ) idx = Math.max( 0, -(idx + 2) );
				
		for( ; idx < numStakes; idx++ ) {
			final EnvSegmentStake	segm		= (EnvSegmentStake) env.get( idx, true );
			final Span				segmSpan	= segm.getSpan();
			final int				vscale2		= bounds.height - hndlExtent;
			if( segmSpan.start >= insideSpan.stop ) return;
			
			// XXX shape
			envLine.setLine( hscale * segmSpan.start,
			                 vscale2 * (1f - segm.getStartLevel() ) + hndlExtent,
			                 hscale * segmSpan.stop,
			                 vscale2 * (1f - segm.getStopLevel() ) + hndlExtent );
			g2.draw( envLine );
			if( drawStart ) {
				envKnob.setRect( envLine.getX1() + envNegRad, envLine.getY1() + envNegRad, envKnobExtent, envKnobExtent );
				g2.fill( envKnob );
				drawStart = false;
			}
			envKnob.setRect( envLine.getX2() + envNegRad, envLine.getY2() + envNegRad, envKnobExtent, envKnobExtent );
			g2.fill( envKnob );
		}
	}
}
