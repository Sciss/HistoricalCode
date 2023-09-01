name := "SubminCreator"

version := "0.0.1"

organization := "de.sciss"

scalaVersion := "2.9.2"

description := "A look-and-feel creator"

homepage := Some( url( "https://github.com/Sciss/SubminCreator" ))

licenses := Seq( "GPL v2+" -> url( "http://www.gnu.org/licenses/gpl-2.0.txt" ))

resolvers += "Oracle Repository" at "http://download.oracle.com/maven"  // required for sleepycat

libraryDependencies ++= Seq(
   "de.sciss" %% "lucredata-core" % "1.4.2+",
   "de.sciss" %% "lucreexpr" % "1.4.+"
)

libraryDependencies <+= scalaVersion { sv =>
   "org.scala-lang" % "scala-swing" % sv
}

retrieveManaged in ThisBuild := true

scalacOptions in ThisBuild ++= Seq( "-deprecation", "-unchecked" )

testOptions in Test += Tests.Argument( "-oDF" )

parallelExecution in ThisBuild := false

// ---- build info ----

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq( name, organization, version, scalaVersion, description,
   BuildInfoKey.map( homepage ) { case (k, opt) => k -> opt.get },
   BuildInfoKey.map( licenses ) { case (_, Seq( (lic, _) )) => "license" -> lic }
)

buildInfoPackage := "de.sciss.submin.creator"

// ---- publishing ----

publishMavenStyle := true

publishTo <<= version { (v: String) =>
   Some( if( v.endsWith( "-SNAPSHOT" ))
      "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
   else
      "Sonatype Releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
   )
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra :=
<scm>
  <url>git@github.com:Sciss/SubminCreator.git</url>
  <connection>scm:git:git@github.com:Sciss/SubminCreator.git</connection>
</scm>
<developers>
   <developer>
      <id>sciss</id>
      <name>Hanns Holger Rutz</name>
      <url>http://www.sciss.de</url>
   </developer>
</developers>

// ---- ls.implicit.ly ----

seq( lsSettings :_* )

(LsKeys.tags in LsKeys.lsync) := Seq( "gui", "swing", "look-and-feel" )

(LsKeys.ghUser in LsKeys.lsync) := Some( "Sciss" )

