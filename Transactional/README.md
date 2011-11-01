## Transactional

### statement

Transactional is an compiler plugin for the Scala programming language. It annotated mutable data structures, in order to both provide a transactional variant of them along with their plain version. It is (C)opyright 2011 by Hanns Holger Rutz. All rights reserved. Transactional is released under the [GNU Lesser General Public License](https://raw.github.com/Sciss/Transactional/master/licenses/Transactional-License.txt) and comes with absolutely no warranties. To contact the author, send an email to `contact at sciss.de`

### requirements / installation

Transactional builds with sbt 0.11 against Scala 2.9.1.

### overview

Imagine a simple data structure, like a linked

<!-- language: scala -->

    @txn trait SkipList[ A ] {
       @txn def contains( v: A ) : Boolean
       @txn def add( v: A ) : Boolean
       @txn def remove( v: A ) : Boolean
    }
