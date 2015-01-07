name            := "LucreMacros"

organization    := "de.sciss"

version         := "0.1.0-SNAPSHOT"

scalaVersion    := "2.11.4"

scalacOptions  ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfuture", "-encoding", "utf8")

libraryDependencies ++= Seq(
  "org.scala-lang"   % "scala-reflect" % scalaVersion.value
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)
