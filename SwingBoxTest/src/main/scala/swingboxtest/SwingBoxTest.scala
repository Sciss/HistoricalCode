package swingboxtest

import scala.swing._
import Swing._

import org.fit.cssbox.swingbox.BrowserPane

object SwingBoxTest extends SimpleSwingApplication {
  lazy val browser = new BrowserPane
  
  lazy val top = new MainFrame {
    title = "SwingBox Test"
    contents = new BorderPanel {
      add(new TextField {
        action = Action(null) {
          browser.setPage(text)
        }
      }, BorderPanel.Position.North)
      add(new ScrollPane(Component.wrap(browser)), BorderPanel.Position.Center)
    }
    size = (640, 480)
    centerOnScreen()
  }
}
