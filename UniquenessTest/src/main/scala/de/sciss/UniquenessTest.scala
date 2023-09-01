/*-enable-unique*/
package de.sciss

import _root_.scala.annotation.unique

object UniquenessTest {
   class C {
      var f: C = _
      def consume(x: C @unique) {}

      def m() {
         val c: C @unique = new C
         consume(c)
         consume(c)
      }
   }
}