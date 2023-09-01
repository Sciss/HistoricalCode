package ccampo133.nbody

import ccampo133.nbody.Drawing._
import ccampo133.nbody.Util._
import org.scalajs.dom.raw.HTMLCanvasElement
import org.scalajs.dom.{KeyboardEvent, MouseEvent, WheelEvent}

class Controller(val canvas: HTMLCanvasElement, val context: Simulation) {

  private var tempBody  : Body    = Body.empty
  private var mouseDown : Boolean = false
  private var paused    : Boolean = false
  private var debug     : Boolean = false

  private var _trails = false

  def trails: Boolean = _trails
  def trails_=(value: Boolean): Unit = {
    _trails = value
    context.clearPositionHistory()
  }

  def run(): Unit = {
    if (!paused) {
      canvas.draw(context.bodies, trails, context.removedBodies.toSet)
      if (mouseDown && tempBody != Body.empty) canvas.drawBody(tempBody)
      if (debug) drawDebugInfo()
      context.run()
    }
  }

  def keyDownListener(event: KeyboardEvent): Unit = {
    // Do nothing if the event was already processed
    if (event.defaultPrevented) return

    event.key match {
      case "t"          => trails = !trails
      case "p"          => paused = !paused
      case "r"          => context.reset()
      case "c"          => context.clear()
      case "d"          => debug = !debug
      case "ArrowUp"    => changeMass(inc = true)
      case "ArrowDown"  => changeMass(inc = false)
    }
  }

  def mouseWheelHandler(event: WheelEvent): Unit = {
    // Do nothing if the event was already processed or the mouse wasn't pressed
    if (event.defaultPrevented || !mouseDown) return

    changeMass(event.deltaY < 0)
  }

  private def changeMass(inc: Boolean): Unit = {
    val mass: Double = if (inc) {
      // Increase mass by multiples of 10 in the range [0, 100000].
      // The max possible mass, 100001, is just for fun (black hole).
      if (tempBody.mass <= 0) 10.0 else math.min(tempBody.mass * 10, 100001.0)

      // Inverse of the above logic (decrease mass in multiples of 10)
    }
    else if (tempBody.mass >= 100001.0) 100000.0
    else if (tempBody.mass >      10.0) tempBody.mass / 10
    else 0.0

    tempBody = tempBody.copy(mass = mass, rad = massToRadius(mass))
  }

  def mouseDownListener(event: MouseEvent): Unit = {
    // Do nothing if paused, or the event was already processed
    if (paused || event.defaultPrevented) return

    // Create a temporary body that will be finalized and added to the simulation when the mouse is released.
    mouseDown   = true
    val bRect   = canvas.getBoundingClientRect()
    val mouseX  = (event.clientX - bRect.left) * (canvas.width / bRect.width) - canvas.width / 2
    val mouseY  = canvas.height / 2 - (event.clientY - bRect.top) * (canvas.height / bRect.height)
    val mass    = tempBody.mass
    val radius  = massToRadius(mass)
    tempBody    = Body(mass, radius, Vec2D(mouseX, mouseY), Vec2D.zero)
    canvas.drawBody(tempBody)
  }

  def mouseUpListener(event: MouseEvent): Unit = {
    // Do nothing if paused, or the event was already processed
    if (paused || event.defaultPrevented) return

    // Add a new body to the simulation on mouse release.
    mouseDown     = false
    val bRect     = canvas.getBoundingClientRect()
    val mouseX    = (event.clientX - bRect.left) * (canvas.width / bRect.width) - canvas.width / 2
    val mouseY    = canvas.height / 2 - (event.clientY - bRect.top) * (canvas.height / bRect.height)

    // Velocity is just the length of the click & drag vector
    val velocity  = Vec2D(mouseX, mouseY) - tempBody.pos
    tempBody      = tempBody.copy(vel = velocity)
    context.addBody(tempBody)
  }

  def drawDebugInfo(): Unit = {
    val debugInfo = """
                      |mouseDown = $mouseDown
                      |trails = $trails (${context.nPos} points)
                      |num bodies = ${context.bodies.size}
                      |tempBody = $tempBody
                    """.stripMargin
    canvas.drawText(debugInfo, 5.0, 10.0)
  }
}
