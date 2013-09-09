name := "Pointillism"

version in ThisBuild := "0.2.0-SNAPSHOT"

organization in ThisBuild := "de.sciss"

scalaVersion in ThisBuild := "2.10.2"

description in ThisBuild := "A library for working with notated (instrumental) music"

homepage in ThisBuild <<= name { n => Some(url("https://github.com/iem-projects/" + n)) }

licenses in ThisBuild := Seq("GPL v2+" -> url("http://www.gnu.org/licenses/gpl-2.0.txt"))

retrieveManaged in ThisBuild := true

scalacOptions in ThisBuild ++= Seq("-deprecation", "-unchecked", "-feature")

initialCommands in console in ThisBuild :=
"""import de.sciss.midi
  |import at.iem.point.illism._
""".stripMargin

// ---- publishing ----

publishMavenStyle in ThisBuild := true

publishTo in ThisBuild <<= version { (v: String) =>
  Some(if (v endsWith "-SNAPSHOT")
    "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  else
    "Sonatype Releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
  )
}

publishArtifact in Test := false

pomIncludeRepository in ThisBuild := { _ => false }

pomExtra in ThisBuild <<= name { n =>
<scm>
  <url>git@github.com:iem-projects/{n}.git</url>
  <connection>scm:git:git@github.com:iem-projects/{n}.git</connection>
</scm>
<developers>
   <developer>
      <id>sciss</id>
      <name>Hanns Holger Rutz</name>
      <url>http://www.sciss.de</url>
   </developer>
</developers>
}
