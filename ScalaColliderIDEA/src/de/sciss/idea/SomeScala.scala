package de.sciss.idea

import com.intellij.openapi.ui.Messages
import com.intellij.openapi.project.Project

class SomeScala( project: Project, txt: String ) {
   Messages.showMessageDialog(project, "Hello, " + txt + "!\n I am glad to see you.", "Information", Messages.getInformationIcon)
}