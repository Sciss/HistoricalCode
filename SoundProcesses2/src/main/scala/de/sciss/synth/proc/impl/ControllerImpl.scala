package de.sciss.synth.proc
package impl

class ControllerImpl[ C ]( val name: String, init: Double )( implicit c: Ctx[ C ]) extends Controller[ C ] {
   private val vr = c.v( init )

   def get( implicit c: Ctx[ C ]) = vr.get
   def set( v: Double )( implicit c: Ctx[ C ]) = vr.set( v )
}