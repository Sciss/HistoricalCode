package de.sciss.gui

import j.{LCDPanel => JLCDPanel}
import swing.FlowPanel

class LCDPanel extends FlowPanel {
   override lazy val peer: JLCDPanel = new JLCDPanel with SuperMixin
}