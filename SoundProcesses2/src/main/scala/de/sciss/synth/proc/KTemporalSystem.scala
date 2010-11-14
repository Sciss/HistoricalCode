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
import impl.{EphemeralModelVarImpl, ModelImpl}
import collection.{Set => ISet}

object KTemporalSystem {
   private type C = Ctx[ KTemporal ]

   def apply() : KTemporalSystem = new SystemImpl

   private class SystemImpl extends KTemporalSystem with ModelImpl[ KTemporal, KTemporal.Update[ KTemporal ]] {
      sys =>

      // XXX BEGIN :::::::::::::::::: DUP FROM BITEMPORAL
      val dagRef = {
         val fat0 = FatValue.empty[ VersionPath ]
         val vp   = VersionPath.init
         val fat1 = fat0.assign( vp.path, vp )
         Ref( fat1 )
      }

      def dag( implicit c: Ctx[ _ ]) : LexiTrie[ OracleMap[ VersionPath ]] = dagRef.get( c.txn ).trie
      // XXX END :::::::::::::::::: DUP FROM BITEMPORAL

      // XXX BEGIN :::::::::::::::::: DUP FROM BITEMPORAL
      val cursorsRef = Ref( ISet.empty[ Cursor[ KTemporal ]])
      def cursors( implicit c: Ctx[ _ ]) : ISet[ Cursor[ KTemporal ]] = cursorsRef.get( c.txn )

      def addCursor( implicit c: C ) : Cursor[ KTemporal ] = {
         val csr = new KTemporalSystem.CursorImpl( sys, new EphemeralModelVarImpl[ KTemporal, VersionPath ]( c.repr.path ))
         cursorsRef.transform( _ + csr )( c.txn )
         sys.fireUpdate( KTemporal.CursorAdded( csr ))
         csr
      }

      def removeCursor( cursor: Cursor[ KTemporal ])( implicit c: C ) {
         cursorsRef.transform( _ - cursor )( c.txn )
         sys.fireUpdate( KTemporal.CursorRemoved( cursor ))
      }
      // XXX END :::::::::::::::::: DUP FROM BITEMPORAL

      override def toString = "KTemporalSystem"

      def in[ T ]( version: VersionPath )( fun: C => T ) : T = STM.atomic { tx =>
         fun( new CtxImpl( sys, tx, version ))
      }
   }

   private class CtxImpl private[proc]( val system: SystemImpl, val txn: Txn, initPath: VersionPath )
   extends KTemporal {
      ctx =>

      private type C = Ctx[ KTemporal ]

      private val pathRef = new TxnLocal[ VersionPath ] {
         override protected def initialValue( txn: Txn ) = initPath
      }

      def repr = this

      def v[ T ]( init: T )( implicit m: ClassManifest[ T ]) : Var[ KTemporal, T ] = {
   //      val ref = Ref[ T ]()
         val fat0 = FatValue.empty[ T ]
         val vp   = writePath
   //println( "ASSIGN AT " + vp + " : " + init )
         val fat1 = fat0.assign( vp.path, init )
         new VarImpl( Ref( fat1 ))
      }

      def path : VersionPath = pathRef.get( txn )

   //   private[proc] def readPath : VersionPath = pathRef.get( txn )

      // XXX BEGIN :::::::::::::::::: DUP FROM BITEMPORAL
      private[proc] def writePath : VersionPath = {
         val p = pathRef.get( txn )
         if( p == initPath ) {
            val pw = p.newBranch
            pathRef.set( pw )( txn )
            system.dagRef.transform( _.assign( pw.path, pw ))( txn )
            system.fireUpdate( KTemporal.NewBranch( p, pw ))( ctx )  // why NewBranch here without type parameter?
            pw
         } else p
      }
      // XXX END :::::::::::::::::: DUP FROM BITEMPORAL
   }

   private class VarImpl[ /* @specialized */ T ]( ref: Ref[ FatValue[ T ]])
   extends Var[ KTemporal, T ] with ModelImpl[ KTemporal, T ] {
//      protected def txn( c: C ) = c.repr.txn

      def get( implicit c: C ) : T = {
         val vp   = c.repr.path // readPath
         ref.get( c.txn ).access( vp.path )
            .getOrElse( error( "No assignment for path " + vp ))
      }

      def set( v: T )( implicit c: C ) {
         ref.transform( _.assign( c.repr.writePath.path, v ))( c.txn )
         fireUpdate( v )
      }

//      def transform( f: T => T )( implicit c: C ) {
//         ref.transform( _.assign( c.repr.writePath.path, v ))( txn( c ))
//         fireUpdate( v, c )
//      }
   }

   private[proc] class CursorImpl[ C <: KTemporalLike ]( system: KTemporalSystemLike[ C ],
                                                         vRef: EphemeralModelVarImpl[ C, VersionPath ])
   extends Cursor[ C ] {
      private val txnInitiator = new TxnLocal[ Boolean ] {
         override protected def initialValue( txn: Txn ) = false
      }

      def isApplicable( implicit c: Ctx[ C ]) = txnInitiator.get( c.txn )

      def t[ T ]( fun: Ctx[ C ] => T ) : T = {
         // XXX todo: should add t to KTemporalSystemLike and pass txn to in
         // variant so we don't call atomic twice
         // (although that is ok and the existing transaction is joined)
         // ; like BitemporalSystem.inRef( vRef.getTxn( _ )) { ... } ?
         STM.atomic { t =>
            val oldPath = vRef.getTxn( t )
            txnInitiator.set( true )( t )
            system.in( oldPath ) { implicit c =>
               val res     = fun( c )
               val newPath = c.repr.path
               if( newPath != oldPath ) {
                  vRef.set( newPath )
               }
               txnInitiator.set( false )( t )
               res
            }
         }
      }
   }
}

trait KTemporalLike {
   def path : VersionPath
   private[proc] def writePath : VersionPath
//   def addCursor : Cursor[ C ]
}

object KTemporal {
   sealed trait Update[ C ]
   case class NewBranch[ C ]( oldPath: VersionPath, newPath: VersionPath ) extends Update[ C ]
   case class CursorAdded[ C ]( cursor: Cursor[ C ]) extends Update[ C ]
   case class CursorRemoved[ C ]( cursor: Cursor[ C ]) extends Update[ C ]
}

trait KTemporal extends KTemporalLike with Ctx[ KTemporal ]

trait KTemporalSystemLike[ C <: KTemporalLike ] extends Model[ C, KTemporal.Update[ C ]] {
   sys =>

   def in[ T ]( version: VersionPath )( fun: Ctx[ C ] => T ) : T
   def dag( implicit c: Ctx[ _ ]) : LexiTrie[ OracleMap[ VersionPath ]]
   def addCursor( implicit c: Ctx[ C ]) : Cursor[ C ]
   def removeCursor( cursor: Cursor[ C ])( implicit c: Ctx[ C ]) : Unit
   def cursors( implicit c: Ctx[ _ ]) : ISet[ Cursor[ C ]]

//   = {
//      val csr = new KTemporalSystem.CursorImpl( sys, new EphemeralModelVarImpl[ C, VersionPath ]( c.repr.path ))
//      // register csr / notify listeners
//      sys.fireUpdate( KTemporal.CursorAdded( csr ))
//      csr
//   }
}

trait KTemporalSystem extends KTemporalSystemLike[ KTemporal ] 

//trait KTemporalSystem /* extends System */ {
//   def in[ T ]( version: VersionPath )( fun: Ctx[ KTemporal ] => T ) : T
//}
