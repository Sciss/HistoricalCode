import sbt._
import Keys._

object BuildSettings {
   val buildOrganization = "de.sciss"
   val buildVersion      = "0.10"
   val buildScalaVersion = "2.9.1"

   val buildSettings = Defaults.defaultSettings ++ Seq(
      organization := buildOrganization,
      version      := buildVersion,
      scalaVersion := buildScalaVersion
  )
}

object PluginBuild extends Build {
   import BuildSettings._

   lazy val root = Project(
      id    = "transactional",
      base  = file( "." )
   ).aggregate( annotation, plugin, examples )

   lazy val annotation = Project(
      "annotation",
      file( "annotation" ),
      settings = buildSettings ++ Seq(
         libraryDependencies <++= scalaVersion { sv => Seq(
            "org.scala-tools" %% "scala-stm" % "0.3"
         )}
      )
   )

   lazy val plugin = Project(
      "plugin",
      file( "plugin" ),
      settings = buildSettings ++ Seq(
//      resolvers := allResolvers,
         libraryDependencies <++= scalaVersion { sv => Seq(
            "org.scala-lang" % "scala-compiler" % sv,
            "org.scala-lang" % "scala-library" % sv
         )}
      )
   ).dependsOn( annotation )

   lazy val examples = Project(
      id    = "examples",
      base  = file( "examples" )
   ).aggregate( simpleExamples )
  
  lazy val simpleExamples = Project(
      "simpleExamples",
      file( "examples/simple" ),
      settings = buildSettings ++ Seq(
//       resolvers := allResolvers,
//       libraryDependencies += "org.scala-tools.testing" %% "specs" % "1.6.9-SNAPSHOT",
         scalacOptions <+= (packagedArtifact in Compile in plugin in packageBin).map(
            jar => "-Xplugin:%s" format jar._2.getAbsolutePath
         ),
         scalacOptions ++= Seq(
//            "-verbose",
//          "-usejavacp",
//          "-nobootcp",
//          "-Xprint:lazyvals",
//          "-Ylog:lambdalift",
//          "-Ydebug",
//            "-Yshow-syms",
            "-Ycheck:txn.annotations",
//          "-Ycheck:lazyvals"
//          "-Ybrowse:lazyvals"
//          "-Yshow-trees"
//          "-Xplugin-list"
//            "-Xshow-phases",
            "-Xprint:txn.annotations",
            "-Ylog:txn.annotations",
            "-Xplugin-require:transactional"
         )
      )
   ).dependsOn( annotation, plugin )
}