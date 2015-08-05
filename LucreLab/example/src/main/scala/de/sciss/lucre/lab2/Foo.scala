package de.sciss.lucre.lab2

//object Foo
//@obj(1) class Foo(var i: Int)
//
//object Test {
//  def test[S <: Sys[S]](implicit tx: S#Tx): Unit = {
//    val f = Foo()
//  }
//}

/*
object Foo {
  def apply[S <: Sys[S]](i: Int)(implicit tx: S#Tx): Foo with Obj[S] = ???
}
@obj(1) trait Foo {
  var i: Int
}

trait Ops[A] {
  def foreach(fun: A => Unit): Unit
}

object Test {
  def $[A](expr: A): Ops[A] = ???  // this would be another macro that looks up the synthetic companion method

  def test[S <: Sys[S]](f: Foo)(implicit tx: S#Tx): Unit = {
    $(f.i).foreach(println)
  }
}
*/

trait Subscription {
  def dispose(): Unit
}

trait Reactor[-A] {
  def react(value: A): Unit
}

object Reactive {
  implicit final class Ops[A](private val self: Reactive[A]) extends AnyVal {
    def filter(p: A => Boolean): Reactive[A] = new Reactive[A] {
      def subscribe(r: Reactor[A]): Subscription = self.subscribe(new Reactor[A] {
        def react(value: A): Unit = if (p(value)) r.react(value)
      })
    }
  }
}
trait Reactive[+A] {
  def subscribe(r: Reactor[A]): Subscription
}

object Foo {
  def apply[S <: Sys[S]](i: Int)(implicit tx: S#Tx): Foo[S] = ???
}
trait Foo[S <: Sys[S]] extends Obj[S] {
  var i: Int

  def iRef: Reactive[Int]
}
