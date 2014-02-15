package de.sciss.markdowntest

import org.fusesource.scalamd.Markdown
import scala.swing.{ScrollPane, Orientation, SplitPane, Component, MainFrame, Swing, EditorPane, Frame, SimpleSwingApplication}
import Swing._
import org.fit.cssbox.swingbox.SwingBoxEditorKit

object Main extends SimpleSwingApplication {
  val bodyUrl = "https://raw2.github.com/Sciss/ScalaCollider/master/README.md"
  val cssUrl  = "https://gist.github.com/andyferra/2554919/raw/2e66cabdafe1c9a7f354aa2ebf5bc38265e638e5/github.css"

  // is this the best way?
  def readURL(url: String): String = io.Source.fromURL(url, "UTF-8").getLines().mkString("\n")

  lazy val top: Frame = {
    val md    = readURL(bodyUrl)
    val css   = readURL(cssUrl )
    val html  = Markdown(md)
    // println(html)

    def mkPane(txt: String, fun: EditorPane => Unit = _ => ()): Component = {
      val p = new EditorPane("text/html", "") {
        editable  = false
        fun(this)
        text      = txt

        // bullet points look shitty, but this does not help:
        //        override def paintComponent(g: Graphics2D): Unit = {
        //          g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        //          super.paintComponent(g)
        //        }
      }
      new ScrollPane(p)
    }

    val pane1 = mkPane(html, _.border = Swing.EmptyBorder(8))

    //    val pane2j = new BrowserPane
    //    // pane2j.setText(html)
    //    // SwingBoxDocument
    //    pane2j.setPage("https://github.com/Sciss/ScalaCollider/blob/master/README.md")
    //    val pane2 = Component.wrap(pane2j)

    val html1 = s"<html><head><style>$css</style></head><body>$html</body></html>"
    val pane2 = mkPane(html1, _.editorKit = new SwingBoxEditorKit)

    val split = new SplitPane(Orientation.Vertical, pane1, pane2)

    new MainFrame {
      title = "Markdown Test"
      contents = split
      pack()
      split.dividerLocation = 0.5
      // size = (800, 800)
      centerOnScreen()
      open()
    }
  }
}
