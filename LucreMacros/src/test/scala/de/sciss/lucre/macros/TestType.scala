package de.sciss.lucre.macros

//import scala.concurrent.stm.{InTxn, Ref}

object TestType {

  //  // input
  //  object Person {
  //    def apply(name: String, age: Int): Person = ???
  //  }
  //  trait Person {
  //    var name: String
  //    var age: Int
  //  }

  // output

  //  trait Foo {
  //    @identity val read: Int
  //
  //    @identity var readWrite: Int
  //
  //    // @identity type Baz = Int
  //
  //    // @identity("foo") val i = 33
  //  }

  // @txn class Bar(@txn val read: Int, @txn var readWrite: Int)

//  @txn class Foo(bar: Int, baz: Float) {
//    // def reset(): Unit = readWrite = 42
//  }

//  @txn class FooR(bar0: Int) {
//    private val _bar = Ref(bar0)
//
//    def bar(implicit tx: InTxn): Int = _bar()
//    def bar_=(value: Int)(implicit tx: InTxn): Unit = _bar() = value
//  }

  /*

    object Bar {
      def apply[S <: Sys[S]](read: Int, readWrite: Int)(implicit tx: S#Tx): Bar[S] = ???

      implicit def serializer[S <: Sys[S]]
    }
    trait Bar[S <: Sys[S]] extends Mutable[S#ID, S#Tx] {
      def read(implicit tx: S#Tx): Int
      def readWrite(implicit tx: S#Tx): Int
      def readWrite_=(value: Int)(implicit tx: S#Tx): Unit

      def reset()(implicit tx: S#Tx): Unit
    }

   */

  /*
  object Person {
    def apply[S <: Sys[S]](name: String, age: Int): Person[S] = ???
  }
  trait Person[S <: Sys[S]] {
    def name(implicit tx: S#Tx): String
    def name_=(value: String)(implicit tx: S#Tx): Unit

    def age(implicit tx: S#Tx): Int
    def age_=(value: Int)(implicit tx: S#Tx): Unit
  }

   */

  object Foo
  @mkCompanion final class Foo
}
