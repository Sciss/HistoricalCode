/*
 * Random.scala
 * (Rays)
 *
 * Copyright (c) 2016 Hanns Holger Rutz.
 * Copyright (c) 2016 Jon Hanson.
 * All rights reserved.
 *
 * This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 * For further information, please contact Hanns Holger Rutz at
 * contact@sciss.de
 */

package de.sciss.rays

import cats.data.State

trait RNG[T] {
  def next: (RNG[T], T)
}

/**
  * @author Jon Hanson
  */
object RNG {
  type Type[T] = State[RNG[Double], T]

  def nextDouble: Type[Double] = State(rng => rng.next)
}

/**
  * @author Jon Hanson
  */
object RandomLCG {
  final val Mul   = 214013L
  final val Inc   = 2531011L
  final val Mod   = 0x100000000L
  final val Scale = Int.MaxValue.toDouble - Int.MinValue.toDouble + 1.0
}

final case class RandomLCG(seed: Long = 0) extends RNG[Double] {

  import RandomLCG._

  def next: (RNG[Double], Double) = {
    val seed2 = (Mul * seed + Inc) % Mod
    (RandomLCG(seed2), seed2 / Scale)
  }
}

/**
  * @author Jon Hanson
  */
final case class XorShiftRNG(seed: Long) extends RNG[Long] {

  def next: (RNG[Long], Long) = {
    val a = seed ^ (seed >>> 12)
    val b = a ^ (a << 25)
    val c = b ^ (b >>> 27)
    val d = if (c == 0) -1 else c
    (XorShiftRNG(d), d * 2685821657736338717L)
  }
}

/**
  * @author Jon Hanson
  */
object DoubleRNG {
  final val Scale = Long.MaxValue.toDouble - Long.MinValue.toDouble + 1.0
  final val Mod   = 1L << 64
}

final case class DoubleRNG(rng: RNG[Long]) extends RNG[Double] {

  import DoubleRNG._

  def next: (RNG[Double], Double) = {
    val (rng2, rl) = rng.next
    val dl = (rl.toDouble - Long.MinValue.toDouble) / Scale
    (DoubleRNG(rng2), dl)
  }
}

/**
  * @author Jon Hanson
  */
object Random {
  def randLong  (seed: Long): RNG[Long]   =           XorShiftRNG(seed)
  def randDouble(seed: Long): RNG[Double] = DoubleRNG(XorShiftRNG(seed))
}