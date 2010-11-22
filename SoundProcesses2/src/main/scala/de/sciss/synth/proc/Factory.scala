package de.sciss.synth.proc

object Factory {
   def proc[ C <: Ct, V[_] <: Vr[ C, _ ]]( name: String )( implicit c: C ) : Proc[ C, V ] =
      error( "SCHNUCKI 3000" ) // new ProcImpl[ C, V ]( name )

   def group[ C <: Ct, V[_] <: Vr[ C, _ ]]( name: String )( implicit c: C ) : ProcGroup[ C, V ] =
      error( "BLABLA" ) // new ProcGroupImpl[ C, V ]( name )

   def ksystem : KSystem = error( "SCREW YOU" )
}