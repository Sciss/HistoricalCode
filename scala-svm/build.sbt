name            := "ScalaSVM"

version         := "0.1.0-SNAPSHOT"

organization    := "de.sciss"

description     := "A Scala implementation of SVM"

homepage        := Some(url("https://github.com/Sciss/" + name.value))

licenses        := Seq("LGPL v3+" -> url("http://www.gnu.org/licenses/lgpl-3.0.txt"))

scalaVersion    := "2.10.3"

retrieveManaged := true

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

libraryDependencies ++= Seq(
  "de.sciss"       %% "fileutil"    % "1.1.+",
  "org.scala-lang" %  "scala-swing" % scalaVersion.value % "test",
  "org.scalatest"  %% "scalatest"   % "1.9.1"            % "test",
  "junit"          %  "junit"       % "4.10"             % "test"
)

initialCommands in console := """import de.sciss.svm._"""