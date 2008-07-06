/*
 *  DefaultStakeRenderer.java
 *  de.sciss.timebased.gui package
 *
 *  Copyright (c) 2007-2008 Hanns Holger Rutz. All rights reserved.
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
package de.sciss.timebased.gui;

import de.sciss.io.Span;
//import de.sciss.swingosc.EnvelopeView;
import de.sciss.swingosc.SoundFileView;
import de.sciss.timebased.BasicTrail;
import de.sciss.timebased.EnvSegmentStake;
import de.sciss.timebased.Fade;
import de.sciss.timebased.ForestAudioRegionStake;
import de.sciss.timebased.ForestEnvRegionStake;
import de.sciss.timebased.ForestRegionStake;
//import de.sciss.timebased.ForestTrack;
import de.sciss.timebased.RegionStake;
import de.sciss.timebased.Stake;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
//import java.awt.geom.CubicCurve2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
//import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;

/**
 *  @version	0.14, 09-Sep-07
 *	@author		Hanns Holger Rutz
 */
public class DefaultStakeRenderer
extends JComponent
implements StakeRenderer
{
	protected final Rectangle			bounds					= new Rectangle();
	protected String					name;
	
//	private static final Color			colrAtom				= new Color( 0x60, 0x00, 0x80, 0x40 );
	private static final Color			colrAtomB				= new Color( 0x9C, 0x40, 0x6F );
	private static final Color			colrAtomBg				= new Color( 0xFF, 0xFF, 0xFF, 0x7F );
//	private static final Color			colrAtomBgS				= new Color( 0xA0, 0xA0, 0xFF, 0x7F );
	private static final Color			colrAtomBgS				= new Color( 0x00, 0x00, 0x00, 0xC0 );
	private static final Color			colrAtomS				= Color.white;
//	private static final Color			colrAtomSel				= new Color( 0x40, 0x00, 0x60, 0x80 );
//	private static final Color			colrAtomBSel			= new Color( 0x40, 0x00, 0x60, 0xC0 );
	private static final Color			colrAtomLab				= Color.white;
	private static final Color			colrAtomLabS			= Color.black;
	private static final Color			colrWave				= Color.black;
	private static final Color			colrWaveS				= Color.white;
//	private static final Color			colrMuted				= new Color( 0x60, 0x60, 0x60, 0x60 );
//	private static final Color			colrMutedSel			= new Color( 0x60, 0x60, 0x60, 0xC0 );

//	private static final int[]			pntHndlGradientPixels	= {
//		0xFFB8588B, 0xFFB7568A, 0xFFB6578A, 0xFFB45588,
//		0xFFB25385, 0xFFAE5082, 0xFFAC4E7E, 0xFFA84B7B,
//		0xFFA64879, 0xFFA24575, 0xFFA04373, 0xFF9D4271,
//		0xFF9C406F };
	private static final int			hndlExtent				= 13; // pntHndlGradientPixels.length;
	private static final float			hndlAdd					= 28f / (hndlExtent - 1);

//	private static Paint				pntHandle;
	private Color						colr;
	private Paint						pntHandle;
	
	private static Map					mapColrToPaint			= new HashMap();
	
	private boolean						fadeIn, fadeOut;
//	private final CubicCurve2D			curveFadeIn				= new CubicCurve2D.Float();
//	private final CubicCurve2D			curveFadeOut			= new CubicCurve2D.Float();
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
	
	private boolean						selected;

	private float						yZoom					= 1f;

	private Span						viewSpan;
	private Span						stakeSpan;
	private double						hscale;
//	private double						vscale;

	// env
	private final Line2D				envLine					= new Line2D.Double();
	private final Rectangle2D			envKnob					= new Rectangle2D.Double();

	static {
		final BufferedImage img;
		img		= new BufferedImage( 4, 2, BufferedImage.TYPE_INT_ARGB );
		img.setRGB( 0, 0, 4, 2, pntFadePixels, 0, 4 );
		pntFade	= new TexturePaint( img, new Rectangle( 0, 0, 4, 2 ));
	}

	public DefaultStakeRenderer()
	{
		super();
		
//		ggSF = new SoundFileView();
//		ggSF.setGridPainted( false );
//		ggSF.setTimeCursorPainted( false );
//		ggSF.setBackground( colrAtomBg );
//		ggSF.setWaveColors( new Color[] { Color.black, Color.black });
//		try {
//			ggSF.readSndFile( "/Users/rutz/scwork/ping/tapes1/msg3.aif", 0, 9966600 );
//		}
//		catch( IOException e1 ) {
//			e1.printStackTrace( System.out );
//		}
//		ggEnv = new EnvelopeView();
//		ggEnv.setClipThumbs( true );
//		ggEnv.setDrawLines( true );
//		ggEnv.setThumbSize( -1, 4f );
//		ggEnv.setStrokeColor( new Color( 0xA0, 0x00, 0xD0 ));
//		ggEnv.setFillColor( -1, new Color( 0xA0, 0x00, 0xD0 ));
//		ggEnv.setBackgroundPainted( false );
//		ggEnv.setBorder( null );
//		ggEnv.setValues( new float[] { 0f, 0.3f, 0.7f, 1f }, new float[] { 0f, 1f, 0.7f, 0f });
	}
	
	public void setYZoom( float val )
	{
		yZoom = val;
	}
	
	public Component getStakeRendererComponent( TrailView tv, Stake value, boolean selected, double hscale, float vscale )
	{
		stakeSpan		= value.getSpan();
		viewSpan		= tv.getVisibleSpan();
		sfv				= null;
		env				= null;
		this.selected	= selected;
		this.hscale		= hscale;
//		this.vscale		= vscale;
		
		bounds.setBounds( 
			(int) ((stakeSpan.start - viewSpan.start) * hscale + 0.5),
			(int) 0, // (a.getY() * h + 0.5),
			(int) (stakeSpan.getLength() * hscale + 0.5),
			(int) tv.getHeight() ); // (a.getHeight() * h + 0.5) );
		
//		System.out.println( "Bounds = " + bounds );
		
		name = (value instanceof RegionStake) ? ((RegionStake) value).name : null;
		
		if( value instanceof ForestRegionStake ) {
			final ForestRegionStake frs = (ForestRegionStake) value;
			bounds.y 		= (int) (frs.track.y * vscale + 0.5f) + 1;
			bounds.height	= (int) (frs.track.height * vscale + 0.5f) - 2;
			colr = (frs.colr != null) ? frs.colr : colrAtomB;
			fadeIn = frs.fadeIn != null;
			if( fadeIn ) {
				shpFillFadeIn.reset();
				final float px = (float) (frs.fadeIn.numFrames * hscale);
				if( frs.fadeIn.type == Fade.TYPE_LINEAR ) {
//					curveFadeIn.setCurve( 1, bounds.height - 1, 1, hndlExtent, 1, hndlExtent, px, hndlExtent ); // x1, y1, ctrlx, ctrly, x2, y2
//					shpFadeIn.append( curveFadeIn, false );
					lineFadeIn.setLine( 0, bounds.height - 1, px, hndlExtent );
					shpFillFadeIn.append( lineFadeIn, false );
					shpFillFadeIn.lineTo( 0, hndlExtent );
					shpDrawFadeIn = lineFadeIn;
				}
			}
			fadeOut = frs.fadeOut != null;
			if( fadeOut ) {
				shpFillFadeOut.reset();
				final float px = (float) (frs.fadeOut.numFrames * hscale);
				if( frs.fadeOut.type == Fade.TYPE_LINEAR ) {
//					curveFadeIn.setCurve( 1, bounds.height - 1, 1, hndlExtent, 1, hndlExtent, px, hndlExtent ); // x1, y1, ctrlx, ctrly, x2, y2
//					shpFadeIn.append( curveFadeIn, false );
					lineFadeOut.setLine( bounds.width - 1, bounds.height - 1, bounds.width - 1 - px, hndlExtent );
					shpFillFadeOut.append( lineFadeOut, false );
					shpFillFadeOut.lineTo( bounds.width - 1, hndlExtent );
					shpDrawFadeOut = lineFadeOut;
				}
			}
			
whichRegion:
			if( frs instanceof ForestEnvRegionStake ) {
				env	= ((ForestEnvRegionStake) frs).getEnv();
				
			} else if( frs instanceof ForestAudioRegionStake ) {
				final ForestAudioRegionStake fars = (ForestAudioRegionStake) value;
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
//System.out.println( "sfvx " + sfvx + ", sfvw " + sfvw + ", viewSpan " + viewSpan + ", stakeSpan " + stakeSpan + ", sfvSpanV " + sfvSpanV + ", sfvSpanF " + sfvSpanF );
				}
			}
//			in = ggEnv.getInsets();
//			ggEnv.setBounds( 1 - in.left, hndlExtent - in.top, bounds.width - 2 + (in.left + in.right), bounds.height/2 - hndlExtent - 1 + (in.top + in.bottom) );
			
		} else {
			colr = colrAtomB;
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

		return this;
	}
	
	public void paint( Graphics g )
	{
		final Graphics2D		g2			= (Graphics2D) g;
		final Shape				clipOrig	= g2.getClip();
		final AffineTransform	atOrig		= g2.getTransform();
		
		g2.translate( bounds.x, bounds.y );
		g2.clipRect( 0, 0, bounds.width, bounds.height );
		g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

//		ar			= new AtomRect( a, bounds.x, bounds.y, bounds.width, bounds.height, selected );
//		collAtomRects.add( ar );
//				
		if( sfv == null ) {
			g2.setColor( selected ? colrAtomBgS : colrAtomBg );
			g2.fillRect( 1, 1 + hndlExtent, bounds.width - 2, bounds.height - hndlExtent - 2 );
			if( env != null ) {
				paintEnv( g2 );
			}
		} else {
			g2.translate( sfv.getX(), sfv.getY() );
			sfv.paintComponent( g2 );
			g2.translate( -sfv.getX(), -sfv.getY() );
		}
//		
//g2.translate( ggEnv.getX(), ggEnv.getY() );
//ggEnv.paintComponent( g2 );
//g2.translate( -ggEnv.getX(), -ggEnv.getY() );
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

		g2.setColor( colr );
		g2.drawRect( 0, 0, bounds.width - 1, bounds.height - 1 );
		g2.setPaint( pntHandle );
		g2.fillRect( 0, 0, bounds.width, hndlExtent );
		if( selected ) {
			g2.setColor( colrAtomS );
			g2.fillRoundRect( 1, 1, bounds.width - 3, hndlExtent - 3, 6, 6 );
		}
		
		if( name != null ) {
//			final Shape clipOrig = g2.getClip();
			g2.setColor( selected ? colrAtomLabS : colrAtomLab );
			g2.clipRect( 1, 1, bounds.width - 2, bounds.height - 2 );
			g2.drawString( name, 4, 9 );	// 4, 9
//			g2.setClip( clipOrig );
		}
		
		g2.setTransform( atOrig );
		g2.setClip( clipOrig );
	}
	
	private void paintEnv( Graphics2D g2 )
	{
		final Span	insideSpan	= viewSpan.intersection( stakeSpan ).shift( -stakeSpan.start );
		final int	numStakes	= env.getNumStakes();
		boolean		drawStart	= true;
		int idx = env.indexOf( insideSpan.start, true );
		if( idx < 0 ) idx = Math.max( 0, -(idx + 2) );
		
		g2.setColor( selected ? Color.white : Color.black );
		
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
				envKnob.setRect( envLine.getX1() - 2, envLine.getY1() - 2, 5, 5 );
				g2.fill( envKnob );
				drawStart = false;
			}
			envKnob.setRect( envLine.getX2() - 2, envLine.getY2() - 2, 5, 5 );
			g2.fill( envKnob );
		}
	}
}
