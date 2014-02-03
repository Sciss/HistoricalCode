package de.sciss.svm

case class Coefficients(coefficients: Vec[Vec[ClassifierCoefficient]]) {
  def get(classifier: Classifier): ClassifierCoefficient =
    coefficients(classifier._1)(classifier._2)
}