import sbt._

class CupolaVideoPlayerProject( info: ProjectInfo ) extends DefaultProject( info ) {
//   val scalaSwing = "org.scala-lang" % "scala-swing" % "2.9.0-1"
   val scalaAudioWidget = "de.sciss" %% "scalaaudiowidgets" % "0.10-SNAPSHOT"
   val scalaOSC         = "de.sciss" %% "scalaosc" % "0.23"
}