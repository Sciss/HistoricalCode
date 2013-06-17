package play.api.libs.json

import scala.reflect.macros.Context
import language.experimental.macros

object SealedTraitFormat {
  private val DEBUG   = true
  private val PACKAGE = false

  def apply[A]: Format[A] = macro applyImpl[A]

  private def log(what: => String) {
    println(s"<sealed> $what")
  }

  def applyImpl[A: c.WeakTypeTag](c: Context): c.Expr[Format[A]] = {
    val aTpeW   = c.weakTypeOf[A]
    val aClazz  = aTpeW.typeSymbol.asClass
    require(aClazz.isSealed, s"Type $aTpeW is not sealed")
    val subs    = aClazz.knownDirectSubclasses
    require(subs.nonEmpty  , s"Type $aTpeW does not have known direct subclasses")

    if (DEBUG) {
      log(s"Known direct subclasses of $aTpeW")
      subs.foreach(s => log(s.toString))
      log("")
    }
    import c.universe._

    val cases = subs.toList.map { sub =>
      val subIdent  = Ident(sub.asClass)
      val subIdentExpr = c.Expr[A](subIdent)
      val patWrite  = Bind(newTermName("x"), Typed(Ident("_"), subIdent))
      val subName0  = if (PACKAGE) sub.fullName else sub.name.toString
      val subName   = Literal(Constant(subName0))
      val patRead   = subName
      val (bodyWrite, bodyRead) = if (sub.isModuleClass) {
        Apply(Ident("writeObject"), subName :: Nil) ->
        (reify { ??? } .tree) // (reify { JsSuccess[A](subIdentExpr.splice) }.tree)

      } else {
        val jsonTree  = Ident(typeOf[Json.type].typeSymbol.asClass)
        val subFmt    = TypeApply(Select(jsonTree, "format"), subIdent :: Nil)
        Apply(TypeApply(Ident("writeClass"), subIdent:: Nil), subName :: Ident("x") :: subFmt :: Nil) ->
        (reify { ??? } .tree)
      }
      CaseDef(patWrite, bodyWrite) ->
      CaseDef(patRead , bodyRead )
    }
    val (casesWrite, casesRead) = cases.unzip
    val matchWrite      = Match(Ident("value"), casesWrite)
    val matchWriteExpr  = c.Expr[JsValue](matchWrite)
    val matchRead       = Match(Ident("name"), casesRead)
    val matchReadExpr   = c.Expr[JsResult[A]](matchRead)
    val r               = reify {
      new Format[A] {
        private def writeClass[A1](name: String, obj: A1, w: Writes[A1]): JsValue =
          JsObject(Seq("class" -> JsString(name), "data" -> w.writes(obj)))

        private def writeObject(name: String): JsValue =
          JsObject(Seq("class" -> JsString(name)))

        def writes(value: A): JsValue = matchWriteExpr.splice
        def reads(json: JsValue): JsResult[A] = json match {
          // this crashes upon macro expansion (probably a bug)
          // case JsObject(Seq(("class", JsString(name)), rest @ _*)) => ...
          case JsObject(sq) =>
            sq.toList match {
              case ("class", JsString(name)) :: ("data", data) :: Nil => ???
              case ("class", JsString(name)) :: Nil => matchReadExpr.splice
              case _ => JsError(s"Unexpected JSON dictionary: $json")
            }
          case _ => JsError(s"Not a JSON dictionary with key 'class': $json")
        }
      }
    }
    val t = r.tree

    if (DEBUG) {
      log("Tree:")
      println(t)
    }
    c.Expr[Format[A]](t)
  }
}
