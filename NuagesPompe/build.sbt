name := "nuagespompe"

version := "0.10-SNAPSHOT"

organization := "de.sciss"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq(
   "de.sciss" %% "wolkenpumpe" % "0.30-SNAPSHOT",
   "de.sciss" %% "scalacolliderswing" % "0.30-SNAPSHOT"
//   "de.sciss" %% "fscapejobs" % "0.16"
)

retrieveManaged := true

scalacOptions += "-deprecation"

// ---- publishing ----

// publishTo := Some(ScalaToolsReleases)
// 
// pomExtra :=
// <licenses>
//   <license>
//     <name>GPL v2+</name>
//     <url>http://www.gnu.org/licenses/gpl-2.0.txt</url>
//     <distribution>repo</distribution>
//   </license>
// </licenses>
// 
// credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

