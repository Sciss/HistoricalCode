# ScalaSVM: A Scala implementation of Support Vector Machines

## Statement

This library provides Support Vector Machines for the Scala programming language.

This project started as a [Scala port](https://github.com/zsxwing/scala-svm) of [libSVM](http://www.csie.ntu.edu.tw/~cjlin/libsvm) by Shixiong Zhu and was released under an [MIT style license](https://raw.github.com/zsxwing/scala-svm/master/LICENSE).

This new version is release under the [GNU Lesser General Public License](http://www.gnu.org/licenses/lgpl-2.1.txt) and comes with absolutely no warranties. All changes and extensions from the original version are (C)opyright 2013 by Hanns Holger Rutz. To contact the author, send an email to `contact at sciss.de`

## Requirements

Builds with sbt 0.13 for Scala 2.10.

## Notes

This is incomplete. Only `OneClassTrainer` is implemented and there is a restriction on `numClasses = 2` (?)