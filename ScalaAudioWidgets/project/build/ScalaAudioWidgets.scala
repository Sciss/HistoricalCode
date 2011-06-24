import sbt._

class ScalaAudioWidgetsProject( info: ProjectInfo ) extends DefaultProject( info ) {
   val audioWidgets  = "de.sciss" %% "audiowidgets" % "0.10-SNAPSHOT"
   val scalaSwing    = "org.scala-lang" % "scala-swing" % "2.9.0"
}