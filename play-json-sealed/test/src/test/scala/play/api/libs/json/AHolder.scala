package play.api.libs.json

object AHolder extends App {
  val bar: A = AImpl.body

  bar.foo()
}