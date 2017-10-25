package de.sciss.citarj2017

import java.awt.event.{MouseAdapter, MouseEvent}

import de.sciss.desktop.FileDialog
import de.sciss.file._
import de.sciss.submin.Submin

import scala.meta._
import scala.swing.{Action, Component, Dimension, Frame, MenuItem, PopupMenu, ScrollPane, Swing}

object Main {
  def main(args: Array[String]): Unit = {
    Submin.install(false)
    runOld() // runNew()
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
    run(srcFiles, prefix = "", dialect = dialects.Scala210)
  }

  def runNew(): Unit = {
    val baseDir   = userHome / "Documents" / "devel" / "Wr_t_ng-M_ch_n_"
    val modules   = Seq("common", /* "control", */ "radio", "sound")
    modules.foreach { mod =>
      val srcFiles = collectScala(baseDir / mod / "src" / "main" / "scala" / "de" / "sciss" / "wrtng")
      run(srcFiles /* .drop(3).take(6) */, prefix = s"$mod.", dialect = dialects.Scala212)
    }
  }

  def run(srcFiles: Seq[File], prefix: String, dialect: Dialect): Unit = {
    var treeNames = Set.empty[String]

    srcFiles.zipWithIndex.foreach { case (f, _) =>
      println(s"\n========== ${f.base} ==========\n")
      val parsed: Parsed[Source] = dialect(f).parse[Source]
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

//      if (fi < 10)
        render(root, title0 = s"$prefix${f.base}")
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

//    println(treeNamesSq.mkString("\n"))
  }

  def nameFor(t: Tree): String = {
    val tn0 = t.getClass.getName // getSimpleName
    val tn1 = tn0.substring(11) // skip `scala.meta.`
    val tn2 = tn1.replace('$', '.')
    val tn  = tn2.substring(0, tn2.lastIndexOf('.'))  // without `...Impl`
    tn
  }

  def getTypeName(t: Tree): String = {
    t match {
      case Pkg(ref, _)                              =>
//       s"package ${getTypeName(ref)}"
       s"package ${ref.toString}"
//      case _: Import                                =>  1
//      case _: Importee                              =>  2
//      case _: Defn                                  =>  3
      case Defn.Class(_, name, _, _, _)     => s"class $name"
      case Defn.Def(_, name, _, _, _, _)    => s"def $name"
      case Defn.Macro(_, name, _, _, _, _)  => s"macro $name"
      case Defn.Object(_, name, _)          => s"object $name"
      case Defn.Trait(_, name, _, _, _  )   => s"trait $name"
      case Defn.Type(_, name, _, _)         => s"type $name"
      case _: Defn.Val              => "val"
      case _: Defn.Var              => "var"
//      case _: Ctor                                  =>  4
//      case _: Decl                                  =>  5
      case Decl.Def(_, name, _, _, _) => name.value
      case Decl.Type(_, name, _, _)   => name.value
//      case _: Enumerator                            =>  6
//      case _: Importer                              => 18
      case Importee.Name(name)        => name.value
      case Importee.Rename(from, to)  => s"${from.value} => ${to.value}"
      case Importee.Unimport(name)    => s"${name.value} => _"
      case Importee.Wildcard()        => "_"
//      case _: Init                                  =>  8
      case Lit(value)                               => if (value == null) "null" else value.toString
      case m: Mod                                   => m match {
        case _: Mod.Abstract        => "abstract"
        case _: Mod.Annot           => "@"
        case _: Mod.Case            => "case"
        case _: Mod.Contravariant   => "-"
        case _: Mod.Covariant       => "+"
        case _: Mod.Final           => "final"
        case _: Mod.Implicit        => "implicit"
        case _: Mod.Inline          => "inline"
        case _: Mod.Lazy            => "lazy"
        case _: Mod.Override        => "override"
        case _: Mod.Private         => "private"
        case _: Mod.Protected       => "protected"
        case _: Mod.Sealed          => "sealed"
        case _: Mod.ValParam        => "val"
        case _: Mod.VarParam        => "var"
        case _ => nameFor(m)
      }
      case Name(value)                              => value
//      case _: Pat                                   => 12
      case _: Case                                  => "case"
//      case _: Term | _: Term.Param                  => 14
//      case Term.Apply(fun, args)                    => args.map(getTypeName).mkString(s"${getTypeName(fun)}(", ", ", ")")
//      case Term.ApplyInfix(lhs, op, _, args)        => args.map(getTypeName).mkString(s"${getTypeName(lhs)} ${getTypeName(op)} (", ", ", ")")
      case Term.Apply(fun, _)                       => s"${getTypeName(fun)}(…)"
      case Term.ApplyInfix(lhs, op, _, _)           => s"${getTypeName(lhs)} ${getTypeName(op)}"
      case Term.ApplyType(fun, _)                   => s"${getTypeName(fun)}[…]"
      case _: Term.Assign                           => "="
      case _: Term.Block                            => "{ … }" // "block"
      case _: Term.Do                               => "do"
      case _: Term.For                              => "for"
      case _: Term.ForYield                         => "yield"
      case Term.Function(_, _)                      => "Function"
      case _: Term.If                               => "if"
      case _: Term.Interpolate                      => "$"
      case _: Term.Match                            => "match"
      case Term.Name(value)                         => value
      case _: Term.New                              => "new"
      case _: Term.NewAnonymous                     => "new { … }"
      case Term.Param(_, name, _, _)                => name.value
      case Term.PartialFunction(_)                  => "PartialFunction"
      case _: Term.Return                           => "return"
      case _: Term.Select                           => "." // "select"
      case _: Term.Super                            => "super"
      case _: Term.This                             => "this"
      case _: Term.Throw                            => "throw"
      case _: Term.While                            => "while"
      case _: Term.Try                              => "try"
      case Self(name, _)                            => name.value
//      case _: Type | _: Type.Bounds | _: Type.Param => 16
//      case _: Template                              => 17
      case _ => nameFor(t)
    }
  }

  def getTypeColor(t: Tree): Int = {
    t match {
      case _: Pkg                                   =>  0
      case _: Pkg.Object                            =>  0
      case _: Import                                =>  1
      case _: Importee                              =>  2
      case _: Defn                                  =>  3
      case _: Ctor                                  =>  4
      case _: Decl                                  =>  5
      case _: Enumerator                            =>  6
      case _: Importer                              => 18
      case _: Init                                  =>  8
      case _: Lit                                   =>  9
      case _: Mod                                   => 10
      case _: Name                                  => 11
      case _: Term.Select                           =>  7
      case _: Term.New                              => 21
      case _: Term.NewAnonymous                     => 21
//      case Term.Name(_)                             => 21
      case _: Pat                                   => 12
      case _: Case                                  => 13
      case _: Term.Block                            => 20
      case _: Term | _: Term.Param                  => 14
      case _: Self                                  => 15
      case _: Type | _: Type.Bounds | _: Type.Param => 16
      case _: Template                              => 17
      case _ =>
        println(s"The fuck? ${nameFor(t)}")
        -1
    }
  }

  def render(meta: Tree, title0: String): Unit = {
    Swing.onEDT {
      import prefuse.data.{Node => PNode, Tree => PTree}
      val t = new PTree
      t.addColumn(TreeMap.COL_TPE, classOf[Int])
      t.addColumn("name", classOf[String])

      def configure(n: PNode, m: Tree): Unit = {
        val tn  = getTypeName(m) // nameFor(m)
        val tpe = getTypeColor(m)
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

      val map   = new TreeMap(t, label = TreeMap.COL_NAME)
      val mapW  = Component.wrap(map)

      new Frame {
        title    = title0
        contents = new ScrollPane(mapW)
        map.setPreferredSize(new Dimension(extent, extent))
        pack().centerOnScreen()
        open()

        override def closeOperation(): Unit = dispose()
      }

      def checkPop(e: MouseEvent): Unit = if (e.isPopupTrigger) {
        val pop = new PopupMenu {
          contents += new MenuItem(new Action("Export as PDF...") {
            def apply(): Unit = {
              val initF = userHome / "Pictures" / s"$title0.pdf"
              val dlg = FileDialog.save(init = Some(initF), title = title)
              dlg.show(None).foreach { f =>
                val size    = map.getSize()
                val width   = size.width
                val height  = size.height // + 8   // XXX TODO --- some bug, last row might be cut
//                import size.{height, width}
                map.saveFrameAsPDF(file = f, width = width, height = height, dpi = 72)

//                val b = map.isDoubleBuffered
//                map.setDoubleBuffered(false)
//
//                val source = new Generate.Source {
//                  val size: Dimension = map.getSize
//                  def preferredSize: Dimension = size
//
//                  def render(g: Graphics2D): Unit = {
//                    val _dsp        = map
//                    val oldWidth    = _dsp.bufWidth
//                    val oldHeight   = _dsp.bufHeight
//                    _dsp.clearBuf()
//                    try {
//                      _dsp.bufWidth   = size.width // width
//                      _dsp.bufHeight  = size.height // height
//                      _dsp.paintComponent(g)
//                    } finally {
//                      _dsp.bufWidth   = oldWidth
//                      _dsp.bufHeight  = oldHeight
//                    }
//                  }
//                }
//
//                Generate(file = f, view = source, usePreferredSize = true, overwrite = true)
//                map.setDoubleBuffered(b)
              }
            }
          })
        }
        pop.show(mapW, e.getX - 18, e.getY - 8)
      }

      map.addMouseListener(new MouseAdapter {
        override def mousePressed (e: MouseEvent): Unit = checkPop(e)
        override def mouseClicked (e: MouseEvent): Unit = checkPop(e)
        override def mouseReleased(e: MouseEvent): Unit = checkPop(e)
      })

    }

//    t match {
//      case Pkg(ref, stats) =>
//        val hd: Stat = stats.head
//    }
  }
}
