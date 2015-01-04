# About

## What to Address

### Serialization

- remove burden from manually writing serializers; i.e. use macros
- avoid cluttering the source code with serialization code; i.e. ditto
- allow multiple protocols (e.g. binary, JSON, ...)
- allow blind de-serialization, so that we can do blind copying
- support cyclic graphs

### Events

- minimise clutter in writing event code
- minimise publish-subscribe visibility, maximise functional composition
- implement functional collection filtering and transformation
- avoid code duplication for reaction and initialisation of observers
- solve mutable state problem with caching/indexing structures
- avoid push-pull double work

### Durable Systems

- automatic garbage collection (reference counting)

### Confluent System

- more thorough exploration of meld
- implications of push-only events and caching/indexing structures
- cross-database links
- multiple users
- ease of cross-system usage (S/D/I)
- state versus action; undo-redo
- improve querying techniques, e.g. for structural changes in a collection

## Look At

Projects to look at more thoroughly for ideas, designs, etc.

### Activate

- https://github.com/fwbrasil/activate
- http://activate-framework.org/

Deals with most of the questions of persistence and the interaction between STM and persistence. I.e. life cycle,
entity ids, custom serializers and formats, txn contention, caching, indexing, schema evolution.

On the downside, will tie to Radon-STM instead of Scala-STM, and will not be directly applicable to confluent
systems. Also has its own 'event' system for observing state changes, thus will interfere with a more functional
reactive layer. Also heavily relies on macros, so might be a bit difficult to adopt.

All in all, should study the code. Seems quite opinionated and not exactly a 'minimal' API (although
persistence is 'transparent', the API is quite database-centric). I like the 'entity enhancement', where
a user-code `var name: String` becomes `val name: Var[String]`. This requires, however, that simple
accessors are still used, so we can't have the `implicit tx` argument.

> Activate doesn't maintain a database transaction during the STM transaction because it should create databases
> locks that brings a blocking behavior incompatible with the non-blocking optimistic approach.

> Activate implements an in-memory repository called LiveCache. It assures that there is only one instance of an
> entity loaded in the memory. The approach to achieve this behavior is to use soft references to all loaded
> entities from the database.

This is kind of the opposite of what we are currently doing (use the db's txn system, and so we can freely
have multiple instances of the same object). The `LiveCache` seems to be similar to what we do.

### Scala-Pickling

- https://github.com/scala/pickling

Addresses many serialization questions, such as multiple formats, blind de-serialization, cyclic graphs.
Unfortunately seems to be slow and horribly buggy and incomplete. Code not documented at all. Heavy use of macros.

### Storm-Enroute Reactive Collections

- https://github.com/storm-enroute/reactive-collections
- http://reactive-collections.com/

```scala
    val stringReps = RMap[Int, String]()
    val prints = stringReps.react.apply(1) onEvent {
      txt => println("String for 1: " + txt)
    }
    stringReps(1) = "1"   // prints: String for 1: 1
    stringReps(1) = "One" // prints: string for 1: One
```

```scala
    trait RContainer[T] {
      def inserts: Reactive[T]
      def removes: Reactive[T]
      def react: RContainer.Lifted[T]
      def foreach(f: T => Unit): Unit
      def size: Int
    }
```

The `inserts` and `removes` methods are not described. Furthermore, it is unclear whether you can really
work functionally reactive with collections, e.g. creating a reactive `map` or `filter` projecting,
getting a reactive `size` values etc.?

Actually it _does_ have a couple of methods:
https://github.com/storm-enroute/reactive-collections/blob/master/src/main/scala/scala/reactive/container/RContainer.scala#L61

The `.lifted` method is kind of a nice solution to distinguish between eager and reactive calls. Some
methods however are defined in the wrong place IMO, e.g. `filter` is defined on `RContainer` not
`RContainer.Lifted`; perhaps because these are unambiguous.

Here is how the 'duplicate' call init versus reaction is 'solved':

```scala
  def foreach(f: T => Unit): Reactive[Unit] with Reactive.Subscription = {
    container        .foreach(f)
    container.inserts.foreach(f)
   }
```
