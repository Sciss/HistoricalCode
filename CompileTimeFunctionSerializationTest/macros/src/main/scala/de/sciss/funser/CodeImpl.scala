package de.sciss.funser

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import scala.collection.immutable.{IndexedSeq => Vec, Seq => ISeq}
import scala.concurrent.{Future, blocking}

object CodeImpl {

  // ---- type ----

  @volatile private var map = Map.empty[Int, Code.Type]

  def addType(tpe: Code.Type): Unit = sync.synchronized {
    val typeID = tpe.id
    if (map.contains(typeID))
      throw new IllegalArgumentException(s"Code type $typeID was already registered ($tpe overrides ${map(typeID)})")

    map += typeID -> tpe
  }

  def getType(id: Int): Code.Type = map.getOrElse(id, sys.error(s"Unknown element type $id"))

  def apply(id: Int, source: String): Code = getType(id).mkCode(source)

  // ----

  def unpackJar(bytes: Array[Byte]): Map[String, Array[Byte]] = {
    import java.util.jar._

    import scala.annotation.tailrec

    val in = new JarInputStream(new ByteArrayInputStream(bytes))
    val b  = Map.newBuilder[String, Array[Byte]]

    @tailrec def loop(): Unit = {
      val entry = in.getNextJarEntry
      if (entry != null) {
        if (!entry.isDirectory) {
          val name  = entry.getName

          // cf. http://stackoverflow.com/questions/8909743/jarentry-getsize-is-returning-1-when-the-jar-files-is-opened-as-inputstream-f
          val bs  = new ByteArrayOutputStream
          var i   = 0
          while (i >= 0) {
            i = in.read()
            if (i >= 0) bs.write(i)
          }
          val bytes = bs.toByteArray
          b += mkClassName(name) -> bytes
        }
        loop()
      }
    }
    loop()
    in.close()
    b.result()
  }

  /* Converts a jar entry name with slashes to a class name with dots
   * and dropping the `class` extension
   */
  private def mkClassName(path: String): String = {
    require(path.endsWith(".class"))
    path.substring(0, path.length - 6).replace("/", ".")
  }

  //  // note: the Scala compiler is _not_ reentrant!!
  //  private implicit val executionContext: ExecutionContextExecutor =
  //    ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())

  def future[A](fun: => A)(implicit compiler: Code.Compiler): Future[A] =
    concurrent.Future(fun)(compiler.executionContext)

  // ---- imports ----

  private val sync = new AnyRef

  private var importsMap = Map[Int, Vec[String]](
    Code.Action.id -> Vec(     // what should go inside?
//      "de.sciss.synth.proc._",
//      "de.sciss.synth.proc.Implicits._",
//      "de.sciss.lucre.artifact.{Artifact, ArtifactLocation}",
//      "de.sciss.lucre.expr.{Expr, BooleanObj, IntObj, LongObj, DoubleObj, StringObj, DoubleVector}",
//      "de.sciss.lucre.expr.Ops._"
    )
  )

  def registerImports(id: Int, imports: Seq[String]): Unit = sync.synchronized {
    importsMap += id -> importsMap.get(id).fold(imports.toIndexedSeq)(_ ++ imports)
  }

  def getImports(id: Int): Vec[String] = importsMap(id)

  // ---- internals ----

  object Wrapper {
  }
  trait Wrapper[In, Out, Repr] {
    protected def id: Int

    final def imports: ISeq[String] = importsMap(id)

    def binding: Option[String]

    /** When `execute` is called, the result of executing the compiled code
      * is passed into this function.
      *
      * @param in   the code type's input
      * @param fun  the thunk that executes the code
      * @return     the result of `fun` wrapped into type `Out`
      */
    def wrap(in: In)(fun: => Any): Out

    /** TypeTag of */
    def blockTag: String // TypeTag[_]
    //    def inTag   : TypeTag[In]
    //    def outTag  : TypeTag[Out]
  }

  final def execute[I, O, Repr <: Code { type In = I; type Out = O }](code: Repr, in: I)
                                                                     (implicit w: Wrapper[I, O, Repr],
                                                                      compiler: Code.Compiler): O = {
    w.wrap(in) {
      compileThunk(code.source, w, execute = true)
    }
  }

  def compileBody[I, O, Repr <: Code { type In = I; type Out = O }](code: Repr)
                                                                   (implicit w: Wrapper[I, O, Repr],
                                                                    compiler: Code.Compiler): Future[Unit] =
    future {
      blocking {
        compileThunk(code.source, w, execute = false)
      }
    }

  /** Compiles a source code consisting of a body which is wrapped in a `Function0` apply method,
    * and returns the function's class name (without package) and the raw jar file produced in the compilation.
    */
  def compileToFunction(name: String, code: Code.Action)(implicit compiler: Code.Compiler): Array[Byte] = {

    val imports = getImports(Code.Action.id)
    val impS  = /* w. */imports.map(i => s"  import $i\n").mkString
    //        val bindS = w.binding.fold("")(i =>
    //          s"""  val __context__ = $pkg.$i.__context__
    //             |  import __context__._
    //             |""".stripMargin)
    // val aTpe  = w.blockTag.tpe.toString
    val source =
    s"""package ${Code.UserPackage}
       |
         |final class $name extends $pkgAction.Body {
       |  def apply[S <: $pkgSys.Sys[S]](universe: $pkgAction.Universe[S])(implicit tx: S#Tx): Unit = {
       |    import universe._
       |$impS
       |${code.source}
       |  }
       |}
       |""".stripMargin

    // println(source)

    compiler.compile(source)
  }

  object Run {
    def apply[A](execute: Boolean)(thunk: => A): A = if (execute) thunk else null.asInstanceOf[A]
  }

  sealed trait Context[A] {
    def __context__(): A
  }

  private val pkgAction = "de.sciss.synth.proc.Action"
  private val pkgCode   = "de.sciss.synth.proc.impl.CodeImpl"
  private val pkgSys    = "de.sciss.lucre.stm"

  // note: synchronous
  private def compileThunk(code: String, w: Wrapper[_, _, _], execute: Boolean)(implicit compiler: Code.Compiler): Any = {
    val impS  = w.imports.map(i => s"  import $i\n").mkString
    val bindS = w.binding.fold("")(i =>
      s"""  val __context__ = $pkgCode.$i.__context__
         |  import __context__._
         |""".stripMargin)
    val aTpe  = w.blockTag // .tpe.toString
    val synth =
      s"""$pkgCode.Run[$aTpe]($execute) {
         |$impS
         |$bindS
         |
        |""".stripMargin + code + "\n}"

    compiler.interpret(synth, execute = execute && w.blockTag != "Unit")
  }
}