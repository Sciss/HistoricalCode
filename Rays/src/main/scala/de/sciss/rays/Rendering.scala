package de.sciss.rays

import java.awt.image.BufferedImage

final class Rendering(_image: BufferedImage) {
  private[this] val width       = _image.getWidth
  private[this] val height      = _image.getHeight

  private[this] val renderData  = new Array[Array[SuperSampling]](height)

  def image: BufferedImage = _image

  def iterate(iter: Int, rdr: Renderer): Unit = {
    println(s"${new java.util.Date()} : iter $iter")
    ConcurrentUtils.parallelFor (0 until rdr.height) { y =>
      val row = new Array[SuperSampling](rdr.width)
      var x = 0
      while (x < rdr.width) {
        val seed = (x+y*rdr.width)*(iter+1)
        implicit val rand = Random(seed)
        row(x) = rdr.render(x, y)
        x += 1
      }

      if (iter == 0)
        renderData(y) = row
      else
        merge(renderData(y), row, iter)

      val mergedRow = renderData(y)

      val sy = height - y - 1
      var sx = 0
      while (sx < width) {
        _image.setRGB(sx, sy, colVecToInt(mergedRow(sx).clamp))
        sx += 1
      }
    }
  }

  @inline
  private[this] def merge(lhs: Array[SuperSampling], rhs: Array[SuperSampling], n: Int): Unit = {
    var i = 0
    while (i < lhs.length) {
      lhs(i) = lhs(i).merge(rhs(i), n)
      i += 1
    }
  }

  @inline
  private[this] def colVecToInt(colour : RGB) : Int =
       colDblToInt(colour.blue )        |
      (colDblToInt(colour.green) <<  8) |
      (colDblToInt(colour.red  ) << 16)

  private[this] def colDblToInt(d: Double): Int = {
    val i = MathUtil.gammaCorr(d)
    val j = i * 255.0 + 0.5
    MathUtil.clamp(j, 0, 255).toInt
  }
}