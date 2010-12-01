/*
*  System.scala
*  (SoundProcesses)
*
*  Copyright (c) 2009-2010 Hanns Holger Rutz. All rights reserved.
*
*	 This software is free software; you can redistribute it and/or
*	 modify it under the terms of the GNU General Public License
*	 as published by the Free Software Foundation; either
*	 version 2, june 1991 of the License, or (at your option) any later version.
*
*	 This software is distributed in the hope that it will be useful,
*	 but WITHOUT ANY WARRANTY; without even the implied warranty of
*	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
*	 General Public License for more details.
*
*	 You should have received a copy of the GNU General Public
*	 License (gpl.txt) along with this software; if not, write to the Free Software
*	 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*
*
*	 For further information, please contact Hanns Holger Rutz at
*	 contact@sciss.de
*
*
*  Changelog:
*/

package de.sciss.synth.proc

import de.sciss.confluent.{OracleMap, LexiTrie, VersionPath}
import collection.immutable.{Set => ISet}
import Double.{PositiveInfinity => dinf}

trait System[ C <: Ct, V[ ~ ] <: Vr[ C, ~ ]] {
   def t[ R ]( fun: ECtx => R ) : R // any system can initiate an ephemeral transaction
   def v[ T ]( init: T )( implicit m: ClassManifest[ T ], c: C ) : V[ T ]
   def modelVar[ T ]( init: T )( implicit m: ClassManifest[ T ], c: C ) : V[ T ] with Model[ C, T ]
   def userVar[ T ]( init: T )( user: (C, T) => Unit )( implicit m: ClassManifest[ T ], c: C ) : V[ T ]
}

object ESystem {
   type Var[ ~ ] = EVar[ ECtx, ~ ]
}
trait ESystem extends System[ ECtx, ESystem.Var ]
/* with Cursor[ ESystem, ECtx, ESystem.Var ] with CursorProvider[ ESystem ] */ {
//   type Var[ T ] = EVar[ Ctx, T ]
//   type Ctx = ECtx
//   def t[ R ]( fun: ECtx => R ) : R
}

///////////////////////////////////////////////////////////////////////////////

object KSystemLike {
   /* sealed */ trait Update[ C <: Ct, Csr <: KProjection[ C ] with Cursor[ C ]]

   case class NewBranch[ C <: Ct, Csr <: KProjection[ C ] with Cursor[ C ]]( oldPath: VersionPath, newPath: VersionPath )
   extends Update[ C, Csr ]
   case class CursorAdded[ C <: Ct, Csr <: KProjection[ C ] with Cursor[ C ]]( cursor: Csr ) extends Update[ C, Csr ]
   case class CursorRemoved[ C <: Ct, Csr <: KProjection[ C ] with Cursor[ C ]]( cursor: Csr ) extends Update[ C, Csr ]
}

trait KSystemLike[ C <: Ct, V[ ~ ] <: KVar[ C, ~ ], Proj <: KProjection[ C ], Csr <: KProjection[ C ] with Cursor[ C ]]
extends System[ C, V ] with Model[ ECtx, KSystemLike.Update[ C, Csr ]] with KProjector[ C, Proj, Csr ] {
//   def in[ R ]( v: VersionPath )( fun: C => R ) : R

   def newBranch( v: VersionPath )( implicit c: ECtx ) : VersionPath
   def dag( implicit c: ECtx ) : LexiTrie[ OracleMap[ VersionPath ]]

//   def addKCursor( implicit c: C ) : KCursor[ C, V ]
//   def removeKCursor( cursor: KCursor[ C, V ])( implicit c: C ) : Unit
//   def kcursors( implicit c: ECtx ) : ISet[ KCursor[ C, V ]]
}

object KSystem {
   type Var[ ~ ]     = KVar[ KCtx, ~ ]
   type Projection   = EProjection[ KCtx ] with KProjection[ KCtx ]
   type Cursor       = ECursor[ KCtx ] with KProjection[ KCtx ]
//   sealed trait Update extends KSystemLike.Update[ KCtx, Var ]
}

trait KSystem extends KSystemLike[ KCtx, KSystem.Var, KSystem.Projection, KSystem.Cursor ]
with KEProjector[ KCtx, KSystem.Var ]

///////////////////////////////////////////////////////////////////////////////

//object PSystem {
//   type Var[ ~ ] = PVar[ PCtx, ~ ]
//
//   sealed trait Update[ C <: Ct, V[ ~ ] <: PVar[ C, ~ ]]
//
//   case class CursorAdded[ C <: Ct, V[ ~ ] <: PVar[ C, ~ ]]( cursor: PCursor[ C, V ])
//   extends Update[ C, V ]
//
//   case class CursorRemoved[ C <: Ct, V[ ~ ] <: PVar[ C, ~ ]]( cursor: PCursor[ C, V ])
//   extends Update[ C, V ]
//}
//
//trait PSystemLike[ C <: Ct, V[ ~ ] <: PVar[ C, ~ ]]
//extends System[ C, V ] with Model[ C, PSystem.Update[ C, V ]] {
////   def in[ R ]( v: VersionPath )( fun: C => R ) : R
//
//   def addPCursor( implicit c: C ) : PCursor[ C, V ]
//   def removePCursor( cursor: PCursor[ C, V ])( implicit c: C ) : Unit
//   def pcursors( implicit c: ECtx ) : ISet[ PCursor[ C, V ]]
//
//   def at[ T ]( period: Period )( thunk: => T )( implicit c: C ) : T
//   def during[ T ]( interval: Interval )( thunk: => T )( implicit c: C ) : T
//}
//
//trait PSystem extends PSystemLike[ PCtx, PSystem.Var ] with Model[ PCtx, PSystem.Update[ PCtx, PSystem.Var ]]

///////////////////////////////////////////////////////////////////////////////

//object BSystem {
//   type Var[ ~ ] = BVar[ BCtx, ~ ]
//}
