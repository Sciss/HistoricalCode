/*
 * MonteCarloRenderer.scala
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

import java.util.concurrent.TimeUnit

import cats.data.State

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  * @author Jon Hanson
  */
object ConcurrentUtils {

  implicit final val ExCtx: ExecutionContext = ExecutionContext.fromExecutor(null)

  final val processorCount = Runtime.getRuntime.availableProcessors

  def parallelFor(range: Range)(impl: Int => Unit) = {
    val wait = Duration.create(10, TimeUnit.DAYS)
    val groupSize = range.size / processorCount

    range
      .sortBy(i => i % processorCount)
      .grouped(groupSize).toList.map({ subRange =>
      Future {
        subRange.foreach {
          impl(_)
        }
      }
    }).foreach(f => Await.result(f, wait))
  }
}

/**
  * Monte-Carlo path tracing renderer.
  *
  * @author Jon Hanson
  * @author Hanns Holger Rutz
  */
final class MonteCarloRenderer(val width: Int, val height: Int, val scene: Scene)
  extends Renderer {

  def radiance(ray: Ray, depth: Int): RNG.Type[RGB] = {
    scene.intersect(ray) match {
      case None => State.pure(RGB.black)
      case Some((prim, iSect)) =>
        val n  = prim.normal(iSect)
        val n1 = if (n.dot(ray.dir) < 0) n else -n

        val newDepth = depth + 1

        val reflect: RNG.Type[RGB] = {
          val color = prim.material.color

          if (newDepth > 5) {
            // Modified Russian roulette.
            val max = color.max * MathUtil.sqr(1.0 - depth / Renderer.MaxDepth)
            RNG.nextDouble.flatMap(rnd => {
              if (rnd >= max) {
                State.pure(RGB.black)
              } else {
                prim.material.radiance(this, ray, newDepth, iSect, n, n1).map(r => r * color / max)
              }
            })
          } else {
            prim.material.radiance(this, ray, newDepth, iSect, n, n1).map(r => r * color)
          }
        }

        reflect.map(r => prim.material.emission + r)
    }
  }
}