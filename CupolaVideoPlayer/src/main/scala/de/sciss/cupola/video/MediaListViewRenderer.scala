package de.sciss.cupola.video

import swing.{Alignment, Label, ListView}
import java.awt.Font

class MediaListViewRenderer extends ListView.AbstractRenderer[ MediaList.Entry, Label ]( new Label ) {
   component.horizontalAlignment = Alignment.Leading
//   component.font = new Font( "Menlo", Font.PLAIN, 10 )
   component.peer.putClientProperty( "JComponent.sizeVariant", "small" )

   def configure( list: ListView[ _ ], isSelected: Boolean, hasFocus: Boolean, a: MediaList.Entry, index: Int ) {
      component.text = a.name
   }
}