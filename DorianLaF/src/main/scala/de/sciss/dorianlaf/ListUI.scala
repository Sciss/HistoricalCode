package de.sciss.dorianlaf

import javax.swing.plaf.basic.BasicListUI
import javax.swing.plaf.{UIResource, ComponentUI}
import javax.swing.{JList, DefaultListCellRenderer, JComponent}
import java.awt.Component

object ListUI {
   def createUI( c: JComponent ) : ComponentUI = new ListUI
}
class ListUI extends BasicListUI {
   
}

class ListCellRenderer extends DefaultListCellRenderer with UIResource {
   putClientProperty( "Dorian.mode", "lcd" )
//   setIcon( new CheckBoxIcon( 20, 22 ))

   override def getListCellRendererComponent( l: JList, v: AnyRef, idx: Int, selected: Boolean,
                                              focused: Boolean ) : Component = {
      super.getListCellRendererComponent( l, v, idx, selected, focused )
      putClientProperty( "Dorian.lcdInverted", selected )
      this
   }
}