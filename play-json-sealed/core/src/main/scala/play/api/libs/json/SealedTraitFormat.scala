/*
 * SealedTraitFormat.scala
 * (play-json-sealed)
 *
 * Copyright (c) 2013 Hanns Holger Rutz. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *
 * For further information, please contact Hanns Holger Rutz at
 * contact@sciss.de
 */

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
    import c.universe._
    val aTpeW   = c.weakTypeOf[A]
    val aClazz  = aTpeW.typeSymbol.asClass

    if (!aClazz.isSealed) { // fall back to Json.format
      if (DEBUG) log(s"Type $aTpeW is not sealed")
      return JsMacroImpl.formatImpl[A](c)
    }

    val aIdent  = Ident(aClazz)

    require(aClazz.isSealed, s"Type $aTpeW is not sealed")
    aClazz.typeSignature  // SI-7046 !
    val subs    = aClazz.knownDirectSubclasses
    require(subs.nonEmpty  , s"Type $aTpeW does not have known direct subclasses")

    if (DEBUG) {
      log(s"Known direct subclasses of $aTpeW")
      subs.foreach(s => log(s.toString))
      log("")
    }

    val subsAll = subs.toList.sortBy(_.name.toString)
    // for each sub type a tuple of writer-case-body, (bool, reader-case-body) where `bool` is true
    // if the sub type is a singleton object and false if it is a case class
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
        // val jsonTree  = Ident(typeOf[Json.type].typeSymbol.asClass)
        // val subFmt    = TypeApply(Select(jsonTree, "format"), subIdent :: Nil)
        val jsonTree  = Ident(typeOf[SealedTraitFormat.type].typeSymbol.asClass)
        val subFmt    = TypeApply(Select(jsonTree, "apply"), subIdent :: Nil)
        Apply(TypeApply(Ident("writeClass"), subIdent:: Nil), subName :: Ident("x") :: subFmt :: Nil) ->
        Apply(Select(subFmt, "reads"), Ident("data") :: Nil)
      }
      CaseDef(patWrite, bodyWrite) -> (isObject, CaseDef(patRead, bodyRead))
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
