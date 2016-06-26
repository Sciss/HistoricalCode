import scala.scalanative.sbtplugin.ScalaNativePlugin

lazy val commonSettings = Seq(
  name          := "scala-native-test",
  version       := "0.1.0-SNAPSHOT",
  organization  := "de.sciss",
  scalaVersion  := "2.11.8",
  licenses      := Seq("LGPL v2.1+" -> url("http://www.gnu.org/licenses/lgpl-2.1.txt"))
)

autoCompilerPlugins := true

lazy val scalaNativeVersion = "0.1-SNAPSHOT"
lazy val toolScalaVersion   = "2.10"

lazy val platformSettings = Seq(
  libraryDependencies ++= Seq(
    compilerPlugin("org.scala-native" %  s"tools_$toolScalaVersion" % scalaNativeVersion),
    compilerPlugin("org.scala-native" %  s"nir_$toolScalaVersion"   % scalaNativeVersion),
    compilerPlugin("org.scala-native" %  s"util_$toolScalaVersion"  % scalaNativeVersion),
    //
    "org.scala-native" %% "nativelib" % scalaNativeVersion,
    "org.scala-native" %% "javalib"   % scalaNativeVersion,
    "org.scala-native" %% "scalalib"  % scalaNativeVersion
  )
)

lazy val root = project.in(file("."))
  .settings(platformSettings)
  .settings(ScalaNativePlugin.projectSettings)
  .settings(commonSettings)
  .settings(
    // scala-native doesn't support this (issue #206)
//    mainClass in (Compile, run) := Some("de.sciss.jacktest.SimpleClient"),
    nativeVerbose := true,
    nativeClangOptions ++= Seq("/usr/lib/x86_64-linux-gnu/libjack.so") // Seq("-O2", "-v")
  )
