package de.sciss.synth.proc

trait Cursor[ C, V[ _ ]] {
//   def repr: Repr
   def t[ T ]( fun: Ctx[ C, V ] => T ) : T
   def isApplicable( implicit c: Ctx[ C, V ]) : Boolean
//   def ctx: Ctx[ C ]  // XXX hmmm, not so good
}