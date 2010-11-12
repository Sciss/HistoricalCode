package de.sciss.synth.proc

object Proc {
   case class Update( vars: Set[ AnyRef ])
}

trait Proc[ C ] extends Model[ C, Proc.Update ] {
   def playing : Switch[ C ]
   def amp : Controller[ C ]
   def freq : Controller[ C ]
}