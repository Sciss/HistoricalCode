package de.sciss.synth.proc

import impl.{ProcImpl, ESystemImpl, ProcGroupImpl, KSystemImpl}

object Factory {
   def proc[ C <: Ct, V[ ~ ] <: Vr[ C, ~ ]]( name: String )( implicit sys: System[ C, V ], c: C ) : Proc[ C, V ] =
      ProcImpl[ C, V ]( name )

   def group[ C <: Ct, V[ ~ ] <: Vr[ C, ~ ]]( name: String )( implicit sys: System[ C, V ], c: C ) : ProcGroup[ C, V ] =
      ProcGroupImpl[ C, V ]( name )

   def ksystem : KSystem = KSystemImpl()
   def esystem : ESystem = ESystemImpl
}