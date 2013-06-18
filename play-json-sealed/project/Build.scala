import sbt._
import Keys._

object Build extends sbt.Build {
  lazy val full: Project = Project(
    id        = "full",
    base      = file("."),
    aggregate = Seq(core, test)
  )

  lazy val core = Project(
    id        = "play-json-sealed",
    base      = file("core")
  )

  lazy val test = Project(
    id            = "test",
    base          = file("test"),
    dependencies  = Seq(core)
  )
}
