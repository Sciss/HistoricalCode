package de.sciss.idea

import java.awt.event.{ActionListener, ActionEvent}
import com.intellij.openapi.wm.{ToolWindow, ToolWindowFactory}
import com.intellij.openapi.project.Project
import com.intellij.ui.content.ContentFactory
import javax.swing.JPanel

class TestToolWindowFactory extends ToolWindowFactory {
   private var myToolWindowContent : JPanel = null
   private var wo = Option.empty[ ToolWindow ]

//   hideToolWindowButton.addActionListener( new ActionListener {
//       def actionPerformed( e: ActionEvent ) {
//         wo.foreach( _.hide( null ))
//       }
//   })
//   refreshToolWindowButton.addActionListener( new ActionListener {
//       def actionPerformed( e: ActionEvent ) {
//          println( "refresh?" )
//       }
//   })

   def createToolWindowContent( p: Project, w: ToolWindow ) {
      wo = Some( w )
      println( "createToolWindowContent" )
       val contentFactory = ContentFactory.SERVICE.getInstance()
       val content = contentFactory.createContent( myToolWindowContent, "", false )
       w.getContentManager().addContent( content )
   }
}