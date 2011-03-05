package de.sciss.idea;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: hhrutz
 * Date: 25/02/2011
 * Time: 19:14
 * To change this template use File | Settings | File Templates.
 */
public class TestAppComp implements ApplicationComponent {
    private boolean check1;
    private boolean check2;

    public TestAppComp() {
    }

    public void initComponent() {
        // TODO: insert component initialization logic here
//        Messages.showMessageDialog( "Welcome to IntelliJ IDEA Huhu!\nToday: " + new java.util.Date(), "Test", null );
    }

    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    @NotNull
    public String getComponentName() {
        return "TestAppComp";
    }

    public boolean isCheck1() {
        return check1;
    }

    public void setCheck1(final boolean check1) {
        this.check1 = check1;
    }

    public boolean isCheck2() {
        return check2;
    }

    public void setCheck2(final boolean check2) {
        this.check2 = check2;
    }
}
