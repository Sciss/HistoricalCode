package de.sciss.citarj2017

import de.sciss.file._

import scala.meta._
import scala.swing.{Component, MainFrame, Swing}

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
    run(srcFiles.drop(2).take(1), dialects.Scala212)
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

      if (fi == 0) render(root)
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

  def render(meta: Tree): Unit = {
    Swing.onEDT {
      import prefuse.data.{Tree => PTree, Node => PNode, _}
      val t = new PTree
      t.addColumn("name", classOf[String])
      val r = t.addRoot()
      r.set("name", nameFor(meta))

      def process(parent: PNode, cm: Tree): Unit = {
        val tn = nameFor(cm)
        val c  = t.addChild(parent)
        c.set("name", tn)
        cm.children.foreach(process(c, _))
      }

      meta.children.foreach(cm => process(parent = r, cm = cm))

      val map = new TreeMap(t, label = "name")
      new MainFrame {
        contents = Component.wrap(map)
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
