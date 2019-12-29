package de.sciss.itunes

import scala.swing.{MainFrame, Orientation, SplitPane, Table}

class MainWindow(albums: Seq[AlbumInfo]) extends MainFrame {
  private val at = AlbumTable()
  private val tt = TrackTable()

  at.albums = albums
  at.table.selection.intervalMode = Table.IntervalMode.Single
  at.table.sort(1)
  at.table.sort(0)

  at.addListener {
    case AlbumTable.Selection(Seq(album)) =>
      tt.tracks = album.tracks
  }

  title     = "Archival"
  contents  = new SplitPane(Orientation.Horizontal, at.component, tt.component)

  pack().centerOnScreen()
  open()
}
