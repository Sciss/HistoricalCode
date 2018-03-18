package ccampo133.nbody

object Util {
  /** that determines a visually pleasing radius as a function of mass.
    * The formula is pretty arbitrary. I got it by picking some radii I liked and
    * plotting them, and then fitting a log defction to the data. Strictly for
    * visualization purposes.
    */
  def massToRadius(mass: Double): Double =
    if (mass > 1) math.max(1.5, (10 * math.log(mass) / math.log(10.0) - 14) / 3) else 1.02

    /** Another silly function that determines the color of an object based on mass.
      * Again, strictly for visualization purposes.
      */
    def massToColor(mass: Double): String =
      if      (mass >= 100001.0)  "black" // black hole!!!
      else if (mass >= 100000.0)  "#FFD699"
      else if (mass >=  10000.0)  "lemonchiffon"
      else                        "white"

    /** True if a point lies outside more than twice the length/width of some rectangular area.
      */
    def isWayOutOfBounds(x: Double, y: Double, width: Int, height: Int): Boolean =
      math.abs(x) > 2 * width || math.abs(y) > 2 * height
}
