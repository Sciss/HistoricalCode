package de.sciss.lucrelab

trait Source[-Tx, +A] {
  def apply()(implicit tx: Tx): A
}

trait Sink[-Tx, -A] {
  def update(value: A)(implicit tx: Tx): Unit
}

trait Var[-Tx, A] extends Source[Tx, A] with Sink[Tx, A]
