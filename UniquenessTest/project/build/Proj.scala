import sbt._

class Proj(info: ProjectInfo) extends DefaultProject(info) with AutoCompilerPlugins
{
//  val cont = compilerPlugin("org.scala-lang.plugins" % "unique" % "2.8.1")
  override def compileOptions = super.compileOptions ++ compileOptions("-P:unique:enable")
}