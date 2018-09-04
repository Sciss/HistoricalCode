/*
 *  Pitch.scala
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

package at.iem.point.illism

object Pitch {
  // cf. http://www.sengpielaudio.com/Rechner-notennamen.htm

  implicit val ordering: Ordering[Pitch] = Ordering.by[Pitch, Int](_.midi)

  def toString(midi: Int, lang: Language = Language.default): String = {
    val pc        = midi % 12
    val register  = midi / 12
    lang match {
      case Language.English =>
        PitchClass.toString(pc, lang) + register
      case Language.German =>
        val caps  = register <= 3
        val pcs   = PitchClass.toString(pc, lang, caps)
        if (register <= 2) {
          val ticks = 3 - register
          ("," * ticks) + pcs
        } else if (register >= 5) {
          val ticks = register - 4
          pcs + ("'" * ticks)
        } else {
          pcs
        }
    }
 	}

  // XXX TODO: issue #1
  //  def fromString(s: String, lang: Language = Language.default): Int = {
  //
  //  }
}
final class Pitch(val midi: Int) extends AnyVal {
  override def toString: String = Pitch.toString(midi)

  def to(that: Pitch): DirectedInterval = (this, that) // new DirectedInterval(math.abs(this.midi - that.midi))

  def +(interval: Interval): Pitch = {
    val steps = interval match {
      case d: DirectedInterval    => d.semitones * d.direction
      case u: UndirectedInterval  => u.semitones
    }
    new Pitch(midi + steps)
  }

  def map(fun: Int => Int): Pitch = new Pitch(fun(midi))

  implicit def `class`: PitchClass = new PitchClass(midi % 12)
}

object PitchClass {
  private final val pcStrings_en = Array("C","C#","D","D#","E","F","F#","G","G#","A","A#","B")
  private final val pcStrings_de = Array("c","cis","d","dis","e","f","fis","g","gis","a","ais","h")

  def toString(step: Int, lang: Language = Language.default, caps: Boolean = false): String = {
    lang match {
      case Language.English => pcStrings_en(step)
      case Language.German =>
        val s = pcStrings_de(step)
        if (caps) s.capitalize else s
    }
 	}

}
final class PitchClass(val step: Int) extends AnyVal {
  override def toString: String = PitchClass.toString(step)
}
