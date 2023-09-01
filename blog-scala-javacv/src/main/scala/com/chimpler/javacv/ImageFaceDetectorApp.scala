package com.chimpler.javacv

import java.awt.{Color, Font}
import java.io.File
import javax.imageio.ImageIO

import org.bytedeco.javacpp.opencv_core.{Mat, RectVector}
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier
import org.bytedeco.javacpp.{opencv_imgcodecs, opencv_imgproc}

/**
 * Created by chimpler on 7/13/14.
 */
object ImageFaceDetectorApp extends App {
  if (args.length != 1) {
    sys.error("Argument: image filename")
    sys.exit()
  }
  val imageFilename = args(0)
//  val mat = opencv_highgui.imread(imageFilename)
  val mat = opencv_imgcodecs.imread(imageFilename)

  // convert image to greyscale
  val greyMat = new Mat()
  opencv_imgproc.cvtColor(mat, greyMat, opencv_imgproc.CV_BGR2GRAY, 1)
  ImageIO.write(toBufferedImage(greyMat), "jpg", new File("output_grey.jpg"))

  // equalize histogram
  val equalizedMat = new Mat()
  opencv_imgproc.equalizeHist(greyMat, equalizedMat)
  ImageIO.write(toBufferedImage(equalizedMat), "jpg", new File("output_equalized.jpg"))

  val faceXml = FaceWebcamDetectorApp.getClass.getClassLoader.getResource("haarcascade_frontalface_alt.xml").getPath
  val faceCascade = new CascadeClassifier(faceXml)
  val faceRects = new RectVector()
  faceCascade.detectMultiScale(equalizedMat, faceRects)


  val image = toBufferedImage(mat)
  val graphics = image.getGraphics
  graphics.setColor(Color.RED)
  for(i <- 0 until faceRects.size().toInt /* limit() */) {
//    val faceRect = faceRects.position(i)
    val faceRect = faceRects.get(i)
    graphics.drawRect(faceRect.x, faceRect.y, faceRect.width, faceRect.height)
    graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18))
    graphics.drawString(s"Face $i", faceRect.x, faceRect.y - 20)
  }
  ImageIO.write(image, "jpg", new File("output_faces.jpg"))
}
