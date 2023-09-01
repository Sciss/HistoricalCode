/*
 *  ScalaInterpreterFrame.scala
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

import java.awt.event.KeyEvent
import java.awt.{Toolkit, GraphicsEnvironment}
import javax.swing._
import tools.nsc.interpreter.NamedParam
import collection.immutable.{IndexedSeq => IIdxSeq}
import de.sciss.scalainterpreter.{CodePane, Interpreter, InterpreterPane}

object InterpreterFrame {
   def apply( settings: Settings = SettingsBuilder().build ) : InterpreterFrame = new InterpreterFrame( settings )

   sealed trait SettingsLike {
      /**
       * Value bindings for the interpreter.
       */
      def bindings : IIdxSeq[ NamedParam ]

      /**
       * Packages initially imported when the interpreter starts up.
       */
      def imports: IIdxSeq[ String ]

      /**
       * Text initially placed in the pane widget.
       */
      def text: String

      /**
       * Initial code to execute upon interpreter startup. Use an empty string
       * to skip initial code execution.
       */
      def code: String

      /**
       * Title of the frame.
       */
      def title : String

      /**
       * Close operation of the frame.
       */
      def closeOperation : Int

   }

   sealed trait Settings extends SettingsLike

   object SettingsBuilder {
      def apply() : SettingsBuilder = new SettingsBuilder()
      def fromSettings( s: SettingsLike ) : SettingsBuilder = {
         val b             = new SettingsBuilder()
         b.bindings        = s.bindings
         b.imports         = s.imports
         b.text            = s.text
         b.code            = s.code
         b.title           = s.title
         b.closeOperation  = s.closeOperation
         b
      }
   }

   final class SettingsBuilder private () extends SettingsLike {
      var bindings         = IIdxSeq.empty[ NamedParam ]
      var imports          = IIdxSeq(
         "math._",
         "de.sciss.synth._",
         "de.sciss.synth.ugen._",
         "de.sciss.synth.swing._",
         "de.sciss.synth.proc._",
         "de.sciss.synth.proc.DSL._"
      )
      var title            = "Scala Interpreter"
      var closeOperation   = WindowConstants.HIDE_ON_CLOSE
      var text             = ""
      var code             = ""

      def build : Settings = SettingsImpl( bindings, imports, text, code, title, closeOperation )
   }

   private final case class SettingsImpl( bindings: IIdxSeq[ NamedParam ],
                                          imports: IIdxSeq[ String ], text: String, code: String,
                                          title: String, closeOperation: Int )
   extends Settings
}
class InterpreterFrame private( val settings: InterpreterFrame.Settings )
extends JFrame( settings.title ) {
   private val txnKeyStroke = {
      val ms = Toolkit.getDefaultToolkit.getMenuShortcutKeyMask
      KeyStroke.getKeyStroke( KeyEvent.VK_T, ms )
   }

   private val codePane = {
      val cCfg = CodePane.Config()
      cCfg.text = """// Press '""" + KeyEvent.getKeyModifiersText( txnKeyStroke.getModifiers ) + " + " +
         KeyEvent.getKeyText( txnKeyStroke.getKeyCode ) + """' to execute transactionally.

""" + settings.text
      cCfg.keyMap += txnKeyStroke -> (() => txnExecute())
      CodePane( cCfg )
   }

   private val intp = {
      val iCfg = Interpreter.Config()
      iCfg.bindings  = settings.bindings
      iCfg.imports   = settings.imports
      Interpreter( iCfg )
   }

   val pane = {
      val cfg  = InterpreterPane.Config()
      if( settings.code.length > 0 ) cfg.code = settings.code
//      InterpreterPane( cfg, iCfg, cCfg )
      InterpreterPane.wrap( intp, codePane )
   }
//   private val sync = new AnyRef
//   private var inCode: Option[ Interpreter => Unit ] = None
//   private var interpreter: Option[ Interpreter ] = None

   // ---- constructor ----
   {
      val cp = getContentPane

//      pane.initialText = pane.initialText +
//"""// Press '""" + KeyEvent.getKeyModifiersText( txnKeyStroke.getModifiers ) + " + " +
//      KeyEvent.getKeyText( txnKeyStroke.getKeyCode ) + """' to execute transactionally.
//
//""" + settings.text

//      pane.initialCode     = if( settings.code.isEmpty ) None else Some( settings.code )
//      pane.customBindings  = settings.bindings
//      pane.customImports   = settings.imports

// XXX TODO
//      pane.out = Some( Setup.logPane.writer )

//      pane.customKeyMapActions += txnKeyStroke -> (() => txnExecute())

//      pane.init()
//      val sp = new JSplitPane( SwingConstants.HORIZONTAL )
//      sp.setTopComponent( pane )
//      sp.setBottomComponent( lp )
//      cp.add( sp )
      cp.add( pane.component )
      val b = GraphicsEnvironment.getLocalGraphicsEnvironment.getMaximumWindowBounds
      setSize( b.width / 2, b.height * 7 / 8 )
//      sp.setDividerLocation( b.height * 2 / 3 )
      setLocationRelativeTo( null )
//    setLocation( x, getY )
      setDefaultCloseOperation( settings.closeOperation )
//    setVisible( true )
   }

   private var txnCount = 0

   def txnExecute() {
      codePane.getSelectedTextOrCurrentLine.foreach( txt => {
         val txnId  = txnCount
         txnCount += 1
         val txnTxt = """class _txnBody""" + txnId + """( implicit t: ProcTxn ) {
""" + txt + """
}
val _txnRes""" + txnId + """ = ProcTxn.atomic( implicit t => new _txnBody""" + txnId + """ )
import _txnRes""" + txnId + """._
"""

//         println( txnTxt )
         intp.interpret( txnTxt )
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