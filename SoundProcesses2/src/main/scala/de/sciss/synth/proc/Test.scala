package de.sciss.synth.proc

import impl.ProcImpl

object Test {
   def main( args: Array[ String ]) { test }
   
   def test {
      implicit val sys = EphemeralSystem

      val p = new ProcImpl
      sys.t { implicit c =>
         println( "playing? " + p.playing.get )
         p.playing.set( true )
         println( "playing? " + p.playing.get )
      }

      // PTemporalSystem
      // KTemporalSystem
      // BitemporalSystem
   }
}