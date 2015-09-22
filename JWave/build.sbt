name              := "JWave"

description       := "A Fourier and Wavelet transform library for Java"

homepage          := Some(url("https://github.com/cscheiblich/JWave"))

licenses          := Seq("MIT License" -> url("https://github.com/cscheiblich/JWave/blob/master/LICENSE.md"))

lazy val commonJava  = Seq("-source", "1.7")
javacOptions        := commonJava ++ Seq("-target", "1.7", "-g")
javacOptions in doc := commonJava

libraryDependencies += "junit" % "junit" % "4.12" % "test"


autoScalaLibrary  := false
crossPaths        := false
scalaVersion      := "2.11.7"

// ---- publishing ----

version           := "1.0.0"
organization      := "de.sciss"   // requirement to publish from my Sonatype account

publishMavenStyle := true

publishTo := {
  Some(if (isSnapshot.value)
    "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  else
    "Sonatype Releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
  )
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := { val n = name.value
  <scm>
    <url>git@github.com:Sciss/{n}.git</url>
    <connection>scm:git:git@github.com:Sciss/{n}.git</connection>
  </scm>
    <developers>
      <developer>
        <id>cscheiblich</id>
        <name>Christian Scheiblich</name>
        <url>https://github.com/cscheiblich</url>
      </developer>
      <developer>
        <id>sciss</id>
        <name>Hanns Holger Rutz</name>
        <url>http://www.sciss.de</url>
      </developer>
    </developers>
}
