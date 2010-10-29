package de.sciss.synth.proc

trait System[ Repr ] {
   def t[ T ]( fun: Ctx[ Repr ] => T ) : T
}
