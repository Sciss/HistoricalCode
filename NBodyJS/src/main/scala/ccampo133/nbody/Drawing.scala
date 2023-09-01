package ccampo133.nbody

import ccampo133.nbody.Util._
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.raw.HTMLCanvasElement

import scala.language.implicitConversions

object Drawing {
  implicit def mkOps(e: HTMLCanvasElement): Drawing = new Drawing(e)
}
/** Contains a series of "canvas" extension functions for drawing the app. */
final class Drawing(private val e: HTMLCanvasElement) extends AnyVal {
  import e._

  private def context2d = getContext("2d").asInstanceOf[CanvasRenderingContext2D]

  /** Returns "true" if a point is within the boundaries of the canvas.
    *
    * @param x the x coordinate of the point to check
    * @param y ths y coordinate of the point to check
    */
  def isInbounds(x: Double, y: Double): Boolean =
    x >= 0 && x <= width && y >= 0 && y <= height
  
  /** Returns a point relative to the center of the canvas via a linear transformation of an (x, y) point.
    *
    * @param x0 The original point's x coordinate
    * @param y0 The original point's y coordinate
    */
  def xy(x0: Double, y0: Double): (Double, Double) =
    ((width / 2.0) + x0, (height / 2.0) - y0)
  
  /** Draws a set of circular bodies (and trails if specified) on the canvas.
    *
    * @param bodies         The set of bodies to draw
    * @param trails         True to draw trails, false otherwise (default: false)
    * @param deletedBodies  The set of deleted bodies; used only for drawing trails
    */
  def draw(bodies: Set[Body], trails: Boolean = false, deletedBodies: Set[Body] = Set.empty): Unit = {
    val ctx = context2d
  
    // Clear the entire canvas
    ctx.clearRect(0.0, 0.0, width.toDouble, height.toDouble)
  
    // Only draw bodies that are in bounds
    bodies.foreach { b =>
      val (x, y) = xy(b.pos.x, b.pos.y)
      if (isInbounds(x, y)) drawBody(b)
      if (trails && b.trail.nonEmpty) drawTrail(b.trail)
    }
  
    // Draw the trails of bodies that have been deleted (collided, too far away, etc)
    if (trails) deletedBodies.foreach { b => drawTrail(b.trail) }
  }

  /** Draws a circular body on the canvas.
    *
    * @param body The body to draw
    */
  def drawBody(body: Body): Unit = {
    val ctx = context2d

    val (x, y) = xy(body.pos.x, body.pos.y)
    val grd = ctx.createRadialGradient(x, y, 0.1, x, y, 10 * Math.log(body.rad))
    grd.addColorStop(0.0, "wheat")
    grd.addColorStop(1.0, "transparent")

    // Fill with gradient and draw the main circle
    ctx.fillStyle = grd
    ctx.fillRect(x - body.rad * 4, y - body.rad * 4, 150.0, 150.0)
    ctx.beginPath()
    ctx.arc(x, y, body.rad, 0.0, 2 * Math.PI, anticlockwise = false)
    ctx.fillStyle = massToColor(body.mass)
    ctx.fill()
  }

  /** Draws the trail of a body using its previous positions.
    *
    * @param positions The positions (points) to draw the trail along, in reverse order
    */
  def drawTrail(positions: List[Vec2D]): Unit = {
    val ctx = context2d

    ctx.beginPath()
    positions.reverseIterator.zipWithIndex.foreach { case (p, pi) =>
      val (px, py) = xy(p.x, p.y)
      if (pi > 0 && isInbounds(px, py)) ctx.lineTo(px, py) else ctx.moveTo(px, py)
    }
    ctx.strokeStyle = "white"
    ctx.lineWidth   = 1.01
    ctx.stroke()
  }

  /** Draws (multi-line) white text at a specific (x, y) position. */
  def drawText(text: String, x: Double, y: Double, font: String = "10px Arial",
               fillStyle: String = "white", textAlign: String = "left"): Unit = {
    val ctx = context2d

    ctx.font      = font
    ctx.fillStyle = fillStyle
    ctx.textAlign = textAlign

    val lineHeight = ctx.measureText("M").width * 1.2
    text.split("\n").foldLeft(y) { case (dy, it) =>
      ctx.fillText(it, x, dy)
      dy + lineHeight
    }
  }
}
