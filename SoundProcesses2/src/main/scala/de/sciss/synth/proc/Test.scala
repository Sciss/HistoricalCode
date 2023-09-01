/*
 *  Test.scala
 *  (SoundProcesses)
 *
 *  Copyright (c) 2009-2010 Hanns Holger Rutz. All rights reserved.
 *
 *	 This software is free software; you can redistribute it and/or
 *	 modify it under the terms of the GNU General Public License
 *	 as published by the Free Software Foundation; either
 *	 version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	 This software is distributed in the hope that it will be useful,
 *	 but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	 General Public License for more details.
 *
 *	 You should have received a copy of the GNU General Public
 *	 License (gpl.txt) along with this software; if not, write to the Free Software
 *	 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	 For further information, please contact Hanns Holger Rutz at
 *	 contact@sciss.de
 *
 *
 *  Changelog:
 */

package de.sciss.synth.proc

import de.sciss.confluent.VersionPath
import java.awt.{BorderLayout, EventQueue}
import javax.swing.{Box, JButton, JFrame, WindowConstants}
import java.awt.event.{ActionListener, ActionEvent}
import view.{OfflineVisualView, GroupView, VersionGraphView}

object Test {
   def main( args: Array[ String ]) { test5 }

   def test5 {
    EventQueue.invokeLater( new Runnable { def run {
       type MyCtx = KCtx
       type MyVar[~] = KSystem.Var[~]
       type MyCsr = KSystem.Cursor
       implicit val sys = Factory.ksystem // BitemporalSystem()
//      type MyCtx = BCtx
//      type MyVar[~] = BSystem.Var[~]
//      type MyCsr = BSystem.KCursor
//      implicit val sys = Factory.bsystem // BitemporalSystem()

       // bloody hell
       val pg = sys.keProjector.in( VersionPath.init ) { implicit c => Factory.group[ MyCtx, MyVar ]( "g1" )( sys, c )}
//      val pg: ProcGroup[ MyCtx, MyVar ] = null

      val vv = new VersionGraphView[ MyCtx, MyVar, MyCsr ]( sys )
      val f  = new JFrame( "Version Graph" )
      val b  = Box.createHorizontalBox()
      val ggCursor         = new JButton( "Add Cursor" )
      val ggGroupView      = new JButton( "View Group" )
      val ggTimelineView   = new JButton( "View Timeline" )
      b.add( ggCursor )
      b.add( ggGroupView )
      b.add( ggTimelineView )
      b.add( Box.createHorizontalGlue() )
      ggCursor.addActionListener( new ActionListener {
         def actionPerformed( e: ActionEvent ) {
            vv.selection match {
               case (path, Nil) :: Nil => sys.t { implicit c => sys.kProjector.cursorIn( path )}
               case _ =>
            }
         }
      })
      ggGroupView.addActionListener( new ActionListener {
         def actionPerformed( e: ActionEvent ) {
            vv.selection match {
               case (path, csr :: Nil) :: Nil => sys.keProjector.in( path ) { implicit c =>
                  new GroupView[ MyCtx, MyVar ]( sys, pg, csr )
               }
               case _ =>
            }
         }
      })
      ggTimelineView.addActionListener( new ActionListener {
         def actionPerformed( e: ActionEvent ) {
            vv.selection match {
               case (path, csr :: Nil) :: Nil => sys.keProjector.in( path ) { implicit c =>
                  new OfflineVisualView[ MyCtx, MyVar ]( sys, pg, csr )
               }
               case _ =>
            }
         }
      })
      ggCursor.setEnabled( false )
      ggGroupView.setEnabled( false )
      ggTimelineView.setEnabled( false )
      vv.addSelectionListener { sel =>
//         println( "JO, SEL = " + sel )
         val (en1, en2) = sel match {
            case (path, Nil) :: Nil          => (true, false)
            case (path, csr :: Nil) :: Nil   => (true, true)
            case _                           => (false, false)
         }
         ggCursor.setEnabled( en1 )
         ggGroupView.setEnabled( en2 )
         ggTimelineView.setEnabled( en2 )
      }
      val cp = f.getContentPane()
      cp.add( vv.panel, BorderLayout.CENTER )
      cp.add( b, BorderLayout.SOUTH )
      f.pack() // setSize( 300, 300 )
      f.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE ) // DISPOSE_ON_CLOSE
      f.setLocationRelativeTo( null )
      f.setVisible( true )

     }})
   }

//   def test {
//      val sys = EphemeralSystem
//
//      sys.t { implicit c =>
//         val p = Factory.proc( "p1" )
//         println( "playing? " + p.playing.get )
//         p.playing.set( true )
//         println( "playing? " + p.playing.get )
//      }
//   }

//   def test2 {
//      val sys = PTemporalSystem()
//      import DSL._
//
//      sys.t { implicit c =>
//         val p = Factory.proc( "p1" )
//         sys.at( 2.0 ) {
//            p.playing.set( true )
//         }
//         sys.during( 4.0 -> 6.0 ) {
//            p.playing.set( false )
//         }
//         (0.0 to 6.0 by 1.0).foreach( t => sys.at( t ) {
//            println( "at " + t + " playing? " + p.playing.get )
//         })
//      }
//   }

//   def test3 {
//      val sys = KTemporalSystem()
//
//      val v0 = VersionPath.init
//      val (p, v1) = sys.in( v0 ) { implicit c =>
//         val p = Factory.proc( "p1" )
//         println( "playing? " + p.playing.get )
//         (p, c.repr.path)
//      }
//
//      val v2 = sys.in( v1 ) { implicit c =>
//         val res = c.repr.path
//         p.playing.set( true )
//         println( "playing? " + p.playing.get )
//         c.repr.path
//      }
//
//      sys.in( v1 ) { implicit c =>
//         println( "in " + v1 + " : playing? " + p.playing.get )
//      }
//
//      sys.in( v2 ) { implicit c =>
//         println( "in " + v2 + " : playing? " + p.playing.get )
//      }
//   }

//   def test4 {
//      val sys = BitemporalSystem()
//      import DSL._
//
//      val v0 = VersionPath.init
//      val (p, v1) = sys.in( v0 ) { implicit c =>
//         val p = Factory.proc( "p1" )
//
//         p.addListener( new Model.Listener[ Bitemporal, Proc.Update ] {
//            def updated( u: Proc.Update )( implicit c: Ctx[ Bitemporal ]) {
////               println( "updated: " + what )
//               u.what match {
//                  case (s: Switch[ _ ], on: Boolean) => {
//                     println( s.name + " " + (if( on ) "ON " else "OFF") + " in " + c.repr.path + " during " + c.repr.interval )
//                  }
//                  case _ =>
//               }
//            }
//         })
//
//         sys.at( 2.0 ) {
//            p.playing.set( true )
//         }
//         sys.during( 4.0 -> 6.0 ) {
//            p.playing.set( false )
//         }
//         (p, c.repr.path)
//      }
//
//      val v2 = sys.in( v1 ) { implicit c =>
//         sys.at( 3.0 ) {
//            p.playing.set( false )
//         }
//         sys.during( 5.0 -> 6.0 ) {
//            p.playing.set( true )
//         }
//         c.repr.path
//      }
//
//      sys.in( v1 ) { implicit c =>
//         (0.0 to 6.0 by 1.0).foreach( t => sys.at( t ) {
//            println( "in " + v1 + " at " + t + " playing? " + p.playing.get )
//         })
//      }
//
//      println()
//
//      sys.in( v2 ) { implicit c =>
//         (0.0 to 6.0 by 1.0).foreach( t => sys.at( t ) {
//            println( "in " + v2 + " at " + t + " playing? " + p.playing.get )
//         })
//      }
//   }
}