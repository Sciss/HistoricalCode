package de.sciss.synth.proc.view

import java.awt.EventQueue

object GUIUtils {
   def defer( thunk: => Unit ) { EventQueue.invokeLater( new Runnable { def run = thunk })}
}