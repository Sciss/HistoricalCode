package de.sciss.lucre.macros

import language.higherKinds

trait Sys[S <: Sys[S]] {
  type Tx
  type Var[A]
}
