/*
 *  KSystemImpl.scala
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
package impl

import edu.stanford.ppl.ccstm.{TxnLocal, Txn, Ref, STM}
import de.sciss.confluent.{LexiTrie, OracleMap, FatValue, VersionPath}
import collection.immutable.{Set => ISet}

object KSystemImpl {
   def apply() : KSystem = new Sys

   private class Sys extends KSystem with ModelImpl[ KCtx, KSystem.Update[ KCtx, KSystem.Var ]] {
      sys =>
      
      override def toString = "KSystem"

      val dagRef = {
         val fat0 = FatValue.empty[ VersionPath ]
         val vp   = VersionPath.init
         val fat1 = fat0.assign( vp.path, vp )
         Ref( fat1 )
      }

      val cursorsRef = Ref( ISet.empty[ KCursor[ KCtx, KSystem.Var ]])

      def dag( implicit c: ECtx ) : LexiTrie[ OracleMap[ VersionPath ]] = dagRef.get( c.txn ).trie
      def cursors( implicit c: ECtx ) : ISet[ KCursor[ KCtx, KSystem.Var ]] = cursorsRef.get( c.txn )

      def addCursor( implicit c: KCtx ) : KCursor[ KCtx, KSystem.Var ] = {
         val csr = new CursorImpl( sys, c.path )
         cursorsRef.transform( _ + csr )( c.txn )
         sys.fireUpdate( KSystem.CursorAdded[ KCtx, KSystem.Var ]( csr ))
         csr
      }

      def removeCursor( cursor: KCursor[ KCtx, KSystem.Var ])( implicit c: KCtx ) {
         cursorsRef.transform( _ - cursor )( c.txn )
         sys.fireUpdate( KSystem.CursorRemoved[ KCtx, KSystem.Var ]( cursor ))
      }

      def in[ R ]( version: VersionPath )( fun: KCtx => R ) : R = STM.atomic { tx =>
         fun( new Ctx( sys, tx, version ))
      }

      def t[ R ]( fun: ECtx => R ) : R = Factory.esystem.t( fun )

      def v[ T ]( init: T )( implicit m: ClassManifest[ T ], c: KCtx ) : KVar[ KCtx, T ] = {
         val fat0 = FatValue.empty[ T ]
         val vp   = c.writePath
         val fat1 = fat0.assign( vp.path, init )
         new Var( Ref( fat1 ), m.toString )
      }

      def newBranch( v: VersionPath )( implicit c: KCtx ) : VersionPath = {
         val pw = v.newBranch
         dagRef.transform( _.assign( pw.path, pw ))( c.txn )
         fireUpdate( KSystem.NewBranch[ KCtx, KSystem.Var ]( v, pw ))
         pw
      } 
   }

   private class Ctx( system: Sys, val txn: Txn, initPath: VersionPath )
   extends KCtx {
      ctx =>

      override def toString = "KCtx"

      private val pathRef = new TxnLocal[ VersionPath ] {
         override protected def initialValue( txn: Txn ) = initPath
      }

      def path : VersionPath = pathRef.get( txn )

      def eph : ECtx = ESystemImpl.join( txn )

   //   private[proc] def readPath : VersionPath = pathRef.get( txn )

      private[proc] def writePath : VersionPath = {
         val p = pathRef.get( txn )
         if( p == initPath ) {
            val pw = system.newBranch( p )( ctx ) // p.newBranch
            pathRef.set( pw )( txn )
//            system.dagRef.transform( _.assign( pw.path, pw ))( txn )
//            system.fireUpdate( KTemporal.NewBranch( p, pw ))( ctx )  // why NewBranch here without type parameter?
            pw
         } else p
      }
   }

   private class Var[ T ]( ref: Ref[ FatValue[ T ]], typeName: String )
   extends KVar[ KCtx, T ] with ModelImpl[ KCtx, T ] {
//      protected def txn( c: C ) = c.repr.txn

      override def toString = "KVar[" + typeName + "]"

      def get( implicit c: KCtx ) : T = {
         val vp   = c.path // readPath
         ref.get( c.txn ).access( vp.path )
            .getOrElse( error( "No assignment for path " + vp ))
      }

      def set( v: T )( implicit c: KCtx ) {
         ref.transform( _.assign( c.writePath.path, v ))( c.txn )
         fireUpdate( v )
      }

      def range( vStart: VersionPath, vStop: VersionPath )( implicit c: ECtx ) : Traversable[ T ] =
         error( "NOT YET IMPLEMENTED" )

//      def transform( f: T => T )( implicit c: C ) {
//         ref.transform( _.assign( c.repr.writePath.path, v ))( txn( c ))
//         fireUpdate( v, c )
//      }
   }

   private class CursorImpl( sys: Sys, initialPath: VersionPath )
   extends KCursor[ KCtx, KSystem.Var ] with ModelImpl[ KCtx, KCursor.Update ] {
      private val vRef = Ref( initialPath )

      private val txnInitiator = new TxnLocal[ Boolean ] {
         override protected def initialValue( txn: Txn ) = false
      }

      def isApplicable( implicit c: KCtx ) = txnInitiator.get( c.txn )
      def path( implicit c: ECtx ) : VersionPath = vRef.get( c.txn )

      def t[ R ]( fun: KCtx => R ) : R = {
         // XXX todo: should add t to KTemporalSystemLike and pass txn to in
         // variant so we don't call atomic twice
         // (although that is ok and the existing transaction is joined)
         // ; like BitemporalSystem.inRef( vRef.getTxn( _ )) { ... } ?
         STM.atomic { t =>
            val oldPath = vRef.get( t )
            txnInitiator.set( true )( t )
            sys.in( oldPath ) { implicit c =>
               val res     = fun( c )
               val newPath = c.path
               if( newPath != oldPath ) {
                  vRef.set( newPath )( c.txn )
                  fireUpdate( KCursor.Moved( oldPath, newPath ))
               }
               txnInitiator.set( false )( t )
               res
            }
         }
      }
   }
}