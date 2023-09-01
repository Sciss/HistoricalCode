package com.chimpler.javacv

import org.bytedeco.javacpp.helper.opencv_core.AbstractCvScalar
import org.bytedeco.javacpp.opencv_core.{Mat, Rect, RectVector}
import org.bytedeco.javacpp.opencv_imgproc.CvFont
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier
import org.bytedeco.javacpp.{opencv_core, opencv_imgproc}
import org.bytedeco.javacv.FrameGrabber.ImageMode
import org.bytedeco.javacv.{CanvasFrame, OpenCVFrameConverter, OpenCVFrameGrabber}

/**
 * Created by chimpler on 7/13/14.
 */
object FaceWebcamDetectorApp extends App {

  // holder for a single detected face: contains face rectangle and the two eye rectangles inside
  case class Face(id: Int, faceRect: Rect, leftEyeRect: Rect, rightEyeRect: Rect)

  // we need to clone the rect because openCV is recycling rectangles created by the detectMultiScale method
  private def cloneRect(rect: Rect): Rect = {
    new Rect(rect.x, rect.y, rect.width, rect.height)
  }

  class FaceDetector() {
    // read the haar classifier xml files for face, left eye and right eye
    val faceXml = FaceWebcamDetectorApp.getClass.getClassLoader.getResource("haarcascade_frontalface_alt.xml").getPath
    val faceCascade = new CascadeClassifier(faceXml)

    val leftEyeXml = FaceWebcamDetectorApp.getClass.getClassLoader.getResource("haarcascade_mcs_lefteye_alt.xml").getPath
    val leftEyeCascade = new CascadeClassifier(leftEyeXml)

    val rightEyeXml = FaceWebcamDetectorApp.getClass.getClassLoader.getResource("haarcascade_mcs_righteye_alt.xml").getPath
    val rightEyeCascade = new CascadeClassifier(rightEyeXml)

    def detect(greyMat: Mat): Seq[Face] = {
//      val faceRects = new Rect()
      val faceRects = new RectVector()
      faceCascade.detectMultiScale(greyMat, faceRects)
      (0 until faceRects.size().toInt).flatMap { i =>
//        val faceRect = faceRects.position(i)
        val faceRect = faceRects.get(i)

        // the left eye should be in the top-left quarter of the face area
        val leftFaceMat  = new Mat(greyMat, new Rect(faceRect.x, faceRect.y, faceRect.width() / 2, faceRect.height() / 2))
        val leftEyeRectV = new RectVector()
        leftEyeCascade.detectMultiScale(leftFaceMat, leftEyeRectV)

        // the right eye should be in the top-right quarter of the face area
        val rightFaceMat  = new Mat(greyMat, new Rect(faceRect.x + faceRect.width() / 2, faceRect.y, faceRect.width() / 2, faceRect.height() / 2))
        val rightEyeRectV = new RectVector()
        rightEyeCascade.detectMultiScale(rightFaceMat, rightEyeRectV)

        if (leftEyeRectV.size() > 0 && rightEyeRectV.size() > 0) {
          val leftEyeRect   = leftEyeRectV .get(0)
          val rightEyeRect  = rightEyeRectV.get(0)
          val fc = Face(i, cloneRect(faceRect), cloneRect(leftEyeRect), cloneRect(rightEyeRect))
          Some(fc)
        } else {
          None
        }
      }
    }
  }

  val canvas = new CanvasFrame("Webcam")

  val faceDetector = new FaceDetector
  //  //Set Canvas frame to close on exit
  canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE)

  //Declare FrameGrabber to import output from webcam
  val grabber = new OpenCVFrameGrabber(0)
  grabber.setImageWidth(640)
  grabber.setImageHeight(480)
  grabber.setBitsPerPixel(opencv_core.CV_8U)
  grabber.setImageMode(ImageMode.COLOR)
  grabber.start()

  var lastRecognitionTime = 0L
  val cvFont = new CvFont()
  cvFont.hscale(0.6f)
  cvFont.vscale(0.6f)
  cvFont.font_face(opencv_core.FONT_HERSHEY_SIMPLEX)

  // val mat     = new Mat(640, 480, opencv_core.CV_8UC3)
  val greyMat = new Mat(640, 480, opencv_core.CV_8U)
  var faces: Seq[Face] = Nil

  // cf. http://bytedeco.org/news/2015/04/04/javacv-frame-converters/
  val toMat       = new OpenCVFrameConverter.ToMat
  val toIplImage  = new OpenCVFrameConverter.ToIplImage

  while (true) {
    val img0 = grabber.grab()
    val mat = toMat     .convert(img0)
    val img = toIplImage.convert(img0)
    opencv_core.cvFlip(img, img, 1)

    // run the recognition every 200ms to not use too much CPU
    if (System.currentTimeMillis() - lastRecognitionTime > 200) {
      // mat.copyFrom(img.getBufferedImage)
      opencv_imgproc.cvtColor(mat, greyMat, opencv_imgproc.CV_BGR2GRAY, 1)
      opencv_imgproc.equalizeHist(greyMat, greyMat)
      faces = faceDetector.detect(greyMat)
      lastRecognitionTime = System.currentTimeMillis()
    }

    // draw the face rectangles with the eyes and caption
    for(f <- faces) {
      // draw the face rectangle
      opencv_imgproc.cvRectangle(img,
        opencv_core.cvPoint(f.faceRect.x, f.faceRect.y),
        opencv_core.cvPoint(f.faceRect.x + f.faceRect.width, f.faceRect.y + f.faceRect.height),
        AbstractCvScalar.RED,
        1, opencv_imgproc.CV_AA, 0)

      // draw the left eye rectangle
      opencv_imgproc.cvRectangle(img,
        opencv_core.cvPoint(f.faceRect.x + f.leftEyeRect.x, f.faceRect.y + f.leftEyeRect.y),
        opencv_core.cvPoint(f.faceRect.x + f.leftEyeRect.x + f.leftEyeRect.width, f.faceRect.y + f.leftEyeRect.y + f.leftEyeRect.height),
        AbstractCvScalar.BLUE,
        1, opencv_imgproc.CV_AA, 0)

      // draw the right eye rectangle
      opencv_imgproc.cvRectangle(img,
        opencv_core.cvPoint(f.faceRect.x + f.faceRect.width / 2 + f.rightEyeRect.x, f.faceRect.y + f.rightEyeRect.y),
        opencv_core.cvPoint(f.faceRect.x + f.faceRect.width / 2 + f.rightEyeRect.x + f.rightEyeRect.width, f.faceRect.y + f.rightEyeRect.y + f.rightEyeRect.height),
        AbstractCvScalar.GREEN,
        1, opencv_imgproc.CV_AA, 0)

      // draw the face number
      val cvPoint = opencv_core.cvPoint(f.faceRect.x, f.faceRect.y - 20)
      opencv_imgproc.cvPutText(img, s"Face ${f.id}", cvPoint, cvFont, AbstractCvScalar.RED)
    }
    canvas.showImage(img0)
  }

}
