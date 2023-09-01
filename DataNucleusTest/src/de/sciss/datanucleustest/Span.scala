package de.sciss.datanucleustest

import javax.jdo.annotations.{ PersistenceCapable }

@PersistenceCapable
case class Span( startVar: Long, stopVar: Long )

/*
object Span {
   def apply( start: Long, stop: Long ) : Span = new Span( start, stop )
}

@PersistenceCapable
class Span private( istart: Long, istop: Long ) {
   private def this() = this( 0L, 0L )
   
   private var startVar = istart
   private var stopVar  = istop

   def start = startVar
   def stop  = stopVar

   override def toString = "Span(" + start + ", " + stop + ")"
}
*/
