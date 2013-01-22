package de.sciss.contextsnake

import org.scalatest.FunSuite

class ContextSnakeSuite extends FunSuite {
  val seed  = 0L
  val N     = 10000 // higher than 10k gets slow because of the `.tails` iterations

  test("sub-sequences searches are performed on the suffix tree") {
    val r   = new util.Random(seed)
    val v   = Vector.fill(N)(r.nextInt(N))
//    val tim1 = System.currentTimeMillis()
    val c   = ContextSnake(v: _*)
//    val tim2 = System.currentTimeMillis()
//println("TOOK:" + (tim2 - tim1))

//    val c   = ContextSnake(v.init: _*)
//println("GRAPH:\n" + c.toDOT(sep = ","))
//c += v.last
    val t1  = v.tails.forall(c.contains)
    assert(t1)
//    println("INPUT: " + v)
//    v.tails.foreach { sub =>
//      if (!c.contains(sub)) {
//        println("FAILED for " + sub)
//        c.contains(sub)
//      }
//    }
//    println("GRAPH:\n" + c.toDOT(sep = ",", tailEdges = true))

    assert(c.size === v.size)
    val t2  = v.tails.filter(_.nonEmpty).map { xs =>
      val idx = r.nextInt(xs.size)
      xs.updated(idx, N)
    }
    val t3  = t2.exists(c.contains)
    assert(!t3)
  }
}