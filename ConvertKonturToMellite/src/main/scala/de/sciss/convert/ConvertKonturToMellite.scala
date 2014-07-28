package de.sciss.convert

import de.sciss.file._
import de.sciss.kontur.session.{Timeline => KTimeline, FadeSpec => KFadeSpec, Diffusion, MatrixDiffusion, AudioFileElement, AudioTrack, Session}
import de.sciss.lucre.event.Sys
import de.sciss.lucre.stm
import de.sciss.mellite.{Code, ProcActions, Workspace}
import de.sciss.span.Span
import de.sciss.synth
import de.sciss.synth.io.{AudioFileSpec, AudioFile}
import de.sciss.synth.proc.{DoubleElem, FolderElem, Folder, SynthGraphs, Proc, IntElem, ObjKeys, FadeSpec, Timeline, AudioGraphemeElem, Grapheme, Obj, ArtifactLocation, graph}
import de.sciss.synth.proc.Implicits._
import de.sciss.lucre.expr.{Long => LongEx, Double => DoubleEx, Int => IntEx}
import de.sciss.lucre.bitemp.{SpanLike => SpanLikeEx}

import scala.annotation.tailrec
import scala.collection.breakOut
import scala.util.Try

object ConvertKonturToMellite {
  final val attrTrackIndex  = "track-index"
  final val attrTrackHeight = "track-height"

  case class Config(in: File = file(""), out: File = file(""), trackFactor: Int = 4, skipErrors: Boolean = false,
                    create: Boolean = false, noDiffusions: Boolean = false, verbose: Boolean = false)

  def main(args: Array[String]): Unit = {
    val parser = new scopt.OptionParser[Config]("ConvertKonturToMellite") {
      opt[Unit]('e', "skip-errors") .action { (_, c) =>
        c.copy(skipErrors = true) } .text("skip errors such as missing audio files")

      opt[Int]('t', "track-scale") .action { (v, c) =>
        c.copy(trackFactor = v) } .validate { v =>
        if (v > 0) success else failure("Value <track-scale> must be >0")
      } .text("track index integer scale factor (default: 4)")

      opt[Unit]('c', "create") .action { (_, c) =>
        c.copy(create = true) } .text("create a new workspace")

      opt[Unit]('d', "no-diffusions") .action { (_, c) =>
        c.copy(noDiffusions = true) } .text("do not create output routing processes")

      //      opt[Unit]('a', "existing-artifacts") .action { (_, c) =>
      //        c.copy(existingArtifacts = true) } .text("try to reuse existing artifacts")

      opt[Unit]('v', "verbose") .action { (_, c) =>
        c.copy(verbose = true) } .text("verbose is a flag")

      arg[File]("<in-file>") .action { (x, c) =>
        c.copy(in = x) } .text("input Kontur file")

      arg[File]("<out-file>") .action { (x, c) =>
        c.copy(out = x) } .text("output Mellite file")
    }

    parser.parse(args, Config()).fold(sys.exit(1)) { config =>
      val m = new ConvertKonturToMellite(config)
      m.run()
    }
  }
}

class ConvertKonturToMellite(config: ConvertKonturToMellite.Config) {
  import config._
  import ConvertKonturToMellite.{attrTrackHeight, attrTrackIndex}

  def log(what: => String): Unit = if (verbose) println(s"[log] $what")

  def run(): Unit = {
    log(s"Open Kontur session '${in.name}'")
    val kDoc  = Session.newFrom(in)
    log(s"Open Mellite session '${out.name}'")
    val mDoc  = if (create) {
      require (!out.exists(), s"Cannot overwrite existing workspace '$out'")
      Workspace.Confluent.empty(out)
    } else {
      Workspace.read(out)
    }
    mDoc match {
      case eph: Workspace.Ephemeral => performSys(kDoc, eph)(eph.cursor)
      case con: Workspace.Confluent => performSys(kDoc, con)(con.cursors.cursor)
    }
    log("Done.")
  }

  @tailrec private def parentOption(p: File, child: File): Option[File] =
    if (p == child) Some(p) else p.parentOption match {
      case Some(p1) => parentOption(p1, child)
      case _        => None
    }

  @tailrec private def isParent(p: File, child: File): Boolean = child.parentOption match {
    case Some(p1) if p1 == p => true
    case Some(p1) => isParent(p, p1)
    case _ => false
  }

  private def collectBaseDirs(afs: List[AudioFileElement]): Vector[File] =
    (Vector.empty[File] /: afs) { (m0, p) =>
      val d = p.path.parent
      val i = m0.indexWhere { p1 => parentOption(p1, d).isDefined }
      if (i < 0) m0 :+ d else {
        val p1 = m0(i)
        val Some(p2) = parentOption(p1, d)
        m0.updated(i, p2)
      }
    }

  private def performSys[S <: Sys[S]](in: Session, out: Workspace[S])(implicit cursor: stm.Cursor[S]): Unit = try {
    performSys1(in, out)
  } finally {
    out.close()
  }

  private def performSys1[S <: Sys[S]](in: Session, out: Workspace[S])(implicit cursor: stm.Cursor[S]): Unit = {
    val afs: List[(AudioFileElement, AudioFileSpec)] = in.audioFiles.toList.flatMap { afe =>
      val t = Try(AudioFile.readSpec(afe.path))
      if (t.isFailure) {
        if (skipErrors) {
          Console.err.println(s"Error: could not read audio file ${afe.path}")
        } else {
          t.get
        }
      }
      t.toOption.map(afe -> _)
    }
    val baseDirs = collectBaseDirs(afs.map(_._1))

    //    if (noDiffusions) Nil else in.diffusions.toList.flatMap { d =>
    //      val mOpt = d match {
    //        case md: MatrixDiffusion =>
    //          val m = md.matrix
    //          Some(m)
    //        case _ => None
    //      }
    //
    //    }

    cursor.step { implicit tx =>
      val folder = out.root()

      def mkFolder(name: String): Folder[S] = {
        val res = Folder[S]
        val obj = Obj(FolderElem(res))
        obj.attr.name = name
        folder.addLast(obj)
        res
      }

      lazy val locsFolder       = mkFolder("locations")
      lazy val audioFilesFolder = mkFolder("audio-files")
      lazy val diffsFolder      = mkFolder("routing")
      lazy val timelinesFolder  = mkFolder("timelines")

      val locs = baseDirs.map(ArtifactLocation(_))
      locs.foreach { loc =>
        val obj       = Obj(ArtifactLocation.Elem(loc))
        obj.attr.name = loc.directory.name
        locsFolder.addLast(obj)
        log(s"Add artifact location '${loc.directory}'")
      }

      val afMap: Map[AudioFileElement, Grapheme.Expr.Audio[S]] = afs.map { case (afe, spec) =>
        // name, path, numFrames, numChannels, sampleRate
        val loc     = locs.find(loc => isParent(loc.directory, afe.path)).get
        val art     = loc.add(afe.path)
        // val gr    = Grapheme.Value.Audio(artifact = art, spec = spec, offset = 0L, gain = 1.0)
        val offset  = LongEx  .newConst[S](0L)
        val gain    = DoubleEx.newConst[S](1.0)
        val gr      = Grapheme.Expr.Audio(artifact = art, spec = spec, offset = offset, gain = gain)
        val obj     = Obj(AudioGraphemeElem(gr))
        obj.attr.name = afe.path.base
        log(s"Add audio file '${afe.path.name}'")
        audioFilesFolder.addLast(obj)
        afe -> gr
      } (breakOut)

      val dMap: Map[Diffusion, Proc.Obj[S]] = if (noDiffusions) Map.empty else in.diffusions.toList.flatMap {
        case md: MatrixDiffusion =>
          val obj = mkDiff(md)
          log(s"Add diffusion '${md.name}'")
          diffsFolder.addLast(obj)
          Some((md: Diffusion) -> obj)
        case _ => None
      } (breakOut)

      in.timelines.foreach { tlIn =>
        log(s"Translating timeline '${tlIn.name}'...")
        val tlOut = performTL(tlIn, afMap, dMap)
        timelinesFolder.addLast(tlOut)
      }
    }
  }

  private def mkDiff[S <: Sys[S]](md: MatrixDiffusion)(implicit tx: S#Tx): Proc.Obj[S] = {
    import synth._
    import ugen._

    val g = SynthGraph {
      import graph._
      val gain = attribute("gain").kr(1)
      val mute = attribute("mute").kr(0)
      val amp  = gain * (1 - mute)
      val in   = scan.In("in") * amp
      val m    = Mix.tabulate(md.numInputChannels) { chIn =>
        val inc = in \ chIn
        val sig0: GE = Vector.tabulate(md.numOutputChannels) { chOut =>
          // val ampc = md.matrix(row = chIn, col = chOut)
          val ampc = attribute(s"m${chIn+1}>${chOut+1}").kr(0)
          inc * ampc
        }
        sig0
      }
      val bus = attribute("bus").kr(0)
      Out.ar(bus, m)
    }

    // val sourceM = md.matrix.toSeq.map(row => row.mkString("Vector(", ",", ")")).mkString("Vector(", ",", ")")
    val source =
      raw"""|val gain = attribute("gain").kr(1)
            |val mute = attribute("mute").kr(0)
            |val amp  = gain * (1 - mute)
            |val in   = scan.In("in") * amp
            |val m    = Mix.tabulate(${md.numInputChannels}) { chIn =>
            |  val inc = in \ chIn
            |  val sig0: GE = Vector.tabulate(${md.numOutputChannels}) { chOut =>
            |    val ampc = attribute(s"m$${chIn+1}>$${chOut+1}").kr(0)
            |    inc * ampc
            |  }
            |  sig0
            |}
            |val bus = attribute("bus").kr(0)
            |Out.ar(bus, m)
            |""".stripMargin

    val p       = Proc[S]
    p.graph()   = SynthGraphs.newVar(SynthGraphs.newConst[S](g))
    p.scans.add("in")
    val obj     = Obj(Proc.Elem(p))
    val code    = Obj(Code.Elem(Code.Expr.newVar(Code.Expr.newConst[S](Code.SynthGraph(source)))))
    val attr    = obj.attr
    attr.put(Proc.Obj.attrSource, code)
    attr.name = md.name
    (0 until md.numInputChannels).foreach { chIn =>
      (0 until md.numOutputChannels).foreach { chOut =>
        val v = md.matrix(row = chIn, col = chOut)
        attr.put(s"m${chIn+1}>${chOut+1}", Obj(DoubleElem(DoubleEx.newVar(DoubleEx.newConst[S](v)))))
      }
    }
    obj
  }

  private def performTL[S <: Sys[S]](in: KTimeline, afMap: Map[AudioFileElement, Grapheme.Expr.Audio[S]],
                                     dMap: Map[Diffusion, Proc.Obj[S]])
                                    (implicit tx: S#Tx): Timeline.Obj[S] = {
    val tl    = Timeline[S]
    val ratio = Timeline.SampleRate / in.rate

    def audioToTL(in: Long): Long = (in * ratio + 0.5).toLong

    var diffAdded = Set.empty[Proc.Obj[S]]

    in.tracks.toList.zipWithIndex.foreach {
      case (at: AudioTrack, trkIdxIn) =>
        val dOpt = at.diffusion.flatMap(dMap.get)
        dOpt.foreach { d =>
          if (!diffAdded.contains(d)) {
            diffAdded += d
            log(s"Include diffusion '${d.attr.name}'")
            tl.add(SpanLikeEx.newConst(Span.All), d)
          }
        }

        at.trail.getAll().foreach { ar =>
          // name, span, audioFile, offset, gain, muted, fadeIn, fadeOut
          def mkAudioRegion(af: Grapheme.Expr.Audio[S]): Unit = {
            val start     = audioToTL(ar.span.start)
            val stop      = audioToTL(ar.span.stop )
            val gOffset   = audioToTL(ar.offset    )
            val span      = Span(start, stop)
            val (_, proc) = ProcActions.insertAudioRegion(tl, time = span, grapheme = af, gOffset = gOffset, bus = None)
            proc.attr.name = ar.name
            if (ar.muted) {
              ProcActions.toggleMute(proc)
            }
            if (ar.gain != 1f) {
              ProcActions.adjustGain(proc, ar.gain)
            }

            def mkFade(fadeOpt: Option[KFadeSpec], key: String): Unit = fadeOpt.foreach {
              case fsIn: KFadeSpec if fsIn.numFrames > 0L =>
                val numFrames = audioToTL(fsIn.numFrames)
                val fsOut     = FadeSpec(numFrames, fsIn.shape, fsIn.floor)
                val fsObj     = Obj(FadeSpec.Elem(FadeSpec.Expr.newVar(FadeSpec.Expr.newConst[S](fsOut))))
                proc.attr.put(key, fsObj)

              case _ =>
            }

            mkFade(ar.fadeIn , ObjKeys.attrFadeIn)
            mkFade(ar.fadeOut, ObjKeys.attrFadeOut)

            val trkIdxOut = trkIdxIn * trackFactor
            val trkHOut = trackFactor
            proc.attr.put(attrTrackIndex , Obj(IntElem(IntEx.newVar(IntEx.newConst(trkIdxOut)))))
            proc.attr.put(attrTrackHeight, Obj(IntElem(IntEx.newVar(IntEx.newConst(trkHOut  )))))

            dOpt.foreach { d =>
              val scanIn  = d   .elem.peer.scans.get("in" ).get
              val scanOut = proc.elem.peer.scans.add("out")
              scanOut addSink   scanIn
              scanIn  addSource scanOut
            }
          }

          val afOpt = afMap.get(ar.audioFile)
          afOpt.foreach(mkAudioRegion)

          if (ar.audioFile.sampleRate != in.rate) {
            val txt1 = s"Warning: audio file '${ar.audioFile.name}' has a sample-rate of ${ar.audioFile.sampleRate}Hz"
            val txt2 = s" but was played back at ${in.rate}Hz in Kontur!"
            Console.err.println(s"$txt1$txt2")
          }
        }

      case _ =>
    }

    val tlObj = Obj(Timeline.Elem(tl))
    tlObj.attr.name = in.name
    tlObj
  }
}
