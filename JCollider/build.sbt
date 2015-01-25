name             := "JCollider"

version          := "1.0.0"

organization     := "de.sciss"

description      := "A Java client for SuperCollider"

homepage         := Some(url("https://github.com/Sciss/" + name.value))

licenses         := Seq("LGPL v2.1+" -> url("http://www.gnu.org/licenses/lgpl-2.1.txt"))

scalaVersion     := "2.11.5"

crossPaths       := false      // this is just a Java only project

autoScalaLibrary := false      // this is just a Java only project

lazy val basicJavaOptions = Seq("-source", "1.6", "-encoding", "utf8")

javacOptions    ++= basicJavaOptions ++ Seq("-target", "1.6", "-Xlint")

javacOptions in (Compile, doc) := basicJavaOptions

libraryDependencies ++= Seq(
  "de.sciss" % "netutil"  % "1.0.0",
  "de.sciss" % "scisslib" % "1.0.0"
)

mainClass in (Compile, run) := Some("de.sciss.jcollider.JCollider")

// ---- publishing ----

publishMavenStyle := true

publishTo :=
  Some(if (isSnapshot.value)
    "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  else
    "Sonatype Releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
  )

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := { val n = name.value
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
