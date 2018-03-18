package ccampo133.nbody

import ccampo133.nbody.Physics._
import ccampo133.nbody.Util._

/** Runs an n-body gravity simulation using Verlet integration.
  *
  * @param dt         The timestep
  * @param initBodies The set of initial bodies
  * @param width      The simulation area width
  * @param height     The simulation area height
  * @param nPos       The number of historical positions to track, per body
  * @param nOld       The number of old bodies to track after they've been removed (collided, escaped, etc)
  */
class Simulation(val dt        : Double,
                 val initBodies: Set[Body] = Set.empty,
                 val width     : Int       = 100,
                 val height    : Int       = 100,
                 val nPos      : Int       = 0,
                 val nOld      : Int       = 0) {

  private var _bodies         = initBodies
  private var _removedBodies  = List.empty[Body]

  def bodies        : Set [Body] = _bodies
  def removedBodies : List[Body] = _removedBodies

  /** Moves the simulation forward one time step.
    */
  def run(): Unit = {
    // Remove bodies that collide or are WAY out of bounds (to preserve resources)
    val bodiesToRemove = _bodies.filter { b =>
      isWayOutOfBounds(b.pos.x, b.pos.y, width, height) || (_bodies - b).exists(b.isCollision)
    }

    _bodies --= bodiesToRemove

    // Keep only the last "nOld" removed bodies
    _removedBodies = (_removedBodies ++ bodiesToRemove).takeRight(nOld)

    // Update the positions of all bodies using the "Velocity Verlet" algorithm
    // and exclude the current body from the acceleration calculation.
    _bodies = _bodies.map { b =>
      val (x, v) = verlet(b.pos, b.vel, dt, { pos => gravityAcceleration(pos, _bodies - b) })
      Body(b.mass, b.rad, x, v, (x :: b.trail).take(nPos))
    }
  }

  def clear(): Unit = {
    _removedBodies = Nil
    _bodies = Set.empty
  }

  def reset(): Unit = {
    _removedBodies = Nil
    _bodies = initBodies
  }

  def clearPositionHistory(): Unit =
    _bodies = _bodies.map(_.copy(trail = Nil))

  def addBody(body: Body): Unit =
    _bodies += body
}
