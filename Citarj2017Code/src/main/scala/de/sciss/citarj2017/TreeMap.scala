package de.sciss.citarj2017

import java.awt.event.{ComponentAdapter, ComponentEvent, MouseEvent}
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.awt.{BorderLayout, Color, Font, Graphics2D, RenderingHints, Shape}
import java.io.{File, FileOutputStream}
import javax.imageio.ImageIO
import javax.swing.{BorderFactory, JComponent, JFrame, JPanel, SwingConstants, Timer}

import com.itextpdf.awt.PdfGraphics2D
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.{Document => IDocument, Rectangle => IRectangle}
import de.sciss.file._
import prefuse.action.animate.ColorAnimator
import prefuse.action.assignment.ColorAction
import prefuse.action.layout.Layout
import prefuse.action.{Action, ActionList, RepaintAction}
import prefuse.controls.ControlAdapter
import prefuse.data.Tree
import prefuse.data.expression.{BooleanLiteral, Predicate}
import prefuse.data.io.TreeMLReader
import prefuse.render.{AbstractShapeRenderer, DefaultRendererFactory}
import prefuse.util.ui.{JFastLabel, UILib}
import prefuse.util.{ColorLib, ColorMap, FontLib, PrefuseLib}
import prefuse.visual.expression.InGroupPredicate
import prefuse.visual.sort.TreeDepthItemSorter
import prefuse.visual.{DecoratorItem, NodeItem, VisualItem}
import prefuse.{Display, Visualization}

import scala.concurrent.{Future, Promise}
import scala.swing.{Dimension, Swing}
import scala.util.Try


/**
  * Demonstration showcasing a TreeMap layout of a hierarchical data
  * set and the use of dynamic query binding for text search. Animation
  * is used to highlight changing search results.
  *
  * @author <a href="http://jheer.org">jeffrey heer</a>
  * @author Hanns Holger Rutz
  */
object TreeMap {
  val TREE_CHI = "/chi-ontology.xml.gz"
  // create data description of labels, setting colors, fonts ahead of time
  private val LABEL_SCHEMA = {
    val res = PrefuseLib.getVisualItemSchema
    res.setDefault(VisualItem.INTERACTIVE, false)
    res.setDefault(VisualItem.TEXTCOLOR, ColorLib.gray(200))
    res.setDefault(VisualItem.FONT, FontLib.getFont("Tahoma", 12))
    res
  }

  final val COL_NAME  = "name"
  final val COL_TPE   = "type"

  private val tree      = "tree"
  private val treeNodes = "tree.nodes"
  private val treeEdges = "tree.edges"
  private val labels    = "labels"

  def main(argv: Array[String]): Unit = {
    UILib.setPlatformLookAndFeel()
    var infile = TREE_CHI
    var label = COL_NAME
    if (argv.length > 1) {
      infile  = argv(0)
      label   = argv(1)
    }
    val treeMap = demo(infile, label)
    val frame   = new JFrame("p r e f u s e  |  t r e e m a p")
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.setContentPane(treeMap)
    frame.pack()
    frame.setVisible(true)
  }

  def demo: JComponent = demo(TREE_CHI, COL_NAME)

  def demo(datafile: String, label: String): JComponent = {
    val t = try
      new TreeMLReader().readGraph(datafile).asInstanceOf[Tree]
    catch {
      case e: Exception =>
        e.printStackTrace()
        sys.exit()
    }
    // create a new treemap
    val treeMap = new TreeMap(t, label)
    // create a search panel for the tree map

//    val search = treeMap.getSearchQuery.createSearchPanel
//    search.setShowResultCount(true)
//    search.setBorder(BorderFactory.createEmptyBorder(5, 5, 4, 0))
//    search.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 11))

    val title = new JFastLabel("                 ")
    title.setPreferredSize(new Dimension(350, 20))
    title.setVerticalAlignment(SwingConstants.BOTTOM)
    title.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0))
    title.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 16))
    treeMap.addControlListener(new ControlAdapter() {
      override def itemEntered(item: VisualItem, e: MouseEvent): Unit =
        title.setText(item.getString(label))

      override def itemExited(item: VisualItem, e: MouseEvent): Unit =
        title.setText(null)
    })
//    val box = UILib.getBox(Array[Component](title, search), true, 10, 3, 0)
    val panel = new JPanel(new BorderLayout)
    panel.add(treeMap, BorderLayout.CENTER)
//    panel.add(box, BorderLayout.SOUTH)
    panel.add(title, BorderLayout.SOUTH)
    UILib.setColor(panel, Color.BLACK, Color.GRAY)
    panel
  }

  /**
    * Set the stroke color for drawing tree-map node outlines. A graded
    * gray scale ramp is used, with higher nodes in the tree drawn in
    * lighter shades of gray.
    */
  class BorderColorAction(val group: String) extends ColorAction(group, VisualItem.STROKECOLOR) {
    override def getColor(item: VisualItem): Int = {
      val nItem = item.asInstanceOf[NodeItem]
      if (nItem.isHover) return ColorLib.rgb(99, 130, 191)
      val depth = nItem.getDepth
      if (depth < 2) ColorLib.gray(100)
      else if (depth < 4) ColorLib.gray(75)
      else ColorLib.gray(50)
    }
  }

  /**
    * Set fill colors for tree-map nodes. Search items are colored
    * in pink, while normal nodes are shaded according to their
    * depth in the tree.
    */
  class FillColorAction(val group: String) extends ColorAction(group, VisualItem.FILLCOLOR) {
    private val cMap = new ColorMap(ColorLib.getInterpolatedPalette(10, ColorLib.rgb(85, 85, 85), ColorLib.rgb(0, 0, 0)), 0, 9)

    override def getColor(item: VisualItem): Int = item match {
      case nItem: NodeItem =>
        if (nItem.getChildCount > 0) 0 // no fill for parent nodes
        else if (m_vis.isInGroup(item, Visualization.SEARCH_ITEMS)) ColorLib.rgb(191, 99, 130)
        else cMap.getColor(nItem.getDepth)
      case _ => cMap.getColor(0)
    }

    // end of inner class TreeMapColorAction
  }

  /**
    * Set label positions. Labels are assumed to be DecoratorItem instances,
    * decorating their respective nodes. The layout simply gets the bounds
    * of the decorated node and assigns the label coordinates to the center
    * of those bounds.
    */
  class LabelLayout(val group: String) extends Layout(group) {
    override def run(frac: Double): Unit = {
      val iter = m_vis.items(m_group)
      while (iter.hasNext) {
        val item = iter.next.asInstanceOf[DecoratorItem]
        val node = item.getDecoratedItem
        val bounds = node.getBounds
        setX(item, null, bounds.getCenterX)
        setY(item, null, bounds.getCenterY)
      }
    }

    // end of inner class LabelLayout
  }

  private def mkRGB(tup: (Double, Double, Double)): Color =
    new Color(tup._1.toFloat, tup._2.toFloat, tup._3.toFloat)

  private def mkRGBI(tup: (Int, Int, Int)): Color =
    new Color(tup._1, tup._2, tup._3)

  final val GMT_wysiwyg: Array[Color] = Array(
    (0.250980, 0.000000, 0.250980),
    (0.250980, 0.000000, 0.752941),
    (0.000000, 0.250980, 1.000000),
    (0.000000, 0.501961, 1.000000),
    (0.000000, 0.627451, 1.000000),
    (0.250980, 0.752941, 1.000000),
    (0.250980, 0.878431, 1.000000),
    (0.250980, 1.000000, 1.000000),
    (0.250980, 1.000000, 0.752941),
    (0.250980, 1.000000, 0.250980),
    (0.501961, 1.000000, 0.250980),
    (0.752941, 1.000000, 0.250980),
    (1.000000, 1.000000, 0.250980),
    (1.000000, 0.878431, 0.250980),
    (1.000000, 0.627451, 0.250980),
    (1.000000, 0.376471, 0.250980),
    (1.000000, 0.125490, 0.250980),
    (1.000000, 0.376471, 0.752941),
    (1.000000, 0.627451, 1.000000),
    (1.000000, 0.878431, 1.000000),
  ).map(mkRGB)

  // 26 colours
  val GreenArmytage: Array[Color] = Array(
    (240,163,255), (  0,117,220), (153, 63,  0), ( 76,  0, 92),
    ( 25, 25, 25), (  0, 92, 49), ( 43,206, 72), (255,204,153),
    (128,128,128), (148,255,181), (143,124,  0), (157,204,  0),
    (194,  0,136), (  0, 51,128), (255,164,  5), (255,168,187),
    ( 66,102,  0), (255,  0, 16), ( 94,241,242), (  0,153,143),
    (224,255,102), (116, 10,255), (153,  0,  0), (255,255,128),
    (255,255,  0), (255, 80,  5)
  ).map(mkRGBI)

  /**
    * A renderer for tree-map nodes. Draws simple rectangles, but defers
    * the bounds management to the layout.
    */
  final class NodeRenderer(numTypes: Int) extends AbstractShapeRenderer {
    private[this] val defaultPalette = GreenArmytage // GMT_wysiwyg

    private[this] val bgColrTable = if (numTypes <= defaultPalette.length) defaultPalette else Array.tabulate(numTypes) { i =>
      import de.sciss.numbers.Implicits._
      val hue = i.linlin(0, numTypes, 0.0f, 1.0f)
      Color.getHSBColor(hue, 1.0f, 1.0f)
    }

    private[this] val fbColrTable = bgColrTable.map { colr =>
      val Y = colr.getRed   / 255.0 * 0.2126 +
              colr.getGreen / 255.0 * 0.7152 +
              colr.getBlue  / 255.0 * 0.0722
      if (Y < 0.5) Color.white else Color.black
    }

    m_manageBounds = false
    private val m_bounds = new Rectangle2D.Double

    override protected def getRawShape(item: VisualItem): Shape = {
      m_bounds.setRect(item.getBounds)
      m_bounds
    }

//    private[this] val ln    = new Line2D.Double
//    private[this] val strk2 = new BasicStroke(2f)

    private[this] val font = new Font("Liberation Sans Narrow", Font.PLAIN, 12)

    override def render(g: Graphics2D, item: VisualItem): Unit = {
      val shape = getShape(item)
      if (shape != null) {
        // drawShape(g, item, shape)
//        g.setColor(Color.red)
//        g.setStroke(strk2)
        val r = shape.getBounds2D
        val ni = item.asInstanceOf[NodeItem]
        // println(ni.getString(COL_NAME))
//        ni.getDepth
        val isLeaf  = ni.getChildCount == 0
        val minX    = r.getMinX
        val minY    = r.getMinY
        val maxY    = if (isLeaf) r.getMaxY else minY + 16.0
//        ln.setLine(r.getMinX, r.getMinY, r. getMaxX, maxY)
//        g.draw(ln)
//        ln.setLine(r.getMaxX, r.getMinY, r. getMinX, maxY)
//        g.draw(ln)
        val colrIdx = ni.getInt(COL_TPE)
        val colrOk  = colrIdx >= 0 && colrIdx < bgColrTable.length
        if (colrOk) g.setColor(bgColrTable(colrIdx))
        r.setFrame(minX, minY, r.getWidth, maxY - minY)
        g.fill(r)
        if (colrOk) g.setColor(fbColrTable(colrIdx))
        g.setFont(font)
        val name = ni.getString(COL_NAME)
//        val atOrig = g.getTransform
//        g.translate(minX + 2, minY + 10)
        val clpOrig = g.getClip
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.clip(r)
        g.drawString(name, (minX + 2).toFloat, (minY + 10).toFloat)
        g.setClip(clpOrig)
//        g.setTransform(atOrig)
      }
    }

    // end of inner class NodeRenderer
  }
}

class TreeMap(val t: Tree, val label: String, numTypes: Int = 19)
  extends Display(new Visualization) { // add the tree to the visualization

  /* private val vt: VisualTree = */ m_vis.addTree(TreeMap.tree, t)

//  private lazy val searchQ = new SearchQueryBinding(vt.getNodeTable, label)

  private[this] val resizeTimer = new Timer(1000, Swing.ActionListener(_ => mkLayout()))

  private[this] var tl: MyTreeMapLayout = _

  // def treeLayout: MyTreeMapLayout = tl

  def rootBounds: Rectangle2D = tl.getBounds

  def runLayout(): Unit = tl.run(0.0)

  private[this] var count = 0

  def runWith[A](body: => A): Future[A] = {
    val c = count + 1
    count = c
    val name = s"action-$c"
    val p = Promise[A]()
    m_vis.putAction(name, new Action {
      def run(frac: Double): Unit = {
        m_vis.removeAction(name)
        p.tryComplete(Try(body))
      }
    })
    m_vis.runAfter("layout", name)
    p.future
  }

  {
    m_vis.setVisible(TreeMap.treeEdges, null, false)
//    // ensure that only leaf nodes are interactive
//    val noLeaf: Predicate = ExpressionParser.parse("childcount()>0").asInstanceOf[Predicate]
//    m_vis.setInteractive(TreeMap.treeNodes, noLeaf, false)
    // add labels to the visualization
    // first create a filter to show labels only at top-level nodes
//    val labelP: Predicate = ExpressionParser.parse("treedepth()=1").asInstanceOf[Predicate]
    val labelP: Predicate = new BooleanLiteral(true)
    // now create the labels as decorators of the nodes
    m_vis.addDecorators(TreeMap.labels, TreeMap.treeNodes, labelP, TreeMap.LABEL_SCHEMA)
    // set up the renderers - one for nodes and one for labels
    val rf = new DefaultRendererFactory
    rf.add(new InGroupPredicate(TreeMap.treeNodes), new TreeMap.NodeRenderer(numTypes))
//    rf.add(new InGroupPredicate(TreeMap.labels)   , new LabelRenderer(label))
    m_vis.setRendererFactory(rf)
    // border colors
    val borderColor = new TreeMap.BorderColorAction (TreeMap.treeNodes)
    val fillColor   = new TreeMap.FillColorAction   (TreeMap.treeNodes)
    // color settings
    val colors = new ActionList
    colors.add(fillColor)
    colors.add(borderColor)
    m_vis.putAction("colors", colors)
    // animate paint change
    val animatePaint = new ActionList(400)
    animatePaint.add(new ColorAnimator(TreeMap.treeNodes))
    animatePaint.add(new RepaintAction)
    m_vis.putAction("animatePaint", animatePaint)
    // create the single filtering and layout action list
    val actionLayout = new ActionList
    val pad = 1.0 // 1.0
    tl = new MyTreeMapLayout(TreeMap.tree, frameL = pad, frameT = 16.0, frameR = pad, frameB = pad)
    actionLayout.add(tl)
//    layout.add(new BalloonTreeLayout(TreeMap.tree))
    actionLayout.add(new TreeMap.LabelLayout(TreeMap.labels))
    actionLayout.add(colors)
    actionLayout.add(new RepaintAction)
    m_vis.putAction("layout", actionLayout)
    // initialize our display
    setSize(700, 600)
    setItemSorter(new TreeDepthItemSorter)

//    addControlListener(new ControlAdapter() {
//      override def itemEntered(item: VisualItem, e: MouseEvent): Unit = {
//        item.setStrokeColor(borderColor.getColor(item))
//        item.getVisualization.repaint()
//      }
//
//      override def itemExited(item: VisualItem, e: MouseEvent): Unit = {
//        item.setStrokeColor(item.getEndStrokeColor)
//        item.getVisualization.repaint()
//      }
//    })

//    m_vis.addFocusGroup(Visualization.SEARCH_ITEMS, searchQ.getSearchSet)
//    searchQ.getPredicate.addExpressionListener(new UpdateListener() {
//      override def update(src: Any): Unit = {
//        m_vis.cancel("animatePaint")
//        m_vis.run("colors")
//        m_vis.run("animatePaint")
//      }
//    })
    // perform layout
    mkLayout()

    addComponentListener(new ComponentAdapter {
      override def componentResized(e: ComponentEvent): Unit = resizeTimer.restart()
    })
  }

  def mkLayout(): Unit = m_vis.run("layout")

//  def getSearchQuery: SearchQueryBinding = searchQ

//  private var _bufWidth  = 0
//  private var _bufHeight = 0
//
//  def bufWidth : Int = if (_bufWidth  > 0) _bufWidth  else getWidth
//  def bufHeight: Int = if (_bufHeight > 0) _bufHeight else getHeight
//
//  def bufWidth_=(value: Int): Unit = if (_bufWidth != value) {
//    _bufWidth   = value
//    m_offscreen = null
//  }
//
//  def bufHeight_=(value: Int): Unit = if (_bufHeight != value) {
//    _bufHeight  = value
//    m_offscreen = null
//  }
//
////  def clearBuf(): Unit = m_offscreen = null
//
//  override def paintComponent(g: Graphics): Unit = {
//    if (m_offscreen == null) {
//      m_offscreen = getNewOffscreenBuffer(bufWidth, bufHeight)
//      damageReport()
//    }
//    val g2D     = g.asInstanceOf[Graphics2D]
//    val buf_g2D = m_offscreen.getGraphics.asInstanceOf[Graphics2D]
//    val sx      = bufWidth .toDouble / getWidth
//    val sy      = bufHeight.toDouble / getHeight
//    if (sx != 1.0 || sy != 1.0) buf_g2D.scale(sx, sy)
//    paintDisplay(buf_g2D, getSize)
//    paintBufferToScreen(g2D)
//    firePostPaint(g2D)
//    buf_g2D.dispose()
//    nframes += 1
//
////    if (mark < 0) {
////      mark      = System.currentTimeMillis()
////      nframes   = 0
////    }
////    else if (nframes == sampleInterval) {
////      val t     = System.currentTimeMillis()
////      frameRate = (1000.0 * nframes) / (t - mark)
////      mark      = t
////      nframes   = 0
////    }
//  }

  def saveFrameAsPDF(file: File, width: Int, height: Int, dpi: Double): Unit = {
    val scale     = 72.0 / dpi
    val widthU    = width  * scale // 'user units'
    val heightH   = height * scale // 'user units'
    val pageSize  = new IRectangle(0, 0, widthU.toFloat, heightH.toFloat)
    val margin    = 0
    val doc       = new IDocument(pageSize, margin, margin, margin, margin)
    val stream    = new FileOutputStream(file)
    val writer    = PdfWriter.getInstance(doc, stream)

    doc.open()
    try {
      val cb = writer.getDirectContent
      val tp = cb.createTemplate(width, height)
      // use `onlyShapes = true` until we deal with the font-mapper!
      val g2 = new PdfGraphics2D(tp, width, height, true /*, fontMapper */)
      g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
      try {
        /* _dsp. */ damageReport() // force complete redrawing
        /* _dsp. */ paintDisplay(g2, new Dimension(width, height))
        // view.render(g2)
      } finally {
        g2.dispose()
      }
      // tp.setHorizontalScaling((scale * 100).toFloat)
      cb.addTemplate(tp, margin, margin)
    } finally {
      doc.close()
    }
  }

  def saveFrameAsBitmap(file: File, width: Int, height: Int): Unit = {
    val format    = if (file.extL == "jpg") "jpg" else "png"

//    val scaleH    = width  / getWidth .toDouble
//    val scaleV    = height / getHeight.toDouble
//    val scale     = math.min(scaleH, scaleV) //  72.0 / dpi
//    val widthU    = width  * scale // 'user units'
//    val heightH   = height * scale // 'user units'

    val img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val g2 = img.createGraphics()
    g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
    try {
      /* _dsp. */ damageReport() // force complete redrawing
//      m_transform.setToScale(scale, scale)
//      g2.scale(scale, scale)
      /* _dsp. */ paintDisplay(g2, new Dimension(width, height))
      // view.render(g2)
    } finally {
      g2.dispose()
    }
    ImageIO.write(img, format, file)
  }
}
