package de.sciss.synth.proc

trait Cursor[ C ] {
   def t[ T ]( fun: Ctx[ C ] => T ) : T
   def isApplicable( implicit c: Ctx[ C ]) : Boolean
}