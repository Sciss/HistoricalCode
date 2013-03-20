name := "Pointillism"

version := "0.1.0-SNAPSHOT"

organization := "de.sciss"

scalaVersion := "2.10.1"

description := "A library for working with notated (instrumental) music"

homepage <<= name { n => Some(url("https://github.com/iem-projects/" + n)) }

licenses := Seq("GPL v2+" -> url("http://www.gnu.org/licenses/gpl-2.0.txt"))

libraryDependencies in ThisBuild ++= Seq(
  "de.sciss" %% "scalamidi" % "0.1.+"
)

retrieveManaged := true

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

initialCommands in console :=
"""import de.sciss.midi
  |import at.iem.point.illism._
""".stripMargin

// ---- publishing ----

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  Some(if (v endsWith "-SNAPSHOT")
    "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  else
    "Sonatype Releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
  )
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra <<= name { n =>
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

