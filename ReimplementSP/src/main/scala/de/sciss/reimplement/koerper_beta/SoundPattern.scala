/*
 *  SoundPattern.scala
 *  (ReimplementSP)
 *
 *  Copyright (c) 2018 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU Affero General Public License v3+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.reimplement.koerper_beta

object SoundPattern {
  def p: Any = {
/*

  type Coord = Array[Float]

  case class Traj(cId: Int, pt: Vector[Coord], age: Int)

  type Frame = Map[Int, Traj]

  def update(frame: Frame)(implicit tx: S#Tx): Unit = {
    import TxnLike.peer
    frame.foreach { case (id, traj) =>
      val didSync = (traj.age == config.minSyncLen) && {
        val ok0   = !fgSoundMap.contains(0)
        val ok1   = !fgSoundMap.contains(1)
        val ok2   = !fgSoundMap.contains(2)
        val ch    = if (ok0) 0 else if (ok1) 1 else if (ok2) 2 else -1
        val ok    = ok0 || ok1 || ok2
        if (ok) {
          println(s"SYNC $id -> $ch")
          playFg(ch = ch, id = id, coord = traj.pt.last)
        }
        ok
      }
      if (!didSync) traj.pt.lastOption.foreach { coord =>
        updateBg(id, coord)
      }
    }
  }


 */
  }
}
