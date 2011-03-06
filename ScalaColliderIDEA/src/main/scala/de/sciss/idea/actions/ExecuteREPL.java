//package de.sciss.idea.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

/**
 * Created by IntelliJ IDEA.
 * User: hhrutz
 * Date: 05/03/2011
 * Time: 20:06
 * To change this template use File | Settings | File Templates.
 */
public class ExecuteREPL extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        Messages.showMessageDialog("Welcom  e to !! ScalaCollider", "ScalaCollider", null);
        // TODO: insert action logic here
    }
}
