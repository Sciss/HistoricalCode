package de.sciss.citarj2017

import java.awt.geom.Rectangle2D
import java.util.{Collections, Comparator}

import prefuse.action.layout.graph.TreeLayout
import prefuse.data.{Graph, Schema}
import prefuse.data.util.TreeNodeIterator
import prefuse.visual.{NodeItem, VisualItem}


/**
  * <p>
  * TreeLayout instance computing a TreeMap layout that optimizes for low
  * aspect ratios of visualized tree nodes. TreeMaps are a form of space-filling
  * layout that represents nodes as boxes on the display, with children nodes
  * represented as boxes placed within their parent's box.
  * </p>
  * <p>
  * This particular algorithm is taken from Bruls, D.M., C. Huizing, and 
  * J.J. van Wijk, "Squarified Treemaps" In <i>Data Visualization 2000, 
  * Proceedings of the Joint Eurographics and IEEE TCVG Sumposium on 
  * Visualization</i>, 2000, pp. 33-42. Available online at:
  * <a href="http://www.win.tue.nl/~vanwijk/stm.pdf">
  * http://www.win.tue.nl/~vanwijk/stm.pdf</a>.
  * </p>
  * <p>
  * For more information on TreeMaps in general, see 
  * <a href="http://www.cs.umd.edu/hcil/treemap-history/">
  * http://www.cs.umd.edu/hcil/treemap-history/</a>.
  * </p>
  *
  * @author <a href="http://jheer.org">jeffrey heer</a>
  * @author Hanns Holger Rutz
  */
object MyTreeMapLayout  { // column value in which layout stores area information
  val AREA = "_area"
  val AREA_SCHEMA = new Schema
  private val s_cmp = new Comparator[VisualItem]() {
    override def compare(o1: VisualItem, o2: VisualItem): Int = {
      val s1 = o1.getDouble(AREA)
      val s2 = o2.getDouble(AREA)
      if (s1 > s2) 1
      else if (s1 < s2) -1 else 0
    }
  }

  AREA_SCHEMA.addColumn(AREA, classOf[Double])

}

/**
  * Creates a new MyTreeMapLayout with the specified spacing between
  * parent areas and their enclosed children.
  */
class MyTreeMapLayout (val group: String, frameL: Double, frameT: Double, frameB: Double, frameR: Double)
  extends TreeLayout(group) {

  private type Items = java.util.List[VisualItem]

  private[this] val m_kids: Items = new java.util.ArrayList[VisualItem]
  private[this] val m_row : Items = new java.util.ArrayList[VisualItem]
  private[this] val m_r     = new Rectangle2D.Double

  private[this] var m_frameL  = 0.0 // space between parents border and children
  private[this] var m_frameT  = 0.0 // space between parents border and children
  private[this] var m_frameB  = 0.0 // space between parents border and children
  private[this] var m_frameR  = 0.0 // space between parents border and children
  private[this] var noFrame   = true

  setFrameWidth(frameL, frameT, frameB, frameR)

  /**
    * Creates a new MyTreeMapLayout with no spacing between
    * parent areas and their enclosed children.
    *
    * @param group the data group to layout. Must resolve to a Graph instance.
    */
  def this(group: String) {
    this(group, 0.0, 0.0, 0.0, 0.0)
  }

  /**
    * Sets the amount of desired framing space between parent rectangles and
    * their enclosed children. Use a value of 0 to remove frames altogether.
    * If you adjust the frame value, you must re-run the layout to see the
    * change reflected. Negative frame values are not allowed and will result
    * in an IllegalArgumentException.
    *
    * @param frameL the left frame width, 0 for no left frame
    * @param frameT the left frame width, 0 for no left frame
    * @param frameB the left frame width, 0 for no left frame
    * @param frameR the left frame width, 0 for no left frame
    */
  def setFrameWidth(frameL: Double, frameT: Double, frameB: Double, frameR: Double): Unit = {
    if (frameL < 0) throw new IllegalArgumentException("frameL value must be greater than or equal to 0.")
    if (frameT < 0) throw new IllegalArgumentException("frameT value must be greater than or equal to 0.")
    if (frameB < 0) throw new IllegalArgumentException("frameB value must be greater than or equal to 0.")
    if (frameR < 0) throw new IllegalArgumentException("frameR value must be greater than or equal to 0.")
    m_frameL  = frameL
    m_frameT  = frameT
    m_frameB  = frameB
    m_frameR  = frameR
    noFrame   = frameL == 0.0 && frameT == 0.0 && frameB == 0.0 && frameR == 0.0
  }

//  /**
//    * Gets the amount of desired framing space, in pixels, between
//    * parent rectangles and their enclosed children.
//    *
//    * @return the frame width
//    */
//  def getFrameWidth: Double = m_frame

  /**
    * @see prefuse.action.Action#run(double)
    */
  override def run(frac: Double): Unit = { // setup
    val root = getLayoutRoot
    val b = getLayoutBounds
    m_r.setRect(b.getX, b.getY, b.getWidth - 1, b.getHeight - 1)
    // process size values
    computeAreas(root)
    // layout root node
    setX(root, null, 0)
    setY(root, null, 0)
    root.setBounds(0, 0, m_r.getWidth, m_r.getHeight)
    // layout the tree
    updateArea(root, m_r)
    layout(root, m_r)
  }

  /**
    * Compute the pixel areas of nodes based on their size values.
    */
  private def computeAreas(root: NodeItem): Unit = {
    var leafCount = 0
    // ensure area data column exists
    val g = m_vis.getGroup(m_group).asInstanceOf[Graph]
    val nodes = g.getNodes
    nodes.addColumns(MyTreeMapLayout.AREA_SCHEMA)
    // reset all sizes to zero
    var iter = new TreeNodeIterator(root)
    while (iter.hasNext) {
      val n = iter.next().asInstanceOf[NodeItem]
      n.setDouble(MyTreeMapLayout.AREA, 0)
    }
    // set raw sizes, compute leaf count
    iter = new TreeNodeIterator(root, false)
    while (iter.hasNext) {
      val n = iter.next().asInstanceOf[NodeItem]
      var area = 0.0
      if (n.getChildCount == 0) {
        area = n.getSize
        leafCount += 1
      }
      else if (n.isExpanded) {
        var c = n.getFirstChild.asInstanceOf[NodeItem]

        while (c != null) {
          area += c.getDouble(MyTreeMapLayout.AREA)
          leafCount += 1

          c = c.getNextSibling.asInstanceOf[NodeItem]
        }
      }
      n.setDouble(MyTreeMapLayout.AREA, area)
    }
    // scale sizes by display area factor
    val b = getLayoutBounds
    val area = (b.getWidth - 1) * (b.getHeight - 1)
    val scale = area / root.getDouble(MyTreeMapLayout.AREA)
    iter = new TreeNodeIterator(root)
    while (iter.hasNext) {
      val n = iter.next().asInstanceOf[NodeItem]
      n.setDouble(MyTreeMapLayout.AREA, n.getDouble(MyTreeMapLayout.AREA) * scale)
    }
  }

  /**
    * Compute the tree map layout.
    */
  private def layout(p: NodeItem, r: Rectangle2D): Unit = { // create sorted list of children
    var childIter = p.children
    while (childIter.hasNext) m_kids.add(childIter.next().asInstanceOf[VisualItem])
    Collections.sort(m_kids, MyTreeMapLayout.s_cmp)
    // do squarified layout of siblings
    val w = Math.min(r.getWidth, r.getHeight)
    squarify(m_kids, m_row, w, r)
    m_kids.clear() // clear m_kids

    // recurse
    childIter = p.children
    while (childIter.hasNext) {
      val c = childIter.next().asInstanceOf[NodeItem]
      if (c.getChildCount > 0 && c.getDouble(MyTreeMapLayout.AREA) > 0) {
        updateArea(c, r)
        layout(c, r)
      }
    }
  }

  private def updateArea(n: NodeItem, r: Rectangle2D): Unit = {
    val b = n.getBounds
    if (noFrame) { // if no framing, simply update bounding rectangle
      r.setRect(b)
      return
    }
    // compute area loss due to frame
//    val dA = 2 * m_frame * (b.getWidth + b.getHeight - 2 * m_frame)
    val dA: Double = {
      val w  = b.getWidth
      val h  = b.getHeight
      val tb = m_frameT + m_frameB
      val lr = m_frameL + m_frameR
      val hi = h  - tb
      val d0 = w  * tb
      val d1 = hi * lr
      d0 + d1
    }
    val A = n.getDouble(MyTreeMapLayout.AREA) - dA
    // compute renormalization factor
    var s = 0.0
    var childIter = n.children
    while (childIter.hasNext) {
      s += childIter.next().asInstanceOf[NodeItem].getDouble(MyTreeMapLayout.AREA)
    }

    val t = A / s
    // re-normalize children areas
    childIter = n.children
    while (childIter.hasNext) {
      val c = childIter.next().asInstanceOf[NodeItem]
      c.setDouble(MyTreeMapLayout.AREA, c.getDouble(MyTreeMapLayout.AREA) * t)
    }
    // set bounding rectangle and return
    r.setRect(b.getX + m_frameL, b.getY + m_frameT, b.getWidth - (m_frameL + m_frameR), b.getHeight - (m_frameT + m_frameB))
  }

  private def squarify(c: Items, row: Items, w0: Double, r0: Rectangle2D): Unit = {
    var worstVal = Double.MaxValue
    var nworst = 0.0
    var len = 0
    var w = w0
    var r = r0
    while ({
      len = c.size
      len > 0
    }) { // add item to the row list, ignore if negative area
      val item = c.get(len - 1)
      val a = item.getDouble(MyTreeMapLayout.AREA)
      if (a <= 0.0) {
        c.remove(len - 1)
      } else {
        row.add(item)
        nworst = worst(row, w)
        if (nworst <= worstVal) {
          c.remove(len - 1)
          worstVal = nworst
        }
        else {
          row.remove(row.size - 1) // remove the latest addition

          r = layoutRow(row, w, r) // layout the current row

          w = Math.min(r.getWidth, r.getHeight) // recompute w

          row.clear() // clear the row

          worstVal = Double.MaxValue
        }
      }
    }
    if (row.size > 0) {
      r = layoutRow(row, w, r)
      row.clear()
    }
  }

  private def worst(rlist: Items, w0: Double) = {
    var rmax = Double.MinValue
    var rmin = Double.MaxValue
    var s = 0.0
    var w = w0
    val iter = rlist.iterator
    while (iter.hasNext) {
      val r = iter.next().getDouble(MyTreeMapLayout.AREA)
      rmin = Math.min(rmin, r)
      rmax = Math.max(rmax, r)
      s += r
    }
    s = s * s
    w = w * w
    Math.max(w * rmax / s, s / (w * rmin))
  }

  private def layoutRow(row: Items, w: Double, r: Rectangle2D) = {
    var s = 0.0
    // sum of row areas
    var rowIter = row.iterator
    while (rowIter.hasNext) {
      s += rowIter.next().getDouble(MyTreeMapLayout.AREA)
    }

    val x = r.getX
    val y = r.getY
    var d = 0.0
    val h = if (w == 0) 0
    else s / w
    val horiz = w == r.getWidth
    // set node positions and dimensions
    rowIter = row.iterator
    while (rowIter.hasNext) {
      val n = rowIter.next().asInstanceOf[NodeItem]
      val p = n.getParent.asInstanceOf[NodeItem]
      if (horiz) {
        setX(n, p, x + d)
        setY(n, p, y)
      }
      else {
        setX(n, p, x)
        setY(n, p, y + d)
      }
      val nw = n.getDouble(MyTreeMapLayout.AREA) / h
      if (horiz) {
        setNodeDimensions(n, nw, h)
        d += nw
      }
      else {
        setNodeDimensions(n, h, nw)
        d += nw
      }
    }
    // update space available in rectangle r
    if (horiz) r.setRect(x, y + h, r.getWidth, r.getHeight - h)
    else r.setRect(x + h, y, r.getWidth - h, r.getHeight)
    r
  }

  private def setNodeDimensions(n: NodeItem, w: Double, h: Double): Unit = {
    n.setBounds(n.getX, n.getY, w, h)
  }

  // end of class MyTreeMapLayout
}
