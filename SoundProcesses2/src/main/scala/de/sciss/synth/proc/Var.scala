package de.sciss.synth.proc

object Var {
//   case class Update[ Repr, T ]( model: Var[ Repr, T ], v: T )

//   trait Listener[ Repr, T ] {
//      // todo: koennte ein implizierter read-only context sein?
//      def update( h: Var[ Repr, T ], v: T )( implicit c: Ctx[ Repr ]) : Unit
//   }
}

trait Var[ Repr, T ] /* extends Model[ Repr, T ] */ {
   import Var._
//   type L = Listener[ Repr, T ]

   def set( v: T )( implicit c: Ctx[ Repr ]) : Unit
   def get( implicit c: Ctx[ Repr ]) : T
//   def transform( f: T => T )( implicit c: Ctx[ Repr ]) : Unit

//   def addListener( l: L )( implicit c: Ctx[ Repr ]) : Unit
//   def removeListener( l: L )( implicit c: Ctx[ Repr ]) : Unit
}

//trait NamedVar[ Repr, T ] extends Var[ Repr, T ] {
//   def name: String
//}

//trait TxnVar[ Repr, T ] extends Var[ Repr, T ] with TxnModel[ Repr, T ]