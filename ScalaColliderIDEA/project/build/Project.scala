import sbt._

class Project( info: ProjectInfo ) extends DefaultProject( info ) {
   val scalaCollider       = "de.sciss" %% "scalacollider" % "0.23"
   val scalaColliderSwing  = "de.sciss" %% "scalacolliderswing" % "0.25"
}
