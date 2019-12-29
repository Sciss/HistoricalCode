package de.sciss.itunes

import de.sciss.file._
import org.rogach.scallop.{ScallopConf, ScallopOption => Opt}
import org.xml.sax.InputSource

import scala.collection.{Seq => CSeq}
import scala.swing.Swing
import scala.util.control.NonFatal
import scala.xml.{Elem, Node, Source, XML}

object Archival {
  final case class Config(xmlLibrary: File, createMeta: Boolean,
                          move: Option[(File, File)])

  def main(args: Array[String]): Unit = {
    object p extends ScallopConf(args) {
      printedName = "mellite"

      val library: Opt[File] = opt[File](required = true,
        descr = "iTunes library XML file"
      )

      val moveFrom: Opt[File] = opt[File](
        descr = "Original base directory"
      )

      val moveTo: Opt[File] = opt[File](
        descr = "New base directory"
      )

      val meta: Opt[Boolean] = opt(
        descr = "Create individual meta data files"
      )


//      val autoRun: Opt[List[String]] = opt[String]("auto-run", short = 'r', default = Some(""),
//        descr = "Run object with given name from root folder's top level. Comma separated list for multiple objects."
//      ).map(_.split(',').toList)

      verify()
      val config: Config = Config(
        xmlLibrary  = library(),
        createMeta  = meta(),
        move        = if (moveFrom.isDefined && moveTo.isDefined) Some((moveFrom(), moveTo())) else None,
      )
    }
//    run("")
    run(p.config)
  }

  def run(config: Config): Unit = {
    val lib: InputSource  = Source.fromFile(config.xmlLibrary)
    val is                = lib.getByteStream
    val root: Elem        = try { XML.load(is) } finally { is.close() }
    val rootDict          = root.\("dict").head
    val pairs: List[CSeq[Node]] = rootDict.child.grouped(2).toList
    val Some(trackNodes) = pairs.collectFirst {
      case CSeq(key @ Node("key", _, _), value @ Node("dict", _, _ @ _*)) if key.text == "Tracks" =>
        value \ "dict"
    }

    val tracks = trackNodes.map { n =>
      val ti = TrackInfo.parse(n)
      if (config.createMeta) {
        val fIn   = new File(ti.url.toURI)
        val fOut0 = config.move match {
          case Some((from, to)) =>
            val pIn   = fIn.path
            val pFrom = from.path
            val pTo   = to  .path
            require (pIn.startsWith(pFrom))
            val pOut  = pTo ++ pIn.substring(pFrom.length)
            new File(pOut)

          case None             => fIn
        }
        val fOut = fOut0.replaceExt("xml")
        if (!fOut.exists()) {
          // println(fOut)
          try {
            XML.save(fOut.path, n, xmlDecl = true)
          } catch {
            case NonFatal(ex) =>
              println(ex)
          }
        }
      }
      ti
    }
//    tracks.foreach(println)
    println(s"Number of tracks parsed: ${tracks.size}")
//    val lenComposer = tracks.map(_.composer.length).max
//    val lenAlbum    = tracks.map(_.album   .length).max
//    val lenTitle    = tracks.map(_.name    .length).max
    // println(s"String lengths: composer - ${lenComposer}; album - ${lenAlbum}; title - ${lenTitle} ")

//    def histo(name: String)(by: TrackInfo => String): Unit = {
//      val max     = tracks.map(ti => by(ti).length).max
//      val bins    = new Array[Int](max + 1)
//      tracks.foreach { ti =>
//        val bin = by(ti).length
//        bins(bin) += 1
//      }
//      val accum = bins.toVector.integrate.sortedT
//      println(s"---- histogram for '$name' ----'")
//      (10 to 100 by 10).foreach { p =>
//        val n = (accum.size * p - 50) / 100 // accum.percentile(p)
//        println(f"$p% 3d%% : $n")
//      }
//    }

//    histo("Artist")(_.artist)
//    histo("Composer")(_.composer)
//    histo("Album"   )(_.album   )
//    histo("Title"   )(_.name    )

//    tracks.foreach { ti =>
//      if (ti.composer.length > 40) println(ti.composer)
//    }

//    val testAlbum = tracks.head.album
//    val tracksSel = tracks.filter(_.album == testAlbum)
    val albums = AlbumInfo.all(tracks)
    println(s"Number of albums: ${albums.size}")

    Swing.onEDT {
      new MainWindow(albums)
    }
  }
}
