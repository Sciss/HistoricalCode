package de.sciss.itunes

object AlbumInfo {
  def all(tracks: Seq[TrackInfo]): Seq[AlbumInfo] = {
    val map = tracks.groupBy(ti => (ti.album, if (ti.isCompilation) "V/A" else ti.artist /*, ti.year*/))
    map.valuesIterator.map(apply).toList
  }

  def apply(tracks: Seq[TrackInfo]): AlbumInfo = {
    require (tracks.nonEmpty)
    val t0 = tracks.head
    require (tracks.forall(_.album          == t0.album         ))
    if (!t0.isCompilation) require (tracks.forall(_.artist == t0.artist))
    // require (tracks.forall(_.year           == t0.year          ))
    val year = tracks.map(_.year).max // require (tracks.forall(_.year           == t0.year          ))
    require (tracks.forall(_.discCount      == t0.discCount     ))
    require (tracks.forall(_.isCompilation  == t0.isCompilation ))
    AlbumInfo(name = t0.album, artist = if (t0.isCompilation) "V/A" else t0.artist,
      year = year, discCount = t0.discCount,
      isCompilation = t0.isCompilation, tracks = tracks)
  }
}
final case class AlbumInfo(name: String, artist: String, year: Int, discCount: Int, isCompilation: Boolean,
                           tracks: Seq[TrackInfo])
