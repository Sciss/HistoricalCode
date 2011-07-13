package de.sciss.cupola.video

import swing.Button
import swing.event.ButtonClicked

case class NiceButton( label: String )( reaction: Button => Unit ) extends Button( label ) {
   peer.putClientProperty( "JButton.buttonType", "bevel" )
   peer.putClientProperty( "JComponent.sizeVariant", "small" )
   focusable = false
   listenTo( this )
   reactions += {
      case ButtonClicked( _ ) => reaction( this )
   }
}
