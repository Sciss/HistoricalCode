//package de.sciss.lucre.macros
//
//import javax.swing._
//import java.awt.EventQueue
//import javax.swing.plaf.MenuBarUI
//
//object SimpleApp extends App with Runnable {
//  sys.props.put("apple.laf.useScreenMenuBar", "true")
//  EventQueue.invokeLater(this)
//
//  def run(): Unit = {
//    UIManager.setLookAndFeel("com.alee.laf.WebLookAndFeel") // "com.sun.java.swing.plaf.motif.MotifLookAndFeel")
//    val aquaClazz     = Class.forName("com.apple.laf.AquaMenuBarUI")
//    val aquaMenuBarUI = aquaClazz.newInstance().asInstanceOf[MenuBarUI]
//    val mb            = new JMenuBar()
//    mb.add(new JMenu("Baz"))
//    mb.setUI(aquaMenuBarUI)
//    new JFrame("Foo") {
//      getContentPane.add(new JButton("Bar"))
//      setJMenuBar(mb)
//      pack()
//      setLocationRelativeTo(null)
//      setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
//      setVisible(true)
//    }
//  }
//}
