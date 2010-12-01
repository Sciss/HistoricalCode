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
import collection.immutable.{Set => ISet}

trait Projection[ C <: Ct ] {
   def isApplicable( implicit c: C ) : Boolean
}

object Cursor {
   sealed trait Update
   case object Moved extends Update
}
trait Cursor[ C <: Ct ] extends Projection[ C ] with Model[ ECtx, Cursor.Update ] {
   def dispose( implicit C: ECtx ) : Unit
}

trait EProjection[ C <: Ct ] extends Projection[ C ] {
   def t[ R ]( fun: C => R ) : R
}

trait ECursor[ C <: Ct ] extends EProjection[ C ] with Cursor[ C ]

//trait Projector[ Dim, CsrType, ProjType ] {
//   def cursorIn( v: Dim ) : CsrType
//   def projectIn( v: Dim ) : ProjType
//}

trait KProjector[ C <: Ct, +Proj, +Csr ] {
   def cursorIn( v: VersionPath )( implicit c: ECtx ) : Csr
   def projectIn( v: VersionPath ) : Proj
   def cursorsInK( implicit c: ECtx ) : Iterable[ Csr ]  // Set doesn't work because of variance...
//   def cursorIn( v: VersionPath )( implicit c: ECtx ) : ECursor[ C ] with KProjection[ C ]
//   def projectIn( v: VersionPath )( implicit c: ECtx ) : EProjection[ C ] with KProjection[ C ]
//   def in[ R ]( v: VersionPath )( fun: C => R ) : Unit
//   def range[ T ]( vr: V[ T ], interval: (VersionPath, VersionPath) )( implicit c: ECtx ) : Traversable[ T ]
}

trait KEProjector[ C <: Ct, V[ ~ ] <: KVar[ C, ~ ]]
extends KProjector[ C, EProjection[ C ] with KProjection[ C ], ECursor[ C ] with KProjection[ C ]] {
   def in[ R ]( v: VersionPath )( fun: C => R ) : R
   def range[ T ]( vr: V[ T ], interval: (VersionPath, VersionPath) )( implicit c: ECtx ) : Traversable[ T ]
}

//object KCursor {
//   sealed trait Update
//   case class Moved( oldPath: VersionPath, newPath: VersionPath ) extends Update
//}

//trait KCursor[ C <: Ct, V[ ~ ] <: KVar[ C, ~ ]]
//extends KProjector[ C, V ] with Cursor[ C ] with Model[ C, KCursor.Update ]

trait KProjection[ C <: Ct ] {
   def path( implicit c: ECtx ) : VersionPath
}

//object PCursor {
//   sealed trait Update
////   case class Moved( oldPath: VersionPath, newPath: VersionPath ) extends Update
//}
//
//trait PCursor[ C <: Ct, V[ ~ ] <: PVar[ C, ~ ]]
//extends Cursor[ C, V ] with Model[ C, PCursor.Update ] {
//}
