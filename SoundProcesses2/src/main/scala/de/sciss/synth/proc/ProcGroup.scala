package de.sciss.synth.proc

object ProcGroup {
   sealed trait Update[ C ]
   case class ProcAdded[ C ]( /* idx: Int, */ p: Proc[ C ]) extends Update[ C ]
   case class ProcRemoved[ C ]( /* idx: Int, */ p: Proc[ C ]) extends Update[ C ]
}

trait ProcGroup[ C ] extends Model[ C, ProcGroup.Update[ C ]] with Named {
   def add( p: Proc[ C ])( implicit c: Ctx[ C ]) : Unit
   def remove( p: Proc[ C ])( implicit c: Ctx[ C ]) : Unit
}