package de.sciss.funser

import scala.reflect.macros.blackbox

object ActionImpl {
  type Action = (Function0[Unit], String)

  private final val DEBUG = false

  // ---- creation ----

  private[this] var count = 0

  private def mkName(): String = {
    val id = count
    count += 1
    s"Action$id"
  }

  def compile(source: Code.Action)
             (implicit compiler: Code.Compiler): Action = {
    val name = mkName()
    performCompile(name, source)
  }

  private[this] lazy val classLoaderInstance  = new MemoryClassLoader
  private[this] lazy val compilerInstance: Code.Compiler = CompilerImpl()

  private def classLoader(): MemoryClassLoader = sync.synchronized {
    classLoaderInstance
  }

  def applyImpl(c: blackbox.Context)(body: c.Expr[Unit]): c.Expr[Action] = {
    val pos     = body.tree.pos
    val source  = pos.source.content
    val portion = if (pos.isRange) new String(source.slice(pos.start, pos.start + pos.end - pos.start)) else {
      c.error(pos, s"Could not extract source")
      ""
    }
    val code  = Code.Action(portion)
    val name  = mkName()
    implicit val compiler: Code.Compiler = compilerInstance
    val jar   = code.execute(name)
    val jarS  = new String(jar, "ISO-8859-1")
    // println(s"jar size = ${jar.length}")
    import c.universe._
    val nameExpr    = c.Expr[String](Literal(Constant(name)))
    val jarExpr     = c.Expr[String](Literal(Constant(jarS)))
    val portionExpr = c.Expr[String](Literal(Constant(portion)))
    reify {
      Action.newConst(nameExpr.splice, jarExpr.splice, portionExpr.splice)
    }
  }

  def execute(/* universe: Action.Universe, */ name: String, jar: Array[Byte]): Unit = {
    val cl = classLoader()
    cl.add(name, jar)
    val fullName  = s"${Code.UserPackage}.$name"
    val clazz     = Class.forName(fullName, true, cl)
    //  println("Instantiating...")
    val fun = clazz.newInstance().asInstanceOf[Action.Body]
    fun() // (universe)
  }

  // ----

  def newConst(name: String, jar: Array[Byte], source: String): Action = {
    val impl = new ConstFunImpl(name, jar)
    (impl, source)
  }

  private def performCompile(name: String,
                             source: Code.Action)
                            (implicit compiler: Code.Compiler): Action = {
    // val jarFut = source.compileToFunction(name)
    val jar = source.execute(name)

    // somehow we get problems with BDB on the compiler context.
    // for simplicity use the main SP context!

    if (DEBUG) println(s"ActionImpl: compileToFunction completed. jar-size = ${jar.length}")
    newConst(name, jar, source = source.source)
  }

  // ---- universe ----


  // ---- constant implementation ----

  private val sync = new AnyRef

  // this is why workspace should have a general caching system
//  private val clMap = new mutable.WeakHashMap[Sys[_], MemoryClassLoader]

  // XXX TODO - should be called ConstJarImpl in next major version
  private final class ConstFunImpl(val name: String, jar: Array[Byte])
    extends Function0[Unit] {


    def apply(): Unit =
      ActionImpl.execute(name, jar)

//    def execute(universe: Action.Universe): Unit = {
//      ActionImpl.execute(universe, name, jar)
//    }
  }
}