lazy val baseName   = "LucreLab"
lazy val baseNameL  = baseName.toLowerCase

version in ThisBuild := "0.1.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.7"

scalacOptions in ThisBuild ++= Seq("-feature", "-unchecked", "-deprecation", "-Xfuture", "-encoding", "utf8")

lazy val root = Project(id = baseNameL, base = file(".")).aggregate(macros, core, example)

lazy val macros = Project(id = s"$baseNameL-macros" , base = file("macros"))
  .settings(
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
  )

lazy val core = Project(id = s"$baseNameL-core", base = file("core")).dependsOn(macros)

lazy val example = Project(id = s"$baseNameL-example", base = file("example")).dependsOn(core)

// XXX TODO -- how to add this only to `macros`?
addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)
