package ccampo133.nbody

/**
 * @author Chris Campo
 */
case class Vec2D(x: Double, y: Double) {
  def + (other : Vec2D ): Vec2D = Vec2D(x + other.x, y + other.y)
  def - (other : Vec2D ): Vec2D = Vec2D(x - other.x, y - other.y)
  def * (scalar: Double): Vec2D = Vec2D(scalar * x, scalar * y)
  def / (scalar: Double): Vec2D = Vec2D(x / scalar, y / scalar)

  def dot(other: Vec2D): Double = (x * other.x) + (y * other.y)

  def length: Double = math.sqrt(this dot this)

  def toUnitVector: Vec2D = this / this.length
}
