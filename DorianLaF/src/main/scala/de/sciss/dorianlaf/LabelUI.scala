package de.sciss.dorianlaf

import javax.swing.JComponent
import javax.swing.plaf.basic.BasicLabelUI
import javax.swing.plaf.ComponentUI
import java.awt.{RenderingHints, Graphics2D, Graphics}

object LabelUI {
   def createUI( c: JComponent ) : ComponentUI = new LabelUI
}
class LabelUI extends BasicLabelUI {
   override def paint( g: Graphics, c: JComponent ) {
      val g2      = g.asInstanceOf[ Graphics2D ]
      val aaOld   = g2.getRenderingHint( RenderingHints.KEY_ANTIALIASING )
      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )
      super.paint( g, c )
      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, aaOld )
   }
}