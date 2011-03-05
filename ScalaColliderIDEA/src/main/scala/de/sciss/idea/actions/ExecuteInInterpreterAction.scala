package de.sciss.idea.actions

import com.intellij.openapi.ui.Messages
import org.jetbrains.plugins.scala.lang.psi.api.ScalaFile
import com.intellij.openapi.actionSystem.{DataKey, LangDataKeys, AnActionEvent, AnAction}

class ExecuteInInterpreterAction extends AnAction( "Execute-In-Interpreter Action" ) {
   Console.err.println( "---HERE----")

   override def update(e: AnActionEvent): Unit = {
      Console.err.println( "---GAGA----")
//      val presentation = e.getPresentation
//      def enable {
//         presentation.setEnabled( true )
//         presentation.setVisible( true )
//      }
//      def disable {
//         presentation.setEnabled( false )
//         presentation.setVisible( false )
//      }
//      try {
//         val dataContext = e.getDataContext
////         val file = DataKey.get .getData( LangDataKeys.PSI_FILE )
//         val file = LangDataKeys.PSI_FILE.getData( dataContext )
//         file match {
//            case _: ScalaFile => enable
//            case _ => disable
//         }
//      }
//      catch {
//         case e: Exception => disable
//      }
   }

   def actionPerformed(e: AnActionEvent): Unit = {
      Messages.showMessageDialog( "Welcome to //// ScalaCollider", "ScalaCollider", null )
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
   }
}