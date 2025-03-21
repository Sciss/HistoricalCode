/*
 *  Ladma.scala
 *  (Pointillism)
 *
 *  Copyright (c) 2013-2018 IEM Graz / Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package at.iem.point.illism.rhythm

import spire.math._

import scala.collection.immutable.{IndexedSeq => Vec}

// reference: http://vladimir_ladma.sweb.cz/english/music/articles/links/mrhythm.htm
object Ladma {
  private def prepare(cell: Cell): (Vec[Rational], BigInt) = {
    // we first incorporate the total duration into the cell's elements
    // (e.g [1, 1, 1], 1/4 becomes [1/12, 1/12, 1/12], 1
    val dursN   = cell.normalized.elements.map(_.dur)
    // we find the least common multiple of the elements' denominators.
    // this is the order k of rhythmical system
    val k       = dursN.map(_.denominator).reduce((a, b) => lcm(a, b))
    // the measure is based on the relative durations (e.g. multiplied by the order)
    val dursM   = dursN.map(_ * k)
    (dursM, k)
  }

  def mobility(cell: Cell): Double = {
    val (dursM, k) = prepare(cell)
    // the geometric average is n-th root of the product of the relative durations
    val prod    = dursM.product
    val avg     = math.pow(prod.toDouble, 1.0/cell.elements.size)
    // mobility is order divided by average
    k.toDouble / avg
  }

  def tension(cell: Cell): Double = {
    val (dursM, k) = prepare(cell)
    // Ladma uses the arithmetic mean instead of the geometric mean here.
    val mean    = dursM.sum / dursM.size
    val sqrdif  = dursM.map { d => val dif = d - mean; dif * dif }
    val msqrdif = sqrdif.sum / dursM.size
    val stddev  = math.sqrt(msqrdif.toDouble)
    // tension is two times standard deviation divided by order
    2 * stddev / k.toDouble
  }

  def entropy(cell: Cell): Double = {
    val (dursM, k) = prepare(cell)
    // Note: Ladma uses symbol `k` twice. The sum obviously goes over the number of elements
    // and not the order of the structure.
    // val ps = dursM.map { d => val quot = (d/k).toDouble; quot * math.log(quot) }

    // There is another problem with the original formula: It assumes that for any note the
    // numerator is less than or equal to the order of the structure, i.e. no note greater
    // than a whole occurs. If that property is violated we could get negative entropies.
    // To solve this, we can something similar to the "reducible patterns" approach of Ladma,
    // e.g. 3/2 = 2/2 + 1/2. We can then omit the whole tones, because log(1) == 0.

    val dursF = dursM.collect {
      case d if (d % k) != 0 => d % k
    }
    val ps = dursF.map { d => val quot = (d/k).toDouble; quot * math.log(quot) }
    val h  = -ps.sum
    h
  }
}