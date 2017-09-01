scalaVersion in ThisBuild := "2.12.3"

// people say I need Yrangepos; no idea why
scalacOptions in ThisBuild ++= Seq("-Yrangepos", "-deprecation")

lazy val root   = project.in(file("")).aggregate(macros, core)

lazy val macros = project.in(file("macros"))
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value
    )
  )

lazy val core = project.in(file("core"))
  .dependsOn(macros)
  .settings( 
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "sourcecode" % "0.1.4"
    )
  )

