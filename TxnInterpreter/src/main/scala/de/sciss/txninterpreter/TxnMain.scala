package de.sciss.txninterpreter

import tools.nsc
import nsc.interpreter
import interpreter.{IMain, IR, isReplDebug, isReplPower}

final class TxnMain(set: nsc.Settings) extends IMain(set) {
  def interpretTxn(line: String): IR.Result = {
    interpretTxn(line, synthetic = false)
  }

  def interpretTxn(line: String, synthetic: Boolean): IR.Result = {
    def loadAndRunReq(req: Request) = {
      classLoader.setAsContext()
      val (result, succeeded) = req.loadAndRun

      /** To our displeasure, ConsoleReporter offers only printMessage,
       *  which tacks a newline on the end.  Since that breaks all the
       *  output checking, we have to take one off to balance.
       */
      if (succeeded) {
        if (printResultsTxn && result != "")
          reporter.printMessage(result stripSuffix "\n")
        else if (isReplDebug) // show quiet-mode activity
          reporter.printMessage(result.trim.lines map ("[quiet] " + _) mkString "\n")

        // Book-keeping.  Have to record synthetic requests too,
        // as they may have been issued for information, e.g. :type
        recordRequest(req)
        IR.Success
      }
      else {
        // don't truncate stack traces
        reporter.withoutTruncating(reporter.printMessage(result))
        IR.Error
      }
    }

    if (global == null) IR.Error
    else requestFromTxnLine(line, synthetic) match {
      case Left(result) => result
      case Right(req)   =>
        // null indicates a disallowed statement type; otherwise compile and
        // fail if false (implying e.g. a type error)
        if (req == null || !req.compile) IR.Error
        else loadAndRunReq(req)
    }
  }

//  override protected def parentClassLoader: ClassLoader = classOf[TxnMain].getClassLoader

  // ---------- need to repeat stuff that is private in IMain ----------

  private def echo(msg: String) { Console println msg }
  private def repldbg(msg: => String) { if (isReplDebug) echo(msg) }
  private def tquoted(s: String) = "\"\"\"" + s + "\"\"\""
  private var printResultsTxn = true

  /** Temporarily be quiet */
  override def beQuietDuring[T](body: => T): T = {
    val saved = printResultsTxn
    printResultsTxn = false
    try {
      super.beQuietDuring(body)
    } finally {
      printResultsTxn = saved
    }
  }

  private def safePos(t: global.Tree, alt: Int): Int =
    try t.pos.startOrPoint
    catch { case _: UnsupportedOperationException => alt }

  // Given an expression like 10 * 10 * 10 we receive the parent tree positioned
  // at a '*'.  So look at each subtree and find the earliest of all positions.
  private def earliestPosition(tree: global.Tree): Int = {
    var pos = Int.MaxValue
    tree foreach { t =>
      pos = math.min(pos, safePos(t, Int.MaxValue))
    }
    pos
  }

  // ---------- ----------

  // ---------- overrides ----------
  private var txnExecutingRequest: Request = _

  override def requestForReqId(id: Int): Option[Request] =
    if (txnExecutingRequest != null && txnExecutingRequest.reqId == id) Some(txnExecutingRequest)
    else super.requestForReqId(id)

//  import global.?
//  import naming.?
//  import memberHandlers.?

  private def requestFromTxnLine(line: String, synthetic: Boolean): Either[IR.Result, Request] = {
    import formatting.indentCode

    val content = indentCode(line)
    val trees = parse(content) match {
      case None         => return Left(IR.Incomplete)
      case Some(Nil)    => return Left(IR.Error) // parse error or empty input
      case Some(t)      => t
    }
//    repltrace( ... }

    // If the last tree is a bare expression, pinpoint where it begins using the
    // AST node position and snap the line off there.  Rewrite the code embodied
    // by the last tree as a ValDef instead, so we can access the value.

    import global.{Assign, TermTree, Ident, Select}

    trees.last match {
      case _:Assign                        => // we don't want to include assignments
      case _:TermTree | _:Ident | _:Select => // ... but do want other unnamed terms.
        val varName  = if (synthetic) naming.freshInternalVarName() else naming.freshUserVarName()
        val rewrittenLine = (
          // In theory this would come out the same without the 1-specific test, but
          // it's a cushion against any more sneaky parse-tree position vs. code mismatches:
          // this way such issues will only arise on multiple-statement repl input lines,
          // which most people don't use.
          if (trees.size == 1) "val " + varName + " =\n" + content
          else {
            // The position of the last tree
            val lastpos0 = earliestPosition(trees.last)
            // Oh boy, the parser throws away parens so "(2+2)" is mispositioned,
            // with increasingly hard to decipher positions as we move on to "() => 5",
            // (x: Int) => x + 1, and more.  So I abandon attempts to finesse and just
            // look for semicolons and newlines, which I'm sure is also buggy.
            val (raw1, raw2) = content splitAt lastpos0
            repldbg("[raw] " + raw1 + "   <--->   " + raw2)

            val adjustment = (raw1.reverse takeWhile (ch => (ch != ';') && (ch != '\n'))).size
            val lastpos = lastpos0 - adjustment

            // the source code split at the laboriously determined position.
            val (l1, l2) = content splitAt lastpos
            repldbg("[adj] " + l1 + "   <--->   " + l2)

            val prefix   = if (l1.trim == "") "" else l1 + ";\n"
            // Note to self: val source needs to have this precise structure so that
            // error messages print the user-submitted part without the "val res0 = " part.
            val combined   = prefix + "val " + varName + " =\n" + l2

            repldbg(List(
              "    line" -> line,
              " content" -> content,
              "     was" -> l2,
              "combined" -> combined) map {
                case (label, s) => label + ": '" + s + "'"
              } mkString "\n"
            )
            combined
          }
        )
        // Rewriting    "foo ; bar ; 123"
        // to           "foo ; bar ; val resXX = 123"
        requestFromTxnLine(rewrittenLine, synthetic) match {
          case Right(req) => return Right(req withOriginalLine line)
          case x          => return x
        }
      case _ =>
    }
    Right(buildTxnRequest(line, trees))
  }

  /** Build a request from the user. `trees` is `line` after being parsed.
   */
  private def buildTxnRequest(line: String, trees: List[global.Tree]): Request = {
    txnExecutingRequest = new TxnRequest(line, trees)
    txnExecutingRequest
  }

  private final class TxnRequest(_line: String, _trees: List[global.Tree]) extends Request(_line, _trees) {
    request: Request =>

    import memberHandlers.MemberHandler
    import IMain.CodeAssembler

    /** generate the source code for the object that computes this request */
    private object TxnObjectSourceCode extends CodeAssembler[MemberHandler] {
      def path = pathToTerm("$intp")
      def envLines = {
        if (!isReplPower) Nil // power mode only for now
        // $intp is not bound; punt, but include the line.
        else if (path == "$intp") List(
          "def $line = " + tquoted(originalLine),
          "def $trees = Nil"
        )
        else List(
          "def $line  = " + tquoted(originalLine),
          "def $req = %s.requestForReqId(%s).orNull".format(path, reqId),
          "def $trees = if ($req eq null) Nil else $req.trees".format(lineRep.readName, path, reqId)
        )
      }

      val preamble = """
        |object %s {
        |%s%s%s
      """.stripMargin.format(lineRep.readName, envLines.map("  " + _ + ";\n").mkString, importsPreamble, formatting.indentCode(toCompute))
      val postamble = importsTrailer + "\n}"
      val generate = (m: MemberHandler) => m extraCodeToEvaluate request
    }

    private object TxnResultObjectSourceCode extends CodeAssembler[MemberHandler] {
      /** We only want to generate this code when the result
       *  is a value which can be referred to as-is.
       */
      val evalResult =
        if (!handlers.last.definesValue) ""
        else handlers.last.definesTerm match {
          case Some(vname) if typeOf contains vname =>
            "lazy val %s = %s".format(lineRep.resultName, request.fullPath(vname))
          case _  => ""
        }
      // first line evaluates object to make sure constructor is run
      // initial "" so later code can uniformly be: + etc
      val preamble = """
      |object %s {
      |  %s
      |  val %s: String = concurrent.stm.atomic { _ => %s {
      |    %s
      |    (""
      """.stripMargin.format(
        lineRep.evalName, evalResult, lineRep.printName,
        executionWrapper, lineRep.readName + accessPath
      )

      val postamble = """
      |    )
      |  }}
      |}
      """.stripMargin
      val generate = (m: MemberHandler) => m resultExtractionCode request
    }

    /** Compile the object file.  Returns whether the compilation succeeded.
     *  If all goes well, the "types" map is computed. */
    override lazy val compile: Boolean = {
      // error counting is wrong, hence interpreter may overlook failure - so we reset
      reporter.reset()

      // compile the object containing the user's code
      lineRep.compile(TxnObjectSourceCode(handlers)) && {
        // extract and remember types
        typeOf
        typesOfDefinedTerms

        // Assign symbols to the original trees
        // TODO - just use the new trees.
        defHandlers foreach { dh =>
          val name = dh.member.name
          definedSymbols get name foreach { sym =>
            dh.member setSymbol sym
            repldbg("Set symbol of " + name + " to " + sym.defString)
          }
        }

        // compile the result-extraction object
        withoutWarnings(lineRep compile TxnResultObjectSourceCode(handlers))
      }
    }
  }
}