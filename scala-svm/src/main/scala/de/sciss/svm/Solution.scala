/*
 *  Solution.scala
 *  (ScalaSVM)
 *
 *  Copyright (c) 2013-2014 Hanns Holger Rutz. All rights reserved.
 *  Copyright (c) 2013-2014 Shixiong Zhu.
 *
 *	This software is published under the GNU Lesser General Public License v3+
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.svm

case class Solution(
  obj         : Double,
  rho         : Double,
  upperBoundP : Double,
  upperBoundN : Double,
  r           : Double,
  alpha       : Vec[Double])
