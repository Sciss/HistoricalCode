enablePlugins(ScalaJSPlugin)

name            := "NBodyJS"
scalaVersion    := "2.12.4"
scalacOptions  ++= Seq("-deprecation", "-feature")
licenses        := Seq("MIT" -> url("https://raw.githubusercontent.com/Sciss/NBodyJS/master/LICENSE.txt"))

scalaJSUseMainModuleInitializer := true

libraryDependencies ++= Seq(
  "org.scala-js"  %%% "scalajs-dom" % "0.9.5",
  "com.lihaoyi"   %%% "utest"       % "0.6.3" % "test"
)

testFrameworks += new TestFramework("utest.runner.Framework")
