/*
 *  Exec.scala
 *  (Lucre 4)
 *
 *  Copyright (c) 2009-2020 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU Affero General Public License v3+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.lucre

trait Exec[T <: Exec[T]] {
  type Id <: Ident[T]

  type I <: Exec[I]

  def inMemory: I

  implicit def inMemoryBridge: T => I

  def newId(): Id

  def newRef[A](init: A): Ref[T, A]
}

trait AnyExec extends Exec[AnyExec]