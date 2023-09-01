package de.sciss.lucre.macros

import scala.reflect.macros.whitebox
import scala.language.experimental.macros
import scala.annotation.StaticAnnotation

class txn extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro txnMacro.impl
}

object txnMacro {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    val inputs = annottees.map(_.tree).toList

    def reportInvalidAnnotationTarget(): Unit =
      c.error(c.enclosingPosition, "This annotation can only be used on a class definition")

    val expandees: List[Tree] = inputs match {
      //      case q"$mods trait $tpname[..$targs] extends ..$parents { $self => ..$stats }" =>
      //        println("Yo crazy mama")
      //        ...

      case cd @ ClassDef(mods, name, tparams, tmp @ Template(parents, self, body)) :: Nil =>
        // println(s"Class name: '$name'")
        // println(s"Parents: $parents")
        // println(s"Self   : $self")
        println("\n---- Body ----")
        println(s"$body")
        body.foreach(t => println(t.getClass))

        val newDefs = body.map {
          // case tree @ q"$vMods val $vName: $tpt = $rhs" =>
          //  tree

          case v @ ValDef(vMods, vName, tpt, rhs) /* if vMods.hasFlag(Flag.LOCAL) */ =>
            val isVar       = vMods.hasFlag(Flag.MUTABLE)
            // val isAbstract  = vMods.hasFlag(Flag.ABSTRACT)

            println(s"${if (isVar) "var" else "val"} - vMods = $vMods, flags = ${vMods.flags}), vName = $vName, tpt = $tpt")
            //            if (vMods.hasFlag(Flag.ABSTRACT    )) println(" - abstract")
            //            if (vMods.hasFlag(Flag.DEFAULTINIT )) println(" - defaultinit")
            //            if (vMods.hasFlag(Flag.DEFAULTPARAM)) println(" - defaultparam")
            //            if (vMods.hasFlag(Flag.DEFERRED    )) println(" - deferred")
            //            if (vMods.hasFlag(Flag.FINAL       )) println(" - final")
            //            if (vMods.hasFlag(Flag.INTERFACE   )) println(" - interface")
            //            if (vMods.hasFlag(Flag.LOCAL       )) println(" - local")
            //            if (vMods.hasFlag(Flag.OVERRIDE    )) println(" - override")
            //            if (vMods.hasFlag(Flag.PARAM       )) println(" - param")
            //            if (vMods.hasFlag(Flag.PRESUPER    )) println(" - presuper")
            //            if (vMods.hasFlag(Flag.PRIVATE     )) println(" - private")
            //            if (vMods.hasFlag(Flag.PROTECTED   )) println(" - protected")
            //            if (vMods.hasFlag(Flag.TRAIT       )) println(" - trait")
            // println(s"Constructor param: $vName")

            if (!rhs.isEmpty) c.abort(v.pos, "@txn members must be abstract")

            val tpt0 = tpt match {
              // case tq"Option[$x]" => println(s"Option of: '$x'"); tpt
              case _ =>
                // val tt = tpt.asInstanceOf[TypeTree]
                tpt
            }

            val tpt1 = tq"S#Var[$tpt0]"
            ValDef(vMods, vName, tpt1, rhs)

          case other =>
            c.abort(other.pos, s"@txn trait body may only contain abstract val and var members (${other.getClass}")
        }

        val implClass = q"""
          object ${name.toTermName} {
            sealed trait Txn[S <: Sys[S], ..$tparams] { ..$newDefs }

            def apply[S <: Sys[S], ..$tparams]()(implicit tx: S#Tx): Txn[S, ..${tparams.map(_.name)}] = ???
          }
          """

        implClass :: inputs

      case _ =>
        reportInvalidAnnotationTarget()
        inputs
    }

    val annottee = inputs match {
      case (param: ValDef ) :: (rest @ (_ :: _)) => param
      case (param: TypeDef) :: (rest @ (_ :: _)) => param
      case _ => EmptyTree
    }
    println((annottee, inputs))
    val outputs = expandees
    c.Expr[Any](Block(outputs, Literal(Constant(()))))
  }
}