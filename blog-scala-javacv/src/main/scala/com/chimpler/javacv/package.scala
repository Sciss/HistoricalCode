package com.chimpler

import java.awt.image.BufferedImage

import org.bytedeco.javacpp.opencv_core.{IplImage, Mat}
import org.bytedeco.javacv.{Java2DFrameConverter, OpenCVFrameConverter}
import org.bytedeco.javacv.OpenCVFrameConverter.ToMat

package object javacv {
  // cf. https://github.com/bytedeco/javacv-examples/blob/c343f976c558005f1354d1b4bf57c2fa587ee5d7/OpenCV2_Cookbook/src/main/scala/opencv2_cookbook/OpenCVUtils.scala
  def toBufferedImage(ipl: IplImage): BufferedImage = {
    val openCVConverter = new OpenCVFrameConverter.ToIplImage()
    val java2DConverter = new Java2DFrameConverter()
    java2DConverter.convert(openCVConverter.convert(ipl))
  }

  def toBufferedImage(mat: Mat): BufferedImage = {
    val openCVConverter = new ToMat()
    val java2DConverter = new Java2DFrameConverter()
    java2DConverter.convert(openCVConverter.convert(mat))
  }
}