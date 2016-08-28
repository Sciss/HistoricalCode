name                  := "jogl-test"
version               := "0.1.0-SNAPSHOT"
organization          := "de.sciss"
scalaVersion          := "2.11.8"
description           := "Sandbox trying to understand movie playback using JOGL"
homepage              := Some(url(s"https://github.com/Sciss/${name.value}"))
licenses              := Seq("LGPL v2.1+" -> url("http://www.gnu.org/licenses/lgpl-2.1.txt"))

lazy val joglVersion = "2.3.2"

libraryDependencies ++= Seq(
  "org.jogamp.jogl"    % "jogl-all" % joglVersion,
  "org.jogamp.gluegen" % "gluegen"  % joglVersion
)

scalacOptions       ++= Seq("-deprecation", "-unchecked", "-feature", "-Xfuture", "-encoding", "utf8")
