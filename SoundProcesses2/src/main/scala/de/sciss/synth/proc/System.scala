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

trait System[ C <: Ct, V[ $ ] <: Vr[ C, $ ]] {
//   type Var[ _ ]
//   type Ctx <: CtxLike
   def t[ R ]( fun: ECtx => R ) : R // any system can initiate an ephemeral transaction
   def v[ T ]( init: T )( implicit m: ClassManifest[ T ], c: C ) : V[ T ]
}

object ESystem {
   type Var[ A ] = EVar[ ECtx, A ]
}
trait ESystem extends System[ ECtx, ESystem.Var ]
/* with Cursor[ ESystem, ECtx, ESystem.Var ] with CursorProvider[ ESystem ] */ {
//   type Var[ T ] = EVar[ Ctx, T ]
//   type Ctx = ECtx
//   def t[ R ]( fun: ECtx => R ) : R
}

object KSystem {
   type Var[ A ] = KVar[ KCtx, A ]

   sealed trait Update[ C <: Ct, V[ $ ] <: KVar[ C, $ ]]

   case class NewBranch[ C <: Ct, V[ $ ] <: KVar[ C, $ ]]( oldPath: VersionPath, newPath: VersionPath )
   extends Update[ C, V ]

   case class CursorAdded[ C <: Ct, V[ $ ] <: KVar[ C, $ ]]( cursor: KCursor[ C, V ])
   extends Update[ C, V ]

   case class CursorRemoved[ C <: Ct, V[ $ ] <: KVar[ C, $ ]]( cursor: KCursor[ C, V ])
   extends Update[ C, V ]
}

trait KSystemLike[ C <: Ct, V[ $ ] <: KVar[ C, $ ]]
extends System[ C, V ] with Model[ C, KSystem.Update[ C, V ]] {
   def in[ R ]( v: VersionPath )( fun: C => R ) : R

   def newBranch( v: VersionPath )( implicit c: C ) : VersionPath
   def dag( implicit c: ECtx ) : LexiTrie[ OracleMap[ VersionPath ]]

   def addCursor( implicit c: C ) : KCursor[ C, V ]
   def removeCursor( cursor: KCursor[ C, V ])( implicit c: C ) : Unit
   def cursors( implicit c: ECtx ) : ISet[ KCursor[ C, V ]]
}

trait KSystem extends KSystemLike[ KCtx, KSystem.Var ] with Model[ KCtx, KSystem.Update[ KCtx, KSystem.Var ]] {
//   type Var[ T ] = KVar[ KCtx, T ]
//   type Ctx = KCtx
//   def in[ R ]( v: VersionPath )( fun: KCtx => R ) : R
//   def newBranch( v: VersionPath )( implicit c: KCtx ) : VersionPath
}

