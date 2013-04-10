package at.iem.point.illism

import collection.immutable.{IndexedSeq => IIdxSeq}

trait ConvertibleToNotes {
  def toNotes: IIdxSeq[OffsetNote]
}