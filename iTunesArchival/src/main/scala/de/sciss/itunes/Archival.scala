package de.sciss.itunes

import de.sciss.kollflitz.Ops._
import org.xml.sax.InputSource

import scala.xml.{Elem, Node, Source, XML}
import scala.collection.{Seq => CSeq}

object Archival {
  def main(args: Array[String]): Unit = {
//    run("/data/temp/itunes-test.xml")
    run("/data/music_scanned/iTunes Music Library.xml")
  }

  def run(pathLibrary: String): Unit = {
    val lib: InputSource  = Source.fromFile(pathLibrary)
    val is                = lib.getByteStream
    val root: Elem        = try { XML.load(is) } finally { is.close() }
    val rootDict          = root.\("dict").head
    val pairs: List[CSeq[Node]] = rootDict.child.grouped(2).toList
    val Some(trackNodes) = pairs.collectFirst {
      case CSeq(key @ Node("key", _, _), value @ Node("dict", _, _ @ _*)) if key.text == "Tracks" =>
        value \ "dict"
    }
    val tracks = trackNodes.map(TrackInfo.parse)
//    tracks.foreach(println)
    println(s"Number of tracks parsed: ${tracks.size}")
//    val lenComposer = tracks.map(_.composer.length).max
//    val lenAlbum    = tracks.map(_.album   .length).max
//    val lenTitle    = tracks.map(_.name    .length).max
    // println(s"String lengths: composer - ${lenComposer}; album - ${lenAlbum}; title - ${lenTitle} ")

    def histo(name: String)(by: TrackInfo => String): Unit = {
      val max     = tracks.map(ti => by(ti).length).max
      val bins    = new Array[Int](max + 1)
      tracks.foreach { ti =>
        val bin = by(ti).length
        bins(bin) += 1
      }
      val accum = bins.toVector.integrate.sortedT
      println(s"---- histogram for '$name' ----'")
      (10 to 100 by 10).foreach { p =>
        val n = (accum.size * p - 50) / 100 // accum.percentile(p)
        println(f"$p% 3d%% : $n")
      }
    }

//    histo("Artist")(_.artist)
//    histo("Composer")(_.composer)
//    histo("Album"   )(_.album   )
//    histo("Title"   )(_.name    )

//    tracks.foreach { ti =>
//      if (ti.composer.length > 40) println(ti.composer)
//    }

  }
}
