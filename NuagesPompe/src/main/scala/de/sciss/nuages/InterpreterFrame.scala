/*
 *  ScalaInterpreterFrame.scala
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

import de.sciss.scalainterpreter.ScalaInterpreterPane
import java.awt.event.KeyEvent
import java.awt.{Toolkit, GraphicsEnvironment}
import javax.swing._

object InterpreterFrame {
   def apply( title: String = "Scala Interpreter" ) : InterpreterFrame = new InterpreterFrame( title )
}
class InterpreterFrame private( title: String ) // ( support: REPLSupport /* s: Server, ntp: NodeTreePanel*/ )
extends JFrame( title ) {
   val pane = new ScalaInterpreterPane
//   private val sync = new AnyRef
//   private var inCode: Option[ Interpreter => Unit ] = None
//   private var interpreter: Option[ Interpreter ] = None

   private val txnKeyStroke = {
      val ms = Toolkit.getDefaultToolkit.getMenuShortcutKeyMask
      KeyStroke.getKeyStroke( KeyEvent.VK_T, ms )
   }

   // ---- constructor ----
   {
      val cp = getContentPane

      pane.initialText = pane.initialText +
"""// Press '""" + KeyEvent.getKeyModifiersText( txnKeyStroke.getModifiers ) + " + " +
      KeyEvent.getKeyText( txnKeyStroke.getKeyCode ) + """' to execute transactionally.

"""

      pane.initialCode = Some(
"""
import math._
import de.sciss.synth._
import de.sciss.synth.ugen._
import de.sciss.synth.swing._
import de.sciss.synth.proc._
import de.sciss.synth.proc.DSL._
// XXX TODO import support._
"""
      )

// XXX TODO
//      pane.customBindings = Seq( NamedParam( "support", support ))

// XXX TODO
//      pane.out = Some( Setup.logPane.writer )

      pane.customKeyMapActions += txnKeyStroke -> (() => txnExecute())

      pane.init()
//      val sp = new JSplitPane( SwingConstants.HORIZONTAL )
//      sp.setTopComponent( pane )
//      sp.setBottomComponent( lp )
//      cp.add( sp )
      cp.add( pane )
      val b = GraphicsEnvironment.getLocalGraphicsEnvironment.getMaximumWindowBounds
      setSize( b.width / 2, b.height * 7 / 8 )
//      sp.setDividerLocation( b.height * 2 / 3 )
      setLocationRelativeTo( null )
//    setLocation( x, getY )
      setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE )
//    setVisible( true )
   }

   private var txnCount = 0

   def txnExecute() {
      pane.getSelectedTextOrCurrentLine.foreach( txt => {
         val txnId  = txnCount
         txnCount += 1
         val txnTxt = """class _txnBody""" + txnId + """( implicit t: ProcTxn ) {
""" + txt + """
}
val _txnRes""" + txnId + """ = ProcTxn.atomic( implicit t => new _txnBody""" + txnId + """ )
import _txnRes""" + txnId + """._
"""

//         println( txnTxt )
         pane.interpret( txnTxt )
      })
   }

//   def withInterpreter( fun: Interpreter => Unit ) {
//      sync.synchronized {
//println( "withInterpreter " + interpreter.isDefined )
//         interpreter.map( fun( _ )) getOrElse {
//            inCode = Some( fun )
//         }
//      }
//   }
}