name            := "LucreMacro"

organization    := "de.sciss"

version         := "0.1.0-SNAPSHOT"

scalaVersion    := "2.11.0-RC1"

// retrieveManaged := true

scalacOptions  ++= Seq("-unchecked", "-deprecation", "-feature")

libraryDependencies ++= Seq(
  // "de.sciss"        %% "lucrestm-core" % "2.0.+",
  "org.scala-lang"   % "scala-reflect" % scalaVersion.value
)

// resolvers += Resolver.sonatypeRepo("snapshots")

addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.0-M3" cross CrossVersion.full)
