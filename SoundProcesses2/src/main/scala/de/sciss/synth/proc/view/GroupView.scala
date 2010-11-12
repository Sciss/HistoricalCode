package de.sciss.synth.proc.view

import de.sciss.synth.proc.ProcGroup
import javax.swing.JFrame

class GroupView[ C ]( g: ProcGroup[ C ]) {
   val frame = {
      val f = new JFrame( "Group" )
      f
   }
}