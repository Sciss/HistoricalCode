package de.sciss.cupola.video

import io.Source
import swing.event.ListSelectionChanged
import de.sciss.gui.LCDPanel
import swing.{Label, Button, FlowPanel, ScrollPane, BorderPanel, Swing, Dialog, ListView, Frame}
import de.sciss.osc.OSCBundle
import java.awt.{Color, Font}

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
   Util.unifiedLook( this )

   private val oscList = new ListView( osc.bundles ) {
      renderer = new OSCBundleListViewRenderer
      peer.setVisibleRowCount( 16 )
      fixedCellWidth  = 160
      fixedCellHeight = 16
      background = Color.black

      listenTo( selection )
      selection.intervalMode = ListView.IntervalMode.Single
      selection.reactions += {
         case ListSelectionChanged( _, _, _ ) => selection.items.headOption.foreach { b =>
            seek( b )
         }
      }
   }

   private val lbTime = new Label {
      text = Util.formatTimeString( 0.0 )
      font = new Font( "Menlo", Font.PLAIN, 11 )
   }

   private val transportBar = new FlowPanel {
      hGap = 6
      contents += NiceButton( "Play" ) { b =>
         println( b.text )
      }
      contents += NiceButton( "Stop" ) { b =>
         println( b.text )
      }
      contents += new LCDPanel {
         contents += lbTime
      }
   }

   contents = new BorderPanel {
      import BorderPanel.Position._
      add( new ScrollPane( oscList ) {
         import ScrollPane.BarPolicy._
         horizontalScrollBarPolicy  = Never
         verticalScrollBarPolicy    = Always
         border                     = null
      }, Center )
      add( transportBar, North )
   }

   pack().open()

   override def closeOperation() {
      dispose()
   }

   def seek( b: OSCBundle ) {
      lbTime.text = Util.formatTimeString( OSCBundle.timetagToSecs( b.timetag ))
   }
}