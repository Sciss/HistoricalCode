package de.sciss.synth.proc

//object Proc {
//   case class Update( vars: Set[ AnyRef ])
//}

trait Proc[ C ] extends Model[ C, AnyRef ] with Named {
   def playing : Switch[ C ]
   def amp : Controller[ C ]
   def freq : Controller[ C ]
}