/*
 *  Model.scala
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

import collection.immutable.{Queue => IQueue}
import edu.stanford.ppl.ccstm.{TxnLocal, Ref, Txn}

object Model {
   trait Listener[ C, V[ _ ], U ] {
      def updated( update: U )( implicit c: Ctx[ C, V ])
   }

   def onCommit[ C, V[ _ ], U ]( committed: Traversable[ U ] => Unit ) : Listener[ C, V, U ] =
      filterOnCommit( (_: U, _: Ctx[ C, V ]) => true )( committed )

//   def collectOnCommit[ Repr, U, V ]( pf: PartialFunction[ (U, Ctx[ Repr ]), V ])( committed: Traversable[ V ] => Unit )

   def filterOnCommit[ C, V[ _ ], U ]( filter: Function2[ U, Ctx[ C, V ], Boolean ])( committed: Traversable[ U ] => Unit ) =
      new Listener[ C, V, U ] {
         val queueRef = new TxnLocal[ IQueue[ U ]] {
            override protected def initialValue( txn: Txn ) = IQueue.empty
         }
         def updated( update: U )( implicit c: Ctx[ C, V ]) {
            if( filter( update, c )) {
               val txn  = c.txn
               val q0   = queueRef.get( txn )
               queueRef.set( q0 enqueue update )( txn )
               if( q0.isEmpty ) {
                  txn.beforeCommit( txn => {
                     val q1 = queueRef.get( txn )
                     txn.afterCommit( _ => committed( q1 ))
                  }, Int.MaxValue )
               }
            }
         }
      }
}

trait Model[ C, V[ _ ], U ] {
   import Model._

   type L = Listener[ C, V, U ]

//   def addListener( l: L )( implicit c: Ctx[ Repr ]) : Unit
//   def removeListener( l: L )( implicit c: Ctx[ Repr ]) : Unit
   def addListener( l: L )( implicit c: Ctx[ _, _ ]) : Unit
   def removeListener( l: L )( implicit c: Ctx[ _, _ ]) : Unit
}
