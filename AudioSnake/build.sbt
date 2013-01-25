name := "AudioSnake"

scalaVersion := "2.10.0"

libraryDependencies ++= Seq(
  "de.sciss" %% "scalaaudiofile" % "1.2.0",
  "de.sciss" %% "contextsnake" % "0.1.0",
  "com.github.scopt" %% "scopt" % "2.1.0"
)

retrieveManaged := true


