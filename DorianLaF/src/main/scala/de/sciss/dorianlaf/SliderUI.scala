/*
 *  SliderUI.scala
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

import javax.swing.plaf.basic.BasicSliderUI
import javax.swing.plaf.ComponentUI
import javax.swing.{SwingConstants, LookAndFeel, JSlider, JComponent}
import java.awt._
import com.jhlabs.composite.{HardLightComposite, OverlayComposite, PinLightComposite}
import geom.{Ellipse2D, Rectangle2D, Area}

object SliderUI {
   def createUI( c: JComponent ) : ComponentUI = new SliderUI( c.asInstanceOf[ JSlider ])
}
class SliderUI( s: JSlider ) extends BasicSliderUI( s ) with ThumbPainter {
   override protected def installDefaults( s: JSlider ) {
      super.installDefaults( s )
      LookAndFeel.installProperty( s, "opaque", java.lang.Boolean.FALSE )
   }

   override def paintTrack( g: Graphics )  {
      val r       = trackRect
      val g2      = g.asInstanceOf[ Graphics2D ]
      g2.setColor( new Color( 0x00, 0x00, 0x00, 0x7F ))
      val atOrig  = g2.getTransform()

      if( slider.getOrientation() == SwingConstants.HORIZONTAL ) {
         val x    = r.x
         val y    = r.y + (r.height - 8) / 2
         val w    = r.width
         val xv   = xPositionForValue( slider.getValue() )
//         val xv   = thumbRect.x + thumbRect.width/2

         g2.drawRect( x - 1, y, w + 1, 7 )
         g2.drawImage( PanelBackgroundPainter.imgLEDs, x, y + 1, slider )
         if( xv > 16 ) {
            g2.translate( x + 16, y + 1 )
            g2.setPaint( PanelBackgroundPainter.pntLEDs )
            g2.fillRect( 0, 0, xv - 16, 6 )
//            g2.translate( -(x + 16), -(y + 1) )
            g2.setTransform( atOrig )
         }
         val bw = x + w - xv
         if( bw > 0 ) {
            g2.setColor( Color.black )
            g2.fillRect( xv, y, bw, 6 )
         }

      } else {
         val cx = (r.width / 2) - 2
         val ch = r.height

         g.translate( r.x + cx, r.y )

         g.setColor( getShadowColor() )
         g.drawLine( 0, 0, 0, ch - 1 )
         g.drawLine( 1, 0, 2, 0 )
         g.setColor( getHighlightColor() )
         g.drawLine( 3, 0, 3, ch )
         g.drawLine( 0, ch, 3, ch )
         g.setColor( Color.black )
         g.drawLine( 1, 1, 1, ch-2 )

//         g.translate( -(r.x + cx), -r.y )
      }

      g2.setTransform( atOrig )
   }

   override def paintThumb( g: Graphics ) {
      val r       = thumbRect
      val g2      = g.asInstanceOf[ Graphics2D ]
      val w       = r.width - 8
      val h       = r.height - 6
      val aaOrig  = g2.getRenderingHint( RenderingHints.KEY_ANTIALIASING )
      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )

      val ew      = 40
// wooops, accidentally created the paintThumbArrowShape version here
//      val outline = new Area( new Ellipse2D.Float( 3, 3 - lulu/2, lulu, lulu ))
//      outline.intersect( new Area( new Ellipse2D.Float( 3 + w - lulu, 3 - lulu/2, lulu, lulu )))
//      outline.intersect( new Area( new Rectangle( 3, 3, w, h ))) // XXX
      val y0 = 3 + (h - ew) * 0.5f
      val outline = new Area( new Ellipse2D.Float( 4, y0, ew, ew ))
      outline.intersect( new Area( new Ellipse2D.Float( 4 + w - 1 - ew, y0, ew, ew )))
      outline.intersect( new Area( new Rectangle( 4, 3, w, h ))) // XXX
//      val atOrig = g2.getTransform
      g2.translate( r.x, r.y )
      // XXX bug: ArrayIndexOutOfBounds in jhlabs composeRGB
      // depending on thumb size!
      paintThumb( slider, slider.getValueIsAdjusting(), g2, outline )
      g2.setPaint( PanelBackgroundPainter.pntKnob )
      val cmpOrig = g2.getComposite
//      g2.setComposite( new PinLightComposite( 1f ))
      g2.setComposite( new HardLightComposite( 0.5f ))
      g2.fill( outline )
      g2.setComposite( cmpOrig )
      g2.translate( -r.x, -r.y )
//      g2.setTransform( atOrig )

      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, aaOrig )
//      g.translate( -r.x, -r.y )
   }

   override protected def getThumbSize() =
      if( slider.getOrientation() == SwingConstants.HORIZONTAL ) {
         new Dimension( 18, 26 )
      } else {
         new Dimension( 26, 18 )
      }
}