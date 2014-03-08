package de.sciss.lucre.macros

@txn trait ListCell[A] {
  val value: A
  var next: Option[ListCell[A]]

  /*  */
}

object TxnTest extends App {
  ListCell()
}