package de.sciss.synth.proc

import de.sciss.confluent.VersionPath

//trait Cursor[ C, V[ _ ]] {
//   def t[ T ]( fun: Ctx[ C, V ] => T ) : T
//   def isApplicable( implicit c: Ctx[ C, V ]) : Boolean
//}

trait Cursor[ C <: Ct, V[ $ ] <: Vr[ C, $ ]] {
   def t[ R ]( fun: C => R ) : R
//   def read[ T ]( vr: V[ T ])( implicit c: C ) : T
//   def write[ T ]( vr: V[ T ], v: T )( implicit c: C ) : Unit
   def isApplicable( implicit c: C ) : Boolean
}

//trait KAccess[ S <: System, C, V[ _ ]] {
//   def range[ T ]( vr: V[ T ], start: Int, stop: Int )( implicit c: C ) : Traversable[ T ]
//}
//
//trait CursorProvider[ S <: System ] {
//   sys: S =>
//   def cursor : Cursor[ S, sys.Ctx, sys.Var ]
//}
//
//trait KAccessProvider[ S <: System ] {
//   sys: S =>
//   def kaccess : KAccess[ S, ECtx, sys.Var ]
//}

object KCursor {
   sealed trait Update
   case class Moved( oldPath: VersionPath, newPath: VersionPath ) extends Update
}

trait KCursor[ C <: Ct, V[ $ ] <: KVar[ C, $ ]]
extends Cursor[ C, V ] with Model[ C, KCursor.Update ] {
   def path( implicit c: ECtx ) : VersionPath
}
