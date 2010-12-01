/*
 *  Cursor.scala
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

import de.sciss.confluent.VersionPath

//trait Cursor[ C, V[ _ ]] {
//   def t[ T ]( fun: Ctx[ C, V ] => T ) : T
//   def isApplicable( implicit c: Ctx[ C, V ]) : Boolean
//}

trait Cursor[ C <: Ct, V[ ~ ] <: Vr[ C, ~ ]] {
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

trait KCursor[ C <: Ct, V[ ~ ] <: KVar[ C, ~ ]]
extends Cursor[ C, V ] with Model[ C, KCursor.Update ] {
//   def path( implicit c: ECtx ) : VersionPath
}

object PCursor {
   sealed trait Update
//   case class Moved( oldPath: VersionPath, newPath: VersionPath ) extends Update
}

trait PCursor[ C <: Ct, V[ ~ ] <: PVar[ C, ~ ]]
extends Cursor[ C, V ] with Model[ C, PCursor.Update ] {
}
