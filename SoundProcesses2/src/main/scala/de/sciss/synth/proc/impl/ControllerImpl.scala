package de.sciss.synth.proc
package impl

// todo: we could mix in something like a VarProxy?
class ControllerImpl[ C ]( val name: String, init: Double )( implicit c: Ctx[ C ])
extends Controller[ C ] with ModelImpl[ C, Double ] {
   private val vr = c.v( init )

   def get( implicit c: Ctx[ C ]) = vr.get
   def set( v: Double )( implicit c: Ctx[ C ]) {
      vr.set( v )
      fireUpdate( v )
   }
//   def addListener( l: L )( implicit c: Ctx[ C ]) = vr.addListener( l )
//   def removeListener( l: L )( implicit c: Ctx[ C ]) = vr.removeListener( l )
}