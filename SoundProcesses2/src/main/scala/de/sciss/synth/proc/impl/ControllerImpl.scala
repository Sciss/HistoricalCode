package de.sciss.synth.proc
package impl

class ControllerImpl[ C ]( init: Double )( implicit sys: System[ C ]) extends Controller[ C ] {
   private val vr = sys.v( init )

   def get( implicit c: C ) = vr.get
   def set( v: Double )( implicit c: C ) = vr.set( v )
}