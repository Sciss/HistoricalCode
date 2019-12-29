package de.sciss.itunes

import java.util.Comparator

import de.sciss.model.Model
import de.sciss.model.impl.ModelImpl
import javax.swing.table.{AbstractTableModel, DefaultTableCellRenderer, TableCellRenderer, TableRowSorter}
import javax.swing.{JTable, SwingConstants}

import scala.collection.immutable.{Seq => ISeq}
import scala.swing.Table.AutoResizeMode
import scala.swing.event.TableRowsSelected
import scala.swing.{Component, ScrollPane, Table}

object TrackTable {
  def apply(): TrackTable = new Impl()

  sealed trait Update
  final case class Selection(tracks: ISeq[TrackInfo]) extends Update

  private case class Column(idx: Int, name: String, minWidth: Int, prefWidth: Int, maxWidth: Int,
                            extract: TrackInfo => Any, cellRenderer: Option[TableCellRenderer] = None,
                            sorter: Option[Comparator[_]] = None, headerRenderer: Option[TableCellRenderer] = None)

  private val RightAlignedRenderer = {
    val res = new DefaultTableCellRenderer
    res.setHorizontalAlignment(SwingConstants.TRAILING)
    res
  }

  private val columns: Array[Column] = {
    Array(
      Column( 0, "Track"          , 32,  40,  64, _.trackNum        , Some(RightAlignedRenderer), Some(Ordering.Int)),
      Column( 1, "Name"           , 64, 144, 256, _.name            , None, None),
      Column( 2, "Artist"         , 64, 144, 256, _.artist          , None, None),
      Column( 3, "Composer"       , 64, 160, 384, _.composer        , None, None),
      Column( 4, "Genre"          , 56,  72, 128, _.genre           , None, None),
      Column( 5, "Year"           , 64,  96, 152, _.year            , None, Some(Ordering.Int)),
      Column( 6, "Disc"           , 64,  96, 152, _.discNum         , None, Some(Ordering.Int)),
      Column( 7, "Id"             , 64,  64,  64, _.id              , Some(RightAlignedRenderer), Some(Ordering.Int)),
    )
  }

  private final class Impl extends TrackTable with ModelImpl[TrackTable.Update] {
    private[this] var _tracks: ISeq[TrackInfo] = Nil

    private object model extends AbstractTableModel {
      def getRowCount   : Int = _tracks.size
      def getColumnCount: Int = columns.length

      override def getColumnName(colIdx: Int): String = columns(colIdx).name

      def getValueAt(rowIdx: Int, colIdx: Int): AnyRef = {
        val track = _tracks(rowIdx)
        val col   = columns(colIdx)
        col.extract(track).asInstanceOf[AnyRef]
      }
    }

    tracks = _tracks  // initializes model

    lazy val table: Table = {
      val res = new Table {
        // https://github.com/scala/scala-swing/issues/47
        override lazy val peer: JTable = new JTable with SuperMixin
      }
      //      import de.sciss.swingplus.Implicits._
      res.model   = model
      val resJ    = res.peer
      val cm      = resJ.getColumnModel
      val sorter  = new TableRowSorter(model)
      columns.foreach { col =>
        val tc = cm.getColumn(col.idx)
        col.sorter.foreach(sorter.setComparator(col.idx, _))
        tc.setMinWidth      (col.minWidth )
        tc.setMaxWidth      (col.maxWidth )
        tc.setPreferredWidth(col.prefWidth)
        col.cellRenderer  .foreach(tc.setCellRenderer  )
        col.headerRenderer.foreach(tc.setHeaderRenderer)
      }
      // cm.setColumnMargin(6)
      resJ.setRowSorter(sorter)
      // cf. http://stackoverflow.com/questions/5968355/horizontal-bar-on-jscrollpane/5970400
      res.autoResizeMode = AutoResizeMode.Off
      // resJ.setPreferredScrollableViewportSize(resJ.getPreferredSize)

      res.listenTo(res.selection)
      res.selection.elementMode
      res.reactions += {
        case TableRowsSelected(_, _, false) =>
          dispatch(TrackTable.Selection(selection))
      }

      res
    }

    lazy val component: Component = {
      val res = new ScrollPane(table)
      //      res.horizontalScrollBarPolicy = BarPolicy.Always
      //      res.verticalScrollBarPolicy   = BarPolicy.Always
      res.peer.putClientProperty("styleId", "undecorated")
      res.preferredSize = {
        val d = res.preferredSize
        d.width = math.min(1024, table.preferredSize.width)
        d
      }
      res
    }

    def tracks: ISeq[TrackInfo] = _tracks
    def tracks_=(xs: ISeq[TrackInfo]): Unit = {
      _tracks = xs
      model.fireTableDataChanged()
    }

    def selection: ISeq[TrackInfo] = {
      val xs      = tracks.toIndexedSeq
      val rows    = table.selection.rows
      val tableJ  = table.peer
      val res     = rows.iterator.map { vi =>
        val mi = tableJ.convertRowIndexToModel(vi)
        xs(mi)
      } .toIndexedSeq
      res
    }

    def selection_=(xs: ISeq[TrackInfo]): Unit = {
      val tableJ  = table.peer
      val indices = xs.iterator.map { s =>
        val mi = tracks.indexOf(s)
        val vi = if (mi < 0) mi else tableJ.convertRowIndexToView(mi)
        vi
      }.filter(_ >= 0).toSet
      val rows    = table.selection.rows
      rows.clear()
      rows ++= indices
    }
  }
}
trait TrackTable extends Model[TrackTable.Update] {
  /** The top level component containing the view. */
  def component: Component

  /** The table component (which is not the top-level component!).
   * Use this to customise the table, install a selection listener, etc.
   */
  def table: Table

  /** Get or set the current list of tracks shown in the table. */
  var tracks: ISeq[TrackInfo]

  /** Get or set the current list of tracks selected in the table. */
  var selection: ISeq[TrackInfo]
}