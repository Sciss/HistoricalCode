/*
 *  ConvertibleToNotes.scala
 *  (Pointillism)
 *
 *  Copyright (c) 2013-2014 IEM Graz / Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package at.iem.point.illism

import collection.immutable.{IndexedSeq => Vec}

trait ConvertibleToNotes {
  def toNotes: Vec[OffsetNote]
}