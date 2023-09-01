package ccampo133.nbody

case class Vec2D(x: Double, y: Double) {
  def + (other : Vec2D ): Vec2D = Vec2D(x + other.x, y + other.y)
  def - (other : Vec2D ): Vec2D = Vec2D(x - other.x, y - other.y)
  def * (scalar: Double): Vec2D = Vec2D(scalar * x, scalar * y)
  def / (scalar: Double): Vec2D = Vec2D(x / scalar, y / scalar)

  def dot(other: Vec2D): Double = (x * other.x) + (y * other.y)

  def length: Double = math.sqrt(this dot this)

  def toUnitVector: Vec2D = this / this.length
}
object Vec2D {
  val zero = Vec2D(0.0, 0.0)
}