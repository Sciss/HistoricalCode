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
    base      = file("core"),
    settings  = Project.defaultSettings ++ Seq(
      // needs paradise for quasi-quotes
      resolvers += Resolver.sonatypeRepo("snapshots"),
      addCompilerPlugin("org.scala-lang.plugins" % "macro-paradise" % "2.0.0-SNAPSHOT" cross CrossVersion.full)
    )
  )

  lazy val test = Project(
    id            = "test",
    base          = file("test"),
    dependencies  = Seq(core)
  )
}
