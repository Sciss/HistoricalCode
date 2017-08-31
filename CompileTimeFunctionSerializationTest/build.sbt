scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "sourcecode" % "0.1.4"
)

scalacOptions += "-Yrangepos"   // people say I need this; no idea why