package scalapt

import java.awt._
import java.awt.event.{MouseAdapter, MouseEvent, WindowAdapter, WindowEvent}
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

import de.sciss.rays.{ConcurrentUtils, MathUtil, MonteCarloRenderer, RGB, Random, SceneIO, SuperSampling}

object MainFrame {
  def main(args: Array[String]): Unit = {
    val inFile  = if (args.length > 0) args(0) else "scenes/cornell2.json"
    val width   = if (args.length > 1) Integer.parseInt(args(1)) else 1024
    val height  = if (args.length > 2) Integer.parseInt(args(2)) else 768
    val frames  = if (args.length > 3) Integer.parseInt(args(3)) else 1024
    val outFile = if (args.length > 4) Option(new File(args(4))) else Option.empty

    /* val frame = */ new MainFrame("ScalaPT", width, height, inFile, frames, outFile)
  }
}

class MainFrame(frameTitle: String, val w: Int, val h: Int, val inFile: String, val frames: Int,
                val outFile: Option[File], var closing: Boolean = false)
  extends Frame(frameTitle) {

  System.out.println(s"Scene:  $inFile")
  System.out.println(s"Width:  $w")
  System.out.println(s"Height: $h")
  System.out.println(s"Frames: $frames")
  outFile.foreach(name => System.out.println(s"Outfile: $name"))

  val scene = SceneIO.load(inFile)

  pack()

  val ins = getInsets
  val dim = new Dimension(w + ins.left + ins.right, h + ins.top + ins.bottom)
  setSize(dim)
  setResizable(false)
  addWindowListener(new WindowAdapter() {
    override def windowClosing(we: WindowEvent): Unit = {
      closing = true
      dispose()
    }
  })

  addMouseListener(new MouseAdapter() {
    override def mouseClicked(me: MouseEvent): Unit = {
      val sx = me.getX
      val sy = me.getY
      val x = sx - ins.left + 1
      val y = h - (sy - ins.top) - 3

      System.out.print(s"$x : $y -> ")

      implicit val rand = Random(x + y * rdr.width)
      val ss = rdr.render(x, y)
      System.out.println(ss)
    }
  })

  setLocationRelativeTo(null)
  setBackground(Color.BLACK)
  setVisible(true)

  val rdr = new MonteCarloRenderer(w, h, scene)

  val renderData = new Array[Array[SuperSampling]](h)

  val image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)

  val gr2d = image.getGraphics
  gr2d.setColor(Color.RED)
  gr2d.drawRect(0, 0, w - 1, h - 1)
  repaint(ins.left, ins.top, w, h)

  for (i <- 0 until frames) {
    if (!closing) {
      render(i)
    }
  }

  System.out.println(s"${new java.util.Date()} : Done")

  outFile.foreach(file => {
    val name = file.getName
    val dotPos = name.lastIndexOf('.')
    val format =
      if (dotPos != -1) {
        name.substring(dotPos + 1)
      } else {
        "png"
      }
    System.out.println(s"Saving to file '$name' as format $format")
    if (!ImageIO.write(image, format, file)) {
      System.out.println(s"ERROR: filename prefix '$format not recognised as a format")
    }
  })

  def render(i: Int): Unit = {
    System.out.println(s"${new java.util.Date()} : Frame $i")
    ConcurrentUtils.parallelFor(0 until rdr.height) { y =>
      val row = new Array[SuperSampling](rdr.width)
      for (x <- 0 until rdr.width) {
        val seed = (x + y * rdr.width) * (i + 1)
        implicit val rand = Random(seed)
        row(x) = rdr.render(x, y)
      }

      if (i == 0)
        renderData(y) = row
      else
        merge(renderData(y), row, i)

      val mergedRow = renderData(y)

      val sy = h - y - 1
      for (sx <- 0 until w) {
        image.setRGB(sx, sy, colVecToInt(mergedRow(sx).clamp))
      }

      repaint(ins.left, ins.top + sy, w, 1)
    }
  }

  private def merge(lhs: Array[SuperSampling], rhs: Array[SuperSampling], n: Int): Unit =
    for (i <- lhs.indices) {
      lhs(i) = lhs(i).merge(rhs(i), n)
    }

  private def colVecToInt(colour: RGB): Int =
     colDblToInt(colour.blue )        |
    (colDblToInt(colour.green) <<  8) |
    (colDblToInt(colour.red  ) << 16)

  private def colDblToInt(d: Double): Int = {
    val i = MathUtil.gammaCorr(d)
    val j = i * 255.0 + 0.5
    MathUtil.clamp(j, 0, 255).toInt
  }

  override def paint(graphics: Graphics): Unit = {
    val g2d = graphics.asInstanceOf[Graphics2D]
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF)
    g2d.drawImage(image, ins.left, ins.top, null)
  }
}