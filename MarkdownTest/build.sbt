scalaVersion := "2.10.3"

retrieveManaged := true

libraryDependencies ++= Seq(
  "org.fusesource.scalamd" %% "scalamd"     % "1.6",    // small foot print markdown to HTML converter
  "net.sf.cssbox"          %  "swingbox"    % "1.0",
  "org.scala-lang"         %  "scala-swing" % scalaVersion.value
)
