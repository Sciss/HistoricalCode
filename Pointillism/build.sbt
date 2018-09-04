val baseName      = "Pointillism"
val baseNameL     = baseName.toLowerCase
val midiVersion   = "0.2.0"
val chartVersion  = "0.4.2"
val spireVersion  = "0.7.5"

lazy val commonSettings = Seq(
  version             := "0.3.1-SNAPSHOT",
  organization        := "de.sciss",
  scalaVersion        := "2.11.12",
  crossScalaVersions  := Seq("2.11.12", "2.10.6"),
  description         := "A library for working with notated (instrumental) music",
  homepage            := Some(url(s"https://git.iem.at/sciss/$baseName")),
  licenses            := Seq("LGPL v2.1+" -> url("http://www.gnu.org/licenses/lgpl-2.1.txt")),
  scalacOptions     ++= Seq("-deprecation", "-unchecked", "-feature", "-Xfuture"),
  initialCommands in console :=
    """import de.sciss.midi
      |import at.iem.point.illism._
      |""".stripMargin
) ++ publishSettings

// ---- publishing ----
lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishTo := {
    Some(if (isSnapshot.value)
      "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    else
      "Sonatype Releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
    )
  },
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  pomExtra := { val n = name.value
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
)

lazy val root = project.withId(baseName).in(file("."))
  .aggregate(core, rhythm, views, chart)
  .dependsOn(core, rhythm, views, chart)  // i.e. root = full sub project. if you depend on root, will draw all sub modules.
  .settings(commonSettings)
  .settings(
    name := baseName,
    publishArtifact in (Compile, packageBin) := false, // there are no binaries
    publishArtifact in (Compile, packageDoc) := false, // there are no javadocs
    publishArtifact in (Compile, packageSrc) := false  // there are no sources
  )

lazy val core = project.withId(s"$baseName-core").in(file("core"))
  .settings(commonSettings)
  .settings(
    name := s"$baseName-core",
    libraryDependencies ++= Seq(
      "de.sciss" %% "scalamidi" % midiVersion
    )
  )

lazy val rhythm = project.withId(s"$baseName-rhythm").in(file("rhythm"))
  .dependsOn(core)
  .settings(commonSettings)
  .settings(
    name := s"$baseName-rhythm",
    libraryDependencies ++= Seq(
      "org.spire-math" %% "spire" % spireVersion   // e.g. Rational numbers
    )
  )

lazy val views = project.withId(s"$baseName-views").in(file("views"))
  .dependsOn(core)
  .settings(commonSettings)
  .settings(
    name := s"$baseName-views"
  )

lazy val chart = project.withId(s"$baseName-chart").in(file("chart"))
  .dependsOn(core)
  .settings(commonSettings)
  .settings(
    name := s"$baseName-chart",
    libraryDependencies += "com.github.wookietreiber" %% "scala-chart" % chartVersion
  )
