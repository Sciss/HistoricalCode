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

import swing.event.ButtonClicked
import java.awt.FileDialog
import java.io.File
import com.xuggle.mediatool.{ToolFactory, IMediaViewer}
import io.Source
import de.sciss.osc.OSCBundle
import swing.{ScrollPane, ListView, Orientation, Button, Label, BoxPanel, BorderPanel, SimpleSwingApplication, MainFrame}

object VideoPlayer extends SimpleSwingApplication {
//   lazy val videoViewer = ToolFactory.makeViewer( IMediaViewer.Mode.VIDEO_ONLY )

//   private val xuggleHome  = new File( new File( new File( sys.props( "user.home" ), "Documents" ), "devel" ), "xuggler" )
//   private val xuggleLib   = new File( xuggleHome, "lib" ).getAbsolutePath
//
//   sys.env += "XUGGLE_HOME" -> xuggleHome.getAbsolutePath
//   sys.env += "DYLD_LIBRARY_PATH" -> sys.env.get( "DYLD_LIBRARY_PATH" ) match {
//      case Some( prev ) => xuggleLib + File.pathSeparator + prev
//      case None         => xuggleLib
//   }

   private val oscList = new ListView[ OSCBundle ] {
      renderer = new OSCBundleListViewRenderer
      peer.setVisibleRowCount( 16 )
      fixedCellHeight = 160
      fixedCellHeight = 16
   }

   val top = new MainFrame {
      frame =>

      title = "Cupola Video Player"
//      IMediaReader reader = ToolFactory.makeReader("videofile.flv");
//      val videoPanel = new Label( "test" )

      val buttonPanel = new BoxPanel( Orientation.Horizontal ) {
         contents += NiceButton( "Open..." ) { b =>
            val fDlg = new FileDialog( frame.peer, b.text )
            fDlg.setVisible( true )
            val fileName   = fDlg.getFile
            val dirName    = fDlg.getDirectory
            if( fileName != null && dirName != null ) {
               val file = new File( dirName, fileName )
//               openVideo( file )
               openOSC( file )
            }
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
         add( buttonPanel, South )
      }
   }

   private def openOSC( file: File ) {
      val osc = OSCStream.fromSource( Source.fromFile( file, "UTF-8" ))
      oscList.listData = osc.bundles
   }

   private def openVideo( file: File ) {
      val videoViewer = ToolFactory.makeViewer( IMediaViewer.Mode.VIDEO_ONLY )
      val reader = ToolFactory.makeReader( file.toURI.toURL.toString )
//      updateInfo( reader.getContainer.getDuration )
      reader.addListener( videoViewer )
      (new Thread {
         override def run() {
            while( reader.readPacket() == null ) {}
            println( "aqui" )
         }
      }).start()
   }

   private case class NiceButton( label: String )( reaction: Button => Unit ) extends Button( label ) {
      peer.putClientProperty( "JButton.buttonType", "bevel" )
      peer.putClientProperty( "JComponent.sizeVariant", "small" )
      focusable = false
      listenTo( this )
      reactions += {
         case ButtonClicked( _ ) => reaction( this )
      }
   }
}