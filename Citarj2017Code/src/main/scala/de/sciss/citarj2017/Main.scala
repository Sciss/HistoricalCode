package de.sciss.citarj2017

import de.sciss.file._

import scala.meta.{Decl, _}
import scala.swing.{Component, Dimension, MainFrame, Swing}

object Main {
  def main(args: Array[String]): Unit = {
    runNew()
  }

  def collectScala(in: File): Seq[File] = {
    val sub     = in.children(_.isDirectory)
    val sources = in.children(f => f.isFile && f.ext == "scala")
    sub.flatMap(collectScala) ++ sources
  }

  def runOld(): Unit = {
    val baseDir   = file("/") / "data" / "temp" / "WritingMachine"
    val srcDir    = baseDir / "src" / "main" / "scala"
    val pkgDir    = srcDir / "de" / "sciss" / "grapheme"
    val srcFiles  = collectScala(pkgDir)
    run(srcFiles, dialects.Scala210)
  }

  def runNew(): Unit = {
    val baseDir   = userHome / "Documents" / "devel" / "Wr_t_ng-M_ch_n_"
    val modules   = Seq("common", /* "control", */ "radio", "sound")
    val srcFiles  = modules.flatMap { mod =>
      collectScala(baseDir / mod / "src" / "main" / "scala" / "de" / "sciss" / "wrtng")
    }
    run(srcFiles.drop(3).take(8), dialects.Scala212)
  }

  def run(srcFiles: Seq[File], d: Dialect): Unit = {
    var treeNames = Set.empty[String]

    srcFiles.zipWithIndex.foreach { case (f, fi) =>
      println(s"\n========== ${f.base} ==========\n")
      val parsed: Parsed[Source] = d(f).parse[Source]
      val root: Tree = parsed.get.children.head
//      println(root.children.size)

      def process(t: Tree): Unit = {
        val tn = nameFor(t)
        treeNames += tn
        t.children.foreach(process)
      }

      process(root)

//      println(s"root type = ${root.getClass.getName}")

//      root.children.foreach { t =>
//        println("\n--------------------------\n")
//        println(t.structure)
//      }

      if (fi < 10) render(root, title0 = f.base)
    }

    val treeNamesSq = treeNames.toSeq.sorted
    // 75 for 2011 version of Writing Machine, 90 for 2017 version
    println(s"Number of tree types: ${treeNamesSq.size}")
    // top level - 18 items
    // - Case
    // - Ctor (e.g. Ctor.Primary)
    // - Decl (e.g. Decl.Def)
    // - Defn (e.g. Defn.Def, Defn.Trait)
    // - Enumerator (Enumerator.Generator)
    // - Import
    // - Importee (e.g. Importee.Name, Importee.Rename)
    // - Importer
    // - Init
    // - Lit (e.g. Lit.Boolean, Lit.Char)
    // - Mod (e.g. Mod.Case, Mod.Covariant)
    // - Name (e.g. Name.Anonymous, Name.Indeterminate)
    // - Pat (e.g. Pat.Bind, Pat.Extract)
    // - Pkg (e.g. Pkg -- the type of each root, Pkg.Object)
    // - Self
    // - Template
    // - Term (e.g. Term.Apply, Term.If)
    // - Type (e.g. Type.Apply, Type.Bounds)

    println(treeNamesSq.mkString("\n"))
  }

  def nameFor(t: Tree): String = {
    val tn0 = t.getClass.getName // getSimpleName
    val tn1 = tn0.substring(11) // skip `scala.meta.`
    val tn2 = tn1.replace('$', '.')
    val tn  = tn2.substring(0, tn2.lastIndexOf('.'))  // without `...Impl`
    tn
  }

  def getType(t: Tree): Int = {
    Ctor
    t match {
      case _: Pkg         =>  0
      case _: Import      =>  1
      case _: Importee    =>  2
      case _: Defn        =>  3
      case _: Ctor        =>  4
      case _: Decl        =>  5
      case _: Enumerator  =>  6
      case _: Importer    =>  7
      case _: Init        =>  8
      case _: Lit         =>  9
      case _: Mod         => 10
      case _: Name        => 11
      case _: Pat         => 12
      case _: Case        => 13
      case _: Term        => 14
      case _: Self        => 15
      case _: Type        => 16
      case _: Template    => 17
      case _              => -1
    }
  }

  def render(meta: Tree, title0: String): Unit = {
    Swing.onEDT {
      import prefuse.data.{Tree => PTree, Node => PNode, _}
      val t = new PTree
      t.addColumn(TreeMap.COL_TPE, classOf[Int])
      t.addColumn("name", classOf[String])

      def configure(n: PNode, m: Tree): Unit = {
        val tn  = nameFor(m)
        val tpe = getType(m)
        n.set(TreeMap.COL_NAME, tn )
        n.set(TreeMap.COL_TPE , tpe)
      }

      def process(parent: PNode, cm: Tree): Unit = {
        val c = t.addChild(parent)
        configure(c, cm)
        cm.children.foreach(process(c, _))
      }

      val r = t.addRoot()
      configure(r, meta)
      meta.children.foreach(cm => process(parent = r, cm = cm))

      val numChildren = t.getNodeTable.getTupleCount
      val extent      = (math.sqrt(numChildren) * 32 + 0.5).toInt

      val map = new TreeMap(t, label = TreeMap.COL_NAME)
      new MainFrame {
        title    = title0
        contents = Component.wrap(map)
        map.setPreferredSize(new Dimension(extent, extent))
        pack().centerOnScreen()
        open()
      }
    }

//    t match {
//      case Pkg(ref, stats) =>
//        val hd: Stat = stats.head
//    }
  }
}
