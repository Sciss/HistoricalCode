package de.sciss.funser

import scala.reflect.macros.blackbox

object ActionImpl {
  private final val DEBUG = false

  // ---- creation ----

  private[this] var count = 0

  def compile(source: Code.Action)
             (implicit compiler: Code.Compiler): Action = {
    val id = count
    count += 1
    val name = s"Action$id}"
    performCompile(name, source)
  }

  private[this] lazy val classLoaderInstance = new MemoryClassLoader

  private def classLoader(): MemoryClassLoader = sync.synchronized {
    classLoaderInstance
  }

  def applyImpl(c: blackbox.Context)(body: c.Expr[Unit]): c.Expr[Action] = {
    ???
  }

  def execute(universe: Action.Universe, name: String, jar: Array[Byte]): Unit = {
    val cl = classLoader()
    cl.add(name, jar)
    val fullName  = s"${Code.UserPackage}.$name"
    val clazz     = Class.forName(fullName, true, cl)
    //  println("Instantiating...")
    val fun = clazz.newInstance().asInstanceOf[Action.Body]
    fun(universe)
  }

  // ----

  private def performCompile(name: String,
                             source: Code.Action)
                            (implicit compiler: Code.Compiler): Action = {
    // val jarFut = source.compileToFunction(name)
    val jar = source.execute(name)

    // somehow we get problems with BDB on the compiler context.
    // for simplicity use the main SP context!

    if (DEBUG) println(s"ActionImpl: compileToFunction completed. jar-size = ${jar.length}")
    new ConstFunImpl(name, jar, source = source.source)
  }

  // ---- universe ----


  // ---- constant implementation ----

  private val sync = new AnyRef

  // this is why workspace should have a general caching system
//  private val clMap = new mutable.WeakHashMap[Sys[_], MemoryClassLoader]

  // XXX TODO - should be called ConstJarImpl in next major version
  private final class ConstFunImpl(val name: String, jar: Array[Byte], val source: String)
    extends Action /* ConstImpl[S] */ {

    def execute(universe: Action.Universe): Unit = {
      ActionImpl.execute(universe, name, jar)
    }
  }
}