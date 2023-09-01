package de.sciss.audiosnake

import de.sciss.synth.io.{SampleFormat, AudioFileType, AudioFile}
import java.io.File
import de.sciss.contextsnake.ContextTree

object AudioSnake {
  def main(args: Array[String]) {
    var inPath    = ""
    var outPath   = ""
    var dur       = 10.0
    var quant     = 10000
    var floatFmt  = false
    var gain      = 1.0
    var backMin   = 5
    var seed      = new (() => Long) {
      override def toString() = "system clock"
      def apply() = System.currentTimeMillis()
    }

//    val p = new org.sellmerfud.optparse.OptionParser
//    p.banner = "AudioSnake [options] input"
//    p.optl[Double]("-l", "--length <value>", "maximum output duration in seconds (default: 5.0)")(duration = _)
//    p.reqd[String]("-o", "--output <file>", "output audio file (required)")(outPath = _)
//    p.parse(args) match {
//      case inPath :: Nil =>
//      case Nil => Console.err("Missing")
//    }

    val parser = new scopt.mutable.OptionParser("AudioSnake") {
      opt(           "float",                 "use floating point output", { floatFmt = true })
      intOpt(   "b", "back", "<value>",      s"minimum sequence length in backtracking (default: ${backMin}})", { v: Int => backMin = v })
      intOpt(   "q", "quantize", "<value>",  s"quantization steps (default: ${quant}})", { v: Int => quant = v })
      intOpt(   "r", "rand", "<value>",      s"random seed value (default: ${seed})", { v: Int => seed = () => v.toLong })
      doubleOpt("l", "length", "<value>",    s"maximum output duration in seconds (default: ${dur})", { v: Double => dur = v })
      doubleOpt("g", "gain", "<value>",      s"linear output amplitude factor (default: ${gain})", { v: Double => gain = v })
      opt(      "o", "output", "<file>",      "output audio file", { v: String => outPath = v })
      arg(                     "<inputfile>", "input audio file",  { v: String => inPath  = v })
    }
    if (parser.parse(args)) {
      if (outPath == "") {
        Console.err.println("Error: required argument: -o <file>")
        parser.showUsage
        sys.exit(1)
      }
      val rand = new util.Random(seed())
      run(inPath = inPath, outPath = outPath, dur = dur, quant = quant, gain = gain.toFloat,
        floatFmt = floatFmt, backMin = backMin, rand = rand)

    } else {
      sys.exit(1)
    }
  }

  private implicit final class StringOps(val s: String) extends AnyVal {
    def extension: String = {
      val n = new File(s).getName
      val i = n.lastIndexOf('.')
      if(i < 0) "" else n.substring(i + 1)
    }
  }

  def run(inPath: String, outPath: String, dur: Double, quant: Int, gain: Float, floatFmt: Boolean, backMin: Int,
          rand: util.Random) {
    val afIn = AudioFile.openRead(inPath)
    try {
      require(afIn.numChannels == 1, "Currently requires input to be monophonic (found " + afIn.numChannels + " channels)")
      val ext     = outPath.extension
      val outTpe  = if (ext == "") afIn.fileType else {
        AudioFileType.writable.find( _.extensions.contains(ext)).getOrElse(afIn.fileType)
      }
      val outSpec = afIn.spec.copy(fileType = outTpe,
                                   sampleFormat = if (floatFmt) SampleFormat.Float else afIn.sampleFormat,
                                   byteOrder = None,
                                   numFrames = 0L)
      val afOut = AudioFile.openWrite(outPath, outSpec)
      try {
        val maxFrames = (afOut.sampleRate * dur + 0.5).toLong
        perform(in = afIn, out = afOut, maxFrames = maxFrames, quant = quant, gain = gain, backMin = backMin,
                rand = rand)
      } finally {
        afOut.close()
      }
    } finally {
      afIn.close()
    }
  }

  private def perform(in: AudioFile, out: AudioFile, maxFrames: Long, quant: Int, gain: Float, backMin: Int,
                      rand: util.Random) {
    println("Building suffix tree...")
    val buf = in.buffer(8192)
    val cb  = buf(0)
    val ctx = ContextTree.empty[Int]
    var off = 0L
    var prg = 0
    val numFrames = in.numFrames

    var x1 = 0f

//    val quant = 1 << 13
    val gainIn  = quant*0.5f
    val gainOut = gain / gainIn

    while (off < numFrames) {
      val chunk = math.min(8192, numFrames - off).toInt
      in.read(buf, 0, chunk)

      // HPZ1
      { var i = 0; while (i < chunk) {
        val x0 = cb(i)
        val y0 = x0 - x1
        x1 = x0
        cb(i) = y0
      i += 1}}

      { var i = 0; while (i < chunk) {
        val x0 = cb(i)
        val j  = (x0 * gainIn).toInt
        ctx += j
      i += 1 }}
//      ctx.appendAll(if (chunk == 8192) cb else cb.view(0, chunk))
      off += chunk
      val prgNew = ((off * 40) / numFrames).toInt
      while (prg < prgNew) {
        print("#")
        prg += 1
      }
    }
    println()

    println("Generating output...")

    val snake = ctx.snake(ctx(0) :: Nil)
    off         = 0L
    var bufOff  = 0
    x1          = 0f
    prg         = 0

    var dcMem0 = 0.0
    var dcMem1 = 0.0
    var xInteg = 0.0

    def flushOut() {
      var i = 0; while (i < bufOff) {
        val x0 = cb(i)
//        val y0 = x0 + x1
//        x1 = x0
      // ---- integrate and remove DC ----
        xInteg += x0
        val y0 = xInteg - dcMem0 + 0.99 * dcMem1
        dcMem0 = xInteg
        dcMem1 = y0
        cb(i) = y0.toFloat
      i += 1}
      out.write(buf, 0, bufOff)
      bufOff = 0
      val prgNew = ((off * 40) / maxFrames).toInt
      while (prg < prgNew) {
        print("#")
        prg += 1
      }
    }

//    val maxSingleChoice = 40 // 100
//    val resetSingleChoice = maxSingleChoice * 100
//    var singleChoice = 0

    while (off < maxFrames && snake.nonEmpty) {
      val sq = snake.successors.take(20).to[Vector]
      val sz = sq.size
      if (sz > 0) {
        if (sz == 1 && snake.size > backMin) {
          snake.trimStart(1) // math.min(snake.size - 1, 2))
        } else {
          val j  = if (sz == 1) sq.head else sq(rand.nextInt(sz))
          snake += j
  //        if (sz == 1) {
  //          singleChoice += 1
  //          if (singleChoice >= resetSingleChoice) singleChoice = 0
  //        } else {
  //          singleChoice = 0
  //        }
  //        if (sz > 1 || singleChoice < maxSingleChoice) {
            val x0 = j.toFloat * gainOut
            cb(bufOff) = x0
            bufOff += 1
            if (bufOff == 8192) flushOut()
            off += 1
  //        }
        }
      } else {
        snake.trimStart(1)
      }
    }

    if (bufOff > 0) flushOut()
    println()
  }
}