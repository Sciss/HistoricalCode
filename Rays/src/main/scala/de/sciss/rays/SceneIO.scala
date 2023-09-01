/*
 * SceneIO.scala
 * (Rays)
 *
 * Copyright (c) 2016 Hanns Holger Rutz.
 * Copyright (c) 2016 Jon Hanson.
 * All rights reserved.
 *
 * This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 * For further information, please contact Hanns Holger Rutz at
 * contact@sciss.de
 */

package de.sciss.rays

import java.io.{FileInputStream, FileOutputStream}
import java.util.NoSuchElementException

import cats.data.Xor
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

/**
  * @author Jon Hanson
  */
object Codecs {

  final val TypeName        = "type"

  final val DiffuseName     = "Diffuse"
  final val ReflectiveName  = "Reflective"
  final val RefractiveName  = "Refractive"

  final val PlaneName       = "Plane"
  final val SphereName      = "Sphere"

  def withType(json: Json, name: String): Json =
    json.mapObject(jo => jo.+:(TypeName, name.asJson))

  implicit val decodeAxis: Decoder[Axis.Type] =
    Decoder.instance(c =>
      c.focus.asString match {
        case Some(s) =>
          try
            Xor.right(Axis.withName(s))
          catch {
            case ex: NoSuchElementException =>
              Xor.left(DecodingFailure(ex.getMessage, c.history))
          }
        case None => Xor.left(DecodingFailure("String", c.history))
      }
    )

  implicit val encodeAxis: Encoder[Axis.Type] =
    Encoder.instance(axis =>
      axis.toString.asJson
    )

  implicit val decodeMaterial: Decoder[Material] =
    Decoder.instance(c =>
      c.downField(TypeName).as[String].flatMap {
        case DiffuseName    => c.as[Diffuse   ]
        case ReflectiveName => c.as[Reflective]
        case RefractiveName => c.as[Refractive]
      }
    )

  implicit val encodeMaterial: Encoder[Material] =
    Encoder.instance {
      case (d: Diffuse   ) => withType(d.asJson, DiffuseName   )
      case (r: Reflective) => withType(r.asJson, ReflectiveName)
      case (r: Refractive) => withType(r.asJson, RefractiveName)
    }

  implicit val decodeShape: Decoder[Shape] =
    Decoder.instance(c =>
      c.downField(TypeName).as[String].flatMap {
        case PlaneName => c.as[Plane]
        case SphereName => c.as[Sphere]
      }
    )

  implicit val encodeShape: Encoder[Shape] =
    Encoder.instance {
      case (p: Plane ) => withType(p.asJson, PlaneName )
      case (s: Sphere) => withType(s.asJson, SphereName)
    }
}

/**
  * @author Jon Hanson
  * @author Hanns Holger Rutz
  */
object SceneIO {
  import Codecs._ // unlike what IntelliJ thinks, this _is_ used

  def load(fileName: String): Scene = {
    val fin = new FileInputStream(fileName)
    val arr = new Array[Byte](fin.available())
    fin.read(arr)
    fin.close()
    val encoded = new String(arr, "UTF-8")
    decode[Scene](encoded).valueOr(err => throw err)
  }

  def save(scene: Scene, fileName: String) = {
    val encoded = scene.asJson.spaces4
    val arr     = encoded.getBytes("UTF-8")
    val fOut    = new FileOutputStream(fileName)
    fOut.write(arr)
    fOut.close()
  }
}