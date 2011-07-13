/*
 *  MediaListEntryFrame.scala
 *  (CupolaVideoPlayer)
 *
 *  Copyright (c) 2011 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation; either
 *	version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	This software is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public
 *	License (gpl.txt) along with this software; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.cupola.video

import io.Source
import swing.event.ListSelectionChanged
import de.sciss.gui.LCDPanel
import swing.{Label, Button, FlowPanel, ScrollPane, BorderPanel, Swing, Dialog, ListView, Frame}
import de.sciss.osc.OSCBundle
import java.io.File
import java.awt.{Dimension, Color, Font}

object MediaListEntryFrame {
   def load( parent: Frame, entry: MediaList.Entry ) {
      (new Thread {
         override def run() {
            try {
               val osc = OSCStream.fromSource( Source.fromFile( entry.oscPath ))
               val vid = VideoHandle.open( new File( entry.videoPath ).toURI.toURL )
               Swing.onEDT( new MediaListEntryFrame( entry.name, osc, vid ))
            } catch {
               case e => Util.displayError( parent, "Load Media Entry", e )
            }
         }
      }).start()
   }
}
class MediaListEntryFrame( name: String, osc: OSCStream, vid: VideoHandle ) extends Frame {
   title = "Entry : " + name
   Util.unifiedLook( this )

   private val oscList = new ListView( osc.bundles ) {
      renderer = new OSCBundleListViewRenderer
      peer.setVisibleRowCount( 16 )
      fixedCellWidth    = 160
      fixedCellHeight   = 16
      background        = Color.black
      foreground        = Color.white

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

   private val videoView = new ImageView
   videoView.preferredSize = new Dimension( vid.width, vid.height )

   contents = new BorderPanel {
      import BorderPanel.Position._
      add( transportBar, North )
      add( videoView, Center )
      add( new ScrollPane( oscList ) {
         import ScrollPane.BarPolicy._
         horizontalScrollBarPolicy  = Never
         verticalScrollBarPolicy    = Always
         border                     = null
      }, South )
   }

   pack().open()

   override def closeOperation() {
      dispose()
   }

   def seek( b: OSCBundle ) {
      seekIgnoreList( OSCBundle.timetagToSecs( b.timetag ))
   }

   private implicit val bundleToTag = (b: OSCBundle) => b.timetag

   def seek( secs: Double ) {
      seekIgnoreList( secs )
      val tag  = OSCBundle.secsToTimetag( secs )
      val pos0 = Util.binarySearch( osc.bundles, tag )
      val pos = if( pos0 >= 0 ) pos0 else -(pos0 + 2)
      if( pos >= 0 ) {
         oscList.selectIndices( pos )
      } else {
         oscList.selectIndices()
      }
   }

   private def seekIgnoreList( secs: Double ) {
      lbTime.text = Util.formatTimeString( secs )
   }
}