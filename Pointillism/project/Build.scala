import sbt._
import Keys._

object Build extends sbt.Build {
  def baseName      = "pointillism"
  def midiVersion   = "0.2.0"
  // def fingerVersion = "1.5.+"
  def chartVersion  = "0.4.2"
  def spireVersion  = "0.7.5"

  lazy val root: Project = Project(
    id            = baseName,
    base          = file("."),
    aggregate     = Seq(core, rhythm, views, chart),
    dependencies  = Seq(core, rhythm, views, chart), // i.e. root = full sub project. if you depend on root, will draw all sub modules.
    settings      = Project.defaultSettings ++ Seq(
      publishArtifact in (Compile, packageBin) := false, // there are no binaries
      publishArtifact in (Compile, packageDoc) := false, // there are no javadocs
      publishArtifact in (Compile, packageSrc) := false  // there are no sources
    )
  )

  lazy val core = Project(
    id            = baseName + "-core",
    base          = file("core"),
    settings      = Project.defaultSettings /* ++ buildInfoSettings */ ++ Seq(
      libraryDependencies ++= Seq(
        "de.sciss" %% "scalamidi" % midiVersion
      )
    )
  )

  lazy val rhythm = Project(
    id            = baseName + "-rhythm",
    base          = file("rhythm"),
    dependencies  = Seq(core),
    settings      = Project.defaultSettings /* ++ buildInfoSettings */ ++ Seq(
      libraryDependencies ++= Seq(
        "org.spire-math" %% "spire" % spireVersion   // e.g. Rational numbers
      )
    )
  )

  lazy val views = Project(
    id            = baseName + "-views",
    base          = file("views"),
    dependencies  = Seq(core),
    settings      = Project.defaultSettings /* ++ Seq(
      libraryDependencies += "de.sciss" %% "fingertree" % fingerVersion
    ) */
  )

  lazy val chart = Project(
    id            = baseName + "-chart",
    base          = file("chart"),
    dependencies  = Seq(core),
    settings      = Project.defaultSettings ++ Seq(
      libraryDependencies += "com.github.wookietreiber" %% "scala-chart" % chartVersion
    )
  )
}
