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

import de.sciss.patterns.Pat

object SoundPattern {
  type Coord = Array[Float]

//  case class Traj(cId: Int, pt: Vector[Coord], age: Int)

  type Traj = (Int, Pat[Coord], Int) // cId, pt, age

  implicit class Tuple2PatOps[A, B](private val pat: Pat[(A, B)]) extends AnyVal {
    def _1: Pat[A] = ???
    def _2: Pat[B] = ???
  }

  implicit class Tuple3PatOps[A, B, C](private val pat: Pat[(A, B, C)]) extends AnyVal {
    def _1: Pat[A] = ???
    def _2: Pat[B] = ???
    def _3: Pat[C] = ???
  }

  implicit class ReimpPatOps[A](private val pat: Pat[A]) extends AnyVal {
    def takeRight(n: Pat[Int]): Pat[A] = ???
  }

  case class If[A, B](in: Pat[A])(Then: Pat[B]) {
    def Else[C >: B](pat: Pat[C]): Pat[C] = ???
  }

  def update(): Unit = {
    import de.sciss.patterns._
    import de.sciss.patterns.graph._

    val minSyncLen: Int = ???

    def timbreFromCoord(coord: Pat[Coord]): Pat[Int] = ???

    val frame: Pat[(Int, Traj)] = ???
    // bubbleMap cannot change type -- see Patterns #4
    val lastCoord : Pat[Coord] = frame.bubble.flatMap { frameIn =>
      val traj  : Pat[(Int, Pat[Coord], Int)] = frameIn._2
      val coord : Pat[Pat[Coord]] = traj._2
      coord.map(_.takeRight(1)).flatten
    }
    val age       : Pat[Int]      = frame.bubble.flatMap(_._2._3)
    val shouldSync: Pat[Boolean]  = age sig_== minSyncLen
    val timbre    : Pat[Int]      = timbreFromCoord(lastCoord)
    val durTimbre : Pat[Double]   = ???

    // this must imply that both branches are
    // are advanced (not filtered), otherwise we end
    // up with unsynchronised `timbre` polls
    If (shouldSync) {
      Bind(Bind.Map(
        "play"    -> "foreground",
        "timbre"  -> timbre
      ))
    } Else {
      Bind(Bind.Map(
        "play"    -> "background",
        "timbre"  -> timbre
      ))
    }

/*

  def playFg(ch: Int, id: Int, coord: Coord)(implicit tx: S#Tx): Unit = {
    import TxnLike.peer
    import numbers.Implicits._
    val timbre      = timbreFromCoord(coord)
    val offsetNorm  = 0.0
    val durNorm     = 1.0
    val syn         = Synth(s, gFg, nameHint = Some("sync"))
    val e           = new Elem(id = id, syn = syn, timbre = timbre, offset = offsetNorm, dur = durNorm)
    val info        = soundInfo(e.timbre)
    val numFrames   = info.numFrames
    val durSec      = numFrames / 44100.0
    val buf         = Buffer.diskIn(s)(path = info.f.path)
    val bufDly      = Buffer(s)(numFrames = 65536) // must be power of two: 44100
    val dep         = buf :: Nil
    val args: List[ControlSet] = List[ControlSet](
      "buf"   -> buf    .id,
      "dly"   -> bufDly .id,
      "dur"   -> durSec,
      "chan"  -> ch,
      "amp"   -> config.fgGain
    )
    syn.play(target = _fgGroup, addAction = addToHead, args = args, dependencies = dep)
    set(e, coord)
    syn.onEndTxn { implicit tx =>
      buf   .dispose()
      bufDly.dispose()
      fgSoundMap.remove(ch)
    }
    fgSoundMap.put(ch, e)
  }

     */

/*

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


  def updateBg(id: Int, coord: Coord)(implicit tx: S#Tx): Unit = {
    import TxnLike.peer
    bgSoundMap.get(id).fold[Unit] {
      import numbers.Implicits._
      val timbre = timbreFromCoord(coord)

      //        val timbre  = (math.random() * NumSounds).toInt
      //        val timbre  = coord(0).linLin(-1, +1, 0, NumSounds - 1).round.clip(0, NumSounds - 1) // XXX TODO
      val offset  = coord(4 /* 6 */).linLin(-0.9, +0.9, 0, 1).clip(0, 1)
      val dur     = coord(5 /* 7 */).linLin(-0.9, +0.9, 0, 1).clip(0, 1)
      val syn     = Synth(s, gBg, nameHint = Some("atom"))
      val e = new Elem(id = id, syn = syn, timbre = timbre, offset = offset, dur = dur)
      playBg(e, coord)
      bgSoundMap.put(id, e)

    } { e =>
      set(e, coord)
    }
  }

 */
  }
}
