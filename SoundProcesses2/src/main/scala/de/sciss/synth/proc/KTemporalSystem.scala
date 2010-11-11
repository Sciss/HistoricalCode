/*
 *  KTemporalSystem.scala
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

import de.sciss.confluent._
import edu.stanford.ppl.ccstm.{TxnLocal, STM, Txn, Ref}

object KTemporalSystem extends System[ KTemporal ] {
   private type C = Ctx[ KTemporal ]

//   private val currentPathRef = Ref( VersionPath.init )
//
//   def t[ T ]( fun: C => T ) : T = STM.atomic( tx => fun( new KTemporal( tx, currentPathRef )))

   def in[ T ]( version: VersionPath )( fun: C => T ) : T = STM.atomic { tx =>
//      val oldCurr = currentPathRef.swap( version )( tx )
//      try {
         fun( new KTemporal( tx, version ))   // currentPathRef
//      } finally {
//         currentPathRef.set( oldCurr )( tx )
//      }
   }
 }

class KTemporal private[proc]( private[proc] val txn: Txn, initPath: VersionPath )
extends Ctx[ KTemporal ] {
//   ctx =>
   
   private type C = Ctx[ KTemporal ]

   private val pathRef = new TxnLocal[ VersionPath ] {
      override protected def initialValue( txn: Txn ) = initPath
   }

   def repr = this
   def system = KTemporalSystem

   def v[ T ]( init: T )( implicit m: ClassManifest[ T ]) : Var[ KTemporal, T ] = {
//      val ref = Ref[ T ]()
      val fat0 = FatValue.empty[ T ]
      val vp   = writePath
//println( "ASSIGN AT " + vp + " : " + init )
      val fat1 = fat0.assign( vp.path, init )
      new KVar( Ref( fat1 ))
   }

   def path : VersionPath = pathRef.get( txn )

//   private[proc] def readPath : VersionPath = pathRef.get( txn )

   private[proc] def writePath : VersionPath = {
      val p = pathRef.get( txn )
//      if( isWriting.get( txn )) p else {
//         isWriting.set( true )( txn )
//         val pw = p.newBranch
//         pathRef.set( pw )( txn )
//         pw
//      }
      if( p == initPath ) {
         val pw = p.newBranch
         pathRef.set( pw )( txn )
         pw
      } else p
   }

   private class KVar[ /* @specialized */ T ]( ref: Ref[ FatValue[ T ]]) extends Var[ KTemporal, T ] {
       def get( implicit c: C ) : T = {
          val vp   = c.repr.path // readPath
          ref.get( c.repr.txn ).access( vp.path )
            .getOrElse( error( "No assignment for path " + vp ))
       }

       def set( v: T )( implicit c: C ) {
          ref.transform( _.assign( c.repr.writePath.path, v ))( c.repr.txn )
       }
    }
}