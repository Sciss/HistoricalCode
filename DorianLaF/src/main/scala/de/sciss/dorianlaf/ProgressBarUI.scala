/*
 *  ProgressBarUI.scala
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
import javax.swing.plaf.basic.BasicProgressBarUI
import javax.swing.{SwingConstants, UIManager, JProgressBar, JComponent}
import java.awt._
import geom.Area

object ProgressBarUI {
   def createUI( c: JComponent ) : ComponentUI = new ProgressBarUI
}
class ProgressBarUI extends BasicProgressBarUI {
   private val thumbRect = new Rectangle()

   // todo: when no string is painted we can avoid
   // the extra image and image-op and use a different
   // lcd-paint
   override def paint( g: Graphics, c: JComponent ) {
      val g2   = g.asInstanceOf[ Graphics2D ]
//      val cg2   = g.asInstanceOf[ Graphics2D ]
      val in   = c.getInsets()
      val x    = in.left
      val y    = in.top
      val w    = c.getWidth() - (x + in.right)
      val h    = c.getHeight() - (y + in.bottom)
//      val x    = 0
//      val y    = 0
//      val w    = c.getWidth() - (in.left + in.right)
//      val h    = c.getHeight() - (in.top + in.bottom)

      if( w <= 0 || h <= 0 ) return

//      val gc      = cg2.getDeviceConfiguration()
//      val img     = gc.createCompatibleImage( w, h )
//      val g2      = img.getGraphics().asInstanceOf[ Graphics2D ]
      
      g2.setColor( UIManager.getColor( "Dorian.lcdBackground" ))
      g2.fillRect( x, y, w, h )

      if( progressBar.isIndeterminate() ) {
         // todo: round to even pixels
         getBox( thumbRect )
      } else {
         val amt = getAmountFull( in, w, h )
         if( progressBar.getOrientation() == SwingConstants.HORIZONTAL ) {
            thumbRect.x       = if( c.getComponentOrientation().isLeftToRight() ) x else x + w - 1 - amt
            thumbRect.y       = y
            thumbRect.width   = amt
            thumbRect.height  = h
         } else {
            thumbRect.x       = x
            thumbRect.y       = y + h - 1 - amt
            thumbRect.width   = w
            thumbRect.height  = amt
         }
      }

      g2.setPaint( PanelBackgroundPainter.pntLCD )
      if( progressBar.isStringPainted() ) {
         val str     = progressBar.getString()
         if( str == null || str == "" ) {
            g2.fillRect( thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height )
         } else {
            val p       = getStringPlacement( g, str, x, y, w, h )
            val aaOld   = g2.getRenderingHint( RenderingHints.KEY_ANTIALIASING )
            g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )
            val frc     = g2.getFontRenderContext()
            val fnt     = c.getFont()
            val glyph	= fnt.createGlyphVector( frc, str )
//            val lineMetr= fnt.getLineMetrics( str, frc )
            // todo: vertical orientation should have text rotated
            val shp     = glyph.getOutline( p.x, p.y /* + lineMetr.getAscent() */ ) // ???
            val a       = new Area( thumbRect )
            a.exclusiveOr( new Area( shp ))
            g2.fill( a )
            g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, aaOld )
         }
      } else {
         g2.fillRect( thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height )
      }

//      cg2.drawImage( img, ButtonPainter.opLCD, in.left, in.top )
//      img.flush()
   }

   override def getPreferredSize( c: JComponent ) : Dimension = {
      val d = if( progressBar.getOrientation() == SwingConstants.HORIZONTAL ) {
         new Dimension( 146, 16 )
      } else {
         new Dimension( 16, 146 )
      }
      val in = c.getInsets()
      d.width  += in.left + in.right
      d.height += in.top + in.bottom
      d
   }
}