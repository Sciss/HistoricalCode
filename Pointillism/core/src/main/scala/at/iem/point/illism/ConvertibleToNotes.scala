package at.iem.point.illism

import collection.immutable.{IndexedSeq => Vec}

trait ConvertibleToNotes {
  def toNotes: Vec[OffsetNote]
}