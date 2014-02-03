/*
 *  Type.scala
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

object SVM {
  def apply(id: String): Type = id /* : @switch */ match {
    case OneClass           .id => OneClass
    case Classification.C   .id => Classification.C
    case Classification.Nu  .id => Classification.Nu
    case Regression.Epsilon .id => Regression.Epsilon
    case Regression.Nu      .id => Regression.Nu
  }

  sealed trait Classification extends Type
  sealed trait Regression     extends Type
  case object  OneClass       extends Type {
    final val name  = "one-class SVM"
    final val id    = "one_class"

    def trainer: train.OneClassTrainer.type = train.OneClassTrainer
  }

  object Classification {
    case object C extends Classification {
      final val name  = "C-SVC"
      final val id    = "c_svc"

      def trainer: Trainer = train.CSVCTrainer
    }
    case object Nu extends Classification {
      final val name  = "Nu-SVC" // "\u03BD-SVC"
      final val id    = "nu_svc"

      def trainer: Trainer = ???
    }
  }

  object Regression {
    case object Epsilon extends Regression {
      final val name  = "Epsilon-SVR" // "\u03F5-SVR"
      final val id    = "epsilon_svr"

      def trainer: Trainer = train.EpsilonSVRTrainer
    }
    case object Nu extends Regression {
      final val name  = "Nu-SVR" // "\u03BD-SVR"
      final val id    = "nu_svr"

      def trainer: Trainer = train.NuSVRTrainer
    }
  }
}
sealed trait Type {
  def name: String
  def trainer: Trainer
  def id: String  // should be compatible with libsvm
}