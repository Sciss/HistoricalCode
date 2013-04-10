import sbt._
import Keys._

object Build extends sbt.Build {
  def baseName      = "pointillism"
  def midiVersion   = "0.1.+"
  def fingerVersion = "1.4.+"

  lazy val root: Project = Project(
    id            = baseName,
    base          = file("."),
    aggregate     = Seq(core, views),
    dependencies  = Seq(core, views), // i.e. root = full sub project. if you depend on root, will draw all sub modules.
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

  lazy val views = Project(
    id            = baseName + "-views",
    base          = file("views"),
    dependencies  = Seq(core),
    settings      = Project.defaultSettings /* ++ Seq(
      libraryDependencies += "de.sciss" %% "fingertree" % fingerVersion
    ) */
  )
}
