lazy val commonSettings = Seq(
  name              := "iTunesArchival",
  organization      := "de.sciss",
  version           := "0.1.0-SNAPSHOT",
  scalaVersion      := "2.13.1",
  licenses          := Seq("GPL v3+" -> url("http://www.gnu.org/licenses/gpl-3.0.txt")),
  description       := "Personal utility for making backups",
  scalacOptions    ++= Seq("-deprecation", "-unchecked", "-feature", "-Xsource:2.13", "-encoding", "utf8"),
  libraryDependencies ++= Seq(
    "de.sciss"                %%  "desktop"   % "0.10.5",
    "de.sciss"                %%  "fileutil"  % "1.1.3",
    "de.sciss"                %   "submin"    % "0.3.4",
    "de.sciss"                %%  "kollflitz" % "0.2.3",
    "org.scala-lang.modules"  %%  "scala-xml" % "1.2.0",
  )
)

lazy val root = project.in(file("."))
  .settings(commonSettings)

