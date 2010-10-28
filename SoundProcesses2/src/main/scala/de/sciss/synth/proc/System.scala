package de.sciss.synth.proc

trait System[ C ] {
   def v[ T ]( v: T )( implicit m: ClassManifest[ T ]) : Var[ C, T ]
   def t[ T ]( fun: C => T ) : T
}

//object SystemConfig {
//   @volatile var system: Option[ System ] = None
//}
//
//object System {
//   private lazy val instance = SystemConfig.system.getOrElse( error( "No System configured" ))
//   def v[ T ]( v: T )( implicit m: ClassManifest[ T ]) : Var[ T ] = instance.v( v )
//}
