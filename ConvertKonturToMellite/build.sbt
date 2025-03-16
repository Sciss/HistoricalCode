lazy val root = project.in(file("."))
  .settings(
    name := "ConvertKonturToMellite",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.11.12",
    resolvers += "Oracle Repository" at "https://download.oracle.com/maven", // required for sleepycat
    libraryDependencies ++= Seq(
      "de.sciss" %% "kontur"       % "1.3.0",
      "de.sciss" %% "mellite-core" % "0.9.0",
      "com.github.scopt" %% "scopt" % "3.2.0"
    )
  )
