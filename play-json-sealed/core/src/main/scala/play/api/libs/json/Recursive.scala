//package play.api.libs.json
//
//import scala.reflect.macros.Context
//
//sealed trait Meta[A]
//case class MetaSealed[A](v: List[Meta[A]]) extends Meta[A]
//case class MetaSingleton[A]() extends Meta[A]
//case class MetaProduct1[A, B](_1: Meta[B]) extends Meta[A]
//
//sealed trait MaybePerson
//case class Person(spouse: MaybePerson) extends MaybePerson
//case object NoPerson extends MaybePerson
//
//object Recursive {
//  // def apply[A]
//
//  def applyImpl[A: c.WeakTypeTag](c: Context): c.Expr[Meta[A]] = {
//    import c.universe._
//    val aTpeW   = c.weakTypeOf[A]
//    val aClazz  = aTpeW.typeSymbol.asClass
//
//    if (aClazz.isSealed) {
//      val subs = aClazz.knownDirectSubclasses
//
//
//    } else if (aClazz.isModuleClass) {
//
//    } else {
//
//    }
//
//    ???
//  }
//}