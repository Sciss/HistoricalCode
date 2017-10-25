package de.sciss.citarj2017

import java.awt.event.MouseEvent
import java.awt.geom.Rectangle2D
import java.awt.{BorderLayout, Color, Dimension, Font, Shape}
import javax.swing.{BorderFactory, JComponent, JFrame, JPanel, SwingConstants}

import prefuse.action.animate.ColorAnimator
import prefuse.action.assignment.ColorAction
import prefuse.action.layout.Layout
import prefuse.action.{ActionList, RepaintAction}
import prefuse.controls.ControlAdapter
import prefuse.data.Tree
import prefuse.data.expression.Predicate
import prefuse.data.expression.parser.ExpressionParser
import prefuse.data.io.TreeMLReader
import prefuse.render.{AbstractShapeRenderer, DefaultRendererFactory, LabelRenderer}
import prefuse.util.ui.{JFastLabel, UILib}
import prefuse.util.{ColorLib, ColorMap, FontLib, PrefuseLib}
import prefuse.visual.expression.InGroupPredicate
import prefuse.visual.sort.TreeDepthItemSorter
import prefuse.visual.{DecoratorItem, NodeItem, VisualItem}
import prefuse.{Display, Visualization}


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
    res.setDefault(VisualItem.FONT, FontLib.getFont("Tahoma", 16))
    res
  }

  private val tree      = "tree"
  private val treeNodes = "tree.nodes"
  private val treeEdges = "tree.edges"
  private val labels    = "labels"

  def main(argv: Array[String]): Unit = {
    UILib.setPlatformLookAndFeel()
    var infile = TREE_CHI
    var label = "name"
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

  def demo: JComponent = demo(TREE_CHI, "name")

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

  /**
    * A renderer for treemap nodes. Draws simple rectangles, but defers
    * the bounds management to the layout.
    */
  class NodeRenderer() extends AbstractShapeRenderer {
    m_manageBounds = false
    private val m_bounds = new Rectangle2D.Double

    override protected def getRawShape(item: VisualItem): Shape = {
      m_bounds.setRect(item.getBounds)
      m_bounds
    }

    // end of inner class NodeRenderer
  }
}

class TreeMap(val t: Tree, val label: String) extends Display(new Visualization) { // add the tree to the visualization

  /* private val vt: VisualTree = */ m_vis.addTree(TreeMap.tree, t)

//  private lazy val searchQ = new SearchQueryBinding(vt.getNodeTable, label)

  {
    m_vis.setVisible(TreeMap.treeEdges, null, false)
    // ensure that only leaf nodes are interactive
    val noLeaf: Predicate = ExpressionParser.parse("childcount()>0").asInstanceOf[Predicate]
    m_vis.setInteractive(TreeMap.treeNodes, noLeaf, false)
    // add labels to the visualization
    // first create a filter to show labels only at top-level nodes
    val labelP: Predicate = ExpressionParser.parse("treedepth()=1").asInstanceOf[Predicate]
    // now create the labels as decorators of the nodes
    m_vis.addDecorators(TreeMap.labels, TreeMap.treeNodes, labelP, TreeMap.LABEL_SCHEMA)
    // set up the renderers - one for nodes and one for labels
    val rf = new DefaultRendererFactory
    rf.add(new InGroupPredicate(TreeMap.treeNodes), new TreeMap.NodeRenderer)
    rf.add(new InGroupPredicate(TreeMap.labels), new LabelRenderer(label))
    m_vis.setRendererFactory(rf)
    // border colors
    val borderColor = new TreeMap.BorderColorAction(TreeMap.treeNodes)
    val fillColor = new TreeMap.FillColorAction(TreeMap.treeNodes)
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
    val layout = new ActionList
    val pad = 0.0 // 1.0
    layout.add(new MyTreeMapLayout(TreeMap.tree, frameL = pad, frameT = 12.0, frameR = pad, frameB = pad))
//    layout.add(new BalloonTreeLayout(TreeMap.tree))
    layout.add(new TreeMap.LabelLayout(TreeMap.labels))
    layout.add(colors)
    layout.add(new RepaintAction)
    m_vis.putAction("layout", layout)
    // initialize our display
    setSize(700, 600)
    setItemSorter(new TreeDepthItemSorter)
    addControlListener(new ControlAdapter() {
      override def itemEntered(item: VisualItem, e: MouseEvent): Unit = {
        item.setStrokeColor(borderColor.getColor(item))
        item.getVisualization.repaint()
      }

      override

      def itemExited(item: VisualItem, e: MouseEvent): Unit = {
        item.setStrokeColor(item.getEndStrokeColor)
        item.getVisualization.repaint()
      }
    })

//    m_vis.addFocusGroup(Visualization.SEARCH_ITEMS, searchQ.getSearchSet)
//    searchQ.getPredicate.addExpressionListener(new UpdateListener() {
//      override def update(src: Any): Unit = {
//        m_vis.cancel("animatePaint")
//        m_vis.run("colors")
//        m_vis.run("animatePaint")
//      }
//    })
    // perform layout
    m_vis.run("layout")
  }

//  def getSearchQuery: SearchQueryBinding = searchQ
}
