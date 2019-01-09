lazy val commonSettings = Seq(
  name          := "scala-native-test",
  version       := "0.1.0-SNAPSHOT",
  organization  := "de.sciss",
  scalaVersion  := "2.11.12",
  licenses      := Seq("LGPL v2.1+" -> url("http://www.gnu.org/licenses/lgpl-2.1.txt"))
)

lazy val root = project.in(file("."))
  .enablePlugins(ScalaNativePlugin)
  .settings(commonSettings)
  .settings(
    // scala-native doesn't support this (issue #206)
//    mainClass in (Compile, run) := Some("de.sciss.jacktest.SimpleClient"),
//    nativeVerbose := true,
//    nativeClangOptions ++= Seq("/usr/lib/x86_64-linux-gnu/libjack.so") // Seq("-O2", "-v")
  )
