package ccampo133.nbody

object Physics {
  final val G                 = 1.0 // Gravitational constant
  final val SOFTENING_LENGTH  = 2.0

  /** Numerically integrate Newton's equations of motion using the "Velocity Verlet" algorithm.
    *
    * See [Wikipedia](https://en.wikipedia.org/wiki/Verlet_integration#Velocity_Verlet)
    *
    * @param pos    The position vector
    * @param vel    The velocity vector
    * @param dt     The timestep
    * @param accel  The acceleration function
    */
  def verlet(pos: Vec2D, vel: Vec2D, dt: Double, accel: Vec2D => Vec2D): (Vec2D, Vec2D) = {
    val pos1 = pos + (vel * dt) + (accel(pos) * math.pow(dt, 2) / 2)
    val vel1 = vel + (accel(pos) + accel(pos1)) * (dt / 2)
    (pos1, vel1)
  }


  /** Using Newton's law of universal gravitation, calculate the total acceleration vector on a two-dimension position
    * vector caused by a set of massive bodies.
    *
    * See [Wikipedia](https://en.wikipedia.org/wiki/Newton%27s_law_of_universal_gravitation)
    *
    * @param pos    The position vector to find the acceleration at.
    * @param bodies The set of bodies to used to contribute to the overall gravitational acceleration.
    */
  def gravityAcceleration(pos: Vec2D, bodies: Set[Body]): Vec2D = {
    def gravity(pos: Vec2D)(body2: Body): Vec2D = {
      val r12 = body2.pos - pos
      r12 * body2.mass * G / math.pow(math.pow(r12.length, 2.0) + math.pow(SOFTENING_LENGTH, 2.0), 3/2.0)
    }
    (bodies map gravity(pos)).foldLeft(Vec2D.zero)(_ + _)
  }
}
