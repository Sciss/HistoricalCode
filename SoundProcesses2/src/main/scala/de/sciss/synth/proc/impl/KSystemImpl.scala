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

package de.sciss.synth.proc.impl

import de.sciss.confluent.{FatValue, VersionPath}
import edu.stanford.ppl.ccstm.{TxnLocal, Txn, Ref, STM}
import de.sciss.synth.proc.{ECtx, KVar, KCtx, KSystem}

object KSystemImpl {
   def apply() : KSystem = new Sys

   private class Sys extends KSystem with ModelImpl[ KCtx, KSystem.Update ] {
      sys =>
      
      override def toString = "KSystem"

      val dagRef = {
         val fat0 = FatValue.empty[ VersionPath ]
         val vp   = VersionPath.init
         val fat1 = fat0.assign( vp.path, vp )
         Ref( fat1 )
      }

      def in[ R ]( version: VersionPath )( fun: KCtx => R ) : R = STM.atomic { tx =>
         fun( new Ctx( sys, tx, version ))
      }

      def v[ T ]( init: T )( implicit m: ClassManifest[ T ], c: KCtx ) : KVar[ KCtx, T ] = {
         val fat0 = FatValue.empty[ T ]
         val vp   = c.writePath
         val fat1 = fat0.assign( vp.path, init )
         new Var( Ref( fat1 ), m.toString )
      }

      def newBranch( v: VersionPath )( implicit c: KCtx ) : VersionPath = {
         val pw = v.newBranch
         dagRef.transform( _.assign( pw.path, pw ))( c.txn )
         fireUpdate( KSystem.NewBranch( v, pw ))( c )
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
}