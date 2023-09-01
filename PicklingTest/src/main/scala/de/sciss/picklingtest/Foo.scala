package de.sciss.picklingtest

trait Foo {
  def name: String
  def age: Int

  override def toString = s"Foo($name, $age)"
}
