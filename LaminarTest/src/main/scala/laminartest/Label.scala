package laminartest

import com.raquo.laminar.api.L._

class Label(text0: String) extends Component {
  private var _text = text0

  var enabled = true

  def text: String = _text

  def text_=(value: String): Unit =
    if (_text != value) {
      _text = value
//      peer.amend(text = value)
      peer.ref.textContent = value
    }

  lazy val peer: Span = {
    span(text, cls := "lucre-label")
  }
}
