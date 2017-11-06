package de.sciss.citarj2017

import de.sciss.file._
import de.sciss.packing2d.Packer.Algorithm

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.meta.{Dialect, Parsed, Source, Tree, dialects}
import scala.swing.{Component, Frame, ScrollPane, Swing}

object Tableau {
  final case class Config(
       inputs     : Seq[File] = Nil,
       output     : File      = file("output.png"),
       dialect    : String    = "Scala212",
       width      : Int       = 1600,
       height     : Int       = 0,
       pad        : Int       = 4,
       tab        : Int       = 4,
       algorithm: Algorithm   = Algorithm.FIRST_FIT_DECREASING_HEIGHT
     )

  private def algoNames: Seq[String] = Algorithm.values().map(_.name())

  private lazy val mapDialect: Map[String, Dialect] = {
    import dialects._
    Map(
      "Dotty"                 -> Dotty,
      "Paradise211"           -> Paradise211,
      "Paradise212"           -> Paradise212,
      "ParadiseTypelevel211"  -> ParadiseTypelevel211,
      "ParadiseTypelevel212"  -> ParadiseTypelevel212,
      "Sbt0136"               -> Sbt0136,
      "Sbt0137"               -> Sbt0137,
      "Sbt1"                  -> Sbt1,
      "Scala210"              -> Scala210,
      "Scala211"              -> Scala211,
      "Scala212"              -> Scala212,
      "Typelevel211"          -> Typelevel211,
      "Typelevel212"          -> Typelevel212
    )
  }

  def main(args: Array[String]): Unit = {
    val default = Config()

    val p = new scopt.OptionParser[Config]("PackImages") {
      arg[File]("inputs")
        .text (s"Base input directory of .scala files, or individual scala files")
        .required()
        .action { (v, c) => c.copy(inputs = c.inputs :+ v) }

      opt[File]('o', "output")
        .text ("Output png image path")
        .required()
        .action { (v, c) => c.copy(output = v) }

      opt[String]('d', "dialect")
        .text (mapDialect.keysIterator.mkString("Scala dialect (one of: ", ", ", s"; default: ${default.dialect})"))
        .validate { v =>
          if (mapDialect.contains(v)) success
          else failure(mapDialect.keysIterator.mkString("Unknown dialect. must be one of ", ", ", ""))
        }
        .action { (v, c) => c.copy(dialect = v) }

      opt[Int]('w', "width")
        .text (s"Output image width in pixels (default: ${default.width})")
        .validate { v => if (v > 0) success else failure("Must be > 0") }
        .action { (v, c) => c.copy(width = v) }

      opt[Int]('h', "height")
        .text (s"Output image height in pixels (default: ${default.height})")
        .validate { v => if (v >= 0) success else failure("Must be >= 0") }
        .action { (v, c) => c.copy(height = v) }

      opt[Int]("pad")
        .text (s"Padding around each image in pixels (default: ${default.pad})")
        .validate { v => if (v >= 0) success else failure("Must be >= 0") }
        .action { (v, c) => c.copy(pad = v) }

      opt[Int]("tab")
        .text (s"Margin around output image in pixels (default: ${default.tab})")
        .validate { v => if (v >= 0) success else failure("Must be >= 0") }
        .action { (v, c) => c.copy(tab = v) }

      opt[String]('a', "algorithm")
        .text (s"Algorithm (default: ${default.algorithm})")
        .validate { v => if (Algorithm.values().exists(_.name() == v)) success else failure(s"None of ${algoNames.mkString(", ")}") }
        .action { (v, c) => c.copy(algorithm = Algorithm.valueOf(v)) }
    }
    p.parse(args, default).fold(sys.exit(1))(run)
  }

  def run(config: Config): Unit = /* Swing.onEDT */ {
    val sources = config.inputs.flatMap { f =>
      if (f.isDirectory) ViewTreeMap.collectScala(f).sorted(File.NameOrdering) else f :: Nil
    }
    val dialect = mapDialect(config.dialect)

    val tempDir = File.createTemp(directory = true)
    val imageFiles = sources.map { fSrc =>
      val fOut                    = tempDir / s"${fSrc.base}.png"
      val parsed: Parsed[Source]  = dialect(fSrc).parse[Source]
      val root: Tree              = parsed.get.children.head
      val tm                      = ViewTreeMap.mkMap(root)
//      val numChildren             = tm.t.getNodeTable.getTupleCount
//      val extent                  = (math.sqrt(numChildren) * 32 + 0.5).toInt
//      tm.runLayout()
//      tm.mkLayout()

//      import scala.concurrent.ExecutionContext.Implicits.global

      val frame = new Frame {
        contents = new ScrollPane(Component.wrap(tm))
        pack() // .open()
      }

//      while({
        val fut = tm.runWith[Boolean] {
          val bounds = tm.rootBounds
          val ok = bounds.getWidth > 20 && bounds.getHeight > 20  // WTF -- prefuse horrible scheduling
          val dw = math.ceil(bounds.getWidth).toInt
          val dh = math.ceil(bounds.getHeight).toInt
          if (ok) {
            println(s"${fOut.name} - width $dw, height $dh")
            tm.saveFrameAsBitmap(fOut, width = dw, height = dh)
            // Swing.onEDT {
              frame.dispose()
            // }
          } else {
            println(s"Illegal bounds $dw, $dh")
          }
          ok
        }

//        Thread.sleep(100)
        Await.result(fut, Duration.Inf)
//      }) ()

      fOut
    }

    val packConfig = PackImages.Config(
      imageDir  = tempDir,
      output    = config.output,
      width     = config.width,
      height    = config.height,
      pad       = config.pad,
      tab       = config.tab,
      algorithm = config.algorithm
    )

    PackImages.run(packConfig)
    imageFiles.foreach(_.delete())
    sys.exit()
  }
}
