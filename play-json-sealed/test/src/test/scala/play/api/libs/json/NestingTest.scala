package play.api.libs.json

/** Note: what still doesn't work, it putting `A` _into_ the object `NestingTest`. */
object NestingTest {
  // implicit val nada = Checker[A]
  import A.A1
  // implicit val fmtA = SealedTraitFormat[A]

  implicit val fmtA2 = Json.format[A1]
}