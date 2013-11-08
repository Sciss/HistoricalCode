package de.sciss.svm

case class Solution(
  obj         : Double,
  rho         : Double,
  upperBoundP : Double,
  upperBoundN : Double,
  r           : Double,
  alpha       : Vec[Double])
