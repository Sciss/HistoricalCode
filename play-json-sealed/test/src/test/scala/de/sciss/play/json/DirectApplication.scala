package de.sciss.play.json

import collection.immutable.{IndexedSeq => Vec}

object DirectApplication {
  AutoFormat[Vec[Boolean]]
  AutoFormat[(String, Int)]
}
