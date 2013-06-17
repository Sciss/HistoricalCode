package play.api.libs.json

import scala.reflect.macros.Context
import language.experimental.macros

object SealedTraitFormat {
  private val DEBUG   = false
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
    val aIdent  = Ident(aClazz)

    val subsAll = subs.toList.sortBy(_.name.toString)
    val cases   = subsAll.map { sub =>
      val subC      = sub.asClass
      val isObject  = subC.isModuleClass
      val subIdent  = Ident(if (isObject) subC.companionSymbol.asModule else subC)
      val patWrite  = Bind(newTermName("x"), Typed(Ident("_"), subIdent))
      val subName0  = if (PACKAGE) sub.fullName else sub.name.toString
      val subName   = Literal(Constant(subName0))
      val patRead   = subName
      val (bodyWrite, bodyRead) = if (isObject) {
        val jsSuccessTree = Ident(typeOf[JsSuccess.type].typeSymbol.asClass.companionSymbol.asModule) // frickin' hell
        Apply(Ident("writeObject"), subName :: Nil) ->
        Apply(TypeApply(jsSuccessTree, aIdent :: Nil), subIdent :: Nil) // JsSuccess[A](MyCaseObject)

      } else {
        val jsonTree  = Ident(typeOf[Json.type].typeSymbol.asClass)
        val subFmt    = TypeApply(Select(jsonTree, "format"), subIdent :: Nil)
        Apply(TypeApply(Ident("writeClass"), subIdent:: Nil), subName :: Ident("x"   ) :: subFmt :: Nil) ->
        Apply(Select(subFmt, "reads"), Ident("data") :: Nil)
      }
      CaseDef(patWrite, bodyWrite) ->
      (isObject, CaseDef(patRead, bodyRead))
    }
    val (casesWrite, casesRead) = cases.unzip
    val matchWrite      = Match(Ident("value"), casesWrite)
    val matchWriteExpr  = c.Expr[JsValue](matchWrite)
    val casesReadC      = casesRead collect { case (false, tree) => tree }
    val matchReadC      = Match(Ident("name"), casesReadC) // XXX TODO add catch all
    val matchReadExprC  = c.Expr[JsResult[A]](matchReadC)
    val casesReadO      = casesRead collect { case (true, tree) => tree }
    val matchReadO      = Match(Ident("name"), casesReadO) // XXX TODO add catch all
    val matchReadExprO  = c.Expr[JsResult[A]](matchReadO)
    val r               = reify {
      new Format[A] {
        private def writeClass[A1](name: String, obj: A1, w: Writes[A1]): JsValue =
          JsObject(Seq("class" -> JsString(name), "data" -> w.writes(obj)))

        private def writeObject(name: String): JsValue =
          JsObject(Seq("class" -> JsString(name)))

        def writes(value: A): JsValue = matchWriteExpr.splice

        def reads(json: JsValue): JsResult[A] = try {
          (json: @unchecked) match {
            // this crashes upon macro expansion (probably a bug)
            // case JsObject(Seq(("class", JsString(name)), rest @ _*)) => ...
            case JsObject(sq) =>
              (sq.toList: @unchecked) match {
                case ("class", JsString(name)) :: tail =>
                  (tail: @unchecked) match {
                    case ("data", data) :: Nil  => matchReadExprC.splice
                    case Nil                    => matchReadExprO.splice
                  }
              }
          }
        } catch {
          case _: MatchError => JsError(json.toString)
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
