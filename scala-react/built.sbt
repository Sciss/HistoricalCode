name := "scala-react"

organization := "github.com.ingoem"

version := "1.0"

scalaVersion := "2.10.2"

addCompilerPlugin("org.scala-lang.plugins" % "continuations" % "2.10.2")

scalacOptions ++= Seq(
	"-deprecation",
	"-unchecked",
	"-P:continuations:enable"
)

libraryDependencies ++= Seq(
	"org.scalatest" %% "scalatest" % "2.0.M6-SNAP8" % "test",
	"junit" % "junit" % "4.10" % "test"
)

initialCommands in console := 
  """object dom extends scala.react.Domain {
    |  val engine    = new Engine
    |  val scheduler = new ManualScheduler // ThreadPoolScheduler
    |}
    |import dom._
    |dom.start()""".stripMargin

