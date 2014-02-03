/*
 *  SupportVector.scala
 *  (ScalaSVM)
 *
 *  Copyright (c) 2013-2014 Hanns Holger Rutz. All rights reserved.
 *  Copyright (c) 2013-2014 Shixiong Zhu.
 *
 *	This software is published under the GNU Lesser General Public License v3+
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.svm

import scala.util.control.NonFatal
import java.io.IOException
import scala.collection.breakOut

object SupportVector {
  def fromString(s: String): SupportVector = try {
    val tokens              = s.trim().split(" ")
    val vector: List[Node]  = tokens.map(Node.fromString)(breakOut)
    SupportVector(vector, 0)
  } catch {
    case NonFatal(e) => throw new IOException(s"Invalid input: $s", e)
  }
}
case class SupportVector(vector: List[Node], index: Int)
