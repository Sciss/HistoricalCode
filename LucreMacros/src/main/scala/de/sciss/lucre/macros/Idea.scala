/*

  @txn trait CellData[A] {
    val value: A
    var next: Option[CellData[A]]
  }

  @txn trait ListData[A] {
    var head: Option[CellData[A]]
    var size: Int
  }

  synthetic:

  object CellData {
    trait Impl[S <: Sys[S], A] extends Writable {
      def value: A
      def next: S#Var[Option[CellData.Impl[S, A]]]
    }

    def apply[S <: Sys[S]](value: A, next: Option[CellData.Impl[S, A]])(implicit tx: S#Tx): Impl[S, A] = ???
  }

  object ListData {
    trait Impl[S <: Sys[S], A] extends Writable {
      def head: S#Var[Option[CellData.Impl[S, A]]
      def size: S#Var[Int]
    }

    def apply[S <: Sys[S]](head: Option[CellData.Impl[S, A]], size: Int)(implicit tx: S#Tx): ListData.Impl[S, A]]
  }

  The problem is with the omission of the system type e.g. in `CellData`, this becomes more severe if one wants
  to include other system-parametrised values... E.g. when A = B[S]

  The other problem is IDE support, as probably none of the synthetic `apply` methods will be recognised by IDEA...

  :::: Encapsulation ::::

  object Opaque {
    @impl[OpaqueData] def apply(): Opaque = ???
  }
  trait Opaque {
    def head: String
  }

  @txn trait OpaqueData {
    protected def list: ListData[String]

    def head: String = list.head.getOrElse(throw new NoSuchElementException).value
  }

 */