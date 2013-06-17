package play.api.libs.json

sealed trait Foo
case class Bar(i: Int   ) extends Foo
case class Baz(j: String) extends Foo

object FooTest extends App {
  implicit val fooWrites = SealedTraitFormat[Foo]

  // val test = writes(Bar(): Foo)

  // implicit val fooFmt = Json.format[Foo]

  // implicit def fooFmt: Format[Foo] = this
  val out = Json.toJson(Bar(33))
  println(Json.prettyPrint(out))
}