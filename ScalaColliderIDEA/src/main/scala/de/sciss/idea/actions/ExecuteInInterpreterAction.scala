package de.sciss.idea
package actions

import com.intellij.openapi.ui.Messages
import org.jetbrains.plugins.scala.lang.psi.api.ScalaFile
import annotation.elidable
import com.intellij.psi.util.PsiUtilBase
import com.intellij.openapi.editor.Editor
import Helper._
import com.intellij.openapi.project.Project
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.util.ActionRunner
import org.jetbrains.plugins.scala.console.{ScalaScriptConsoleRunConfigurationFactory, ScalaScriptConsoleRunConfiguration, ScalaScriptConsoleConfigurationType, RunConsoleAction}
import com.intellij.execution.{RunnerAndConfigurationSettings, RunnerRegistry, ExecutionException, ExecutionBundle, RunManagerEx}
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.actionSystem.{DataContext, ActionManager, PlatformDataKeys, DataKey, LangDataKeys, AnActionEvent, AnAction}
import com.intellij.execution.runners.{ProgramRunner, ExecutionEnvironment}
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openapi.util.{Key, TextRange}
import com.intellij.execution.process.{OSProcessHandler, ProcessListener, ProcessEvent, ProcessHandler}
import java.io.{PrintWriter, OutputStreamWriter, DataOutputStream}

object ExecuteInInterpreterAction {
   val verbose = true
   /* @elidable( elidable.INFO ) */ def inform( what: => String ) { if( verbose ) Console.err.println( "ScalaCollider :: ExecuteInInterpreterAction :: " + what )}

   private case class Handler( ph: ProcessHandler, w: PrintWriter )
}
class ExecuteInInterpreterAction extends AnAction( "Execute-In-Interpreter Action" ) {
   import ExecuteInInterpreterAction._

   inform( "instantiated" )

   private var replHandler = Option.empty[ Handler ]

   override def update(e: AnActionEvent): Unit = {
      inform( "update" )
      val presentation = e.getPresentation
      def enable {
         inform( "enable" )
         presentation.setEnabled( true )
         presentation.setVisible( true )
      }
      def disable {
         inform( "disable" )
         presentation.setEnabled( false )
         presentation.setVisible( false )
      }
      try {
         val dataContext = e.getDataContext
         val file = LangDataKeys.PSI_FILE.getData( dataContext )
         file match {
            case _: ScalaFile => enable
            case x            => disable
         }
      }
      catch {
         case e: Exception => disable
      }
   }

   def actionPerformed( e: AnActionEvent ) {
//      Messages.showMessageDialog( "Welcome to //// ScalaCollider", "ScalaCollider", null )
      val context = e.getDataContext
      val project = PlatformDataKeys.PROJECT.getData( context )
      val editor  = PlatformDataKeys.EDITOR.getData( context )
      PsiUtilBase.getPsiFileInEditor( editor, project ) match {
         case _ : ScalaFile => prepareConsole( editor, project )
      }
   }

   private def interpret( h: Handler, editor: Editor ) {
      val sm   = editor.getSelectionModel
      val text = if (sm.hasSelection) {
         sm.getSelectedText
      } else {
         val cm   = editor.getCaretModel
         val doc  = editor.getDocument
         val n    = doc.getLineNumber( cm.getOffset )
         val off1 = doc.getLineStartOffset( n )
         val off2 = doc.getLineEndOffset( n )
         doc.getText( new TextRange( off1, off2 ))
      }
      inform( "interpret " + (if( text.size < 8 ) text else text.substring( 0, 7 ) + "...") )
      if( text != "" ) {
         val text1   = if( text.endsWith( "\n" )) text else text + "\n"
         h.w.print( text1 )
         h.w.flush()
      }
//      Messages.showMessageDialog( text, "ScalaCollider", null )

   }

   private def prepareConsole( editor: Editor, project: Project ) {
//         val am = ActionManager.getInstance
//         val actionRunConsole = am.getAction( "Scala.RunConsole" )
//         inform( "FOUND : " + actionRunConsole )
      inform( "prepare console" )
      replHandler match {
         case Some( h ) => interpret( h, editor )
         case None =>
            openConsole( project ) {
               case oph: OSProcessHandler =>
                  oph.addProcessListener( new ProcessListener {
                     def startNotified( pe: ProcessEvent ) {
                        inform( "startNotified" )
                        val h       = Handler( oph, new PrintWriter( oph.getProcess.getOutputStream ))
                        replHandler = Some( h )
//                        interpret( h, editor )
                     }
                     def processTerminated( pe: ProcessEvent ) {
                        inform( "processTerminated" )
                     }
                     def processWillTerminate( pe: ProcessEvent, willBeDestroyed: Boolean ) {
                        inform( "processWillTerminate " + willBeDestroyed )
                        replHandler = None
                     }
                     def onTextAvailable( pe: ProcessEvent, outputType: Key[_]) {}
                  })
                  inform( "console launched" )

               case ph => inform( "what kind of ProcessHandler is this ? " + ph )
//               val h       = Handler( ph, new DataOutputStream( ph.getProcessInput ))
//               replHandler = Some( h )
//               interpret( h, editor )
            }
      }
//         val am = origin.getActionManager
//         val actionRunConsole = am.getAction( "org.jetbrains.plugins.scala.console.RunConsoleAction" )
//         if( actionRunConsole == null ) {
//            inform( "did not find console action" )
//            return
//         }
//         val actionEvent = new AnActionEvent( null, DataManager.getInstance().getDataContext(myCheckBox),
//                                                              ActionPlaces.UNKNOWN, CheckboxAction.this.getTemplatePresentation(),
//                                                              ActionManager.getInstance(), 0)
//         val callBack = am.tryToExecute( actionRunConsole, origin.getInputEvent, null, origin.getPlace, true )
//         callBack.doWhenDone {
//            inform( "console opened" )
//            didOpenConsole = true
//         }
//         callBack.doWhenProcessed { inform( "console processed" )}
//         callBack.doWhenRejected { inform( "console rejected" )}
//         val actionRunConsole = new RunConsoleAction
   }

   private def openConsole( project: Project )( onSuccess: ProcessHandler => Unit ) {
      inform( "openconsole" )

      val rmEx       = RunManagerEx.getInstanceEx( project )
      val cfgType    = ConfigurationTypeUtil.findConfigurationType( classOf[ ScalaScriptConsoleConfigurationType ])
      val settings   = rmEx.getConfigurationSettings( cfgType )

      def execute( sett: RunnerAndConfigurationSettings, cfg: ScalaScriptConsoleRunConfiguration ) {
         inform( "launching scala console" )
         rmEx.setActiveConfiguration( sett )
         val runExecutor   = DefaultRunExecutor.getRunExecutorInstance
         val runner        = RunnerRegistry.getInstance().getRunner( runExecutor.getId, cfg )
         if( runner != null ) try {
            runner.execute( runExecutor, new ExecutionEnvironment( runner, sett, project ), new ProgramRunner.Callback {
               def processStarted( d: RunContentDescriptor ) {
                  onSuccess( d.getProcessHandler )
               }
            })
         } catch {
            case e: ExecutionException =>
               Messages.showErrorDialog( project, e.getMessage, ExecutionBundle.message( "error.common.title" ))
         }
      }

      settings.headOption.map( sett => sett -> sett.getConfiguration ) match {
         case Some( (sett, cfg: ScalaScriptConsoleRunConfiguration) ) =>
            inform( "found scala console config" )
            ActionRunner.runInsideReadAction { execute( sett, cfg )}

         case None =>   // no settings found -- open dialog instead
            ActionRunner.runInsideReadAction {
               cfgType.getConfigurationFactories.headOption match {
                  case Some( fact: ScalaScriptConsoleRunConfigurationFactory ) =>
                      val sett = RunManagerEx.getInstanceEx( project ).createConfiguration( "Scala Console", fact )
                      rmEx.setTemporaryConfiguration( sett )
                      sett match {
                         case cfg: ScalaScriptConsoleRunConfiguration => execute( sett, cfg )
                         case _ =>
                      }

                  case None =>   // XXX warn?
               }
            }
      }
   }

//    val context = e.getDataContext
//    val project = PlatformDataKeys.PROJECT.getData(context)
//    val editor = PlatformDataKeys.EDITOR.getData(context)
//    val file = PsiUtilBase.getPsiFileInEditor(editor, project)
//    if (!file.isInstanceOf[ScalaFile]) return
//    val scalaFile = file.asInstanceOf[ScalaFile]
//    if (!editor.getSelectionModel.hasSelection) return
//    val selectionStart = editor.getSelectionModel.getSelectionStart
//    val selectionEnd = editor.getSelectionModel.getSelectionEnd
//    val opt = ScalaRefactoringUtil.getExpression(project, editor, file, selectionStart, selectionEnd)
//    opt match {
//      case Some((expr, _)) => {
//        val implicitConversions = expr.getImplicitConversions
//        val funs = implicitConversions._1
//        if (funs.length == 0) return
//        var selectedIndex = -1
//        val conversionFun = implicitConversions._2
//        conversionFun match {
//          case Some(fun) => selectedIndex = funs.findIndexOf(_ == fun)
//          case _ =>
//        }
//        val model: DefaultListModel = new DefaultListModel
//        for (element <- funs) {
//          model.addElement(element)
//        }
//        val list: JList = new JList(model)
//        list.setCellRenderer(new ScImplicitFunctionListCellRenderer(if (selectedIndex == -1) null else funs(selectedIndex)))
//
//        val builder = JBPopupFactory.getInstance.createListPopupBuilder(list)
//        builder.setTitle("Choose implicit conversion method:").
//                setMovable(false).setResizable(false).setRequestFocus(true).
//                setItemChoosenCallback(new Runnable {
//          def run: Unit = {
//            val method = list.getSelectedValue.asInstanceOf[PsiNamedElement]
//            method match {
//              case n: NavigatablePsiElement => n.navigate(true)
//              case _ => //do nothing
//            }
//          }
//        }).createPopup.showInBestPositionFor(editor)
//      }
//      case _ => return
//    }
//   }
}