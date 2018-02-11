package de.sciss.webcamtest

import java.awt.image.BufferedImage
import java.io.File

import com.github.sarxos.webcam.Webcam
import javax.imageio.ImageIO

object Main {
  def main(args: Array[String]): Unit = {
    println("Getting cam")
    val cam: Webcam = Webcam.getDefault
    println("Opening cam")
    cam.open()
    println("Grabbing image")
    val img: BufferedImage = cam.getImage
    println("Saving image file")
    ImageIO.write(img, "PNG", new File("test.png"))
    println("Bye")
  }
}