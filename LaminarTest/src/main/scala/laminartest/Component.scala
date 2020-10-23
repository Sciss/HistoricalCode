package laminartest

import com.raquo.laminar.nodes.ReactiveElement.Base

trait Component {
  def peer: Base

  var enabled: Boolean
}
