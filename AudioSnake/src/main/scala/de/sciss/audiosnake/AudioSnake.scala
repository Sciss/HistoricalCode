package de.sciss.audiosnake

import de.sciss.synth.io.{AudioFileType, AudioFile}
import java.io.File
import de.sciss.contextsnake.ContextTree

object AudioSnake {
  def main(args: Array[String]) {
    var inPath    = ""
    var outPath   = ""
    var duration  = 5.0

//    val p = new org.sellmerfud.optparse.OptionParser
//    p.banner = "AudioSnake [options] input"
//    p.optl[Double]("-l", "--length <value>", "maximum output duration in seconds (default: 5.0)")(duration = _)
//    p.reqd[String]("-o", "--output <file>", "output audio file (required)")(outPath = _)
//    p.parse(args) match {
//      case inPath :: Nil =>
//      case Nil => Console.err("Missing")
//    }

    val parser = new scopt.mutable.OptionParser("AudioSnake") {
      doubleOpt("l", "length", "<value>",     "maximum output duration in seconds (default: 5.0)", { v: Double => duration = v })
      opt(      "o", "output", "<file>",      "output audio file", { v: String => outPath = v })
      arg(                     "<inputfile>", "input audio file",  { v: String => inPath  = v })
    }
    if (parser.parse(args)) {
      if (outPath == "") {
        Console.err.println("Error: required argument: -o <file>")
        parser.showUsage
        sys.exit(1)
      }
      run(inPath, outPath, duration)

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

  def run(inPath: String, outPath: String, dur: Double) {
    val afIn = AudioFile.openRead(inPath)
    try {
      require(afIn.numChannels == 1, "Currently requires input to be monophonic (found " + afIn.numChannels + " channels)")
      val ext     = outPath.extension
      val outTpe  = if (ext == "") afIn.fileType else {
        AudioFileType.writable.find( _.extensions.contains(ext)).getOrElse(afIn.fileType)
      }
      val outSpec = afIn.spec.copy(fileType = outTpe, byteOrder = None, numFrames = 0L)
      val afOut = AudioFile.openWrite(outPath, outSpec)
      try {
        val maxFrames = (afOut.sampleRate * dur + 0.5).toLong
        perform(afIn, afOut, maxFrames)
      } finally {
        afOut.close()
      }
    } finally {
      afIn.close()
    }
  }

  private def perform(in: AudioFile, out: AudioFile, maxFrames: Long) {
    println("Building suffix tree...")
    val buf = in.buffer(8192)
    val cb  = buf(0)
    val ctx = ContextTree.empty[Float]
    var off = 0L
    var prg = 0
    val numFrames = in.numFrames

    var x1 = 0f

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

      ctx.appendAll(if (chunk == 8192) cb else cb.view(0, chunk))
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
    val rnd = new util.Random()
    off = 0L
    var bufOff = 0
    x1 = 0f
    prg = 0

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

    while (off < maxFrames && snake.nonEmpty) {
      val sq = snake.successors.take(10).to[Vector]
      val sz = sq.size
      if (sz > 0) {
        val e = if (sz == 1) sq.head else sq(rnd.nextInt(sz))
        snake += e
        cb(bufOff) = e
        bufOff += 1
        if (bufOff == 8192) flushOut()
        off += 1
      }
      if (sz == 0 || (sz == 1 && snake.size > 1)) snake.trimStart(1)
    }

    if (bufOff > 0) flushOut()
  }
}