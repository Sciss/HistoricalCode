lazy val baseName   = "ReimplementSP"
lazy val baseNameL  = baseName.toLowerCase

lazy val commonSettings = Seq(
  name         := baseName,
  version      := "0.1.0-SNAPSHOT",
  description  := "Research on reimplementing sound installations through high-level SoundProcesses",
  organization := "de.sciss",
  homepage     := Some(url(s"https://git.iem.at/sciss/${name.value}")),
  licenses     := Seq("agpl v3+" -> url("http://www.gnu.org/licenses/agpl-3.0.txt")),
  scalaVersion := "2.12.7",
  scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8", "-Xlint", "-Xsource:2.13"),
  updateOptions := updateOptions.value.withLatestSnapshots(false),
  resolvers    += "Oracle Repository" at "http://download.oracle.com/maven"  // required for sleepycat
)

lazy val root = project.in(file("."))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "de.sciss" %% "patterns"        % deps.main.patterns,
      "de.sciss" %% "soundprocesses"  % deps.main.soundProcesses
    )
  )

lazy val deps = new {
  val main = new {
    val patterns       = "0.6.1"
    val soundProcesses = "3.21.3"
  }
}
