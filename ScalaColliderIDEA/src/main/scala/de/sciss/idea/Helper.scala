package de.sciss.idea

import com.intellij.util.ActionRunner

object Helper {
   implicit def thunkToRunnable( code: => Unit ) = new Runnable { def run = code }
   implicit def thunkToInterruptibleRunnable( code: => Unit ) = new ActionRunner.InterruptibleRunnable { def run = code }
}