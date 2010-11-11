package de.sciss.synth.proc

trait Proc[ C ] {
//   def playing : Switch[ C ]
//   def amp : Controller[ C ]
//   def freq : Controller[ C ]
   def playing : Var[ C, Boolean ]
   def amp : Var[ C, Double ]
   def freq : Var[ C, Double ]
}