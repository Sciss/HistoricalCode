package de.sciss.synth.proc

object DSL {
   implicit def doubleToTime( d: Double ) = Time( d )
}