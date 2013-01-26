package de.sciss.txninterpreter

import concurrent.stm.InTxn

object TxnProvider {
  implicit def currentTxn: InTxn = sys.error("TODO")
}