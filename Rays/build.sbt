name            := "Rays"
version         := "0.1.0-SNAPSHOT"
scalaVersion    := "2.11.8"
organization    := "de.sciss"
licenses        := Seq("LGPL v2.1+" ->  url("http://www.gnu.org/licenses/lgpl-2.1.txt"))
homepage        := Some(url(s"https://www.github.com/Sciss/${name.value}"))
description     := "Global illumination library for Scala"
scalacOptions  ++= Seq("-deprecation", "-unchecked", "-feature", "-Xfuture", "-encoding", "utf8")

val circeVersion = "0.4.1"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies ++= Seq(
  "de.sciss" %% "numbers"   % "0.1.1",
  "de.sciss" %% "fileutil"  % "1.1.1",
  "de.sciss" %% "swingplus" % "0.2.1"
)
