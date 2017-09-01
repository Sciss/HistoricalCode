package de.sciss.funser

object CounterTest {
  val a = Counter()
  val b = Counter()
  val c = Counter()

  def main(args: Array[String]): Unit =
    assert(a == 1 && b == 2 && c == 3, s"Failed. a = $a, b = $b, c = $c")
}
