package play.api.libs.json

trait AHolder {
  val bar: A = AImpl.body
}