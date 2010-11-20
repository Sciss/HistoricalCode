package de.sciss.synth.proc

import impl.{ProcGroupImpl, ProcImpl}

object Factory {
   def proc[ K, P ]( name: String )( implicit c: Ctx[ K ], p: PFactory[ P ]) : Proc[ K, P ] =
      new ProcImpl( name )

   def group[ K, P ]( name: String )( implicit c: Ctx[ K ], p: PFactory[ P ]) : ProcGroup[ K, P ] =
      new ProcGroupImpl( name )
}