import xml._
import sbt.{ FileUtilities => FU, _}

/**
 *    @version 0.10, 27-Oct-10
 */
class SoundProcesses2Project( info: ProjectInfo ) extends DefaultProject( info ) {
   val scalaCollider    = "de.sciss" %% "scalacollider" % "0.24"
   val temporalObjects  = "de.sciss" %% "temporalobjects" % "0.16" // 0.18
   val scalaSTM         = "org.scala-tools" %% "scala-stm" % "0.3"
   val prefuse          = "prefuse" % "prefuse" % "beta-SNAPSHOT" from "http://github.com/downloads/Sciss/ScalaColliderSwing/prefuse-beta-SNAPSHOT.jar"

   val oracleRepo = "Oracle Repository" at "http://download.oracle.com/maven"

   // override def compileOptions = super.compileOptions ++ Seq(Unchecked)
}