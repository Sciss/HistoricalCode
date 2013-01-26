name := "TxnInterpreter"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.10.0"

libraryDependencies <+= scalaVersion { sv => "org.scala-lang" % "scala-compiler" % sv }

libraryDependencies += "org.scala-stm" %% "scala-stm" % "0.7"

retrieveManaged := true
