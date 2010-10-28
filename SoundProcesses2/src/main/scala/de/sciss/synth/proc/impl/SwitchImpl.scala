package de.sciss.synth.proc
package impl

class SwitchImpl[ C ]( init: Boolean )( implicit sys: System[ C ]) extends Switch[ C ] {
   private val vr = sys.v( init )

   def get( implicit c: C ) = vr.get
   def set( v: Boolean )( implicit c: C ) = vr.set( v )
}