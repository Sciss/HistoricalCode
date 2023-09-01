/*
 * Created by IntelliJ IDEA.
 * User: rutz
 * Date: 08.03.2010
 * Time: 15:33:41
 */
package de.sciss.datanucleustest

import javax.jdo.annotations.{ PersistenceCapable }

// @PersistenceCapable
class SpanCollection {
   private var spansVar = Set.empty[ Span ]

   def addSpan( span: Span ) {
      spansVar += span
   }

   def removeSpan( span: Span ) {
      spansVar -= span
   }

   def spans = spansVar
}