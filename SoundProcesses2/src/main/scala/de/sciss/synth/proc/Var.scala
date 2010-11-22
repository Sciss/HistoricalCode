package de.sciss.synth.proc

//trait Var[ C, V[ _ ], T ] extends Repr[ V[ T ]] {
//   def set( v: T )( implicit c: Ctx[ C, V ]) : Unit
//   def get( implicit c: Ctx[ C, V ]) : T
//}

trait EVar[ C, T ] {
   def get( implicit c: C ) : T
   def set( v: T )( implicit c: C ) : Unit
//   def addListener( l: Listener[ C, T ])( implicit c: ECtx ) : Unit
//   def removeListener( l: Listener[ C, T ])( implicit c: ECtx ) : Unit
}

trait KVar[ C, T ] extends EVar[ C, T ] {
   def range( vStart: Int, vStop: Int )( implicit c: ECtx ) : Traversable[ T ]
}
