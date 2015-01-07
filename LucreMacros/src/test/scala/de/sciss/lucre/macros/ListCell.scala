package de.sciss.lucre.macros

//object ListCell {
//  def apply(): ListCell = ??? // macro applyImpl
//
//  // def applyImpl()
//}
@txn trait ListCell[A] {
  val value: A
  var next: Option[ListCell[A]]

  /*   */
}

object TxnTest extends App {
  def test[S <: Sys[S]](implicit tx: S#Tx): Unit = {
    val cell = ListCell[S, Int] // ()
  }
}