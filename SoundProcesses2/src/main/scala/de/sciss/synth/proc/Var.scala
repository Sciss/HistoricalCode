package de.sciss.synth.proc

trait Var[ C, /* @specialized */ T ] {
//   def apply( implicit c: C ) : T
//   def update( v: T )( implicit c: C ) : Unit
   def get( implicit c: C ) : T
   def set( v: T )( implicit c: C ) : Unit
}