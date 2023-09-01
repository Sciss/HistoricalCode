package de.sciss.webcamtest

import java.awt.image.BufferedImage
import java.io.File

import com.github.sarxos.webcam.Webcam
import javax.imageio.ImageIO

import scala.collection.JavaConverters._

object Main {
  case class Config(camName: Option[String] = None, list: Boolean = false)

  def main(args: Array[String]): Unit = {
    val p = new scopt.OptionParser[Config]("WebcamCapture-Test") {
      opt[String] ('c', "cam")
        .text ("Camera name (default: none)")
        .action   { (v, c) => c.copy(camName = Some(v)) }

      opt[Unit] ('l', "list")
        .text ("List available cameras")
        .action   { (_, c) => c.copy(list = true) }
    }
    p.parse(args, Config()).fold(sys.exit(1))(run)
  }

  def run(config: Config): Unit = {
    import config._

    if (list) {
      println("Detected cameras:")
      Webcam.getWebcams.asScala.foreach { cam0 =>
        println(s"- '${cam0.getName}'")
      }
    }
    val cam: Webcam = camName match {
      case Some(n) =>
        println(s"Getting camera '$n'")
        Webcam.getWebcams.asScala.find(_.getName == n).getOrElse(sys.error("No camera of that name present"))

      case None =>
        println("Getting default camera")
        Webcam.getDefault
    }
    println("Opening cam")
    cam.open()
    println("Grabbing image")
    val img: BufferedImage = cam.getImage
    println("Saving image file")
    ImageIO.write(img, "PNG", new File("test.png"))
    println("Bye")
  }
}