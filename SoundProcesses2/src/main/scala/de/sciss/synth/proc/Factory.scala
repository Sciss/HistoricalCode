package de.sciss.synth.proc

import impl.{ProcGroupImpl, ProcImpl}

object Factory {
   def proc[ C ]( name: String )( implicit c: Ctx[ C ]) : Proc[ C ] = new ProcImpl( name )
   def group[ C ]( name: String )( implicit c: Ctx[ C ]) : ProcGroup[ C ] = new ProcGroupImpl( name )
}