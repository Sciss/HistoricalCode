package de.sciss.idea.windows

import java.awt.event.{ActionListener, ActionEvent}
import com.intellij.openapi.wm.{ToolWindow, ToolWindowFactory}
import com.intellij.openapi.project.Project
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import javax.swing.{JLabel, JPanel}
import de.sciss.synth.swing.ServerStatusPanel

class OnlineWindowFactory extends ToolWindowFactory {
   private val panel = {
      val p = new JPanel()
      p.setLayout( new BorderLayout() )
//      p.add( new JLabel( "Jo chuck" ), BorderLayout.CENTER )
      p.add( new ServerStatusPanel(), BorderLayout.NORTH )
      p
   }

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
//      println( "createToolWindowContent" )
      val contentFactory = ContentFactory.SERVICE.getInstance()
      val content = contentFactory.createContent( panel, "", false )
      w.getContentManager().addContent( content )
   }
}