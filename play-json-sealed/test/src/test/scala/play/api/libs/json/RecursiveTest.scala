//package play.api.libs.json
//
//sealed trait Goo
//case class Gar(i: Int) extends Goo
//case class Gaz(more: Goo) extends Goo
//
//case class Bobo(i: Option[Bobo])
//
//object RecursiveTest extends App {
//  implicit val fmt = SealedTraitFormat[Goo]
//
//  // implicit val fmt = Json.format[Bobo]
//  // val out = Json.toJson(Bobo(Some(Bobo(None))))
//  // println(Json.prettyPrint(out))
//}