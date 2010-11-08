/*
 *  CheckBoxUI.scala
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

import javax.swing.plaf.basic.BasicCheckBoxUI
import javax.swing.plaf.ComponentUI
import javax.swing._
import java.awt._
import geom._
import com.jhlabs.composite._

object CheckBoxUI {
   def createUI( c: JComponent ) : ComponentUI = new CheckBoxUI
}
class CheckBoxUI extends BasicCheckBoxUI {
   override protected def installDefaults( b: AbstractButton ) {
      super.installDefaults( b )
      LookAndFeel.installProperty( b, "opaque", java.lang.Boolean.FALSE )
   }

   override def paint( g: Graphics, c: JComponent ) {
      val g2      = g.asInstanceOf[ Graphics2D ]
      val aaOld   = g2.getRenderingHint( RenderingHints.KEY_ANTIALIASING )
      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )
      super.paint( g, c )
      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, aaOld )
   }
}

class CheckBoxIcon( width: Int = 28, height: Int = 22 ) extends Icon with ButtonPainter {
   def getIconWidth  = width
   def getIconHeight = height

   protected def paintButtonText( b: AbstractButton, g2: Graphics2D, colrBg: Color ) {}
   
   private val pasOutline : Shape = {
      val gp = new GeneralPath()
      gp.append( new Rectangle( 3, 3, 1, getIconHeight  - 6 ), false )
      gp.append( new Rectangle( getIconWidth - 4, 3, 1, getIconHeight  - 6 ), false )
      gp
   }

   private val actOutline : Shape = new Rectangle( 6, 3, getIconWidth - 12, getIconHeight  - 6 )

   def paintIcon( c: Component, g: Graphics, x: Int, y: Int ) {
      val b = c.asInstanceOf[ AbstractButton ]
//      g.setColor( if( b.isSelected ) Color.yellow else Color.white )
//      g.fillRect( x, y, 32, 22 )
      paintButton( b, g.asInstanceOf[ Graphics2D ], x, y, pasOutline, actOutline ) //
   }
}