package de.sciss.synth.proc

case class Period( v: Double ) extends Ordered[ Period ] {
   def compare( t2: Period ) = v.compare( t2.v )
}

case class Interval( start: Period, stop: Period ) {
   def contains( pos: Period ) : Boolean = pos >= start && pos < stop

   def contains( other: Interval ) : Boolean =
        (other.start >= start) && (other.stop <= stop)

   def overlaps( other: Interval ) : Boolean =
     ((other.start < stop) && (other.stop > start))

   def touches( other: Interval ) : Boolean =
     if( start <= other.start ) {
        stop >= other.start
     } else {
        other.stop >= start
     }
}