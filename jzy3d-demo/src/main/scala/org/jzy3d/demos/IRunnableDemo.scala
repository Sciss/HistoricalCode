package org.jzy3d.demos

trait IRunnableDemo extends IDemo {
  def start(): Unit
  def stop (): Unit
}