package play.api.libs.json

import scala.reflect.macros.Context
import language.experimental.macros

object SealedTraitFormat {
  val DEBUG = false

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
      val subName   = Literal(Constant(sub.fullName))
      val body      = if (sub.isModuleClass) {
        // val jsStringTree = Ident(typeOf[JsString.type].typeSymbol.asClass)
        // Apply(Select(jsStringTree, "apply"), subName :: Nil)
        // val subNameExpr = c.Expr[String](subName)
        // reify { JsObject(Seq("class" -> JsString(subNameExpr.splice))) } .tree
        Apply(Ident("writeObject"), subName :: Nil)

      } else {
        //        val subType     = sub.companionSymbol.typeSignature
        //        val subUnapplyM = subType.declaration(stringToTermName("unapply")).asMethod
        //        // Json.format does not handle case classes which have no parameters!
        //        // Therefore, check this out here.
        //        val unapplyReturnTypes = subUnapplyM.returnType match {
        //          case TypeRef(_, _, Nil) =>
        //          case _ =>
        //
        val jsonTree  = Ident(typeOf[Json.type].typeSymbol.asClass)
        val subFmt    = TypeApply(Select(jsonTree, "format"), subIdent :: Nil)
        Apply(TypeApply(Ident("writeClass"), subIdent:: Nil), subName :: Ident("x") :: subFmt :: Nil)
      }

      // val subFmt  = reify { Json.format[Int] }
      // val body    = Apply(Select(Literal(Constant("schoko")), "$minus$greater"), subFmt :: Nil) // reify(???).tree // EmptyTree // TODO

      CaseDef(pat, body)
    }
    val m = Match(Ident("value"), cases)

    val mExpr = c.Expr[JsValue](m)
    val r     = reify {
      new Format[A] {
        private def writeClass[A1](name: String, obj: A1, w: Writes[A1]): JsValue =
          JsObject(Seq("class" -> JsString(name), "data" -> w.writes(obj)))

        private def writeObject(name: String): JsValue =
          JsObject(Seq("class" -> JsString(name)))

        def writes(value: A): JsValue = mExpr.splice
        def reads(json: JsValue): JsResult[A] = ???
      }
    }
    val t = r.tree

    if (DEBUG) println(t)
    c.Expr[Format[A]](t)
  }
}
