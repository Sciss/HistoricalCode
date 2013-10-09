package de.sciss.play.json

import play.api.libs.json.Json

/** Note: what still doesn't work, it putting `A` _into_ the object `NestingTest`. */
object NestingTest {
  // implicit val nada = Checker[A]
  import A.A1
  // implicit val fmtA = SealedTraitFormat[A]

  implicit val fmtA2 = Json.format[A1]
}