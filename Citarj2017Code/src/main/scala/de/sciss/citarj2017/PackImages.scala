package de.sciss.citarj2017

import java.awt.image.BufferedImage
import java.awt.{Graphics2D, Rectangle}
import java.util
import javax.imageio.ImageIO

import de.sciss.file._
import de.sciss.packing2d.Packer
import de.sciss.packing2d.Packer.Algorithm

object PackImages {
  final case class Config(
                           dpi        : Int     = 72,
                           convert    : String  = "convert",
                           imageDir   : File    = file("images"),
                           output     : File    = file("output.png"),
                           inputIsPDF : Boolean = false,
                           width      : Int     = 400,
                           height     : Int     = 0,
//                           timeOut    : Int     = 60,
                           pad        : Int     = 4,
                           tab        : Int     = 4,
                           algorithm: Algorithm = Algorithm.FIRST_FIT_DECREASING_HEIGHT
                         )

  private def algoNames: Seq[String] = Algorithm.values().map(_.name())

  def main(args: Array[String]): Unit = {
    val default = Config()

    val p = new scopt.OptionParser[Config]("PackImages") {
      opt[File]('i', "input")
        .text (s"Base input directory of png or pdf images")
        .required()
        .action { (v, c) => c.copy(imageDir = v) }

      opt[File]('o', "output")
        .text ("Output png image path")
        .required()
        .action { (v, c) => c.copy(output = v) }

      opt[Unit] ("pdf")
        .text (s"Input is pdf files (default: ${default.inputIsPDF})")
        .action   { (_, c) => c.copy(inputIsPDF = true) }

      opt[Unit] ("png")
        .text (s"Input is png files (default: ${!default.inputIsPDF})")
        .action   { (_, c) => c.copy(inputIsPDF = false) }

      opt[String]("convert")
        .text (s"Program to convert from pdf to png (default: ${default.convert})")
        .action { (v, c) => c.copy(convert = v) }

      opt[Int]("density")
        .text (s"Density in dpi for pdf-to-png conversion (default: ${default.dpi})")
        .action { (v, c) => c.copy(dpi = v) }

      opt[Int]('w', "width")
        .text (s"Output image width in pixels (default: ${default.width})")
        .validate { v => if (v > 0) success else failure("Must be > 0") }
        .action { (v, c) => c.copy(width = v) }

      opt[Int]('h', "height")
        .text (s"Output image height in pixels (default: ${default.height})")
        .validate { v => if (v >= 0) success else failure("Must be >= 0") }
        .action { (v, c) => c.copy(height = v) }

      opt[Int]("pad")
        .text (s"Padding around each image in pixels (default: ${default.pad})")
        .validate { v => if (v >= 0) success else failure("Must be >= 0") }
        .action { (v, c) => c.copy(pad = v) }

      opt[Int]("tab")
        .text (s"Margin around output image in pixels (default: ${default.tab})")
        .validate { v => if (v >= 0) success else failure("Must be >= 0") }
        .action { (v, c) => c.copy(tab = v) }

//      opt[Int]('t', "timeout")
//        .text (s"Calculation timeout in seconds (default: ${default.timeOut})")
//        .validate { v => if (v > 0) success else failure("Must be > 0") }
//        .action { (v, c) => c.copy(timeOut = v) }

      opt[String]('a', "algorithm")
        .text (s"Algorithm (default: ${default.algorithm})")
        .validate { v => if (Algorithm.values().exists(_.name() == v)) success else failure(s"None of ${algoNames.mkString(", ")}") }
        .action { (v, c) => c.copy(algorithm = Algorithm.valueOf(v)) }
    }
    p.parse(args, default).fold(sys.exit(1))(run)
  }

  case class Image(f: File, w: Int, h: Int) extends Rectangle(0, 0, w, h)

  def factorial(i: Int): Long = {
    var j = i
    var res = 1L
    while (j > 0) {
      res *= j
      j -= 1
    }
    res
  }

  def run(config: Config): Unit = {
    val inputFiles  =
      if (config.inputIsPDF) config.imageDir.children(_.extL == "pdf")
      else                   config.imageDir.children(f => f.extL == "png" || f.extL == "jpg")

    require(inputFiles.nonEmpty, s"No input images detected.")

    val pad2 = config.pad * 2

    val imageFiles = if (!config.inputIsPDF) inputFiles else {
      inputFiles.map { inF =>
        val tempF = File.createTemp(suffix = ".png", deleteOnExit = true)
        import sys.process._
        val cmd = Seq(config.convert, "-density", config.dpi.toString, inF.path, tempF.path)
        println(cmd.mkString(" "))
        cmd.!!
        tempF
      }
    }

    val boxes = imageFiles.map { f =>
      val in      = ImageIO.createImageInputStream(f)
      val reader  = ImageIO.getImageReaders(in).next()
      try {
        reader.setInput(in)
        val w = reader.getWidth (0) + pad2
        val h = reader.getHeight(0) + pad2
        Image(f, w = w, h = h)

      } finally {
        reader.dispose()
      }
    }

    import scala.collection.JavaConverters._

    val boxesJ = new util.ArrayList[Image](boxes.size)
    boxes.foreach(boxesJ.add)
    val packed = Packer.pack[Image](boxesJ, config.algorithm, config.width - (config.tab * 2)).asScala

    val height  = if (config.height > 0) config.height else {
      val res = packed.map(r => r.y + r.height).max + config.tab
      println(s"Image height is $res")
      res
    }
    val img     = new BufferedImage(config.width, height, BufferedImage.TYPE_INT_ARGB)
    val g       = img.getGraphics.asInstanceOf[Graphics2D]

    val xOff    = config.pad + config.tab
    val yOff    = config.pad + config.tab

    packed.foreach { pi =>
      val imageF  = pi.f
      val img     = ImageIO.read(imageF)
      import pi.{x, y}
      g.drawImage(img, x + xOff, y + yOff, null)
    }

    g.dispose()
    val fmt = if (config.output.extL == "jpg") "jpg" else "png"
    println("Writing...")
    config.output.delete()
    ImageIO.write(img, fmt, config.output)
    println("Done.")
    sys.exit()
  }
}
