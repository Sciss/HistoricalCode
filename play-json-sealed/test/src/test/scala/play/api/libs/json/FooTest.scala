package play.api.libs.json

sealed trait Foo
case class Bar() extends Foo
case class Baz() extends Foo

object FooTest extends SealedTraitFormat[Foo] /* with Format[Foo] */ with App {
  // val test = writes(Bar(): Foo)

  // implicit val fooFmt = Json.format[Foo]

  // implicit def fooFmt: Format[Foo] = this
  // Json.toJson(Bar())
}