package de.sciss.cupola.video

import swing.{ScrollPane, BorderPanel, Swing, Dialog, ListView, Frame}
import io.Source

object MediaListEntryFrame {
   def load( parent: Frame, entry: MediaList.Entry ) {
      (new Thread {
         override def run() {
            try {
               val osc = OSCStream.fromSource( Source.fromFile( entry.oscPath ))
               Swing.onEDT( new MediaListEntryFrame( entry.name, osc ))
            } catch {
               case e => Util.displayError( parent, "Load Media Entry", e )
            }
         }
      }).start()
   }
}
class MediaListEntryFrame( name: String, osc: OSCStream ) extends Frame {
   title = "Entry : " + name

   private val oscList = new ListView( osc.bundles ) {
      renderer = new OSCBundleListViewRenderer
      peer.setVisibleRowCount( 16 )
      fixedCellWidth  = 160
      fixedCellHeight = 16
   }

   contents = new BorderPanel {
      import BorderPanel.Position._
      add( new ScrollPane( oscList ) {
         import ScrollPane.BarPolicy._
         horizontalScrollBarPolicy  = Never
         verticalScrollBarPolicy    = Always
         border                     = null
      }, Center )
   }

   pack().open()

   override def closeOperation() {
      dispose()
   }
}