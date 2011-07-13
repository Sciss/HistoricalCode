package de.sciss.cupola.video

import swing.event.ButtonClicked
import java.awt.FileDialog
import java.io.File
import swing.{Orientation, Button, Label, BoxPanel, BorderPanel, SimpleSwingApplication, MainFrame}
import com.xuggle.mediatool.{ToolFactory, IMediaViewer}

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
               openVideo( file )
            }
         }
      }
      contents = new BorderPanel {
         import BorderPanel.Position._
//         add( videoPanel, Center )
         add( buttonPanel, South )
      }
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