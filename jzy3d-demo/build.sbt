lazy val jzy2dVersion = "0.9.1"

lazy val arch = "linux.x86_64" // "windows-amd64" "windows-i586" "linux-amd64" "linux-i586" "macosx"

lazy val root = project.in(file("."))
  .settings(
    name                 := "jzy-demo",
    version              := "0.1.0-SNAPSHOT",
    scalaVersion         := "2.12.5",
    scalacOptions       ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8", "-Xfuture", "-Xlint:-stars-align,_"),
    resolvers            += "jzv3d releases" at "http://maven.jzy3d.org/releases",
    libraryDependencies ++= Seq(
      "org.jzy3d" % "jzy3d-api"             % jzy2dVersion,
      "org.jzy3d" % "jzy3d-convexhull-awt"  % jzy2dVersion,
      "org.jzy3d" % "jzy3d-depthpeeling"    % jzy2dVersion,
      "org.jzy3d" % "jzy3d-extensions"      % jzy2dVersion,
      "org.jzy3d" % "jzy3d-g2d"             % jzy2dVersion,
      "org.jzy3d" % "jzy3d-jdt-core"        % jzy2dVersion,
      "org.jzy3d" % "jzy3d-master"          % jzy2dVersion,
      "org.jzy3d" % "jzy3d-svm-mapper"      % jzy2dVersion,
      "org.jzy3d" % "jzy3d-swt"             % jzy2dVersion,
      "org.jzy3d" % "jzy3d-tools-libsvm"    % jzy2dVersion,
      "org.eclipse.swt" % s"org.eclipse.swt.gtk.$arch" % "4.3"  // override the version to find it on Maven Central
    )
  )
