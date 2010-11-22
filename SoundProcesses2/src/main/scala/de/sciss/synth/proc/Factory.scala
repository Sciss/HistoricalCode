//package de.sciss.synth.proc
//
//import impl.{ProcGroupImpl, ProcImpl}
//
//object Factory {
//   def proc[ C, V[ _ ]]( name: String )( implicit c: Ctx[ C, V ]) : Proc[ C, V ] =
//      new ProcImpl[ C, V ]( name )
//
//   def group[ C, V[ _ ]]( name: String )( implicit c: Ctx[ C, V ]) : ProcGroup[ C, V ] =
//      new ProcGroupImpl[ C, V ]( name )
//}