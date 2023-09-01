package laminartest

import com.raquo.laminar.api.L.{Button => _, Label => _, _}
import org.scalajs.dom

object Test {
  def main(args: Array[String]): Unit = {
    documentEvents.onDomContentLoaded.foreach { _ =>
      run()
    } (unsafeWindowOwner)
  }

  def run(): Unit = {
    val appContainer: dom.Element = dom.document.body // .querySelector("#appContainer")

    val b1      = new Button("One")
    val b2      = new Button("Two")
    b2.enabled  = false
    b1.clicked(b2.text = "clicked")
    val panel = new FlowPanel(new Label("Button:"), b1, b2, new Label("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"))

    /*val root: RootNode =*/ render(appContainer, panel.peer)
  }
}
