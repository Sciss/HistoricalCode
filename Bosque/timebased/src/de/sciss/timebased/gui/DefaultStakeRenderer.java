package de.sciss.timebased.gui;

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
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;

import de.sciss.io.Span;
import de.sciss.timebased.RegionStake;
import de.sciss.timebased.Stake;

public class DefaultStakeRenderer
extends JComponent
implements StakeRenderer
{
	protected Rectangle					bounds;
	protected String					name;
	protected boolean					selected;
	protected Color						colr;
	protected Paint						pntHandle;
	protected Span						stakeSpan;
	protected double					hscale;
	protected Span						viewSpan;
	protected static Map				mapColrToPaint			= new HashMap();
	protected static final int			hndlExtent				= 13; // pntHndlGradientPixels.length;
	protected static final float		hndlAdd					= 28f / (hndlExtent - 1);

	protected static final Color		colrAtomB				= new Color( 0x9C, 0x40, 0x6F );
	protected static final Color		colrAtomBg				= new Color( 0xFF, 0xFF, 0xFF, 0x7F );
	protected static final Color		colrAtomS				= Color.white;
	protected static final Color		colrAtomBgS				= new Color( 0x00, 0x00, 0x00, 0xC0 );
	protected static final Color		colrAtomLab				= Color.white;
	protected static final Color		colrAtomLabS			= Color.black;

	public Component getStakeRendererComponent( TrailView tv, Stake value, boolean isSelected,
												double horizScale, Rectangle stakeBounds )
	{
		stakeSpan		= value.getSpan();
		viewSpan		= tv.getVisibleSpan();
		selected		= isSelected;
		hscale			= horizScale;
		bounds			= stakeBounds;
		name			= (value instanceof RegionStake) ? ((RegionStake) value).name : null;
		colr 			= colrAtomB;
		
		customizeRendererComponent( tv, value );

		pntHandle		= (Paint) mapColrToPaint.get( colr );
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
	
	/**
	 *	@param	tv
	 *	@param	value
	 */
	protected void customizeRendererComponent( TrailView tv, Stake value )
	{
		// nada
	}

	protected void paintInnerPart( Graphics2D g2 )
	{
		g2.setColor( selected ? colrAtomBgS : colrAtomBg );
		g2.fillRect( 1, 1 + hndlExtent, bounds.width - 2, bounds.height - hndlExtent - 2 );
	}

	public void paint( Graphics g )
	{
		final Graphics2D		g2			= (Graphics2D) g;
		final Shape				clipOrig	= g2.getClip();
		final AffineTransform	atOrig		= g2.getTransform();

		g2.translate( bounds.x, bounds.y );
		g2.clipRect( 0, 0, bounds.width, bounds.height );
		g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		
		paintInnerPart( g2 );
		
		g2.setColor( colr );
		g2.drawRect( 0, 0, bounds.width - 1, bounds.height - 1 );
		g2.setPaint( pntHandle );
		g2.fillRect( 0, 0, bounds.width, hndlExtent );
		if( selected ) {
			g2.setColor( colrAtomS );
			g2.fillRoundRect( 1, 1, bounds.width - 3, hndlExtent - 3, 6, 6 );
		}
		if( name != null ) {
			g2.setColor( selected ? colrAtomLabS : colrAtomLab );
			g2.clipRect( 1, 1, bounds.width - 2, bounds.height - 2 );
			g2.drawString( name, 4, 9 );	// 4, 9
		}
		g2.setTransform( atOrig );
		g2.setClip( clipOrig );
	}

	public Insets getInsets( Insets in, TrailView tv, Stake value )
	{
		if( in == null ) return new Insets( hndlExtent, 0, 0, 0 );
		in.top	  = hndlExtent;
		in.left   = 0;
		in.bottom = 0;
		in.right  = 0;
		return in;
	}

	public int getHitIndex( Stake s, long frame, float innerLevel,
		 Rectangle stakeBounds, MouseEvent me )
	{
		return -1;
	}
}
