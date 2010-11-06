package de.sciss.dorianlaf

import javax.swing.plaf.basic.BasicLookAndFeel
import javax.swing.{JComponent, UIDefaults}
import javax.swing.plaf.{ColorUIResource, InsetsUIResource, ComponentUI}
import java.io.File
import java.awt.{Font, Insets}

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
         "LabelUI",        packageName + "LabelUI",
         "ButtonUI",       packageName + "ButtonUI",
         "ToggleButtonUI", packageName + "ButtonUI",
         "CheckBoxUI",     packageName + "CheckBoxUI",
         "PanelUI",        packageName + "PanelUI",
         "FileChooserUI",  packageName + "FileChooserUI" // laffy needs it... 
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

      // XXX the InputStream retrieved by getResourceAsStream causes
      // a loading problem for some reason...
      val fontBase = Font.createFont( Font.TRUETYPE_FONT,
         new File( "/Users/rutz/Documents/devel/DorianLaF/src/main/resources/de/sciss/dorianlaf/CAFETA__.ttf" ))
      val dialogPlain15 = fontBase.deriveFont( 15f )
      val dialogPlain16 = fontBase.deriveFont( 16f )

      val uiDefaults = Array[ AnyRef ](
         "Button.border", new ButtonBorder, // packageName + "ButtonBorder"
         "Button.font", dialogPlain15,
         "Button.textForeground", new ColorUIResource( 30, 30, 30 ),
         "Panel.startBackground", new ColorUIResource( 70, 70, 70 ),
         "Panel.endBackground", new ColorUIResource( 50, 50, 50 ),
         "Label.font", dialogPlain16,
         "Label.foreground", new ColorUIResource( 0xFF, 0xFF, 0xFF ),
         "CheckBox.font", dialogPlain16,
         "CheckBox.foreground", new ColorUIResource( 0xFF, 0xFF, 0xFF )
// XXX why this has no effect??
//         "Button.margin", new InsetsUIResource( 20, 30, 20, 30 )
      )
      table.putDefaults( uiDefaults )
   }
}