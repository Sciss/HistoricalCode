package de.sciss.svm

object SVM {
  sealed trait Classification extends Type
  sealed trait Regression     extends Type
  case object  OneClass       extends Type {
    final val name = "one-class SVM"

    def trainer: Trainer = train.OneClassTrainer
  }

  object Classification {
    case object C extends Classification {
      final val name = "C-SVC"

      def trainer: Trainer = ???
    }
    case object Nu extends Classification {
      final val name = "Nu-SVC" // "\u03BD-SVC"

      def trainer: Trainer = ???
    }
  }

  object Regression {
    case object Epsilon extends Regression {
      final val name = "Epsilon-SVR" // "\u03F5-SVR"

      def trainer: Trainer = train.EpsilonSVRTrainer
    }
    case object Nu extends Regression {
      final val name = "Nu-SVR" // "\u03BD-SVR"

      def trainer: Trainer = train.NuSVRTrainer
    }
  }
}
sealed trait Type {
  def name: String
  def trainer: Trainer
}