name in ThisBuild := "play-json-sealed"

version in ThisBuild := "0.0.1-SNAPSHOT"

organization in ThisBuild := "de.sciss"

scalaVersion in ThisBuild := "2.10.2"

description in ThisBuild := "Automatic sealed traits JSON format for play-json-alone"

homepage in ThisBuild <<= name { n => Some(url("https://github.com/Sciss/" + n)) }

licenses in ThisBuild := Seq("LGPL v2.1+" -> url("http://www.gnu.org/licenses/lgpl-2.1.txt"))

resolvers in ThisBuild += "Mandubian repository snapshots" at "https://github.com/mandubian/mandubian-mvn/raw/master/snapshots/"

libraryDependencies in ThisBuild ++= Seq(
  "play" %% "play-json" % "2.2-SNAPSHOT"
)

retrieveManaged in ThisBuild := true

scalacOptions in ThisBuild ++= Seq("-deprecation", "-unchecked", "-feature")

// ---- publishing ----

publishMavenStyle in ThisBuild := true

publishTo in ThisBuild <<= version { v =>
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
  <url>git@github.com:Sciss/{n}.git</url>
  <connection>scm:git:git@github.com:Sciss/{n}.git</connection>
</scm>
<developers>
  <developer>
    <id>sciss</id>
    <name>Hanns Holger Rutz</name>
    <url>http://www.sciss.de</url>
  </developer>
</developers>
}
