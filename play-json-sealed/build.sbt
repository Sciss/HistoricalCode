lazy val projectName = "play-json-sealed"

name         in ThisBuild := projectName

version      in ThisBuild := "0.2.0"

organization in ThisBuild := "de.sciss"

scalaVersion in ThisBuild := "2.11.1"

crossScalaVersions in ThisBuild := Seq("2.11.1", "2.10.4")

description  in ThisBuild := "Automatic formats for case classes based on Play-JSON"

homepage     in ThisBuild := Some(url("https://github.com/Sciss/" + projectName))

licenses     in ThisBuild := Seq("LGPL v2.1+" -> url("http://www.gnu.org/licenses/lgpl-2.1.txt"))

resolvers    in ThisBuild += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/maven-releases/"

libraryDependencies in ThisBuild ++= {
  val sv = scalaVersion.value
  // Quasiquotes in macro paradise for Scala 2.10 require extra dependency
  val sq0 = if (sv startsWith "2.10") {
    val qq = "org.scalamacros" %% "quasiquotes" % "2.0.0"
    qq :: Nil
  } else Nil
  //
  sq0 ++ Seq(
    "com.typesafe.play" %% "play-json" % "2.3.0",
    "org.scalatest"     %% "scalatest" % "2.2.0" % "test"
  )
}


// retrieveManaged in ThisBuild := true

scalacOptions in ThisBuild ++= Seq("-deprecation", "-unchecked", "-feature", "-Xfuture")

// ---- publishing ----

publishMavenStyle in ThisBuild := true

publishTo in ThisBuild :=
  Some(if (version.value endsWith "-SNAPSHOT")
    "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  else
    "Sonatype Releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
  )

publishArtifact in Test := false

pomIncludeRepository in ThisBuild := { _ => false }

pomExtra in ThisBuild := { val n = projectName
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

