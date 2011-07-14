/*
 *  VideoPlayer.scala
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

import collection.immutable.{IndexedSeq => IIdxSeq}
import swing.{FlowPanel, ScrollPane, ListView, Orientation, BoxPanel, BorderPanel, SimpleSwingApplication, MainFrame}
import java.net.{InetSocketAddress, InetAddress}
import de.sciss.osc.{OSCTransport, TCP, UDP}

object VideoPlayer extends SimpleSwingApplication {
   val DEFAULT_PORT        = 1201
   val DEFAULT_TRANSPORT   = TCP

//   lazy val videoViewer = ToolFactory.makeViewer( IMediaViewer.Mode.VIDEO_ONLY )

//   private val xuggleHome  = new File( new File( new File( sys.props( "user.home" ), "Documents" ), "devel" ), "xuggler" )
//   private val xuggleLib   = new File( xuggleHome, "lib" ).getAbsolutePath
//
//   sys.env += "XUGGLE_HOME" -> xuggleHome.getAbsolutePath
//   sys.env += "DYLD_LIBRARY_PATH" -> sys.env.get( "DYLD_LIBRARY_PATH" ) match {
//      case Some( prev ) => xuggleLib + File.pathSeparator + prev
//      case None         => xuggleLib
//   }

   // prevent actor starvation!!!
   // --> http://scala-programming-language.1934581.n4.nabble.com/Scala-Actors-Starvation-td2281657.html
   sys.props.put( "actors.enableForkJoin", "false" )

   lazy val mediaList = {
      try {
         MediaList.read( "medialist.xml" )
      } catch {
         case e =>
            Util.displayError( null, "List Media List", e )
            IIdxSeq.empty[ MediaList.Entry ]
      }
   }

   val top = new MainFrame {
      frame =>

      title = "Cupola Video Player"
      Util.unifiedLook( this )

//      IMediaReader reader = ToolFactory.makeReader("videofile.flv");
//      val videoPanel = new Label( "test" )

      private val mediaListView = new ListView( mediaList ) {
         renderer = new MediaListViewRenderer
         peer.setVisibleRowCount( 8 )
         fixedCellWidth = 80
         selection.intervalMode = ListView.IntervalMode.Single
      }

      private val oscHost  = NiceTextField( try { InetAddress.getLocalHost.getHostAddress } catch { case e => "127.0.0.1" })()
      private val oscPort  = NiceTextField( DEFAULT_PORT.toString )()
      private val oscTrnsp = NiceCombo[ OSCTransport ]( TCP :: UDP :: Nil, DEFAULT_TRANSPORT )

      private val buttonPanel = new BoxPanel( Orientation.Horizontal ) {
         contents += NiceButton( "Load Entry" ) { b =>
//            val fDlg = new FileDialog( frame.peer, b.text )
//            fDlg.setVisible( true )
//            val fileName   = fDlg.getFile
//            val dirName    = fDlg.getDirectory
//            if( fileName != null && dirName != null ) {
//               val file = new File( dirName, fileName )
//               openVideo( file )
//               openOSC( file )
            try {
               val addr = new InetSocketAddress( oscHost.text, oscPort.text.toInt )
               mediaListView.selection.items.headOption.foreach { entry =>
                  MediaListEntryFrame.load( frame, entry, addr, oscTrnsp.selection.item )
               }
            } catch {
               case e => Util.displayError( frame, b.text, e )
            }
//            }
         }
      }

      private val oscPanel = new FlowPanel {
         hGap = 4
         contents += NiceLabel( "OSC Server:" )
         contents += oscHost
         contents += oscPort
         contents += oscTrnsp
      }

      contents = new BorderPanel {
         import BorderPanel.Position._
         add( new ScrollPane( mediaListView ) {
            import ScrollPane.BarPolicy._
            horizontalScrollBarPolicy  = Never
            verticalScrollBarPolicy    = Always
            border                     = null
         }, Center )
         add( buttonPanel, South )
         add( oscPanel, North )
      }
   }

//   private def openVideo( file: File ) {
//      val videoViewer = ToolFactory.makeViewer( IMediaViewer.Mode.VIDEO_ONLY )
//      val reader = ToolFactory.makeReader( file.toURI.toURL.toString )
////      updateInfo( reader.getContainer.getDuration )
//      reader.addListener( videoViewer )
//      (new Thread {
//         override def run() {
//            while( reader.readPacket() == null ) {}
//            println( "aqui" )
//         }
//      }).start()
//   }
}