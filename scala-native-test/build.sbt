import scala.scalanative.sbtplugin.ScalaNativePlugin

lazy val commonSettings = Seq(
  name          := "scala-native-test",
  version       := "0.1.0-SNAPSHOT",
  organization  := "de.sciss",
  scalaVersion  := "2.11.8"
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
    nativeVerbose := true,
    nativeClangOptions ++= Nil // Seq("-O2")
  )
