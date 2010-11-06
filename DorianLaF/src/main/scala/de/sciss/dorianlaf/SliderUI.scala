package de.sciss.dorianlaf

import javax.swing.plaf.basic.BasicSliderUI
import javax.swing.plaf.ComponentUI
import javax.swing.{LookAndFeel, JSlider, JComponent}

object SliderUI {
   def createUI( c: JComponent ) : ComponentUI = new SliderUI( c.asInstanceOf[ JSlider ])
}
class SliderUI( s: JSlider ) extends BasicSliderUI( s ) {
   override protected def installDefaults( s: JSlider ) {
      super.installDefaults( s )
      LookAndFeel.installProperty( s, "opaque", java.lang.Boolean.FALSE )
   }
}