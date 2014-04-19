// name               := "org.scala-refactoring.library"

name               := "ScalaRefactoring"   // in order to better understand we publish a different version

// version            := "0.6.2-SNAPSHOT"

version            := "0.1.0" // in order to better understand we publish a different version

scalaVersion       := "2.11.0"

// moduleName         := name.value

// ensure the Java classes use 1.6 compatible file format, even when compiling on JDK 7 or higher
javacOptions in (Compile, compile) ++= Seq("-source", "1.6", "-target", "1.6")

// organization       := "org.scala-refactoring"

organization       := "de.sciss" // in order to publish to our sonatype account

crossScalaVersions := Seq("2.11.0", "2.10.4")

publishMavenStyle  := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomExtra := (
 <url>http://scala-refactoring.org</url>
  <licenses>
    <license>
      <name>Scala License</name>
      <url>http://www.scala-lang.org/node/146</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <connection>scm:git:https://github.com/scala-ide/scala-refactoring.git</connection>
    <developerConnection>scm:git:git@github.com:scala-ide/scala-refactoring.git</developerConnection>
    <tag>master</tag>
    <url>https://github.com/scala-ide/scala-refactoring</url>
  </scm>
  <developers>
    <developer>
      <id>misto</id>
      <name>Mirko Stocker</name>
      <email>me@misto.ch</email>
    </developer>
  </developers>)

// credentials += Credentials(Path.userHome / ".m2" / "credentials")

libraryDependencies +=
  "org.scala-lang" % "scala-compiler" % scalaVersion.value

libraryDependencies += "com.novocode" % "junit-interface" % "0.10" % "test"

parallelExecution in Test := false
