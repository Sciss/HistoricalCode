package de.sciss.convert

import de.sciss.file._
import de.sciss.kontur.session.{AudioFileElement, AudioTrack, Diffusion, MatrixDiffusion, Session, FadeSpec => KFadeSpec, Timeline => KTimeline}
import de.sciss.lucre.artifact.{Artifact, ArtifactLocation}
import de.sciss.lucre.expr.{DoubleObj, IntObj, LongObj, SpanLikeObj}
import de.sciss.lucre.stm
import de.sciss.lucre.stm.store.BerkeleyDB
import de.sciss.lucre.stm.{Cursor, Folder, Sys}
import de.sciss.mellite.ProcActions
import de.sciss.span.Span
import de.sciss.synth
import de.sciss.synth.io.{AudioFile, AudioFileSpec}
import de.sciss.synth.proc
import de.sciss.synth.proc.Implicits._
import de.sciss.synth.proc.{AudioCue, Code, FadeSpec, ObjKeys, Proc, SoundProcesses, TimeRef, Timeline, Workspace}

import scala.annotation.tailrec
import scala.collection.breakOut
import scala.util.Try

object ConvertKonturToMellite {
  final val attrTrackIndex  = "track-index"
  final val attrTrackHeight = "track-height"

  case class Config(
                     in           : File = file(""),
                     out          : File = file(""),
                     trackFactor  : Int = 4,
                     skipErrors   : Boolean = false,
                     create       : Boolean = false,
                     noDiffusions : Boolean = false,
                     verbose      : Boolean = false,
                     confluent    : Boolean = false,
                     mapAudio     : Map[String, String] = Map.empty
                   )

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

      opt[Unit]('l', "confluent") .action { (_, c) =>
        c.copy(confluent = true) } .text("create confluent workspace")

      arg[File]("<in-file>") .action { (x, c) =>
        c.copy(in = x) } .text("input Kontur file")

      arg[File]("<out-file>") .action { (x, c) =>
        c.copy(out = x) } .text("output Mellite file")

      opt[Map[String,String]]("map").valueName("old1=new1,old2=new2...").action( (x, c) =>
        c.copy(mapAudio = x) ).text("map old audio file paths to new paths")
    }

    parser.parse(args, Config()).fold(sys.exit(1)) { config =>
      val m = new ConvertKonturToMellite(config)
      m.run()
    }
  }
}

class ConvertKonturToMellite(config: ConvertKonturToMellite.Config) {
  import ConvertKonturToMellite.{attrTrackHeight, attrTrackIndex}
  import config._

  def log(what: => String): Unit = if (verbose) println(s"[log] $what")

  def run(): Unit = {
    SoundProcesses.init()

    log(s"Open Kontur session '${in.name}'")
    val kDoc  = Session.newFrom(in)
    log(s"Open Mellite session '${out.name}'")
    import scala.language.existentials
    val mDoc  = if (create) {
      require (!out.exists(), s"Cannot overwrite existing workspace '$out'")
      val ds = BerkeleyDB.factory(out, createIfNecessary = true)
      if (config.confluent) {
        Workspace.Confluent.empty(out, ds)
      } else {
        Workspace.Durable.empty(out, ds)
      }
    } else {
      val ds = BerkeleyDB.factory(out, createIfNecessary = false)
      Workspace.read(out, ds)
    }
    mDoc match {
      case eph: Workspace.Durable   => performSys(kDoc, eph)(eph.cursor.asInstanceOf[Cursor[proc.Durable]])
      case con: Workspace.Confluent => performSys(kDoc, con)(con.cursor.asInstanceOf[Cursor[proc.Confluent]])
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
    out.cursor.step { implicit tx =>
      out.dispose() // close()
    }
  }

  private def performSys1[S <: Sys[S]](in: Session, out: Workspace[S])(implicit cursor: stm.Cursor[S]): Unit = {
    val afs: List[(AudioFileElement, AudioFileSpec)] = in.audioFiles.toList.flatMap { afe =>
      val t = Try {
        val p0 = afe.path.getPath
        val p1 = config.mapAudio.getOrElse(p0, p0)
        AudioFile.readSpec(p1)
      }
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
      val folder = out.root // ()

      def mkFolder(name: String): Folder[S] = {
        val res = Folder[S]()
        val obj = res // Obj(FolderElem(res))
        obj.name = name
        folder.addLast(obj)
        res
      }

      lazy val locsFolder       = mkFolder("locations")
      lazy val audioFilesFolder = mkFolder("audio-files")
      lazy val diffsFolder      = mkFolder("routing")
      lazy val timelinesFolder  = mkFolder("timelines")

      val locs = baseDirs.map(d => ArtifactLocation.newVar(ArtifactLocation.newConst(d)))
      locs.foreach { loc =>
        val obj   = loc // Obj(ArtifactLocation.Elem(loc))
        obj.name  = loc.directory.name
        locsFolder.addLast(obj)
        log(s"Add artifact location '${loc.directory}'")
      }

      val afMap: Map[AudioFileElement, AudioCue.Obj[S]] = afs.map { case (afe, spec) =>
        // name, path, numFrames, numChannels, sampleRate
        val loc     = locs.find(loc => isParent(loc.directory, afe.path)).get
//        val art     = loc.add(afe.path)
        val art     = Artifact(loc, afe.path)
        // val gr    = Grapheme.Value.Audio(artifact = art, spec = spec, offset = 0L, gain = 1.0)
        val offset  = LongObj  .newConst[S](0L)
        val gain    = DoubleObj.newConst[S](1.0)
        val gr      = AudioCue.Obj(artifact = art, spec = spec, offset = offset, gain = gain)
        val obj     = gr // Obj(AudioGraphemeElem(gr))
        obj.name    = afe.path.base
        log(s"Add audio file '${afe.path.name}'")
        audioFilesFolder.addLast(obj)
        afe -> gr
      } (breakOut)

      val dMap: Map[Diffusion, Proc[S]] = if (noDiffusions) Map.empty else in.diffusions.toList.flatMap {
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

  private def mkDiff[S <: Sys[S]](md: MatrixDiffusion)(implicit tx: S#Tx): Proc[S] = {
    import de.sciss.synth.proc.graph.Ops._
    import de.sciss.synth.proc.graph._
    import synth._
    import ugen._

    val g = SynthGraph {
      val gain = "gain".kr(1)
      val mute = "mute".kr(0)
      val amp  = gain * (1 - mute)
      val in   = ScanIn("in") * amp
      val m    = Mix.tabulate(md.numInputChannels) { chIn =>
        val inc = in.out(chIn)
        val sig0: GE = Vector.tabulate(md.numOutputChannels) { chOut =>
          val ampc = s"m${chIn+1}>${chOut+1}".kr(0)
          inc * ampc
        }
        sig0
      }
      val bus = "bus".kr(0)
      Out.ar(bus, m)
    }

    // val sourceM = md.matrix.toSeq.map(row => row.mkString("Vector(", ",", ")")).mkString("Vector(", ",", ")")
    val source =
      raw"""|val gain = "gain".kr(1)
            |val mute = "mute".kr(0)
            |val amp  = gain * (1 - mute)
            |val in   = ScanIn("in") * amp
            |val m    = Mix.tabulate(${md.numInputChannels}) { chIn =>
            |  val inc = in.out(chIn)
            |  val sig0: GE = Vector.tabulate(${md.numOutputChannels}) { chOut =>
            |    val ampc = s"m$${chIn+1}>$${chOut+1}".kr(0)
            |    inc * ampc
            |  }
            |  sig0
            |}
            |val bus = "bus".kr(0)
            |Out.ar(bus, m)
            |""".stripMargin

    val p       = Proc[S]()
    p.graph()   = g // SynthGraphs.newVar(SynthGraphs.newConst[S](g))
//    p.scans.add("in")
    p.attr.put("in", Folder[S]())
    val obj     = p // Obj(Proc.Elem(p))
    val code    = Code.Obj.newVar(Code.Obj.newConst[S](Code.SynthGraph(source)))
    val attr    = obj.attr
    attr.put(Proc.attrSource, code)
    obj.name = md.name
    (0 until md.numInputChannels).foreach { chIn =>
      (0 until md.numOutputChannels).foreach { chOut =>
        val v = md.matrix(row = chIn, col = chOut)
        attr.put(s"m${chIn+1}>${chOut+1}", DoubleObj.newVar(DoubleObj.newConst[S](v)))
      }
    }
    obj
  }

  private def performTL[S <: Sys[S]](in: KTimeline, afMap: Map[AudioFileElement, AudioCue.Obj[S]],
                                     dMap: Map[Diffusion, Proc[S]])
                                    (implicit tx: S#Tx): Timeline[S] = {
    val tl    = Timeline[S]()
    val ratio = TimeRef.SampleRate / in.rate

    def audioToTL(in: Long): Long = (in * ratio + 0.5).toLong

    var diffAdded = Set.empty[Proc[S]]

    in.tracks.toList.zipWithIndex.foreach {
      case (at: AudioTrack, trkIdxIn) =>
        val dOpt = at.diffusion.flatMap(dMap.get)
        dOpt.foreach { d =>
          if (!diffAdded.contains(d)) {
            diffAdded += d
            log(s"Include diffusion '${d.name}'")
            tl.add(SpanLikeObj.newConst(Span.All), d)
          }
        }

        at.trail.getAll().foreach { ar =>
          // name, span, audioFile, offset, gain, muted, fadeIn, fadeOut
          def mkAudioRegion(af: AudioCue.Obj[S]): Unit = {
            val start     = audioToTL(ar.span.start)
            val stop      = audioToTL(ar.span.stop )
            val gOffset   = audioToTL(ar.offset    )
            val span      = Span(start, stop)
            val (_, proc) = ProcActions.insertAudioRegion(tl, time = span, audioCue = af, gOffset = gOffset)
            proc.name     = ar.name
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
                val fsObj     = FadeSpec.Obj.newVar(FadeSpec.Obj.newConst[S](fsOut))
                proc.attr.put(key, fsObj)

              case _ =>
            }

            mkFade(ar.fadeIn , ObjKeys.attrFadeIn)
            mkFade(ar.fadeOut, ObjKeys.attrFadeOut)

            val trkIdxOut = trkIdxIn * trackFactor
            val trkHOut = trackFactor
            proc.attr.put(attrTrackIndex , IntObj.newVar(IntObj.newConst(trkIdxOut)))
            proc.attr.put(attrTrackHeight, IntObj.newVar(IntObj.newConst(trkHOut  )))

            dOpt.foreach { d =>
              val scanIn  = d.attr.$[Folder]("in" ).get
              val scanOut = proc.outputs.add("out")
//              scanOut addSink   scanIn
//              scanIn  addSource scanOut
              scanIn.addLast(scanOut)
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

    val tlObj = tl // Timeline(tl))
    tlObj.name = in.name
    tlObj
  }
}
