package de.sciss.txninterpreter

import concurrent.stm.{TxnUnknown, Txn, InTxn}

object TxnProvider {
  implicit def currentTxn: InTxn = Txn.findCurrent(TxnUnknown)
    .getOrElse(sys.error("Executing outside of open transaction"))
}