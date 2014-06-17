/*
 *  Language.scala
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

object Language {
  var default: Language = Language.English

  case object English extends Language
  case object German  extends Language
}
sealed trait Language