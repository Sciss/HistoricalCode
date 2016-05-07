/*
 * Random.scala
 * (Rays)
 *
 * Copyright (c) 2016 Hanns Holger Rutz. All rights reserved.
 *
 * This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 * For further information, please contact Hanns Holger Rutz at
 * contact@sciss.de
 */

package de.sciss.rays

object Random {
  def apply(seed: Long): Random = new RandomImpl(seed)
}
trait Random {
  def nextDouble(): Double
  def nextLong  (): Long
}

object RandomImpl {
  final val Scale = Long.MaxValue.toDouble - Long.MinValue.toDouble + 1.0
  final val Mod   = 1L << 64
}

/**
  * @author Hanns Holger Rutz
  * @author Jon Hanson
  */
final class RandomImpl(private[this] var seed: Long) extends Random {
  def nextLong(): Long = {
    val a = seed ^ (seed >>> 12)
    val b = a ^ (a << 25)
    val c = b ^ (b >>> 27)
    val d = if (c == 0) -1 else c
    seed = d
    d * 2685821657736338717L
  }

  import RandomImpl._

  def nextDouble(): Double = {
    val rl = nextLong()
    val dl = (rl.toDouble - Long.MinValue.toDouble) / Scale
    dl
  }
}