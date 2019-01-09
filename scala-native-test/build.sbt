lazy val commonSettings = Seq(
  name          := "scala-native-test",
  version       := "0.1.0-SNAPSHOT",
  organization  := "de.sciss",
  scalaVersion  := "2.11.12"
)

lazy val root = project.in(file("."))
  .enablePlugins(ScalaNativePlugin)
  .settings(commonSettings)
  .settings(
    // scala-native doesn't seem to suppor this:
//    mainClass in (Compile, run) := Some("de.sciss.jacktest.SimpleClient"),
//    nativeVerbose := true,
//    nativeClangOptions ++= Seq("/usr/lib/x86_64-linux-gnu/libjack.so") // Seq("-O2", "-v")
  )
