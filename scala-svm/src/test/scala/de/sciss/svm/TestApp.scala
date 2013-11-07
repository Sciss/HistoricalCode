package de.sciss.svm

import java.awt.{Color => JColor}
import scala.swing.{Button, Orientation, BoxPanel, Color, GridPanel, CheckBox, Swing, Graphics2D, MainFrame, Frame, Component, SimpleSwingApplication}
import Swing._
import scala.collection.breakOut
import scala.swing.event.ButtonClicked

object TestApp extends SimpleSwingApplication {
  def numInstances = 10

  class ColorBox extends Component {
    private var _color = Color.white
    def color = _color
    def color_=(value: Color): Unit = {
      _color = value
      repaint()
    }

    preferredSize = (48, 48)
    opaque        = true

    override def paintComponent(g: Graphics2D): Unit = {
      g.setColor(_color)
      g.fillRect(0, 0, peer.getWidth, peer.getHeight)
      super.paintComponent(g)
    }
  }

  implicit class RichColor(val c: Color) extends AnyVal {
    private def hsb: Array[Float] = JColor.RGBtoHSB(c.getRed, c.getGreen, c.getBlue, null)

    def hue       : Double = hsb(0)
    def brightness: Double = hsb(1)
    def saturation: Double = hsb(2)

    def asFeature: List[Node] =
      hsb.zipWithIndex.map { case (v, idx) => Node(idx + 1, v) } (breakOut)
  }

  //  implicit class RichColorCompanion(val c: Color.type) extends AnyVal {
  //    def random(): Color = Color.getHSBColor(math.random.toFloat, math.random.toFloat, math.random.toFloat)
  //  }

  object Color {
    def random(): Color = JColor.getHSBColor(math.random.toFloat, math.random.toFloat, math.random.toFloat)

    val white     = JColor.white
    val black     = JColor.black
    val red       = JColor.red
    val green     = JColor.green
    val blue      = JColor.blue
    val yellow    = JColor.yellow
    val cyan      = JColor.cyan
    val magenta   = JColor.magenta
    val gray      = JColor.gray
    val lightGray = JColor.lightGray
    val darkGray  = JColor.darkGray
  }

  def randomizeData(): Unit = {
    model = Vec.fill(numInstances)(Color.random())
    (model zip ggColors).foreach { case (m, gg) => gg.color = m }
    categ = Vec.fill(numInstances)(false)
    ggClasses.foreach(_.selected = false)
  }

  private var model: Vec[Color] = _
  private var target: Color = _
  private var categ: Vec[Boolean] = _

  lazy val ggColors   = Vec.fill(numInstances)(new ColorBox)
  lazy val ggClasses  = Vec.tabulate(numInstances) { idx =>
    new CheckBox {
      listenTo(this)
      reactions += {
        case ButtonClicked(_) => categ = categ.updated(idx, selected)
      }
    }
  }

  lazy val grid = new GridPanel(numInstances, 2) {
    contents ++= Seq(ggColors, ggClasses).transpose.flatten
  }

  lazy val ggTarget   = new ColorBox
  lazy val ggPredict  = new CheckBox

  def predict(): Double = {
    // val kernel    = new LinearKernel
    val gamma     = 1.0 / numInstances
    val kernel    = new RBFKernel(gamma)
    val param     = new SVMParameter(kernel)
    val instances: Array[Instance] = model.zipWithIndex.map { case (c, idx) =>
      Instance(c.asFeature, if (categ(idx)) 1 else 0)
    } (breakOut)

    val problem   = new Problem(instances)
    val m         = SVM.OneClass.trainer.train(param, problem)
    instances.zipWithIndex.foreach { case (ins, idx) =>
      val ir = m.predict(ins)
      println(s"Index $idx, selected ${ins.y} prediction $ir")
    }
    val res       = m.predict(target.asFeature)
    println(res)
    res
  }

  def randomizeTarget(): Unit = {
    target = Color.random()
    ggTarget.color = target
  }

  lazy val top: Frame = new MainFrame {
    randomizeData()
    contents = new BoxPanel(Orientation.Vertical) {
      contents += grid
      contents += Button("Randomize")(randomizeData())
      contents += new GridPanel(1, 2) {
        contents += ggTarget
        contents += ggPredict
      }
      contents += Button("Predict") {
        randomizeTarget()
        val p = predict()
        ggPredict.selected = p >= 0
      }
    }
    pack().centerOnScreen()
  }
}
