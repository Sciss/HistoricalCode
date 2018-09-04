/*
 *  Cell.scala
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

import scala.annotation.tailrec
import scala.collection.immutable.{IndexedSeq => Vec}

object Cell {
  private val durationMap = Map(
    1 -> "64", /* r"3/2" -> "64.", */ 2 -> "32", 3 -> "32.", 4 -> "16", 6 -> "16.", 8 -> "8",
    12 -> "8.", 16 -> "4", 24 -> "4.", 32 -> "2", 48 -> "2.", 64 -> "1", 96 -> "1."
  )

  private def lilyDurations(dur: Int): Vec[String] = {
    if (dur > 96) {
      return durationMap(96) +: lilyDurations(dur - 96)
    }
 		// require(dur <= 96, s"Rhythmic value $dur exceeds 96")
 		durationMap.get(dur) match {
      case Some(single) => Vector(single)
      case _ =>
        // e.g. 13 = 8 + 4 + 1
        val c = (6 to 0 by -1).map(1 << _).collect {
          case i if (dur & i) != 0 => i
        }
        c.map(durationMap)
    }
 	}
}
final case class Cell(id: Int, elements: Vec[NoteOrRest], dur: Rational) {
  override def toString = s"Cell#$id($prettyElements}, dur = $dur)"

  /** Number of elements in the cell */
  def size: Int = elements.size

  /** Pretty formatted string representation of the cell's elements */
  def prettyElements: String = elements.map(_.toNumber).mkString("[", ", ", "]")

  /** Pretty formatted string representation of the cell */
  def pretty: String = {
    val s = f"#$id%2s: $prettyElements, ${dur.toString}" // "${dur.toString}%3s"
    s + (" " * math.max(0, 40 - s.length))
  }

  /** Multiplies the elements by a factor so that their sum will become the nominal total duration. */
  def normalized: Cell = {
    val factor = dur / elements.map(_.dur).sum
    copy(elements = elements.map(_ * factor) /* , dur = 1 */)
  }

  /** Scales the total duration of the cell by a given factor */
  def * (factor: Rational): Cell = copy(dur = dur * factor)

  /** 'Unnormalizes' this cell, so that the elements are all integers, possibly multiplying
    * them by the least common multiple.
    */
  def usingIntegers: Cell = {
    val durs    = elements.map(_.dur)
    val denoms  = durs.map(_.denominator)
    val k       = denoms.reduce((a, b) => lcm(a, b))
    if (k == 1) return this
    val elemsM  = elements.map(_ * k)
    copy(elements = elemsM)
  }

  private[Cell] def factor: Int = {
    val sum   = elements.map(_.dur).sum
    val dur64 = dur * 64

    @tailrec def loop(f: Int): Int = if (sum * f < dur64) loop(f << 1) else f

    loop(1)
  }

  private[Cell] def adjusted: Vec[NoteOrRest] = {
    val f = factor
    elements.map(_ * f)
  }

  private[Cell] def tuplet: Rational = {
    val adj   = adjusted
    val dur64 = dur * 64
    val r1    = adj.map(_.dur).sum / dur64

    @tailrec def loop(r: Rational): Rational = if (r <= 2) r else loop(r / 2)

    loop(r1)
  }

  private[Cell] def hasTuplet: Boolean = tuplet != 1

  /** Produces a string representation of this cell, suitable for rendering with Lilypond.
    * For example, a cell with elements (12, 5, 11) and duration of 21/32, will result in the following string:
    *
    * {{
    *     \time 21/32
    *     \times 3/4 { c'4.  c'8 ~ c'32  c'4 ~ c'16 ~ c'32 }
    * }}
    *
    * (This is mostly adapted from LilyCollider)
    *
    *
    * @return
    */
  def toLilypondString(timeSig: Option[LilyTimeSignature] = Some(LilyTimeSignature.Raw),
                       annotation: String = ""): String = {
    val cell0       = this.usingIntegers
    val (cell, err) = timeSig.fold(cell0 -> 0f) {
      case sr @ LilyTimeSignature.Rounded(_) =>
        val durIn           = cell0.dur
        val (durOut, err)   = sr(durIn)
        val res             = cell0 * (durOut/durIn)
        res -> err

      case _ => cell0 -> 0f
    }

    val ns0       = cell.adjusted.map { n =>
      val d = n.dur
      val i = d.toInt
      assert(d == i, d)
      val s = if (n.isRest) "r" else "c'"
      Cell.lilyDurations(i).map(ds => s"$s$ds").mkString(" ~ ")
    }
    val ns  = if (annotation == "") ns0 else {
      val annotStr = raw"""\annotation "$annotation" """
      ns0 match {
        case head +: tail => head +: annotStr +: tail
        case _ => ns0 :+ annotStr
      }
    }
    val nss = ns.mkString(" ")
    val tup = if (cell.hasTuplet) {
      s"\\times ${cell.tuplet.reciprocal} { $nss }"
    } else {
      nss
    }

    timeSig.fold(tup) { sig =>
      val dur       = cell.dur
      val sigStr    = s"\\time ${dur.numerator}/${dur.denominator}"  // Lilypond doesn't handle integers
      val tempoStr  = sig match {
        case LilyTimeSignature.Rounded(_) if err != 0f =>
          val factor = (cell0.dur / dur).toDouble - 1
          f""" \\tempo "${if (factor > 0) "+" else "–"}${math.abs(factor)*100}%1.1f%%"""" // note: signum is inverted

        case LilyTimeSignature.Decimal =>
          val sec   = (dur * 2).toDouble  // assuming 1/4 = 120, or 1/1 = 30
          val secS0 = f"$sec%1.2f"
          val secS  = if (secS0.endsWith("0")) secS0.substring(0,secS0.length - 1) else secS0 // suckers never die

          f""" \\override Staff.TimeSignature #'transparent = ##t
             |\\tempo \\markup { \\concat { $secS \\char ##x201D }}""".stripMargin

        case _ => ""
      }

      s"$sigStr$tempoStr \n$tup\n"
    }
  }
}