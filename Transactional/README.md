## Transactional

### statement

Transactional is an compiler plugin for the Scala programming language. It annotated mutable data structures, in order to both provide a transactional variant of them along with their plain version. It is (C)opyright 2011 by Hanns Holger Rutz. All rights reserved. Transactional is released under the [GNU Lesser General Public License](https://raw.github.com/Sciss/Transactional/master/licenses/Transactional-License.txt) and comes with absolutely no warranties. To contact the author, send an email to `contact at sciss.de`

### requirements / installation

Transactional builds with sbt 0.11 against Scala 2.9.1.

### overview

Imagine a simple data structure, like a linked

```scala
    @txn trait SkipList[ A ] {
       @txn def contains( v: A ) : Boolean
       @txn def add( v: A ) : Boolean
       @txn def remove( v: A ) : Boolean
    }

This will be transformed into two distinct traits, a "plain" one:

 ```scala
     trait SkipList[ A ] {
        def contains( v: A ) : Boolean
        def add( v: A ) : Boolean
        def remove( v: A ) : Boolean
     }

...and a "transactional" one:

```scala
    trait TxnSkipList[ Txn <: TxnContext[ Txn ], A ] {
       def contains( v: A )( implicit txn: Txn ) : Boolean
       def add( v: A )( implicit txn: Txn ) : Boolean
       def remove( v: A )( implicit txn: Txn ) : Boolean
    }

Where `TxnContext` and another trait `TxnRef` are defined as follows:

```scala
    trait TxnContext[ Self ] {
       type Ref[ A ] <: TxnRef[ Self ]
       def newRef[ A ]( initialValue: A ) : Self#Ref[ A ]
    }

    trait TxnRef[ Txn <: TxnContext[ Txn ], A ] {
       def apply( implicit txn: Txn ) : A
       def update( value: A )( implicit txn: Txn ) : Unit
       def transform( f: A => A )( implicit txn: Txn ) : Unit
    }

Now furthermore imagine an implementation:

```scala
    @txn class SkipListImpl[ A ] extends SkipList[ A ] {
       @txn private var underlying = Set.empty[ A ]

       def contains( v: A ) = underlying.contains( v )
       def add( v: A ) { underlying += v }
       def remove( v: A ) = {
          val res = contains( v )
          if( res ) underlying -= v
          res
       }
    }

This plugin would transform this into the plain variant, and the following transactional variant:

```scala
    class TxnSkipListImpl[ Txn <: TxnContext[ Txn ], A ]( implicit txn0: Txn ) extends TxnSkipList[ Txn, A ] {
       private val underlying = txn0.newRef( Set.empty[ A ])

       def contains( v: A )( implicit txn: Txn ) = underlying().contains( v )
       def add( v: A )( implicit txn: Txn ) { underlying() = underlying() + v }
       def remove( v: A )( implicit txn: Txn ) = {
          val res = contains( v )
          if( res ) underlying() = underlying() - v
          res
       }
    }

It could also, given the method `transform` in `TxnRef`, be more efficient and instead do something like

```scala
    def add( v: A )( implicit txn: Txn ) { underlying.transform( _ + v )}

### problems

- How to minimize access to the refs. We could prepare the code with
- How to deal with `@txn` annotated classes implementing non-transactional traits. A possibility is to enforce annotating these super classes with another trait that specifies whether the super class will be omitted in the transactional case. For instance in the above example this could be the case for `collection.mutable.Set`
- How to deal with irreversible side-effects. That is, what in pure txn code would be written as `txn.afterCommit( ... )`.
- How can this be pluggable into `scala.concurrent.stm`.

### Minimize Access

Idea:

```scala
    @txn def add( v: A ) {
       txn.transform( underlying )( _ + v )
    }

Which would translate in the plain case to

```scala
    def add( v: A ) {
       underlying = underlying + v
    }

...and in the transactional case to

```scala
    def add( v: A )( implicit txn: Txn ) {
       underlying.transform( _ + v )
    }

### Compatibility with Scala-STM

- We could use this instead in the first version to see how far we get. It might be possible to piggy-back on `StubSTMImpl`?
- The advantage would be that we do not need to mess around with the constructor arguments, as refs can be directly created outside of transactions, using `Ref.apply`.