package play.api.libs.json

sealed trait Goo
case class Gar(i: Int) extends Goo
case class Gaz(more: Goo) extends Goo

object RecursiveTest extends App {
  // implicit val fmt = SealedTraitFormat[Goo]
}