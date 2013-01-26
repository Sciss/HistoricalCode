package de.sciss.txninterpreter

import tools.nsc

object TxnInterpreter extends App with Runnable {
  run()

  def run() {
    println("Initializing interpreter...")
    val set   = new nsc.Settings
    set.usejavacp.value = true
    set.embeddedDefaults[TxnInterpreter.type]
//    set.classpath.value += File.pathSeparator + sys.props( "java.class.path" )
    val intp = new TxnMain(set)
    intp.initializeSynchronous()
    intp.quietImport("concurrent.stm._", "de.sciss.txninterpreter.TxnProvider._")

    println("Ok.")

    val res = intp.interpretTxn(
      """
        |val r = Ref(33)
        |val v = r()
        |v
      """.stripMargin)
    println(res)
  }
}