package de.sciss.audiosnake

import de.sciss.synth.io.{AudioFileType, AudioFile}
import java.io.File

object AudioSnake {
  def main(args: Array[String]) {
    var inPath    = ""
    var outPath   = ""
    var duration  = 5.0

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

    } else {
      sys.exit(1)
    }
  }

  private implicit final class StringOps(s: String) extends AnyVal {
    def extension: String = {
      val n = new File(s).getName
      val i = n.lastIndexOf('.')
      if(i < 0) "" else n.substring(i + 1)
    }
  }

  def run(inPath: String, outPath: String, dur: Double) {
    val afIn = AudioFile.openRead(inPath)
    try {
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

  }
}