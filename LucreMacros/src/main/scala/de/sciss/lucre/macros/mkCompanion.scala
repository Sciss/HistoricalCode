package de.sciss.lucre.macros

import scala.annotation.StaticAnnotation
import scala.reflect.macros._
import language.experimental.macros

class mkCompanion extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro mkCompanionMacro.impl
}
object mkCompanionMacro {
  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    val inputs : List[Tree] = annottees.map(_.tree)(collection.breakOut)
    val outputs: List[Tree] = inputs match {
      case (cd @ ClassDef(_, cName, _, _)) :: tail =>
        val mod0: ModuleDef = tail match {
          case (md @ ModuleDef(_, mName, mTemp)) :: Nil if cName.decoded == mName.decoded =>
            //            mTemp.children.foreach {
            //              case DefDef(mods, name, tparams, vparamss, tpt, rhs) =>
            //                println(s"Mods '$mods' name '$name', tparams '$tparams', vparamss '$vparamss', tpt '$tpt', rhs '$rhs'")
            //              case _ =>
            //            }
            md

          case Nil =>
            val cMod = cd.mods
            var mModF = NoFlags
            if (cMod hasFlag Flag.PRIVATE  ) mModF |= Flag.PRIVATE
            if (cMod hasFlag Flag.PROTECTED) mModF |= Flag.PROTECTED
            if (cMod hasFlag Flag.LOCAL    ) mModF |= Flag.LOCAL
            val mMod = Modifiers(mModF, cMod.privateWithin, Nil)

            val mkSuperSelect = Select(Super(This(tpnme.EMPTY), tpnme.EMPTY), nme.CONSTRUCTOR)
            val superCall     = Apply(mkSuperSelect, Nil)
            val constr        = DefDef(NoMods, nme.CONSTRUCTOR, Nil, List(Nil), TypeTree(),
              Block(List(superCall), Literal(Constant())))

            val mTemp = Template(parents = List(TypeTree(typeOf[AnyRef])), self = noSelfType, body = constr :: Nil)
            val mName = TermName(cName.decoded) // or encoded?

            ModuleDef(mMod, mName, mTemp)

          case _ => c.abort(c.enclosingPosition, s"Expected a companion object for '$cName'")
        }

        val Template(mTempParents, mTempSelf, mTempBody0) = mod0.impl
        val fooDef = DefDef(mods = NoMods, name = TermName("hasFoo"), tparams = Nil, vparamss = Nil,
          tpt = TypeTree(typeOf[Int]) /* EmptyTree */ /* TypeDef(NoMods, TypeName("Int"), Nil, EmptyTree) */, rhs = Literal(Constant(33)))
        val mTempBody1 = fooDef :: mTempBody0
        val mTemp1 = Template(mTempParents, mTempSelf, mTempBody1)
        val mod1 = ModuleDef(mod0.mods, mod0.name, mTemp1)

        cd :: mod1 :: Nil

      case _ => c.abort(c.enclosingPosition, "Must annotate a class or trait")
    }

    c.Expr[Any](Block(outputs, Literal(Constant(()))))
  }
}