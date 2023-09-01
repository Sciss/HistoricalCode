/*
 *  NuagesLauncher.scala
 *  (NuagesPompe)
 *
 *  Copyright (c) 2010-2012 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either
 *  version 2, june 1991 of the License, or (at your option) any later version.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public
 *  License (gpl.txt) along with this software; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.nuages

import de.sciss.synth.swing.j.JServerStatusPanel
import java.io.File
import de.sciss.synth.proc.{ProcTxn, ProcDemiurg}
import javax.swing.Box
import java.awt.{EventQueue, GraphicsEnvironment}
import de.sciss.synth.{ServerConnection, Server}
import collection.immutable.{IndexedSeq => IIdxSeq}
import util.control.NonFatal

object NuagesLauncher {
   sealed trait SettingsLike {
      def serverConfig : Server.ConfigLike
      def masterChannels: Option[ IIdxSeq[ Int ]]
      def soloChannels: Option[ IIdxSeq[ Int ]]
      def recordPath: Option[ String ]
      def meters: Boolean
      def collector: Boolean
      def fullScreenKey: Boolean
      def tapeFolder: Option[ File ]
      def tapeAction: List[ TapesPanel.TapeInfo ] => Unit
      def beforeShutdown: () => Unit

      /**
       * This is invoked after booting and setting up nuages, on the event dispatcher thread.
       */
      def doneAction : Ready => Unit
      def controlSettings: ControlPanel.SettingsLike
      def antiAliasing : Boolean
   }

   object Settings {
      implicit def fromBuilder( b: SettingsBuilder ) : Settings = b.build
   }
   sealed trait Settings extends SettingsLike {
      def serverConfig : Server.Config
   }

   final case class SettingsBuilder() extends SettingsLike {
      var serverConfig    = {
         val b = Server.Config()
         // some more sane settings
         b.audioBusChannels   = 512
         b.loadSynthDefs      = false
         b.memorySize         = 65536
         b.zeroConf           = false
         b
      }
      var masterChannels   = Option( IIdxSeq( 0, 1 ))
      var soloChannels     = Option.empty[ IIdxSeq[ Int ]]
      var recordPath       = Option.empty[ String ]
      var meters           = true
      var collector        = true
      var fullScreenKey    = false
      var tapeFolder       = Option.empty[ File ]
      var tapeAction: List[ TapesPanel.TapeInfo ] => Unit = list => ()
      var beforeShutdown: () => Unit = () => ()
      var doneAction : Ready => Unit = r => ()
      var controlSettings  = ControlPanel.SettingsBuilder()
      var antiAliasing     = false

      def build : Settings = SettingsImpl(
         serverConfig.build,
         masterChannels, soloChannels, recordPath, meters, collector, fullScreenKey, tapeFolder, tapeAction,
         beforeShutdown, doneAction, controlSettings.build, antiAliasing )
   }

   private final case class SettingsImpl( serverConfig: Server.Config,
                                          masterChannels: Option[ IIdxSeq[ Int ]],
                                          soloChannels: Option[ IIdxSeq[ Int ]],
                                          recordPath: Option[ String ],
                                          meters: Boolean, collector: Boolean, fullScreenKey: Boolean,
                                          tapeFolder: Option[ File ], tapeAction: List[ TapesPanel.TapeInfo ] => Unit,
                                          beforeShutdown: () => Unit, doneAction: Ready => Unit,
                                          controlSettings: ControlPanel.Settings, antiAliasing: Boolean )
   extends Settings

   def apply( settings: Settings = SettingsBuilder().build ) : NuagesLauncher =
      new NuagesLauncher( settings )

   sealed trait Ready {
      def server : Server
      def frame : NuagesFrame
      def launcher : NuagesLauncher
      def controlPanel : ControlPanel
      def tapesPanel : Option[ TapesPanel ]
   }

   private final case class ReadyImpl( server: Server, frame: NuagesFrame, launcher: NuagesLauncher,
                                       controlPanel: ControlPanel, tapesPanel: Option[ TapesPanel ])
   extends Ready
}
final class NuagesLauncher private( val settings: NuagesLauncher.Settings ) {
   launcher =>

   launch()

   private def defer( code: => Unit ) { EventQueue.invokeLater( new Runnable { def run() { code }})}

   private def launch() {
      // Dummy call to initialize AWT.
      // Technically we don't need EDT here --
      // but that way the icon doesn't bounce forever in the OS X dock
      // (which is probably due to the first other process being scsynth otherwise)
      EventQueue.isDispatchThread

      // prevent actor starvation!!!
      // --> http://scala-programming-language.1934581.n4.nabble.com/Scala-Actors-Starvation-td2281657.html
//      sys.props += "actors.enableForkJoin" -> "false"

      val booting = Server.boot( config = settings.serverConfig ) {
         case ServerConnection.Running( s ) => defer( running( s ))
      }
      sys.runtime.addShutdownHook( new Thread { override def run() { shutdown( booting )}})
   }

   private def running( s: Server ) {
      ProcDemiurg.addServer( s )

      val screenBounds =
         GraphicsEnvironment.getLocalGraphicsEnvironment.getDefaultScreenDevice.getDefaultConfiguration.getBounds

      val ssp     = new JServerStatusPanel( JServerStatusPanel.COUNTS )
      ssp.server  = Some( s )
      val maxX    = screenBounds.x + screenBounds.width - 48
      val maxY    = screenBounds.y + screenBounds.height - 35 /* sspw.getHeight() + 3 */

      // nuages
      val nuagesCfg  = NuagesConfig( s, settings.masterChannels, settings.soloChannels, settings.recordPath,
         settings.meters, settings.collector, settings.fullScreenKey )
      val f          = new NuagesFrame( nuagesCfg )
      val np         = f.panel
//      masterBus      = np.masterBus.get // XXX not so elegant
      val disp       = np.display
      disp.setHighQuality( settings.antiAliasing )
      val y0 = screenBounds.y + 22
      f.setBounds( screenBounds.x, y0, maxX - screenBounds.x, maxY - y0 )
      f.setUndecorated( true )
      f.setVisible( true )
//      initFScape( s, f )

      val recOption = (np.masterBus, settings.recordPath) match {
         case (Some( bus ), Some( path )) =>
            val recS    = NuagesRecorder.SettingsBuilder()
            recS.folder = new File( path )
            require( if( recS.folder.isDirectory ) recS.folder.canWrite else recS.folder.mkdirs(),
               "Can't access live recording folder: " + path )
            recS.bus    = bus
            val rec     = NuagesRecorder( recS )
            Some( rec )
         case _ => None
      }

      val ctrlS = ControlPanel.SettingsBuilder.fromSettings( settings.controlSettings )
      (ctrlS.clock, recOption) match {
         case (true, Some( rec )) =>
            ctrlS.clockAction = (on, fun) => settings.controlSettings.clockAction.apply( on, () => {
               ProcTxn.spawnAtomic { implicit tx =>
                  val succ = if( on ) rec.start else rec.stop
                  if( succ ) tx.afterCommit( _ => fun() )
               }
            })
         case _ =>
      }
      val ctrl = ControlPanel( ctrlS )
      val ctrlB = f.bottom
      ctrlB.add( ssp )
      ctrlB.add( Box.createHorizontalStrut( 8 ))
      ctrlB.add( ctrl )
      ctrlB.add( Box.createHorizontalStrut( 4 ))

      val tapes = settings.tapeFolder.map { folder =>
         val p = TapesPanel.fromFolder( folder )
         p.installOn( f )( settings.tapeAction( _ ))
         p
      }

//      ProcTxn.atomic( settings.procInit( _ ))
      val res = NuagesLauncher.ReadyImpl( s, f, launcher, ctrl, tapes )
      try {
         settings.doneAction( res )
      } catch {
         case NonFatal( e ) => e.printStackTrace()
      }
   }

   private def shutdown( booting: ServerConnection ) {
      try {
         settings.beforeShutdown()
      } catch {
         case NonFatal( e ) => e.printStackTrace()
      }
      val s = Server.default
      if( (s != null) && (s.condition != Server.Offline) ) {
         s.quit()
         s.dispose()
      } else {
         booting.abort()
      }
   }
}