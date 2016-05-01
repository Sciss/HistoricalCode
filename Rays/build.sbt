name := "ScalaPT"

version := "1.0"

scalaVersion := "2.11.8"

val circeVersion = "0.4.1"

libraryDependencies ++= Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies ++= Seq(
  "de.sciss" %% "numbers"  % "0.1.1",
  "de.sciss" %% "fileutil" % "1.1.1"
)

mainClass in Compile := Some("scalapt.MainFrame")
