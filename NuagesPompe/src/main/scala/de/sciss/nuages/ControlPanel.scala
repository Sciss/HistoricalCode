/*
 *  ControlPanel.scala
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

import java.awt.event.{ComponentEvent, ComponentAdapter}
import java.io.PrintStream
import javax.swing._
import de.sciss.scalainterpreter.LogPane
import java.awt.{Dimension, EventQueue, Font, Color}
import de.sciss.audiowidgets.j.PeakMeter

//import Setup._
//import de.sciss.synth.proc.ProcTxn
import collection.immutable.{IndexedSeq => IIdxSeq}

object ControlPanel {
   sealed trait SettingsLike {
      def numOutputChannels : Int
      def numInputChannels : Int
      def clock : Boolean
      def log : Boolean
      def repl : Boolean
      def replSettings : InterpreterFrame.SettingsLike

      /**
       * A function invoked when the clock is about to be
       * started (first argument is `true`) or
       * stopped (first argument is `false`). The function
       * should decided whether it wants to veto or allow
       * the action to happen in the GUI. The latter is done by invoking
       * `apply` on the second argument.
       */
      def clockAction : (Boolean, () => Unit) => Unit
   }

   object Settings {
      implicit def fromBuilder( b: SettingsBuilder ) : Settings = b.build
   }
   sealed trait Settings extends SettingsLike {
      def replSettings : InterpreterFrame.Settings
   }

   object SettingsBuilder {
      def fromSettings( s: SettingsLike ) : SettingsBuilder = {
         val b = apply()
         b.numOutputChannels  = s.numOutputChannels
         b.numInputChannels   = s.numInputChannels
         b.clock              = s.clock
         b.clockAction        = s.clockAction
         b.log                = s.log
         b.repl               = s.repl
         b.replSettings       = InterpreterFrame.SettingsBuilder.fromSettings( s.replSettings )
         b
      }

      def apply() : SettingsBuilder = new SettingsBuilder()
   }
   final class SettingsBuilder private () extends SettingsLike {
      var numOutputChannels : Int   = 2
      var numInputChannels : Int    = 0
      var clock : Boolean           = true
      var clockAction : (Boolean, () => Unit) => Unit = (_, fun) => fun()
      var log : Boolean             = true
      var repl : Boolean            = true
      var replSettings : InterpreterFrame.SettingsBuilder = InterpreterFrame.SettingsBuilder()

      def build : Settings = SettingsImpl( numOutputChannels, numInputChannels, clock, clockAction, log,
                                           repl, replSettings.build )
   }

   private final case class SettingsImpl( numOutputChannels: Int, numInputChannels: Int, clock: Boolean,
                                          clockAction: (Boolean, () => Unit) => Unit, log: Boolean, repl: Boolean,
                                          replSettings: InterpreterFrame.Settings )
   extends Settings

   def apply( settings: Settings = SettingsBuilder().build ) : ControlPanel = new ControlPanel( settings )
}

class ControlPanel private ( val settings: ControlPanel.Settings )
extends BasicPanel {
   panel =>

   private def makeMeter( numChannels: Int ) : Option[ PeakMeter ] = if( numChannels > 0 ) {
      val p = new PeakMeter()
      p.orientation = SwingConstants.HORIZONTAL
      p.numChannels = numChannels
      p.borderVisible = true
      val d = p.getPreferredSize
      val dn = 30 / numChannels
      d.height = numChannels * dn + 7
      p.setMaximumSize( d )
      p.setPreferredSize( d )
      Some( p )
   } else None

   private val outMeterPanel  = makeMeter( settings.numOutputChannels )
   private val inMeterPanel   = makeMeter( settings.numInputChannels  )
   private val inDataOffset   = settings.numOutputChannels << 1

   private var interpreter : Option[ InterpreterFrame ] = None

   private def space( width: Int = 8 ) {
      panel.add( Box.createHorizontalStrut( width ))
   }

  private val logPane = if( settings.log ) {
     val lps = LogPane.Settings()
     lps.columns  = 30
     lps.rows     = 2
     val p = LogPane( lps )
//     p.init()
     /* val scroll = */ p.component match {
        case scroll: JScrollPane =>
           scroll.setBorder( null )
           scroll.getViewport.getView.setFont( new Font( "Menlo", Font.PLAIN, 8 ))
        case _ =>
     }
     val printStream = new PrintStream( p.outputStream )
     System.setErr( printStream )
     System.setOut( printStream )
     Console.setErr( p.outputStream )
     Console.setOut( p.outputStream )
     val d = outMeterPanel.orElse( inMeterPanel ).map( _.getPreferredSize ).getOrElse( new Dimension( 0, 36 ))
     val d1 = p.component.getPreferredSize // getPreferredSize
     d1.height = d.height
     p.component.setPreferredSize( d1 )
     Some( p )
  } else None

   private val clock = if( settings.clock ) Some( Wallclock() ) else None

   private val repl = if( settings.repl ) {
      Some( BasicToggleButton( "REPL" )( if( _ ) openREPL() else closeREPL() ))
   } else None

   // ---- constructor ----

   panel.setLayout( new BoxLayout( panel, BoxLayout.X_AXIS ))

   clock.foreach { ggClock =>
      val ggRecStart = BasicButton( "\u25B6" ) { startClock() }
      ggRecStart.setBackground( Color.black )
      ggRecStart.setForeground( Color.white )
      panel.add( ggRecStart )
      val ggRecStop = BasicButton( "\u25FC" ) { stopClock() }
      ggRecStop.setBackground( Color.black )
      ggRecStop.setForeground( Color.white )
      panel.add( ggRecStop )
      panel.add( ggClock )
      space()
   }

   outMeterPanel.foreach( panel.add( _ ))
   inMeterPanel.foreach( panel.add( _ ))

   logPane.foreach { p =>
      space()
      panel.add( p.component )
   }

   repl.foreach { p =>
      val glue = Box.createHorizontalGlue()
      glue.setBackground( Color.black )
      panel.add( glue )
      panel.add( p )
   }

   def startClock() {
      clock.foreach { ggClock =>
         settings.clockAction( true, () => defer {
            ggClock.reset()
            ggClock.start()
         })
      }
   }

   def stopClock() {
      clock.foreach { ggClock =>
         settings.clockAction( false, () => defer {
            ggClock.stop()
         })
      }
   }

   def openREPL() {
      repl.foreach { ggREPL =>
         val f = interpreter.getOrElse {
            val res = InterpreterFrame( settings.replSettings )
            interpreter = Some( res )
            res.setAlwaysOnTop( true )
//            res.setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE )
            res.addComponentListener( new ComponentAdapter {
               override def componentHidden( e: ComponentEvent ) {
                  ggREPL.setSelected( false )
               }
            })
            // for some reason the console is lost,
            // this way restores it
            Console.setErr( System.err )
            Console.setOut( System.out )
            res
         }
         f.setVisible( true )
      }
   }

   def closeREPL() {
      interpreter.foreach( _.setVisible( false ))
   }

   private def defer( code: => Unit ) { EventQueue.invokeLater( new Runnable { def run() { code }})}

   def makeWindow( undecorated: Boolean = true ) : JFrame = {
      val f = new JFrame( "Nuages Controls" )
      if( undecorated ) f.setUndecorated( true )
      f.setContentPane( panel )
      f.pack()
      f
   }

   def meterUpdate( peakRMSPairs: IIdxSeq[ Float ]) {
      val tim = System.currentTimeMillis
      outMeterPanel.foreach( _.update( peakRMSPairs, 0, tim ))
      inMeterPanel.foreach(  _.update( peakRMSPairs, inDataOffset, tim ))
   }
}