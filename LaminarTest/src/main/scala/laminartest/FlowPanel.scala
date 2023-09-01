package laminartest

import com.raquo.laminar.nodes.ReactiveElement.Base

import com.raquo.laminar.api.L._
import org.scalajs.dom

class FlowPanel(contents: Component*) extends Component {
  var enabled = true

  lazy val peer: Base = {
    val childPeers = contents.map(_.peer)
    div(
      childPeers: _*,
    ).amend(
      cls := "lucre-flow-panel"
    )
  }
}
