package de.sciss.macrocosm

object MacrocosmTest extends App {
   import Macrocosm._

//   println( "Aqui" )

   object WhiteNoiseAr extends GE { val rate = audio }

   println( "---- anon ----" )
   LPF.ar( new GE { val rate = audio })
   println( "---- WhiteNoise.ar ----" )
   LPF.ar( WhiteNoiseAr )
   println( "---- val -> WhiteNoise.ar ----" )
   val wn = WhiteNoiseAr
   LPF.ar( wn )
   println( "---- val:GE -> WhiteNoise.ar ----" )
   val wn2: GE = WhiteNoiseAr
   LPF.ar( wn2 )
}
