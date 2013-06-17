package play.api.libs.json

sealed trait Foo
case class Bar(i: Int   ) extends Foo
case class Baz(j: String) extends Foo
case object Gaga          extends Foo

object FooTest extends App {
  implicit val fooWrites = SealedTraitFormat[Foo]

  // val test = writes(Bar(): Foo)

  // implicit val fooFmt = Json.format[Foo]

  // implicit def fooFmt: Format[Foo] = this
  val obj1  = Bar(33)
  val out1  = Json.toJson(obj1)
  println(Json.prettyPrint(out1))

  val in1 = Json.fromJson[Foo](out1)
  assert(in1.asOpt == Some(obj1), s"result $in1")

  val obj2  = Gaga
  val out2  = Json.toJson(obj2)
  println(Json.prettyPrint(out2))

  val in2 = Json.fromJson[Foo](out2)
  assert(in2.asOpt == Some(obj2), s"result $in2")
}