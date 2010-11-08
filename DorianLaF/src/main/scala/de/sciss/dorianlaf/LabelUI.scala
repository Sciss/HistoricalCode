/*
 *  LabelUI.scala
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

import javax.swing.plaf.ComponentUI
import sun.swing.SwingUtilities2
import java.awt._
import geom.Area
import image.BufferedImage
import javax.swing._
import text.View
import plaf.basic.{BasicHTML, BasicLabelUI}

object LabelUI {
   def createUI( c: JComponent ) : ComponentUI = new LabelUI
}
class LabelUI extends BasicLabelUI with LabelPainter {
//   override protected def installDefaults( lb: JLabel ) {
//      super.installDefaults( lb )
////
////
////      LookAndFeel.installProperty( b, "opaque", java.lang.Boolean.FALSE )
////      b.setMargin( new Insets( 30, 30, 30, 30 ))
//      println( "YO CHUCK : " + lb.getClientProperty( "Dorian.mode" ))
//   }

   override def paint( g: Graphics, c: JComponent ) {
      val g2      = g.asInstanceOf[ Graphics2D ]
      paintLabel( g2, c.asInstanceOf[ JLabel ])
//      val aaOld   = g2.getRenderingHint( RenderingHints.KEY_ANTIALIASING )
//      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )
//      // XXX DUPLICATE ACTION
//      if( c.getClientProperty( "Dorian.mode" ) == "lcd" ) {
//         val gc      = g2.getDeviceConfiguration()
//         val img     = gc.createCompatibleImage( c.getWidth(), c.getHeight() )
//         val imgG    = img.getGraphics()
//         val imgG2   = imgG.asInstanceOf[ Graphics2D ]
//         imgG2.setColor( UIManager.getColor( "Dorian.lcdBackground" ))
//         imgG2.fillRect( 0, 0, c.getWidth(), c.getHeight() )
//         imgG2.setFont( c.getFont() )
//         super.paint( imgG2, c )
//         g2.drawImage( img, ButtonPainter.opLCD, 0, 0 )
//         img.flush()
//      } else {
//         super.paint( g, c )
//      }
//      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, aaOld )
   }

//   override protected def paintEnabledText( lb: JLabel, g: Graphics, s: String, x0: Int, y: Int ) {
//      val g2      = g.asInstanceOf[ Graphics2D ]
//      val aaOld   = g2.getRenderingHint( RenderingHints.KEY_ANTIALIASING )
//      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )
//      val mnemo   = lb.getDisplayedMnemonicIndex()
//      val x       = if( lb.getClientProperty( "Dorian.mode" ) == "lcd" ) {
////         g2.setColor( new Color( 0x20, 0x20, 0x20 ))
////         g2.fillRect( 0, 0, lb.getWidth(), lb.getHeight() )
//         g2.setPaint( PanelBackgroundPainter.pntLCD )
//         x0 + 5
//      } else {
//         g2.setColor( lb.getForeground() )
//         x0
//      }
//      SwingUtilities2.drawStringUnderlineCharAt( lb, g, s, mnemo, x, y )
//      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, aaOld )
//   }

   override def getPreferredSize( c: JComponent ) : Dimension = {
      val d = super.getPreferredSize( c )
      if( c.getClientProperty( "Dorian.mode" ) == "lcd" ) {
         d.width  += 10
      }
      d
   }
}

trait LabelPainter {
   def paintLabel( cg2: Graphics2D, lb: JLabel ) {
      val text    = lb.getText()
      val enabled = lb.isEnabled()
      val icon    = if( enabled ) lb.getIcon() else lb.getDisabledIcon()
      val w       = lb.getWidth()
      val h       = lb.getHeight()
      val in      = lb.getInsets( null )
      val rv      = new Rectangle( in.left, in.top, w - (in.left + in.right), h - (in.top + in.bottom) )
      val lcd     = lb.getClientProperty( "Dorian.mode" ) == "lcd"
      val selected= lb.getClientProperty( "Dorian.lcdInverted" ) == true 

      if( (icon == null) && ((text == null) || text == "") && !selected ) {
         if( lcd ) {
            cg2.setColor( UIManager.getColor( "Dorian.lcdBackground" ))
            cg2.fillRect( rv.x, rv.y, rv.width, rv.height )
         }
         return
      }

      val (g2, img) = if( lcd ) {
         val gc      = cg2.getDeviceConfiguration()
         val img     = gc.createCompatibleImage( w, h )
         val g2      = img.getGraphics().asInstanceOf[ Graphics2D ]
         g2.setColor( UIManager.getColor( "Dorian.lcdBackground" ))
         g2.fillRect( rv.x, rv.y, rv.width, rv.height )
         g2.setFont( lb.getFont() )
         g2 -> img
      } else {
         cg2 -> null.asInstanceOf[ BufferedImage ]
      }

      val aaOld   = g2.getRenderingHint( RenderingHints.KEY_ANTIALIASING )
      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )

      val fm      = g2.getFontMetrics()
      val ri      = new Rectangle( 0, 0, 0, 0 )
      val rt      = new Rectangle( 0, 0, 0, 0 )
      val textC   = SwingUtilities.layoutCompoundLabel( lb, fm, text, icon, lb.getVerticalAlignment(),
         lb.getHorizontalAlignment(), lb.getVerticalTextPosition(), lb.getHorizontalTextPosition(),
         rv, ri, rt, lb.getIconTextGap() )
      
      if( icon != null ) icon.paintIcon( lb, g2, ri.x, ri.y )
      if( textC != null ) {
         val v = lb.getClientProperty( BasicHTML.propertyKey ).asInstanceOf[ View ]
         if( v != null ) {
            v.paint( g2, rt )
         } else {
//            paintEnabledText( lb, g2, textC, rt.x, rt.y + fm.getAscent() )
            val mnemo   = lb.getDisplayedMnemonicIndex()
            if( lcd ) {
               val tx = rt.x + 5
               val ty = rt.y + fm.getAscent()
               if( selected ) {
                  g2.setPaint( PanelBackgroundPainter.pntLCDDark )
                  val frc     = g2.getFontRenderContext()
                  val fnt     = g2.getFont()
                  val glyph	= fnt.createGlyphVector( frc, textC )
                  val shp     = glyph.getOutline( tx, ty )
                  val a       = new Area( rv )
                  a.subtract( new Area( shp ))
                  g2.fill( a )
               } else {
                  g2.setPaint( PanelBackgroundPainter.pntLCD )
                  SwingUtilities2.drawStringUnderlineCharAt( lb, g2, textC, mnemo, tx, ty )
               }
            } else {
               g2.setColor( lb.getForeground() )
               SwingUtilities2.drawStringUnderlineCharAt( lb, g2, textC, mnemo, rt.x, rt.y + fm.getAscent() )
            }
         }
      }

      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, aaOld )
      if( img != null ) {
         cg2.drawImage( img, ButtonPainter.opLCD, 0, 0 )
         img.flush()
      }
   }

//   private def paintDisabledText( lb: JLabel, g2: Graphics2D, s: String, x0: Int, y: Int ) {
//      paintEnabledText( lb, g2, s, x0, y )// XXX ooops
//   }

//   private def paintEnabledText( lb: JLabel, g2: Graphics2D, s: String, x0: Int, y: Int ) {
////      val aaOld   = g2.getRenderingHint( RenderingHints.KEY_ANTIALIASING )
////      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )
//      val mnemo   = lb.getDisplayedMnemonicIndex()
//      val x       = if( lb.getClientProperty( "Dorian.mode" ) == "lcd" ) {
//         g2.setPaint( PanelBackgroundPainter.pntLCD )
//         x0 + 5
//      } else {
//         g2.setColor( lb.getForeground() )
//         x0
//      }
//      SwingUtilities2.drawStringUnderlineCharAt( lb, g, s, mnemo, x, y )
////      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, aaOld )
//   }
}