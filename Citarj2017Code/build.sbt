lazy val baseName   = "Citarj2017Code"
lazy val baseNameL  = baseName.toLowerCase

lazy val projectVersion = "0.1.0-SNAPSHOT"

lazy val scalaMetaVersion = "2.0.1"
lazy val fileUtilVersion  = "1.1.3"
lazy val prefuseVersion   = "1.0.1"
lazy val swingPlusVersion = "0.2.4"
lazy val numbersVersion   = "0.1.3"
lazy val pdflitzVersion   = "1.2.2"
lazy val subminVersion    = "0.2.2"
lazy val desktopVersion   = "0.8.0"

lazy val commonSettings = Seq(
  version            := projectVersion,
  organization       := "de.sciss",
  homepage           := Some(url(s"https://github.com/Sciss/$baseName")),
  description        := "Code for an article",
  licenses           := Seq("GPL v3+" -> url("http://www.gnu.org/licenses/gpl-3.0.txt")),
  scalaVersion       := "2.12.4",
  resolvers          += "Oracle Repository" at "http://download.oracle.com/maven",  // required for sleepycat
  scalacOptions     ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8", "-Xfuture", "-Xlint:-stars-align,_")
)

// ---- sub-projects ----

lazy val root = Project(id = baseNameL, base = file("."))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalameta" %% "scalameta"    % scalaMetaVersion,
      "de.sciss"      %% "fileutil"     % fileUtilVersion,
      "de.sciss"      %  "prefuse-core" % prefuseVersion,
      "de.sciss"      %% "swingplus"    % swingPlusVersion,
      "de.sciss"      %% "numbers"      % numbersVersion,
      "de.sciss"      %% "pdflitz"      % pdflitzVersion,
      "de.sciss"      %  "submin"       % subminVersion,
      "de.sciss"      %% "desktop"      % desktopVersion
    )
  )
