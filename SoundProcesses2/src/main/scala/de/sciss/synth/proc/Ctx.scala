/*
 *  Ctx.scala
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

import edu.stanford.ppl.ccstm.Txn

//trait Ctx[ C, V[ _ ]] extends Repr[ C ] {
//   def v[ T ]( init : T )( implicit m: ClassManifest[ T ]) : Var[ C, V, T ]
//   def txn: Txn
//}

trait TxnHolder { def txn: Txn }

trait ECtx extends TxnHolder {
   def v[ T ]( init : T )( implicit m: ClassManifest[ T ]) : EVar[ ECtx, T ]
//   def txn: Txn
}

trait KCtx extends TxnHolder {
   def v[ T ]( init: T ) : KVar[ KCtx, T ]
//   def txn: Txn
   def eph : ECtx
}
