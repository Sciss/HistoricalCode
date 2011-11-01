package de.sciss.transactional.test

import de.sciss.transactional.annotation.txn

object Example extends App {
   @txn class TestClass
}