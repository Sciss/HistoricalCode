package de.sciss.svm

object AssertUtil {

  val DELTA = 10e-6

  implicit def svmAssertEquals(excepted: Double, actual: Double): Unit =
    org.junit.Assert.assertEquals(excepted, actual, DELTA)

  implicit def svmAssertEquals(excepted: SupportVector, actual: SupportVector): Unit = {
    org.junit.Assert.assertEquals(excepted.vector, actual.vector)
    // org.junit.Assert.assertEquals(excepted.coefficient, actual.coefficient, DELTA)
    org.junit.Assert.assertEquals(excepted.index, actual.index)
  }

  implicit def svmAssertEquals(excepted: Node, actual: Node): Unit = {
    org.junit.Assert.assertEquals(excepted.value, actual.value, DELTA)
    org.junit.Assert.assertEquals(excepted.index, actual.index)
  }

  implicit def svmAssertEquals[T](excepted: Seq[T], actual: Seq[T])(implicit assertEquals: (T, T) => Unit): Unit = {
    org.junit.Assert.assertEquals(excepted.size, actual.size)
    (excepted zip actual) map (z => assertEquals(z._1, z._2))
  }

  implicit def svmAssertEquals(excepted: List[Double], actual: List[Double]): Unit = {
    org.junit.Assert.assertEquals(excepted.size, actual.size)
    (excepted zip actual) map (z => svmAssertEquals(z._1, z._2))
  }

  implicit def svmAssertSeqSVMNodeEquals(excepted: List[Node], actual: List[Node]): Unit = {
    org.junit.Assert.assertEquals(excepted.size, actual.size)
    (excepted zip actual) map (z => svmAssertEquals(z._1, z._2))
  }

  implicit def svmAssertSeqSupportVectorEquals(excepted: Array[SupportVector], actual: Array[SupportVector]): Unit = {
    org.junit.Assert.assertEquals(excepted.size, actual.size)
    (excepted zip actual) map (z => svmAssertEquals(z._1, z._2))
  }

  implicit def svmAssertSeqSupportVecEquals(excepted: Vec[SupportVector], actual: Vec[SupportVector]): Unit = {
    org.junit.Assert.assertEquals(excepted.size, actual.size)
    (excepted zip actual) map (z => svmAssertEquals(z._1, z._2))
  }
}