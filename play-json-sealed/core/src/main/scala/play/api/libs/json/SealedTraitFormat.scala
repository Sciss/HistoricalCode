package play.api.libs.json

import scala.reflect.macros.Context
import language.experimental.macros

//object SealedTraitFormat {
//  def writesImpl[A: c.TypeTag](c: Context)(expr: c.Expr[A]): c.Expr[JsValue] = {
//
//
//    ???
////    val (prod: Product, sub) = foo match {
////      case b: Bar => (b, Json.toJson(b)(barFmt))
////      case b: Baz => (b, Json.toJson(b)(bazFmt))
////    }
////    JsObject(Seq("class" -> JsString(prod.productPrefix), "data" -> sub))
//  }
//}
//trait SealedTraitFormat[A] {
//  def writes(value: A): JsValue = macro SealedTraitFormat.writesImpl[A]
//}

object SealedTraitFormat {
  def apply[A]: Format[A] = macro applyImpl[A]

  def applyImpl[A: c.WeakTypeTag](c: Context): c.Expr[Format[A]] = {
    val aTpeW   = c.weakTypeOf[A]
    val aClazz  = aTpeW.typeSymbol.asClass
    require(aClazz.isSealed, s"Type $aTpeW is not sealed")
    val subs    = aClazz.knownDirectSubclasses
    require(subs.nonEmpty  , s"Type $aTpeW does not have known direct subclasses")
    //    println("Mira Charlie:")
    //    subs.foreach(println)
    //    println()
    import c.universe._

    val cases = subs.toList.map { sub =>
      val subIdent  = Ident(sub.asClass)
      val pat       = Bind(newTermName("x"), Typed(Ident("_"), subIdent))
      // c.inferImplicitValue()
      val jsonTree  = Ident(typeOf[Json.type].typeSymbol.asClass)
      val subFmt    = TypeApply(Select(jsonTree, "format"), subIdent :: Nil)

      // val subFmt  = reify { Json.format[Int] }
      // val body    = Apply(Select(Literal(Constant("schoko")), "$minus$greater"), subFmt :: Nil) // reify(???).tree // EmptyTree // TODO

      val body = Apply(TypeApply(Ident("writeSub"), subIdent:: Nil), Ident("x") :: Literal(Constant("schoko")) :: subFmt :: Nil)

      CaseDef(pat, body)
    }
    val m = Match(Ident("value"), cases)

    //    val test = reify {
    //      ("gaga": Any) match {
    //        case x: String => x.reverse
    //        case x: Int => x + 1
    //      }
    //    }

    //    test match {
    //      case Match(_, cases) =>
    //        cases.foreach { cd =>
    //          println("PAT")
    //          println(cd.pat)
    //        }
    //    }

    // println(test) // show(test))

    val mExpr = c.Expr[JsValue](m)
    val r     = reify {
      new Format[A] {
        private def writeSub[A](obj: A, name: String, w: Writes[A]): JsValue =
          JsObject(Seq("class" -> JsString(name), "data" -> w.writes(obj)))

        def writes(value: A): JsValue = mExpr.splice
        def reads(json: JsValue): JsResult[A] = ???
      }
    }
    val t = r.tree

    println(t)
    c.Expr[Format[A]](t)
//    val (prod: Product, sub) = foo match {
//      case b: Bar => (b, Json.toJson(b)(barFmt))
//      case b: Baz => (b, Json.toJson(b)(bazFmt))
//    }
//    JsObject(Seq("class" -> JsString(prod.productPrefix), "data" -> sub))
  }
}
//trait SealedTraitFormat[A] {
//  def writes(value: A): JsValue = macro SealedTraitFormat.writesImpl[A]
//}