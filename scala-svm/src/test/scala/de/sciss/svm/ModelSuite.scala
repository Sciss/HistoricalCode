package de.sciss.svm

import org.scalatest._
import org.junit.Assert._

/*
  to run only this suite:

  test-only de.sciss.svm.ModelSuite
 */
class ModelSuite extends FunSuite with BeforeAndAfter {
  val DELTA = 10e-6

  test("OneClassModel") {
    // ./svm-train -s 2 -t 0 -r 0.2 test.data test.model
    val param = new Parameters(LinearKernel, 0, 0)
    val supportVector = Vec(
      SupportVector.fromString("1:1 2:22.08 3:11.46"),
      SupportVector.fromString("1:19"))
    val coefficientVector = Vec(0.3627269429755065, 0.6372730570244935)
    val m = new model.OneClassModel(param, supportVector, coefficientVector, 236.947)

    assertPredictInstance("+1 1:1.000000 2:22.080000 3:11.460000", m)
    assertPredictInstance("+1 1:19", m)
  }

  test("NuSVRModel") {
    // ./svm-train -s 4 -t 0 -r 0.2 test.data test.model
    val param = new Parameters(LinearKernel, 0, 0)
    val supportVector = Vec(
      SupportVector.fromString("1:1 2:22.08 3:11.46"),
      SupportVector.fromString("1:19"))
    val coefficientVector = Vec(-0.002121210192839229, 0.002121210192839219)
    val m = new model.NuSVRModel(param, supportVector, coefficientVector, -0.274546)

    assertPredictInstance("-1 1:1.000000 2:22.080000 3:11.460000", m)
    assertPredictInstance("+1 1:19", m)
  }

  test("EpsilonSVRModel") {
    // ./svm-train -s 3 -t 0 -r 0.2 test.data test.model
    val param = new Parameters(LinearKernel, 0, 0)
    val supportVector = Vec(
      SupportVector.fromString("1:19"),
      SupportVector.fromString("1:1 2:22.08 3:11.46"))
    val coefficientVector = Vec(0.002121210192839219, -0.002121210192839219)
    val m = new model.EpsilonSVRModel(param, supportVector, coefficientVector, -0.274546)

    assertPredictInstance("-1 1:1.000000 2:22.080000 3:11.460000", m)
    assertPredictInstance("+1 1:19", m)
  }

  test("CSVCModel") {
    // ./svm-train -s 0 -t 0 -r 0.2 test.data test.model
    val param = new Parameters(LinearKernel, 0, 0)
    val supportVector = Vec(
      Vec(SupportVector.fromString("1:1 2:22.08 3:11.46")),
      Vec(SupportVector.fromString("1:19")),
      Vec(SupportVector.fromString("2:34 3:15")),
      Vec(SupportVector.fromString("1:10 3:22")))
    val coefficients = Coefficients(
      Vec(
        Vec(
          null,
          (Vec(0.002121210192839219), Vec(-0.002121210192839219)),
          (Vec(0.01285198370368465 ), Vec(-0.01285198370368465 )),
          (Vec(0.002942829648420142), Vec(-0.002942829648420142))),
        Vec(
          null,
          null,
          (Vec(0.001148105625717566), Vec(-0.001148105625717566)),
          (Vec(0.003539823008849557), Vec(-0.003539823008849557))),
        Vec(
          null,
          null,
          null,
          (Vec(0.001532567049808429), Vec(-0.001532567049808429)))))
    val rho   = Vec(0.274546, -4.89109, 0.052762, -0.585534, -0.39469, 0.610728)
    val label = 1 to 4
    val m     = new model.CSVCModel(/* 4, */ param, supportVector, coefficients,
      rho = rho, label = label, probA = Vec.empty, probB = Vec.empty)
    assertPredictInstance("1 1:1.000000 2:22.080000 3:11.460000", m)
    assertPredictInstance("2 1:19", m)
    assertPredictInstance("3 2:34 3:15", m)
    assertPredictInstance("4 1:10 3:22", m)
  }

  test("NuSVCModel") {
    // ./svm-train -s 1 -t 0 -r 0.2 test.data test.model
    val param = new Parameters(LinearKernel, 0, 0)
    val supportVector = Vec(
      Vec(SupportVector.fromString("2:13.08 3:7")),
      Vec(SupportVector.fromString("1:19")),
      Vec(SupportVector.fromString("2:34 3:15"), SupportVector.fromString("2:-1")),
      Vec(SupportVector.fromString("1:10 3:22")))
    val coefficients = new Coefficients(
      Vec(
        Vec(
          null,
          (Vec(0.003441828960374912), Vec(-0.002121210192839219)),
          (Vec(1.934584364555769   ), Vec(-0.7975824366423717  , -1.137001927913397)),
          (Vec(0.004031555793506938), Vec(-0.004031555793506938))),
        Vec(
          null,
          null,
          (Vec(0.005537785840454479), Vec(-0.0001336706927006254, -0.005404115147753854)),
          (Vec(0.003539823008849557), Vec(-0.003539823008849557 ))),
        Vec(
          null,
          null,
          null,
          (Vec(0.001020943323660012, 0.003034858921016748), Vec(-0.00405580224467676)))))
    val rho   = Vec(-0.2425, 1.67644, -0.733569, 0.999141, -0.39469, -1.03168)
    val label = 1 to 4
    val m     = new model.NuSVCModel(/* 4, */ param, supportVector, coefficients,
      rho = rho, label = label, probA = Vec.empty, probB = Vec.empty)

    assertPredictInstance("1 1:1.000000 2:22.080000 3:11.460000", m)
    assertPredictInstance("1 2:13.080000 3:7.0000", m)
    assertPredictInstance("2 1:19", m)
    assertPredictInstance("3 2:34 3:15", m)
    assertPredictInstance("3 2:-1", m)
    assertPredictInstance("4 1:10 3:22", m)
  }

  def assertPredictInstance(testCase: String, model: Model): Unit = {
    val instance = Instance.fromString(testCase)
    val label = model.predict(instance)
    assertEquals(instance.y, label, DELTA)
  }
}