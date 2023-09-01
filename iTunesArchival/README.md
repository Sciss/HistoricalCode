# iTunes Archival

## statement

This is a utility that helps me copying audio CDs to hard-disc via iTunes 12, eventually converting the
full resolution AIFF files to mp3 with the appropriate tags. This is probably not useful for
anyone but myself, although it could come handy if you want to parse the iTunes library XML file.

This project is (C)opyright 2019 by Hanns Holger Rutz. All rights reserved. It is released under
the [GNU General Public License](https://raw.github.com/Sciss/iTunesArchival/master/LICENSE) v3+ and
comes with absolutely no warranties. To contact the author, send an email to `contact at sciss.de`

## building

Builds with [sbt](http://www.scala-sbt.org/) against Scala 2.13.

Needs `lame` installed for mp3 transcoding.

## running

Example arguments:

```
--library "/data/music_scanned/iTunes Music Library.xml" --move-from "/Users/rutz/Music/iTunes/iTunes Media/Music" --move-to "/data/music_scanned" --meta
```