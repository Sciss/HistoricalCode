package de.sciss.synth.proc

import impl.ProcImpl

object Test {
   def main( args: Array[ String ]) { test }
   
   def test {
      val sys = EphemeralSystem

      sys.t { implicit c =>
         val p = new ProcImpl
         println( "playing? " + p.playing.get )
         p.playing.set( true )
         println( "playing? " + p.playing.get )
      }
   }

//   def test2 {
//      implicit val sys = PTemporalSystem
//      import DSL._
//
//      val p = new ProcImpl
//      sys.t { implicit c =>
//         sys.at( 2.0 ) {
//            p.playing.set( true )
//         }
//         sys.during( 4.0 -> 6.0 ) {
//            p.playing.set( false )
//         }
//         (0.0 to 6.0 by 1.0).foreach( t => sys.at( t ) {
//            println( "at " + t + " playing? " + p.playing.get )
//         })
//      }
//   }
//
//   def test3 {
//      implicit val sys = KTemporalSystem
//
//      val p = new ProcImpl
//      sys.t { implicit c =>
//         println( "playing? " + p.playing.get )
//         p.playing.set( true )
//         println( "playing? " + p.playing.get )
//      }
//
//      // BitemporalSystem
//   }
}