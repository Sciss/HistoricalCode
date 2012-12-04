package de.sciss.submin.creator

import swing.BorderPanel

class MainFrame( doc: Document ) extends swing.Frame {
   title = "Submin Creator"

   lazy val documentView = new DocumentView( doc )

   contents = new BorderPanel {
      import BorderPanel.Position._
      add( documentView, Center )
   }
   centerOnScreen()
   open()
}