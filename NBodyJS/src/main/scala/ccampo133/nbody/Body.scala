package ccampo133.nbody

/** Represents a circular moving body in 2D space.
  *
  * @param mass       The body's mass.
  * @param rad        The body's radius.
  * @param pos        The body's position.
  * @param vel        The body's velocity.
  * @param trail      A list of the body's previous positions (in reverse order)
  */
case class Body(mass: Double, rad: Double, pos: Vec2D, vel: Vec2D, trail: List[Vec2D] = Nil) {

  def isCollision(other: Body): Boolean = {
    val r = pos - other.pos
    r.length < other.rad && mass <= other.mass
  }
}
object Body {
  def empty: Body = Body(0.0, 0.0, Vec2D.zero, Vec2D.zero)
}