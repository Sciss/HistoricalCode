package de.sciss.funser

final class MemoryClassLoader extends ClassLoader {
  private[this] var setAdded    = Set.empty[String]
  private[this] var mapClasses  = Map.empty[String, Array[Byte]]
  private[this] val DEBUG       = false

  def add(name: String, jar: Array[Byte]): Unit = {
    val isNew = !setAdded.contains(name)
    if (DEBUG) println(s"ActionImpl: Class loader add '$name' - isNew? $isNew")
    if (isNew) {
      setAdded += name
      val entries = Code.unpackJar(jar)
      if (DEBUG) {
        entries.foreach { case (n, _) =>
          println(s"...'$n'")
        }
      }
      mapClasses ++= entries
    }
  }

  override protected def findClass(name: String): Class[_] =
    mapClasses.get(name).map { bytes =>
      if (DEBUG) println(s"ActionImpl: Class loader: defineClass '$name'")
      defineClass(name, bytes, 0, bytes.length)

    } .getOrElse {
      if (DEBUG) println(s"ActionImpl: Class loader: not found '$name' - calling super")
      super.findClass(name) // throws exception
    }
}
