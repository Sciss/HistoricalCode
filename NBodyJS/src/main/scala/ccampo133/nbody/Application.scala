package ccampo133.nbody

import ccampo133.nbody.Util._
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLCanvasElement
import org.scalajs.dom.{KeyboardEvent, MouseEvent, WheelEvent}

import scala.scalajs.js

object Application {
  // Initial conditions
  val initBodies: Set[Body] = Set(
    Body(100000.0, massToRadius(100000.0), Vec2D(  0.0, 0.0), Vec2D(0.0,  0.0)),
    Body(     0.0, massToRadius(    10.0), Vec2D( 20.0, 0.0), Vec2D(0.0, 70.0)),
    Body(     0.0, massToRadius(    75.0), Vec2D( 50.0, 0.0), Vec2D(0.0, 45.0)),
    Body(     0.0, massToRadius(    80.0), Vec2D( 75.0, 0.0), Vec2D(0.0, 37.0)),
    Body(     0.0, massToRadius(    80.0), Vec2D(120.0, 0.0), Vec2D(0.0, 29.0)),
    Body(     0.0, massToRadius(  1000.0), Vec2D(220.0, 0.0), Vec2D(0.0, 21.0))
  )

  val numTrailPts : Int     = 1000
  val dt          : Double  = 0.05
  val targetFps   : Int     = 60

  val canvas      : HTMLCanvasElement = dom.document.getElementById("canvas").asInstanceOf[HTMLCanvasElement]
  var context     : Simulation = new Simulation(dt, initBodies, canvas.width, canvas.height, numTrailPts)
  val controller  : Controller        = new Controller(canvas, context)

  // Configure canvas event listeners
  canvas.addEventListener     [MouseEvent   ]("mousedown" , controller.mouseDownListener, useCapture = false)
  canvas.addEventListener     [MouseEvent   ]("mouseup"   , controller.mouseUpListener  , useCapture = false)

  // Configure window event listeners
  dom.window.addEventListener [KeyboardEvent]("keydown"   , controller.keyDownListener  , useCapture = false)
  dom.window.addEventListener [WheelEvent   ]("wheel"     , controller.mouseWheelHandler, useCapture = false)

  def main(args: Array[String]): Unit = {
    // Start the main animation loop
    js.timers.setInterval(1000 / targetFps)(controller.run())
  }
}
