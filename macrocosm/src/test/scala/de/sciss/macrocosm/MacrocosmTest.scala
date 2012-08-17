package de.sciss.macrocosm

object MacrocosmTest extends App {
   import Macrocosm._

   println( "Aqui" )
   LPF.ar( new GE { val rate = audio })
}
