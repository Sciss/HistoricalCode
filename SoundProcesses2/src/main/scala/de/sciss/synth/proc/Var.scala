package de.sciss.synth.proc

trait Var[ C, V[ _ ], T ] extends Repr[ V[ T ]] {
   def set( v: T )( implicit c: Ctx[ C, V ]) : Unit
   def get( implicit c: Ctx[ C, V ]) : T
}
