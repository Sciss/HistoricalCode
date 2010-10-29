package de.sciss.synth.proc

trait Ctx[ Repr ] {
   def repr : Repr
   def system : System[ Repr ]
   def v[ T ]( init : T )( implicit m: ClassManifest[ T ]) : Var[ Repr, T ]
}
