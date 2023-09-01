name := "PicklingTest"

organization := "de.sciss"

scalaVersion := "2.10.3"

libraryDependencies += "org.scala-lang" %% "scala-pickling" % "0.8.0-SNAPSHOT"

resolvers += Resolver.sonatypeRepo("snapshots")

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")
