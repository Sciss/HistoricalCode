/*
 *  AudioWidgets.scala
 *  (AudioWidgets)
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

package de.sciss.gui

import java.awt.{EventQueue, BorderLayout}
import javax.swing.{Timer, JFrame, WindowConstants}
import java.awt.event.{WindowEvent, WindowAdapter, ActionEvent, ActionListener}
import swing.event.{WindowClosing, WindowOpened}
import swing.{Swing, BorderPanel, MainFrame, Frame, SimpleSwingApplication, SwingApplication}

object ScalaAudioWidgets extends SimpleSwingApplication {
   val name          = "ScalaAudioWidgets"
   val version       = 0.10
   val copyright     = "(C)opyright 2011 Hanns Holger Rutz"
   val isSnapshot    = true

   def versionString = {
      val s = (version + 0.001).toString.substring( 0, 4 )
      if( isSnapshot ) s + "-SNAPSHOT" else s
   }

   lazy val top = new MainFrame {
      title = name

      val m = new PeakMeter
      m.ticks  = 60
      contents = new BorderPanel {
         add( m, BorderPanel.Position.West )
         border = Swing.EmptyBorder( 20, 20, 20, 20 )
      }

      val t = new Timer( 30, new ActionListener {
         val rnd  = new util.Random()
         var peak = 0.5f
         var rms  = 0f
         def actionPerformed( e: ActionEvent ) {
            peak = math.max( 0f, math.min( 1f, peak + math.pow( rnd.nextFloat() * 0.5, 2 ).toFloat * (if( rnd.nextBoolean() ) 1 else -1) ))
            rms  = math.max( 0f, math.min( peak, rms * 0.98f + (rnd.nextFloat() * 0.02f * (if( rnd.nextBoolean() ) 1 else -1) )))
            m.update( peak, rms )
         }
      })
      reactions += {
         case WindowOpened( top )   => t.start()
         case WindowClosing( top )  => t.stop()
      }
   }
}