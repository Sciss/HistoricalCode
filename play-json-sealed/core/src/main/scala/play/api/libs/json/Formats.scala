package play.api.libs.json

import java.io.File
import collection.immutable.{IndexedSeq => IIdxSeq}

object Formats {
  implicit object File extends Format[File] {
    def reads(json: JsValue): JsResult[File] = json match {
      case s: JsString => JsSuccess(new java.io.File(s.value))
      case _ => JsError(s"Expecting JSON string, but found $json")
    }

    def writes(f: File): JsValue = JsString(f.getPath)
  }

  private final class IndexedSeq[A](implicit elem: Format[A]) extends Format[IIdxSeq[A]] {
    def reads(json: JsValue): JsResult[IIdxSeq[A]] = {
      ???
    }

    def writes(seq: IIdxSeq[A]): JsValue = JsArray(seq.map(elem.writes _))
  }
  implicit def IndexedSeq[A](implicit elem: Format[A]): Format[IIdxSeq[A]] = new IndexedSeq[A]
}