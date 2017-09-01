/*
 *  Code.scala
 *  (SoundProcesses)
 *
 *  Copyright (c) 2010-2017 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.funser

import de.sciss.funser.{CodeImpl => Impl}

import scala.collection.immutable.{IndexedSeq => Vec}
import scala.concurrent.{ExecutionContext, Future, blocking}

object Code {
  final val typeID = 0x20001

  final val UserPackage = "user"

  final case class CompilationFailed() extends Exception
  final case class CodeIncomplete   () extends Exception

  def future[A](fun: => A)(implicit compiler: Code.Compiler): Future[A] = Impl.future(fun)

  def registerImports(id: Int, imports: Seq[String]): Unit = Impl.registerImports(id, imports)

  def getImports(id: Int): Vec[String] = Impl.getImports(id)

  // ---- type ----

  def apply(id: Int, source: String): Code = Impl(id, source)

  def addType(tpe: Type): Unit = Impl.addType(tpe)

  def getType(id: Int): Code.Type = Impl.getType(id)

  trait Type {
    def id: Int

    type Repr <: Code

    private[this] lazy val _init: Unit = Code.addType(this)

    def init(): Unit = _init

    def mkCode(source: String): Repr
  }

  // ---- compiler ----

  def unpackJar(bytes: Array[Byte]): Map[String, Array[Byte]] = Impl.unpackJar(bytes)

  trait Compiler {
    implicit def executionContext: ExecutionContext

    /** Synchronous call to compile a source code consisting of a body which is wrapped in a `Function0` apply method,
      * returning the raw jar file produced in the compilation.
      *
      * May throw `CompilationFailed` or `CodeIncomplete`
      *
      * @param  source  the completely formatted source code to compile which should contain
      *                 a proper package and class definition. It must contain any
      *                 necessary `import` statements.
      * @return the jar file as byte-array, containing the opaque contents of the source code
      *         (possible the one single class defined)
      */
    def compile(source: String): Array[Byte]

    /** Synchronous call to compile and execute the provided source code.
      *
      * May throw `CompilationFailed` or `CodeIncomplete`
      *
      * @param  source  the completely formatted source code to compile which forms the body
      *                 of an imported object. It must contain any necessary `import` statements.
      * @return the evaluation result, or `()` if there is no result value
      */
    def interpret(source: String, execute: Boolean): Any
  }

  // ---- type: Action ----

  object Action extends Type {
    final val id    = 2
    final val name  = "Action"
    type Repr = Action

    def mkCode(source: String): Repr = Action(source)
  }
  final case class Action(source: String) extends Code {
    type In     = String
    type Out    = Array[Byte]
    def id: Int = Action.id

    def compileBody()(implicit compiler: Code.Compiler): Future[Unit] = future(blocking { execute("Unnamed"); () })

    def execute(in: In)(implicit compiler: Code.Compiler): Out = {
      // Impl.execute[In, Out, Action](this, in)
      Impl.compileToFunction(in, this)
    }

    def contextName: String = Action.name

    def updateSource(newText: String): Action = copy(source = newText)

    // def compileToFunction(name: String): Future[Array[Byte]] = Impl.compileToFunction(name, this)
  }

  // ---- expr ----
}
trait Code { me =>
  type Self = Code { type In = me.In; type Out = me.Out }

  /** The interfacing input type */
  type In
  /** The interfacing output type */
  type Out

  /** Identifier to distinguish types of code. */
  def id: Int

  /** Source code. */
  def source: String

  def updateSource(newText: String): Self

  /** Human readable name. */
  def contextName: String

  /** Compiles the code body without executing it. */
  def compileBody()(implicit compiler: Code.Compiler): Future[Unit]

  /** Compiles and executes the code. Returns the wrapped result. */
  def execute(in: In)(implicit compiler: Code.Compiler): Out // = compile()(in)
}