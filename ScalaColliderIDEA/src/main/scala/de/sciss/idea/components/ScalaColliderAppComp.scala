package de.sciss.idea.components

import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.ui.Messages

class ScalaColliderAppComp extends ApplicationComponent {
    def initComponent {
        // TODO: insert component initialization logic here
//        Messages.showMessageDialog( "Welcome to //// ScalaCollider", "ScalaCollider", null )
    }

    def disposeComponent {
        // TODO: insert component disposal logic here
    }

    def getComponentName = "ScalaColliderAppComp"
}
