/*
 *  PanelPainters.scala
 *  (DorianLaF)
 *
 *  Copyright (c) 2010 Hanns Holger Rutz. All rights reserved.
 *
 *	 This software is free software; you can redistribute it and/or
 *	 modify it under the terms of the GNU General Public License
 *	 as published by the Free Software Foundation; either
 *	 version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	 This software is distributed in the hope that it will be useful,
 *	 but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	 General Public License for more details.
 *
 *	 You should have received a copy of the GNU General Public
 *	 License (gpl.txt) along with this software; if not, write to the Free Software
 *	 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	 For further information, please contact Hanns Holger Rutz at
 *	 contact@sciss.de
 *
 *
 *  Changelog:
 */

package de.sciss.dorianlaf

import javax.swing.plaf.synth.{SynthContext, SynthPainter}
import javax.swing.UIManager
import javax.imageio.ImageIO
import java.awt._
import image.BufferedImage
import com.jhlabs.composite.OverlayComposite

object PanelBackgroundPainter {
   private def imgARGB( name: String ) = {
      val imgGray = ImageIO.read( PanelBackgroundPainter.getClass.getResourceAsStream( name ))
      val w = imgGray.getWidth
      val h = imgGray.getHeight
//      val imgARGB = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
//         .getDefaultConfiguration().createCompatibleImage( w, h )
      val imgARGB = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB )  // XXX required by the jhlabs composites
//val imgARGB = imgGray
      val g = imgARGB.createGraphics()
      g.drawImage( imgGray, 0, 0, null )
      g.dispose()
      imgARGB
   }

   private def imgTexture( name: String ) = {
      val img = imgARGB( name )
      new TexturePaint( img, new Rectangle( 0, 0, img.getWidth(), img.getHeight() ))
   }

   lazy val pntCanvas   = imgTexture( "canvas.png" )
   lazy val pntVertical = imgTexture( "verticalk.png" )
   lazy val imgGlow     = imgARGB(    "glow2.png" )
   lazy val pntLCD      = imgTexture( "lcd.png" )
   lazy val pntLCDDark  = imgTexture( "lcd_dark.png" )
   lazy val pntKnob     = imgTexture( "knob2.png" )
   lazy val imgLEDs     = imgARGB(    "leds_l.png" )
   lazy val pntLEDs     = imgTexture( "leds_r.png" )

   lazy val cmpCanvas : Composite = new ApplyCanvas( 64, 1 )
//   lazy val cmpCanvas : Composite = new OverlayComposite( 0.25f )
}

class PanelBackgroundPainter extends SynthPainter {
   import PanelBackgroundPainter._
   
   override def paintPanelBackground( context: SynthContext, g: Graphics, x: Int, y: Int, w: Int, h: Int ) {
      val start   = UIManager.getColor( "Panel.startBackground" )
      val end     = UIManager.getColor( "Panel.endBackground" )
      val g2      = g.asInstanceOf[ Graphics2D ]
      val pntGrad = new GradientPaint( x, y, start, w, h, end )
      g2.setPaint( pntGrad )
      g2.fillRect( x, y, w, h )

      g2.setPaint( pntCanvas )
      val cmpOrig = g2.getComposite
      g2.setComposite( cmpCanvas )
      g2.fillRect( x, y, w, h )
      g2.setComposite( cmpOrig )

//      g2.setPaint( null )
//      g2.setColor( new Color( 255, 255, 255, 120 ))
//      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )
//      val arc2d   = new CubicCurve2D.Double( 0, h/4, w/3, h/10, 66 * w, 1.5 * h, w, h/8 )
//      g2.draw( arc2d )
//      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF )
   }
}