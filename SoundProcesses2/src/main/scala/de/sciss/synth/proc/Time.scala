package de.sciss.synth.proc

case class Time( v: Double ) extends Ordered[ Time ] {
   def compare( t2: Time ) = v.compare( t2.v )
}