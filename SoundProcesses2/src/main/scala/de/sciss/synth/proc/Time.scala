package de.sciss.synth.proc

case class Period( v: Double ) extends Ordered[ Period ] {
   def compare( t2: Period ) = v.compare( t2.v )
}

case class Interval( start: Period, end: Period )