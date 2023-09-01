package laminartest

import com.raquo.laminar.api.L.{Button => _Button, _}
import org.scalajs.dom

class Button(text0: String) extends Component {
  private var _text     = text0
  private var _enabled  = true

  def enabled: Boolean = _enabled

  def enabled_=(value: Boolean): Unit =
    if (_enabled != value) {
      _enabled = value
      peer.ref.disabled = !value
    }

  def text: String = _text

  def text_=(value: String): Unit =
    if (_text != value) {
      _text = value
//      peer.amend(text = value)
      peer.ref.textContent = value
    }

  lazy val peer: _Button = {
    button(text, cls := "lucre-button", disabled := !enabled)
  }

  def clicked(body: => Unit): Unit = {
    val observer = Observer[dom.MouseEvent](_ => body)
    peer.amend(onClick --> observer)
  }
}
