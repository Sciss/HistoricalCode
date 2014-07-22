name := "ConvertKonturToMellite"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "de.sciss" %% "kontur"       % "1.3.0-SNAPSHOT",
  "de.sciss" %% "mellite-core" % "0.9.0-SNAPSHOT",
  "com.github.scopt" %% "scopt" % "3.2.0"
)
