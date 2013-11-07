name            := "scala-svm"

version         := "0.1.0-SNAPSHOT"

organization    := "me.iamzsx"

description     := "A Scala implementation of SVM"

homepage        := Some(url("https://github.com/Sciss/" + name.value))

licenses        := Seq("MIT" -> url("http://opensource.org/licenses/MIT"))

scalaVersion    := "2.10.3"

retrieveManaged := true

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "1.9.1" % "test",
  "junit" % "junit" % "4.10"
)

initialCommands in console := """import me.iamzsx.scala.svm._"""