lazy val jzy2dVersion = "1.0.2"

lazy val root = project.in(file("."))
  .settings(
    name                 := "jzy-demo",
    version              := "0.1.0-SNAPSHOT",
    scalaVersion         := "2.12.5",
    scalacOptions       ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8", "-Xfuture", "-Xlint:-stars-align,_"),
    resolvers            += "jzv3d releases" at "http://maven.jzy3d.org/releases",
    libraryDependencies ++= Seq(
      "org.jzy3d" % "jzy3d-api" % jzy2dVersion
    )
  )
