package de.sciss.play.json

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import play.api.libs.json.Json

sealed trait Foo
case class Bar(i: Int   ) extends Foo
case class Baz(j: String) extends Foo
case object Gaga          extends Foo

object SimpleTest extends FlatSpec with ShouldMatchers {
  def print = false

  "A sealed trait" should "find an automatic serializer" in {
    implicit val fooWrites = AutoFormat[Foo]

    val obj1: Foo = Bar(33)
    val out1 = Json.toJson(obj1)
    if (print) println(Json.prettyPrint(out1))

    val in1 = Json.fromJson[Foo](out1)
    assert(in1.asOpt === Some(obj1))

    val obj2: Foo = Gaga
    val out2 = Json.toJson(obj2)
    if (print) println(Json.prettyPrint(out2))

    val in2 = Json.fromJson[Foo](out2)
    assert(in2.asOpt === Some(obj2))
  }
}