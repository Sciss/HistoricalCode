package de.sciss.synth.proc

import impl.ProcImpl

object Test {
   def main( args: Array[ String ]) { test2 }
   
   def test {
      implicit val sys = EphemeralSystem

      val p = new ProcImpl
      sys.t { implicit c =>
         println( "playing? " + p.playing.get )
         p.playing.set( true )
         println( "playing? " + p.playing.get )
      }
   }

   def test2 {
      implicit val sys = PTemporalSystem
      import DSL._

      val p = new ProcImpl
      sys.t { implicit c =>
         sys.at( 2.0 ) {
            p.playing.set( true )
         }
         println( "at 0.0 playing? " + p.playing.get )
         sys.at( 3.0 ) {
            println( "at 3.0 playing? " + p.playing.get )
         }
      }

      // KTemporalSystem
      // BitemporalSystem
   }
}