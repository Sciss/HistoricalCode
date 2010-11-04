package de.sciss.dorianlaf

import javax.swing.plaf.basic.BasicLookAndFeel
import javax.swing.{JComponent, UIDefaults}
import java.awt.Insets
import javax.swing.plaf.{ColorUIResource, InsetsUIResource, ComponentUI}

object DorianLookAndFeel {
   private val packageName = "de.sciss.dorianlaf.";
}

class DorianLookAndFeel extends BasicLookAndFeel /* MetalLookAndFeel */ {
   import DorianLookAndFeel._
   
   def getName                = "Dorian"
   def getID                  = "Dorian"
   def getDescription         = "Dorian Look and Feel"
   def isNativeLookAndFeel    = false
   def isSupportedLookAndFeel = true

   override protected def initClassDefaults( table: UIDefaults ) {
      super.initClassDefaults( table )

      val uiDefaults = Array[ AnyRef ](
         "ButtonUI", packageName + "ButtonUI",
         "PanelUI", packageName + "PanelUI"
//                "MenuUI", packageName + "SceMenuUI",
//         "ProgressBarUI", packageName + "SceProgressBarUI",
//           "ScrollBarUI", packageName + "SceScrollBarUI",
//           "SplitPaneUI", packageName + "SceSplitPaneUI",
//          "TabbedPaneUI", packageName + "SceTabbedPaneUI",
//         "TableHeaderUI", packageName + "SceTableHeaderUI",
//        "ToggleButtonUI", packageName + "SceToggleButtonUI",
//             "ToolBarUI", packageName + "SceToolBarUI",
      )
      table.putDefaults( uiDefaults )
   }

   override protected def initComponentDefaults( table: UIDefaults ) {
       super.initComponentDefaults( table )

      val uiDefaults = Array[ AnyRef ](
         "Button.border", new ButtonBorder, // packageName + "ButtonBorder"
         "Panel.startBackground", new ColorUIResource( 70, 70, 70 ),
         "Panel.endBackground", new ColorUIResource( 50, 50, 50 )
// XXX why this has no effect??
//         "Button.margin", new InsetsUIResource( 20, 30, 20, 30 )
      )
      table.putDefaults( uiDefaults )
   }
}