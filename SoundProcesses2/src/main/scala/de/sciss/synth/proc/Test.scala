package de.sciss.synth.proc

import impl.ProcImpl
import de.sciss.confluent.VersionPath

object Test {
   def main( args: Array[ String ]) { test4 }
   
   def test {
      val sys = EphemeralSystem

      sys.t { implicit c =>
         val p = new ProcImpl
         println( "playing? " + p.playing.get )
         p.playing.set( true )
         println( "playing? " + p.playing.get )
      }
   }

   def test2 {
      val sys = PTemporalSystem
      import DSL._

      sys.t { implicit c =>
         val p = new ProcImpl
         sys.at( 2.0 ) {
            p.playing.set( true )
         }
         sys.during( 4.0 -> 6.0 ) {
            p.playing.set( false )
         }
         (0.0 to 6.0 by 1.0).foreach( t => sys.at( t ) {
            println( "at " + t + " playing? " + p.playing.get )
         })
      }
   }

   def test3 {
      val sys = KTemporalSystem

      val v0 = VersionPath.init
      val (p, v1) = sys.in( v0 ) { implicit c =>
         val p = new ProcImpl
         println( "playing? " + p.playing.get )
         (p, c.repr.path)
      }

      val v2 = sys.in( v1 ) { implicit c =>
         val res = c.repr.path
         p.playing.set( true )
         println( "playing? " + p.playing.get )
         c.repr.path
      }

      sys.in( v1 ) { implicit c =>
         println( "in " + v1 + " : playing? " + p.playing.get )
      }

      sys.in( v2 ) { implicit c =>
         println( "in " + v2 + " : playing? " + p.playing.get )
      }
   }

   def test4 {
      val sys = BitemporalSystem
      import DSL._

      val v0 = VersionPath.init
      val (p, v1) = sys.in( v0 ) { implicit c =>
         val p = new ProcImpl
         sys.at( 2.0 ) {
            p.playing.set( true )
         }
         sys.during( 4.0 -> 6.0 ) {
            p.playing.set( false )
         }
         (p, c.repr.path)
      }

      val v2 = sys.in( v1 ) { implicit c =>
         sys.at( 3.0 ) {
            p.playing.set( false )
         }
         sys.during( 5.0 -> 6.0 ) {
            p.playing.set( true )
         }
         c.repr.path
      }

      sys.in( v1 ) { implicit c =>
         (0.0 to 6.0 by 1.0).foreach( t => sys.at( t ) {
            println( "in " + v1 + " at " + t + " playing? " + p.playing.get )
         })
      }

      println()

      sys.in( v2 ) { implicit c =>
         (0.0 to 6.0 by 1.0).foreach( t => sys.at( t ) {
            println( "in " + v2 + " at " + t + " playing? " + p.playing.get )
         })
      }
   }
}