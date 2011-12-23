/*
 *  PanelUI.scala
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

import javax.swing.plaf.basic.BasicPanelUI
import javax.swing.plaf.ComponentUI
import javax.swing.{UIManager, AbstractButton, JComponent}
import java.awt._
import com.jhlabs.composite.{PinLightComposite, OverlayComposite}

object PanelUI {
   def createUI( c: JComponent ) : ComponentUI = new PanelUI
}
class PanelUI extends BasicPanelUI {
   override def update( g: Graphics, c: JComponent ) {
      val g2   = g.asInstanceOf[ Graphics2D ]
//      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )
      paintPanelBackground( c, g2, 0, 0, c.getWidth, c.getHeight )
//      super.paint( g, c )
   }

   private def paintPanelBackground( c: JComponent, g: Graphics, x: Int, y: Int, w: Int, h: Int ) {
      g.setColor( new Color( 25, 25, 25, 255 ))
      g.fillRect( x, y, w, h )
   }

   private def paintPanelBackgroundX( c: JComponent, g: Graphics, x: Int, y: Int, w: Int, h: Int ) {
      val start   = UIManager.getColor( "Panel.startBackground" )
      val end     = UIManager.getColor( "Panel.endBackground" )
//val start = new Color( 35, 35, 35 )
//val end   = new Color( 0, 0, 0 )
      val g2      = g.asInstanceOf[ Graphics2D ]
      val pntGrad = new GradientPaint( x, y, start, w, h, end )
      g2.setPaint( pntGrad )
      g2.fillRect( x, y, w, h )

      g2.setPaint( PanelBackgroundPainter.pntCanvas )
      val cmpOrig = g2.getComposite
      g2.setComposite( PanelBackgroundPainter.cmpCanvas )
//      g2.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.25f ))
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
