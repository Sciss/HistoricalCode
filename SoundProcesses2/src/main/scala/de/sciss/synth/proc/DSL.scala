package de.sciss.synth.proc

object DSL {
   implicit def doubleToPeriod( d: Double ) = Period( d )
   implicit def periodsToInterval[ T <% Period ]( tup: (T, T) ) = Interval( tup._1, tup._2 )
}