package de.sciss.itunes

import java.util.Comparator

import de.sciss.model.Model
import de.sciss.model.impl.ModelImpl
import javax.swing.JTable
import javax.swing.table.{AbstractTableModel, TableCellRenderer, TableRowSorter}

import scala.collection.immutable.{Seq => ISeq}
import scala.swing.Table.AutoResizeMode
import scala.swing.event.TableRowsSelected
import scala.swing.{Component, ScrollPane, Table}

object AlbumTable {
  def apply(): AlbumTable = new Impl()

  sealed trait Update
  final case class Selection(albums: ISeq[AlbumInfo]) extends Update

  private case class Column(idx: Int, name: String, minWidth: Int, prefWidth: Int, maxWidth: Int,
                            extract: AlbumInfo => Any, cellRenderer: Option[TableCellRenderer] = None,
                            sorter: Option[Comparator[_]] = None, headerRenderer: Option[TableCellRenderer] = None)

//  private val RightAlignedRenderer = {
//    val res = new DefaultTableCellRenderer
//    res.setHorizontalAlignment(SwingConstants.TRAILING)
//    res
//  }

  private val columns: Array[Column] = {
    Array(
      Column( 0, "Artist"         , 64, 200, 400, _.artist        , None, None),
      Column( 1, "Name"           , 64, 320, 640, _.name          , None, None),
      Column( 2, "Year"           , 32,  64, 128, _.year          , None, Some(Ordering.Int)),
      Column( 3, "Discs"          , 32,  32, 128, _.discCount     , None, Some(Ordering.Int)),
      Column( 4, "Compilation"    , 32,  40,  40, _.isCompilation , None, None),
    )
  }

  private final class Impl extends AlbumTable with ModelImpl[AlbumTable.Update] {
    private[this] var _albums: ISeq[AlbumInfo] = Nil

    private object model extends AbstractTableModel {
      def getRowCount   : Int = _albums.size
      def getColumnCount: Int = columns.length

      override def getColumnName(colIdx: Int): String = columns(colIdx).name

      def getValueAt(rowIdx: Int, colIdx: Int): AnyRef = {
        val album = _albums(rowIdx)
        val col   = columns(colIdx)
        col.extract(album).asInstanceOf[AnyRef]
      }
    }

    albums = _albums  // initializes model

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
          dispatch(AlbumTable.Selection(selection))
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

    def albums: ISeq[AlbumInfo] = _albums
    def albums_=(xs: ISeq[AlbumInfo]): Unit = {
      _albums = xs
      model.fireTableDataChanged()
    }

    def selection: ISeq[AlbumInfo] = {
      val xs      = albums.toIndexedSeq
      val rows    = table.selection.rows
      val tableJ  = table.peer
      val res     = rows.iterator.map { vi =>
        val mi = tableJ.convertRowIndexToModel(vi)
        xs(mi)
      } .toIndexedSeq
      res
    }

    def selection_=(xs: ISeq[AlbumInfo]): Unit = {
      val tableJ  = table.peer
      val indices = xs.iterator.map { s =>
        val mi = albums.indexOf(s)
        val vi = if (mi < 0) mi else tableJ.convertRowIndexToView(mi)
        vi
      }.filter(_ >= 0).toSet
      val rows    = table.selection.rows
      rows.clear()
      rows ++= indices
    }
  }
}
trait AlbumTable extends Model[AlbumTable.Update] {
  /** The top level component containing the view. */
  def component: Component

  /** The table component (which is not the top-level component!).
   * Use this to customise the table, install a selection listener, etc.
   */
  def table: Table

  /** Get or set the current list of albums shown in the table. */
  var albums: ISeq[AlbumInfo]

  /** Get or set the current list of albums selected in the table. */
  var selection: ISeq[AlbumInfo]
}