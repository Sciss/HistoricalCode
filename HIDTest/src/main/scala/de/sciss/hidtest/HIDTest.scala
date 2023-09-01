package de.sciss.hidtest

import com.codeminders.hidapi

object HIDTest extends App {
  init()
  listAll()
  // openTablet()

  def init(): Unit = hidapi.ClassPathLibraryLoader.loadNativeHIDLibrary()

  def openTablet(): Unit = {
    val m       = hidapi.HIDManager.getInstance()
    println("Opening...")
    // val device  = m.openById(0x56A, 0xB1, null)
    val infoOpt = m.listDevices().find { i => i.getVendor_id == 0x56A && i.getProduct_id == 0xB1 }
    val info    = infoOpt.getOrElse(sys.error("Did not find tablet device"))
    val device  = info.open()
    println("Closing...")
    device.close()
  }

  def listAll(): Unit = {
    val m     = hidapi.HIDManager.getInstance()
    val info0 = m.listDevices()
    val info  = info0.sortBy(i => (i.getVendor_id, i.getProduct_id))
    info.foreach { i =>
      val ifNum   = i.getInterface_number
      val man     = i.getManufacturer_string
      val path    = i.getPath
      val prodID  = i.getProduct_id
      val prod    = i.getProduct_string
      val release = i.getRelease_number
      val serial  = i.getSerial_number
      val usage   = i.getUsage
      val page    = i.getUsage_page
      val vendor  = i.getVendor_id

      println(s"Vendor      : ${vendor.toHexString}")
      println(s"Product-ID  : ${prodID.toHexString}")
      println(s"Path        : $path")
      println(s"Product     : $prod")
      println(s"Serial      : $serial")
      println(s"Manufacturer: $man")
      println(s"Interface   : $ifNum")
      println(s"Release     : $release")
      println(s"Usage       : $usage")
      println(s"Usage-page  : $page")
      println()
    }
  }
}
