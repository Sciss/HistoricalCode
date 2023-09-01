name := "anemone_551f62e3"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.7"

organization := "de.sciss"

licenses := Seq("gpl v2+" -> url("https://www.gnu.org/licenses/gpl-2.0.txt"))

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8", "-Xfuture")

lazy val audioFileVersion = "1.4.5"
lazy val fileUtilVersion  = "1.1.1"

libraryDependencies ++= Seq(
  "de.sciss" %% "scalaaudiofile" % audioFileVersion,
  "de.sciss" %% "fileutil"       % fileUtilVersion
)
