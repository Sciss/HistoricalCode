scalaVersion := "2.11.0-M7"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value

addCompilerPlugin("org.scala-lang.plugins" % "macro-paradise" % "2.0.0-SNAPSHOT" cross CrossVersion.full)

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")