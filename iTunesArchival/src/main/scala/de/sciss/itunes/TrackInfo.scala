package de.sciss.itunes

import java.net.URL

import scala.collection.{Seq => CSeq}
import scala.xml.Node

/*
  example:

		<dict>
			<key>Track ID</key><integer>520</integer>
			<key>Size</key><integer>91236390</integer>
			<key>Total Time</key><integer>517200</integer>
			<key>Disc Number</key><integer>1</integer>
			<key>Disc Count</key><integer>1</integer>
			<key>Track Number</key><integer>1</integer>
			<key>Track Count</key><integer>10</integer>
			<key>Year</key><integer>1994</integer>
			<key>Date Modified</key><date>2019-12-27T14:05:36Z</date>
			<key>Date Added</key><date>2019-12-27T14:04:11Z</date>
			<key>Bit Rate</key><integer>1411</integer>
			<key>Sample Rate</key><integer>44100</integer>
			<key>Compilation</key><true/>
			<key>Persistent ID</key><string>84CC7B50F39BAC41</string>
			<key>Track Type</key><string>File</string>
			<key>File Type</key><integer>1095321158</integer>
			<key>File Folder Count</key><integer>5</integer>
			<key>Library Folder Count</key><integer>1</integer>
			<key>Name</key><string>Release to the System</string>
			<key>Artist</key><string>Mark Franklin</string>
			<key>Composer</key><string>Mark Franklin</string>
			<key>Album</key><string>Artificial Intelligence II</string>
			<key>Genre</key><string>Electronica</string>
			<key>Kind</key><string>AIFF audio file</string>
			<key>Location</key><string>file:///Users/rutz/Music/iTunes/iTunes%20Media/Music/Compilations/Artificial%20Intelligence%20II/01%20Release%20to%20the%20System.aif</string>
		</dict>

 */

object TrackInfo {
  def parse(n: Node): TrackInfo = {
    require (n.label == "dict")
    var keysRem = keySet
    val pairs: List[CSeq[Node]] = n.child.grouped(2).toList
    var res: TrackInfo = invalid
    pairs.foreach {
      case CSeq(keyN @ Node("key", _, _), valueN @ Node(value, _, _ @ _*)) =>
        val key = keyN.text

        def parseInt    (): Int     = valueN.text.toInt
        def parseString (): String  = valueN.text

        val m: PartialFunction[(String, String), TrackInfo] = {
          case ("Track ID"    , "integer" ) => res.copy(id            = parseInt())
          case ("Disc Number" , "integer" ) => res.copy(discNum       = parseInt())
          case ("Disc Count"  , "integer" ) => res.copy(discCount     = parseInt())
          case ("Track Number", "integer" ) => res.copy(trackNum      = parseInt())
          case ("Track Count" , "integer" ) => res.copy(trackCount    = parseInt())
          case ("Year"        , "integer" ) => res.copy(year          = parseInt())
          case ("Compilation" , "true"    ) => res.copy(isCompilation = true)
          case ("Name"        , "string"  ) => res.copy(name          = parseString())
          case ("Artist"      , "string"  ) => res.copy(artist        = parseString())
          case ("Composer"    , "string"  ) => res.copy(composer      = parseString())
          case ("Album"       , "string"  ) => res.copy(album         = parseString())
          case ("Genre"       , "string"  ) => res.copy(genre         = parseString())
          case ("Location"    , "string"  ) => res.copy(url           = new URL(parseString()))
        }

        val kv = (key, value)
        if (m.isDefinedAt(kv)) {
          require (keysRem.contains(key), s"Duplicate key: $key")
          keysRem -= key
          res = m(kv)
        }
    }
    keysRem -= "Compilation"  // may be absent
    if (keysRem.contains("Composer") && !keysRem.contains("Artist")) {
      // println(s"Warning: missing composer info for ${res.name} - ${res.album} - artist is ${res.artist}")
      keysRem -= "Composer"
      res = res.copy(composer = res.artist)
    }
    if (keysRem.contains("Disc Number") && keysRem.contains("Disc Count")) {
      // println(s"Warning: missing disk info for ${res.composer} - ${res.album}")
      keysRem -= "Disc Number"
      keysRem -= "Disc Count"
      res = res.copy(discNum = 1, discCount = 1)
    }
    if (keysRem.contains("Year")) {
      // println(s"Warning: missing year info for ${res.composer} - ${res.album}")
      keysRem -= "Year" // leave at -1
    }
    require (keysRem.isEmpty, keysRem.mkString("Missing keys: ", ", ", s"; partial: $res"))
    res
  }

  private val keySet = Set(
    "Track ID", "Disc Number", "Disc Count", "Track Number", "Track Count", "Year", "Compilation",
    "Name", "Artist", "Composer", "Album", "Genre", "Location"
  )

  private val invalid = TrackInfo(id = -1, name = "", composer = "", artist = "", genre = "", album = "",
    year = -1, isCompilation = false, discNum = -1, discCount = -1, trackNum = -1, trackCount = -1, url = null)
}
final case class TrackInfo(id: Int, name: String, composer: String, artist: String, genre: String,
                           album: String, year: Int, isCompilation: Boolean,
                           discNum: Int, discCount: Int, trackNum: Int, trackCount: Int,
                           url: URL) {
  def yearValid: Boolean = year > 0
}
