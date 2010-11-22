package de.sciss.synth.proc

import impl.{ESystemImpl, ProcGroupImpl, KSystemImpl}

object Factory {
   def proc[ C <: Ct, V[_] <: Vr[ C, _ ]]( name: String )( implicit sys: System[ C, V ], c: C ) : Proc[ C, V ] =
      error( "SCHNUCKI 3000" ) // new ProcImpl[ C, V ]( name )

   def group[ C <: Ct, V[_] <: Vr[ C, _ ]]( name: String )( implicit sys: System[ C, V ], c: C ) : ProcGroup[ C, V ] =
      ProcGroupImpl[ C, V ]( name )

   def ksystem : KSystem = KSystemImpl()
   def esystem : ESystem = ESystemImpl
}