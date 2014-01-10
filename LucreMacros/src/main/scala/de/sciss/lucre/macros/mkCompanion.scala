package de.sciss.lucre.macros

import scala.annotation.StaticAnnotation
import scala.reflect.macros._
import language.experimental.macros
import scala.collection

class mkCompanion extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro mkCompanionMacro.impl
}
object mkCompanionMacro {
  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    val inputs: List[Tree] = annottees.map(_.tree)(collection.breakOut)
    val newTrees: List[Tree] = inputs match {
      case (cd @ ClassDef(_, cName, _, _)) :: tail =>
        val mod0: ModuleDef = tail match {
          case (md @ ModuleDef(_, mName, mTemp)) :: Nil if cName.decoded == mName.decoded =>
            //            // mModF      carries stuff like `private`, `implicit`, `final`
            //            // mModName   carries privacy scope if any; e.g. with `private[schoko]`, the name is `schoko`
            //            // mModTrees  carries the annotations, or Nil if not annotated
            //            //            note: the macro annotation is _not_ included in cd.mods!
            //            val Modifiers(mModF, mModName, mModTrees) = cd.mods
            //            // for an otherwise unspecified module:
            //            // - parents is `List(scala.AnyRef)`
            //            // - self is of type `scala.reflect.internal.Trees$noSelfType$`
            //            // - body is DefDef :: Nil
            //            val Template(mTempParents, mTempSelf, mTempBody) = mTemp
            //            // println(s"Module '$mName' with mods (flags = '$mModF'; name = '$mModName'; trees '$mModTrees'); and template '${mTemp.getClass}'")
            //            println(s"Template parents = '$mTempParents', self = '${mTempSelf.getClass}', body = '${mTempBody.map(_.getClass)}'")

            // println(s"Class '${cName.decoded}', module '${mName.decoded}', same? ${cName == mName}, ${cName.decoded == mName.decoded}")
            // println(md)
            md

          case Nil =>
            // ModuleDef(mods, name, template)
            val cMod = cd.mods
            //            val Modifiers(cModF, cModName, cModTrees) = cMod
            //            println(s"Class mods $cModF")
            var mModF = NoFlags
            if (cMod hasFlag Flag.PRIVATE  ) mModF |= Flag.PRIVATE
            if (cMod hasFlag Flag.PROTECTED) mModF |= Flag.PROTECTED
            if (cMod hasFlag Flag.LOCAL    ) mModF |= Flag.LOCAL
            val mMod = Modifiers(mModF, cMod.privateWithin, Nil)

            // or should we have parents = List(AnyRef) and body = List(DefDef(???))
            val mTemp = Template(parents = Nil, self = noSelfType, body = Nil)
            val mName = TermName(cName.decoded) // or encoded?

            ModuleDef(mMod, mName, mTemp)

            // cModF.
            // ModuleDef(cd.mods.)

          case _ =>
            println(s"Got $tail")
            c.abort(c.enclosingPosition, s"Expected a companion object for '$cName'")
        }

        val Template(mTempParents, mTempSelf, mTempBody0) = mod0.impl
        val fooDef = DefDef(mods = NoMods, name = TermName("hasFoo"), tparams = Nil, vparamss = Nil,
          tpt = TypeDef(NoMods, TypeName("Int"), Nil, EmptyTree), rhs = Literal(Constant(33)))
        val mTempBody1 = fooDef :: mTempBody0
        val mTemp1 = Template(mTempParents, mTempSelf, mTempBody1)
        val mod1 = ModuleDef(mod0.mods, mod0.name, mTemp1)

        cd :: mod1 :: Nil

      case _ => c.abort(c.enclosingPosition, "Must annotate a class or trait")
    }

    c.Expr[Any](Block(newTrees: _*))
  }
}