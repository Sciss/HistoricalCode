package de.sciss.synth.proc

trait Cursor[ C ] {
//   def repr: Repr
   def t[ T ]( fun: Ctx[ C ] => T ) : T
   def isApplicable( implicit c: Ctx[ C ]) : Boolean
//   def ctx: Ctx[ C ]  // XXX hmmm, not so good
}