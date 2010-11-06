package de.sciss.dorianlaf

import javax.swing.plaf.basic.BasicCheckBoxUI
import javax.swing.plaf.ComponentUI
import javax.swing.{AbstractButton, LookAndFeel, JComponent, JSlider}
import java.awt.{Graphics, Graphics2D, RenderingHints}

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