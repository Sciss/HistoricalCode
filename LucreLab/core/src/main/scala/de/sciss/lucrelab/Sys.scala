package de.sciss.lucrelab

import scala.language.higherKinds

trait Sys[S <: Sys[S]] {
  type Tx <: Txn[S]

  type Var[A] <: de.sciss.lucrelab.Var[S#Tx, A]
}

trait Txn[S <: Sys[S]] {
  def newVar[A](init: A): S#Var[A]
}