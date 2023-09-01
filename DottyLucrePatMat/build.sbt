lazy val root = project.in(file("."))
  .settings(
    scalaVersion := "3.0.0-M1",
    crossScalaVersions := Seq("2.12.12", "2.13.3", "3.0.0-M1"),
    scalacOptions ++= Seq(
      "-deprecation", "-unchecked", "-feature", "-encoding", "utf8", "-Xlint", "-Xsource:2.13", "-Wvalue-discard",
    ),
  )
