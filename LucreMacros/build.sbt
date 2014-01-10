name            := "LucreMacro"

organization    := "de.sciss"

version         := "0.1.0-SNAPSHOT"

scalaVersion    := "2.11.0-M7"

retrieveManaged := true

scalacOptions  ++= Seq("-unchecked", "-deprecation", "-feature")

libraryDependencies ++= Seq(
  // "de.sciss"        %% "lucrestm-core" % "2.0.+",
  "org.scala-lang"   % "scala-reflect" % scalaVersion.value
)

resolvers += Resolver.sonatypeRepo("snapshots")

addCompilerPlugin("org.scala-lang.plugins" % "macro-paradise" % "2.0.0-SNAPSHOT" cross CrossVersion.full)