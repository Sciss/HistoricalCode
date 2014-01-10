package de.sciss.lucre.macros

import scala.annotation.StaticAnnotation
import scala.reflect.macros._
import language.experimental.macros

trait Foo[A]

class mkCompanion extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro mkCompanionMacro.impl
}
object mkCompanionMacro {
  def impl1(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    val inputs = annottees.map(_.tree).toList
    println(s"---- RAW ----\n${showRaw(inputs.head)}")
    val (validAnnottees, _) = inputs partition {
      case x: ClassDef => true
      case _ => false
    }
    for (v <- validAnnottees ) {
      val tpe = v.tpe // <- tpe is null as the annotated type is not yet type checked!
      val tpe2 = if (tpe == null) {
          println(s"---- Input ----\n${showRaw(v)}")
          val blk = c.typeCheck(Block(v))
          val Block((cd1 @ ClassDef(_, _, _, _)) :: Nil, _) = blk
          println(s"---- Output ----\n${showRaw(blk)}")
          cd1.tpe // <- fails with a compiler error (assertion failure)
        }
        else
          tpe
      println(s"type = '$tpe2'")
    }
    c.Expr[Any](Literal(Constant()))
  }

  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    val inputs : List[Tree] = annottees.map(_.tree)(collection.breakOut)
    val outputs: List[Tree] = inputs match {
      case (cd @ ClassDef(_, cName, _, _)) :: tail =>
        // val cTpe = cd.tpe
        val mod0: ModuleDef = tail match {
          case (md @ ModuleDef(_, mName, mTemp)) :: Nil if cName.decoded == mName.decoded => md

          case Nil =>
            val cMod = cd.mods
            var mModF = NoFlags
            if (cMod hasFlag Flag.PRIVATE  ) mModF |= Flag.PRIVATE
            if (cMod hasFlag Flag.PROTECTED) mModF |= Flag.PROTECTED
            if (cMod hasFlag Flag.LOCAL    ) mModF |= Flag.LOCAL
            val mMod = Modifiers(mModF, cMod.privateWithin, Nil)

            // XXX TODO: isn't there a shortcut for creating the constructor? ast.Trees apparently has stuff for it
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
        //        val fooDef = DefDef(mods = NoMods, name = TermName("hasFoo"), tparams = Nil, vparamss = Nil,
        //          tpt = TypeTree(typeOf[Int]), rhs = Literal(Constant(33)))

        //        val cTpe = if (cd.tpe != null) cd.tpe else {
        //          val Block(cd1 :: Nil, _) = c.typeCheck(Block(cd))
        //          cd1.tpe
        //        }

        // cf. http://stackoverflow.com/questions/21044957/type-of-a-macro-annottee
        val cTpe = Ident(TypeName(cd.name.decoded))

        val fooName = TermName("hasFoo")
        // val cTpe    = annottees.head.tree.tpe // staticType // .actualType // yes?
        // println(s"cTpe '$cTpe'")

        // val fooTpt  = TypeTree(cTpe)
        val fooDef  = q"implicit def $fooName: Foo[$cTpe] = ???"
        val mTempBody1 = fooDef :: mTempBody0
        val mTemp1 = Template(mTempParents, mTempSelf, mTempBody1)
        val mod1 = ModuleDef(mod0.mods, mod0.name, mTemp1)

        cd :: mod1 :: Nil

      case _ => c.abort(c.enclosingPosition, "Must annotate a class or trait")
    }

    c.Expr[Any](Block(outputs, Literal(Constant(()))))
  }
}