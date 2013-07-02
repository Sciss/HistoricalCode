package play.api.libs.json

import java.io.File
import collection.immutable.{IndexedSeq => IIdxSeq}

object Formats {
  implicit object FileFormat extends Format[File] {
    def reads(json: JsValue): JsResult[File] = json match {
      case s: JsString => JsSuccess(new java.io.File(s.value))
      case _ => JsError(s"Expecting JSON string, but found $json")
    }

    def writes(f: File): JsValue = JsString(f.getPath)
  }

  //  private final class IndexedSeqFormat[A](implicit elem: Format[A]) extends Format[IIdxSeq[A]] {
  //    def reads(json: JsValue): JsResult[IIdxSeq[A]] = {
  //      ???
  //    }
  //
  //    def writes(seq: IIdxSeq[A]): JsValue = JsArray(seq.map(elem.writes _))
  //  }
  //  implicit def IndexedSeqFormat[A](implicit elem: Format[A]): Format[IIdxSeq[A]] = new IndexedSeqFormat[A]

  private final class Tuple2[T1, T2](implicit _1: Format[T1], _2: Format[T2]) extends Format[(T1, T2)] {
    def reads(json: JsValue): JsResult[(T1, T2)] = json match {
      case arr: JsArray =>
        for {
          _1r <- _1.reads(arr(0))
          _2r <- _2.reads(arr(1))
        } yield
          (_1r, _2r)

      case _ => JsError(s"Expected JSON Array but found $json")
    }

    def writes(tup: (T1, T2)): JsValue = {
      val _1w = _1.writes(tup._1)
      val _2w = _2.writes(tup._2)
      JsArray(Seq(_1w, _2w))
    }
  }

  implicit def Tuple2Format[T1, T2](implicit _1: Format[T1], _2: Format[T2]): Format[(T1, T2)] = new Tuple2[T1, T2]
}