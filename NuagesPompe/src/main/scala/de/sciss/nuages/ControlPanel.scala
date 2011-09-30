/*
 *  ControlPanel.scala
 *  (NuagesPompe)
 *
 *  Copyright (c) 2010-2011 Hanns Holger Rutz. All rights reserved.
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
 *
 *
 *  Changelog:
 */

package de.sciss.nuages

import java.awt.event.{ComponentEvent, ComponentAdapter}
import de.sciss.gui.j.PeakMeter
import java.io.PrintStream
import java.awt.{Font, Color}
import javax.swing._
import de.sciss.scalainterpreter.LogPane

//import Setup._
//import de.sciss.synth.proc.ProcTxn
import collection.immutable.{IndexedSeq => IIdxSeq}

class ControlPanel( masterNumChannels: Int ) /* ( tapesPanel: JComponent ) */ extends BasicPanel {
   panel =>

   private val masterMeterPanel  = new PeakMeter()
// XXX TODO
//   private val peopleOffset      = masterBus.numChannels << 1
//   private val peopleMeterPanel: Option[ PeakMeter ] =
//      if( PEOPLE_CHANGROUPS.nonEmpty ) Some( new PeakMeter() ) else None
   
   private var interpreter : Option[ ScalaInterpreterFrame ] = None

   private val ggClock = new Wallclock

   private def space( width: Int = 8 ) {
      panel.add( Box.createHorizontalStrut( width ))
   }

  val logPane = {
     val res = new LogPane( 2, 30 )
     res.init()
     val scroll = res.getComponent( 0 ).asInstanceOf[ JScrollPane ]
     scroll.setBorder( null )
     scroll.getViewport.getView.setFont( new Font( "Menlo", Font.PLAIN, 8 ))
     val printStream = new PrintStream( res.outputStream )
     System.setErr( printStream )
     System.setOut( printStream )
//      ggLog.writer.write( "Make noise.\n" )
     Console.setErr( res.outputStream )
     Console.setOut( res.outputStream )
     res
  }

   {
      panel.setLayout( new BoxLayout( panel, BoxLayout.X_AXIS ))

      val ggRecStart = BasicButton( "\u25B6" ) {
// XXX TODO
//         ProcTxn.spawnAtomic { implicit tx =>
//            if( Nuages.startRecorder ) tx.afterCommit( _ => defer {
               ggClock.reset()
               ggClock.start()
//            })
//         }
      }
      ggRecStart.setBackground( Color.black )
      ggRecStart.setForeground( Color.white )
      panel.add( ggRecStart )
      val ggRecStop = BasicButton( "\u25FC" ) {
// XXX TODO
//         ProcTxn.spawnAtomic { implicit tx =>
//            Nuages.stopRecorder
//            tx.afterCommit( _ => defer {
               ggClock.stop()
//            })
//         }
      }
      ggRecStop.setBackground( Color.black )
      ggRecStop.setForeground( Color.white )
      panel.add( ggRecStop )
      panel.add( ggClock )
      space()

//      val ggTapes = new JToggleButton( "Tapes" )
//      ggTapes.setUI( new BasicToggleButtonUI )
//      ggTapes.putClientProperty( "JButton.buttonType", "bevel" )
//      ggTapes.putClientProperty( "JComponent.sizeVariant", "small" )
//      ggTapes.setFocusable( false )
//      ggTapes.addActionListener( new ActionListener {
//         def actionPerformed( e: ActionEvent ) {
//            val sel = ggTapes.isSelected
//            tapesPanel.setVisible( sel )
//            if( sel ) tapesPanel.toFront()
//         }
//      })
//      tapesPanel.addComponentListener( new ComponentAdapter {
//         override def componentHidden( e: ComponentEvent ) {
//            ggTapes.setSelected( false )
//         }
//      })

//      val ggTapes = BasicButton( "Tapes" ) {
//         val p = Nuages.f.panel
//         val x = (p.getWidth - tapesPanel.getWidth) >> 1
//         val y = (p.getHeight - tapesPanel.getHeight) >> 1
//         p.showOverlayPanel( tapesPanel, new Point( x, y ))
//      }
//
////      panel.add( Box.createHorizontalStrut( 4 ))
//      panel.add( ggTapes )
//      panel.add( Box.createHorizontalStrut( 4 ))

//      val m1 = new PeakMeter( SwingConstants.HORIZONTAL )
//      val m2 = new PeakMeter( SwingConstants.HORIZONTAL )
//      val mg = new PeakMeterGroup( Array( m1, m2 ))
//      panel.add( m1 )
//      panel.add( m2 )

//      val numCh = masterBus.numChannels
     val numCh = masterNumChannels
      masterMeterPanel.orientation = SwingConstants.HORIZONTAL
      masterMeterPanel.numChannels = numCh
      masterMeterPanel.borderVisible = true
      val d = masterMeterPanel.getPreferredSize
      val dn = 30 / numCh
      d.height = numCh * dn + 7
      masterMeterPanel.setPreferredSize( d )
      masterMeterPanel.setMaximumSize( d )
      panel.add( masterMeterPanel )

// XXX TODO
//      peopleMeterPanel.foreach { p =>
////         p.setOrientation( SwingConstants.HORIZONTAL )
//         p.orientation = SwingConstants.HORIZONTAL
////         p.setNumChannels( PEOPLE_CHANGROUPS.size )
//         p.numChannels = PEOPLE_CHANGROUPS.size
////         p.setBorder( true )
//         p.borderVisible = true
//         val d = p.getPreferredSize
//         val dn = 30 / numCh
//         d.height = numCh * dn + 7
//         p.setPreferredSize( d )
//         p.setMaximumSize( d )
//         panel.add( p )
//      }

      val d1 = logPane.getPreferredSize
      d1.height = d.height
      logPane.setPreferredSize( d1 )
      space()
      panel.add( logPane )
//      space( 16 )

      val glue = Box.createHorizontalGlue()
glue.setBackground( Color.black )
//      glue.setBackground( Color.darkGray )
      panel.add( glue )

      lazy val ggInterp: JToggleButton = BasicToggleButton( "REPL" ) { sel =>
         if( sel ) {
            val f = interpreter.getOrElse {
               val res = new ScalaInterpreterFrame( /* support */ /* ntp */ )
               interpreter = Some( res )
               res.setAlwaysOnTop( true )
               res.setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE )
               res.addComponentListener( new ComponentAdapter {
                  override def componentHidden( e: ComponentEvent ) {
                     ggInterp.setSelected( false )
                  }
               })
               // for some reason the console is lost,
               // this way restores it
               Console.setErr( System.err )
               Console.setOut( System.out )
               res
            }
            f.setVisible( true )
         } else interpreter.foreach( _.setVisible( false ))
      }
      panel.add( ggInterp )
   }

//   private def defer( code: => Unit ) { EventQueue.invokeLater( new Runnable { def run() { code }})}

   def makeWindow : JFrame = makeWindow()
   def makeWindow( undecorated: Boolean = true ) : JFrame = {
      val f = new JFrame( "Nuages Controls" )
      if( undecorated ) f.setUndecorated( true )
//      val cp = f.getContentPane()
//      cp.add( panel, BorderLayout.CENTER )
      f.setContentPane( panel )
      f.pack()
      f
   }

   def meterUpdate( peakRMSPairs: IIdxSeq[ Float ]) {
      val tim = System.currentTimeMillis 
//      masterMeterPanel.meterUpdate( peakRMSPairs, 0, tim )
      masterMeterPanel.update( peakRMSPairs, 0, tim )
// XXX TODO
//      peopleMeterPanel.foreach( _.update( peakRMSPairs, peopleOffset, tim ))
   }
}