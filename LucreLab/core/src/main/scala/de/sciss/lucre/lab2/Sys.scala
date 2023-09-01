package de.sciss.lucre.lab2

import scala.language.higherKinds

trait Sys[S <: Sys[S]] {
  type Tx <: Txn[S]

  // type Var[A] <: de.sciss.lucre.lab.Var[S#Tx, A]
}

trait Txn[S <: Sys[S]] {
  // def newVar[A](init: A): S#Var[A]
}