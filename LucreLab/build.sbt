name := "LucreLab"

version in ThisBuild := "1.0"

scalaVersion in ThisBuild := "2.11.4"

scalacOptions in ThisBuild ++= Seq("-feature", "-unchecked", "-deprecation", "-Xfuture", "-encoding", "utf8")

lazy val lucrelab           = project.in(file(".")).aggregate(`lucrelab-core`, `lucrelab-example`)

lazy val `lucrelab-core`    = project.in(file("core"))

lazy val `lucrelab-example` = project.in(file("example"))dependsOn(`lucrelab-core`)
