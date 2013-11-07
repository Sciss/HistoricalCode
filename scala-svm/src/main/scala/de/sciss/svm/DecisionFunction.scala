package de.sciss.svm

case class DecisionFunction(alpha: Vec[Double], rho: Double) {
  def nonZero: Boolean = alpha.exists(_ > 0)
}