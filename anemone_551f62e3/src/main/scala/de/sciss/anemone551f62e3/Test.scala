/*
 *  Test.scala
 *  (anemone_551f62e3)
 *
 *  Copyright (c) 2015 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU General Public License v2+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.anemone551f62e3

import de.sciss.file._
import de.sciss.synth.io.{AudioFileSpec, AudioFile}

object Test {
  def main(args: Array[String]): Unit = run()

  case class Rect(x1: Int, y1: Int, x2: Int, y2: Int) {
    def *(scalar: Int) = Rect(x1 = x1 * scalar, y1 = y1 * scalar, x2 = x2 * scalar, y2 = y2 * scalar)
  }

  def run(): Unit = {
    // val rect = Rect(x1 = 2993, y1 = 3016, x2 = 3437, y2 = 3103) * 2
    val rect        = Rect(x1 = 3672, y1 = 1021, x2 = 3894, y2 = 2615) * 2
    val hInner      = true
    val rndHi       = 16
    import rect._
    val fOut = userHome / "Documents" / "temp" / s"a_551f62e3_$x1-$y1-$x2-$y2-${if (hInner) "h" else "v"}-f$rndHi.aif"
    if (fOut.exists()) {
      Console.err.println(s"File '$fOut' already exists. Aborting")
      sys.exit(1)
    }

    val fIn     = userHome / "Documents" / "temp" / "SIM_OUT_hpz1.aif"
    val extIn   = 6467
    val extOut  = extIn * 2

    def offset(x: Int, y: Int): Int =
      if (x + y >= extOut) /* flip diagonal */ offset(extOut - x - 1, extOut - y - 1) else {
        val xm = extOut - x
        xm * x + (x * (x + 1))/2 + (xm - 1 - y)
      }

    val rnd = new util.Random(0L)
    def rrand(lo: Int, hi: Int): Int = rnd.nextInt(hi - lo + 1) + lo
    val rndLo = -rndHi

    def fuzzy(in: Int): Int = in + rrand(rndLo, rndHi)

    val afIn = AudioFile.openRead(fIn)
    try {
      val afOut = AudioFile.openWrite(fOut, AudioFileSpec(numChannels = 1, sampleRate = 44100.0))
      try {
        val buf = afIn.buffer(1)

//        xc <-       x1  to       x2
//        yc <- fuzzy(y1) to fuzzy(y2)

        def process(xc: Int, yc: Int): Unit = {
          val off = offset(xc, yc)
          afIn.seek(off)
          afIn.read(buf, 0, 1)
          afOut.write(buf, 0, 1)
        }

        if (hInner)
          for {
            yc <-       y1  to       y2
            xc <- fuzzy(x1) to fuzzy(x2)
          } process(xc, yc)
        else
          for {
            xc <-       x1  to       x2
            yc <- fuzzy(y1) to fuzzy(y2)
          } process(xc, yc)

      } finally {
        afOut.close()
      }

    } finally {
      afIn.close()
    }
  }
}
