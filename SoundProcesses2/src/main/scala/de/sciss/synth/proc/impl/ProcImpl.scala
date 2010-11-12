package de.sciss.synth.proc
package impl

import collection.immutable.{Queue => IQueue}

class ProcImpl[ C ]( implicit c: Ctx[ C ]) extends Proc[ C ] {
//   val playing : Switch[ C ]  = new SwitchImpl( false )
//   val amp : Controller[ C ]  = new ControllerImpl( 1.0 )
//   val freq : Controller[ C ] = new ControllerImpl( 441.0 )
   val playing = c.v( false )
   val amp     = c.v( 1.0 )
   val freq    = c.v( 441.0 )

//   private val listeners = c.v( IQueue.empty[ L ])
//
//   def addListener( l: L )( implicit c: Ctx[ C ]) {
//      listeners.transform( _ enqueue l )( txn( c ))
//   }
//
//   def removeListener( l: L )( implicit c: Ctx[ C ]) {
//      listeners.transform( _.filter( _ != l ))( txn( c ))
//   }
}