package de.sciss.synth.proc

trait Proc[ C ] {
   def playing : Switch[ C ]
   def amp : Controller[ C ]
   def freq : Controller[ C ]
}