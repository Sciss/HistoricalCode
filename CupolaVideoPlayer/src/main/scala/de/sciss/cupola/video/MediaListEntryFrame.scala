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
               val osc = OSCStream.fromSource( Source.fromFile( entry.oscPath ), entry.oscOffset )
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

   private var seekedTime = -1.0

   private val selReact: PartialFunction[ AnyRef, Unit ] = {
         case /* sel @ */ ListSelectionChanged( _, _, _ ) =>
//println( "sel : " + selection.items.headOption )
            oscList.selection.items.headOption.foreach( seek( _ ))
      }

   private val oscList = new ListView( osc.bundles ) {
      renderer = new OSCBundleListViewRenderer
      peer.setVisibleRowCount( 16 )
      fixedCellWidth    = 160
      fixedCellHeight   = 16
      background        = Color.black
      foreground        = Color.white

      listenTo( selection )
      selection.intervalMode = ListView.IntervalMode.Single
      selection.reactions += selReact
   }

   private val lbTime = new Label {
      text = Util.formatTimeString( 0.0 )
      font = new Font( "Menlo", Font.PLAIN, 11 )
   }

   private val transportBar = new FlowPanel {
      hGap = 6
      contents += NiceButton( "Play" ) { b =>
         vid.play
      }
      contents += NiceButton( "Stop" ) { b =>
         vid.stop
      }
      contents += new LCDPanel {
         contents += lbTime
      }
   }

   private val videoView = new ImageView
   videoView.preferredSize = new Dimension( vid.width, vid.height )
   vid.videoView  = Some( videoView )
//   vid.timeView   = Some( lbTime )
   vid.timeView = (source, secs, playing) => Swing.onEDT {
      lbTime.text = Util.formatTimeString( secs )
      if( !(source eq oscList) ) seekOSCList( secs )
   }

   private implicit val bundleToTag = (b: OSCBundle) => b.timetag

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
   seek( this, 0.0 )

   override def closeOperation() {
      vid.dispose()
      dispose()
   }

   def seek( b: OSCBundle ) {
      seekIgnoreList( oscList, OSCBundle.timetagToSecs( b.timetag ))
   }

   def seek( source: AnyRef, secs: Double ) {
      seekIgnoreList( source, secs )
      if( !(source eq oscList) ) seekOSCList( secs )
   }

   private def seekOSCList( secs: Double ) {
      val tag  = OSCBundle.secsToTimetag( secs )
      val pos0 = Util.binarySearch( osc.bundles, tag )
      val pos = if( pos0 >= 0 ) pos0 else -(pos0 + 2)
      try {
         oscList.selection.reactions -= selReact
         if( pos >= 0 ) {
            oscList.selectIndices( pos )
         } else {
            oscList.selectIndices()
         }
         oscList.peer.ensureIndexIsVisible( pos )
      } finally {
         oscList.selection.reactions += selReact
      }
   }

   private def seekIgnoreList( source: AnyRef, secs: Double ) {
      if( secs == seekedTime ) return
      seekedTime = secs
      vid.seek( source, secs )
   }
}