package de.sciss.synth.proc
package impl

class ProcImpl[ C ]( implicit c: Ctx[ C ]) extends Proc[ C ] {
   val playing : Switch[ C ]  = new SwitchImpl( false ) 
   val amp : Controller[ C ]  = new ControllerImpl( 1.0 )
   val freq : Controller[ C ] = new ControllerImpl( 441.0 )
}