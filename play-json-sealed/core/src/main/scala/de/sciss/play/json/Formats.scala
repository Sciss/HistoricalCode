/*
 * Formats.scala
 * (play-json-sealed)
 *
 * Copyright (c) 2013-2014 Hanns Holger Rutz. All rights reserved.
 *
 * This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 * For further information, please contact Hanns Holger Rutz at
 * contact@sciss.de
 */

package de.sciss.play.json

import java.io.File
import collection.immutable.{IndexedSeq => Vec}
import play.api.libs.json.{JsError, JsResult, JsValue, Format, JsArray, JsSuccess, JsString}
import scala.annotation.tailrec

/** The object contains some additional formats for common types. */
object Formats {
  /** A format which encodes a `java.io.File` using its path string. */
  implicit object FileFormat extends Format[File] {
    def reads(json: JsValue): JsResult[File] = json match {
      case s: JsString => JsSuccess(new java.io.File(s.value))
      case _ => JsError(s"Expecting JSON string, but found $json")
    }

    def writes(f: File): JsValue = JsString(f.getPath)
  }

  private final class Tuple2Format[T1, T2](implicit _1: Format[T1], _2: Format[T2]) extends Format[(T1, T2)] {
    override def toString = s"Tuple2Format(${_1}, ${_2})"

    def reads(json: JsValue): JsResult[(T1, T2)] = json match {
      case arr: JsArray =>
        for {
          _1r <- _1.reads(arr(0))
          _2r <- _2.reads(arr(1))
        } yield
          (_1r, _2r)

      case _ => JsError(s"Expected JSON Array but found $json")
    }

    def writes(tup: (T1, T2)): JsValue = {
      val _1w = _1.writes(tup._1)
      val _2w = _2.writes(tup._2)
      JsArray(Seq(_1w, _2w))
    }
  }

  /** A format which encodes a `Tuple2`, given codecs for its two elements. */
  implicit def Tuple2Format[T1, T2](implicit _1: Format[T1], _2: Format[T2]): Format[(T1, T2)] =
    new Tuple2Format[T1, T2]

  /** A format which encodes a `collection.immutable.IndexedSeq`, given a codec for its element type. */
  implicit def VecFormat[A](implicit elem: Format[A]): Format[Vec[A]] = new VecFormat[A]

  private final class VecFormat[A](implicit peer: Format[A]) extends Format[Vec[A]] {
    override def toString = s"VecFormat($peer)"
    
    def reads(json: JsValue): JsResult[Vec[A]] = json match {
      case JsArray(xsj) =>
        @tailrec def loop(rem: Vec[JsValue], res: Vec[A]): JsResult[Vec[A]] =
          rem match {
            case head +: tail =>
              peer.reads(head) match {
                case JsSuccess(value, _)  => loop(tail, res :+ value)
                case err @ JsError(_)     => err
              }

            case _ => JsSuccess(res)
          }

        loop(xsj.toIndexedSeq, Vec.empty)

      case _ => JsError(s"Not an array $json")
    }

    def writes(xs: Vec[A]): JsValue = JsArray(xs.map(peer.writes))
  }
}