package de.sciss.dorianlaf

import javax.swing.plaf.basic.BasicFileChooserUI
import javax.swing.plaf.ComponentUI
import javax.swing.{JFileChooser, JComponent}

object FileChooserUI {
   def createUI( c: JComponent ) : ComponentUI = new FileChooserUI( c.asInstanceOf[ JFileChooser ])
}
class FileChooserUI( f: JFileChooser ) extends BasicFileChooserUI( f ) {
   
}