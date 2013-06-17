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
  def writesImpl[A: c.WeakTypeTag](c: Context)(value: c.Expr[A]): c.Expr[JsValue] = {
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
      val pat   = Bind(newTermName("x"), Typed(Ident("_"), Ident(sub.asClass)))
      val body  = reify(???).tree // EmptyTree // TODO
      CaseDef(pat, body)
    }
    val m = Match(value.tree, cases)

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


    println(m)
    c.Expr[JsValue](m)
//    val (prod: Product, sub) = foo match {
//      case b: Bar => (b, Json.toJson(b)(barFmt))
//      case b: Baz => (b, Json.toJson(b)(bazFmt))
//    }
//    JsObject(Seq("class" -> JsString(prod.productPrefix), "data" -> sub))
  }
}
trait SealedTraitFormat[A] {
  def writes(value: A): JsValue = macro SealedTraitFormat.writesImpl[A]
}