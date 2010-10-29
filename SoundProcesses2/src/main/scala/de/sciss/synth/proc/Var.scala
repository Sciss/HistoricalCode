package de.sciss.synth.proc

trait Var[ Repr, T ] {
   def set( v: T )( implicit c: Ctx[ Repr ]) : Unit
   def get( implicit c: Ctx[ Repr ]) : T
}
