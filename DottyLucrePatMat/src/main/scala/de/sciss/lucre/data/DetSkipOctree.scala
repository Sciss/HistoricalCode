/*
 *  DetSkipOctree.scala
 *  (Lucre 4)
 *
 *  Copyright (c) 2009-2020 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU Affero General Public License v3+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.lucre.data

import java.io.DataOutput

import de.sciss.lucre.{Exec, Ident, Var}

import scala.annotation.tailrec
import scala.collection.immutable.{IndexedSeq => Vec}
import scala.collection.mutable

/** A transactional deterministic skip octree as outlined in the paper by Eppstein et al.
 * It is constructed from a given space (dimensions) and a skip-gap parameter
 * which determines the kind of skip list which is used to govern the
 * level decimation.
 *
 * The tree is a mutable data structure which supports lookup, insertion and removal
 * in O(log n), as well as efficient range queries and nearest neighbour search.
 *
 * The current implementation, backed by `impl.SkipOctreeImpl`, uses the types of
 * the `geom` package, assuming that coordinates are integers, with the maximum
 * root hyper-cube given by a span from `0` to `0x7FFFFFFF` (e.g. in `Space.IntTwoDim`,
 * this is `IntSquare( 0x40000000, 0x40000000, 0x40000000 )`.
 */
object DetSkipOctree {
  private final val SER_VERSION = 79  // 'O'

  sealed trait Child[+T, +P, +H, +A]

  sealed trait LeftNonEmpty[T <: Exec[T], P, H] extends Left with NonEmpty[T, P, H]

  /** Utility trait which elements the rightward search `findPN`. */
  sealed trait ChildBranch[T <: Exec[T], P, H, A]
    extends Branch       [T, P, H, A] /* Like */
      with  NonEmptyChild[T, P, H, A]

  sealed trait Next[+T, +P, +H, +A] extends Child[T, P, H, A]

  /** A node is an object that can be stored in a orthant of a branch. */
  sealed trait NonEmpty[T <: Exec[T], P, H] {

    /** Computes the greatest interesting hyper-cube within
     * a given hyper-cube `mq` so that this (leaf's or node's)
     * hyper-cube and the given point will be placed in
     * separated orthants of this resulting hyper-cube.
     */
    def union(mq: H, point: P)(implicit tx: T): H

    /**
     * Queries the orthant index for this (leaf's or node's) hyper-cube
     * with respect to a given outer hyper-cube `iq`.
     */
    def orthantIndexIn(iq: H)(implicit tx: T): Int
  }

  sealed trait Left
  sealed trait LeftChild[+T, +P, +H, +A] extends Left with Child[T, P, H, A]

  sealed trait Branch[T <: Exec[T], P, H, A] extends Child[T, P, H, A] with NonEmpty[T, P, H] {
    /** Returns the hyper-cube covered by this node. */
    def hyperCube: H

    def nextOption(implicit tx: T): Option[Branch[T, P, H, A]]

    /** Returns the corresponding interesting
     * node in Qi+1, or `empty` if no such
     * node exists.
     */
    def next(implicit tx: T): Next[T, P, H, A]

    /** Sets the corresponding interesting
     * node in Qi+1.
     */
    private[DetSkipOctree] def next_=(n: Next[T, P, H, A])(implicit tx: T): Unit

    def prevOption: Option[Branch[T, P, H, A]]

    /** Returns the child for a given orthant index. */
    def child(idx: Int)(implicit tx: T): Child[T, P, H, A]

    /** Assuming that the given `leaf` is a child of this node,
     * removes the child from this node's children. This method
     * will perform further clean-up such as merging this node
     * with its parent if it becomes uninteresting as part of the
     * removal.
     */
    private[DetSkipOctree] def demoteLeaf(point: P, leaf: Leaf[T, P, H, A])(implicit tx: T): Unit
  }

  sealed trait Leaf[T <: Exec[T], P, H, A]
    extends Child             [T, P, H, A]
      with  LeftNonEmptyChild [T, P, H, A]
      with  RightNonEmptyChild[T, P, H, A]
      with  LeafOrEmpty       [T, P, H, A] {

    def value: A

    private[DetSkipOctree] def parent_=(b: Branch[T, P, H, A])(implicit tx: T): Unit

    private[DetSkipOctree] def remove()(implicit tx: T): Unit
  }

  /** A common trait used in pattern matching, comprised of `Leaf` and `LeftChildBranch`. */
  sealed trait LeftNonEmptyChild[T <: Exec[T], P, H, A]
    extends LeftNonEmpty [T, P, H]
      with  NonEmptyChild[T, P, H, A]
      with  LeftChild    [T, P, H, A] {

    private[DetSkipOctree] def updateParentLeft(p: LeftBranch[T, P, H, A])(implicit tx: T): Unit
  }

  sealed trait RightChild[+T, +P, +H, +A] extends Child[T, P, H, A]

  /** An inner non empty tree element has a mutable parent node. */
  sealed trait NonEmptyChild[T <: Exec[T], P, H, A] extends NonEmpty[T, P, H] with Child[T, P, H, A] {
    def parent(implicit tx: T): Branch[T, P, H, A]
  }

  protected sealed trait LeafOrEmpty[+T, +P, +H, +A] extends LeftChild[T, P, H, A]

  /** A common trait used in pattern matching, comprised of `Leaf` and `RightChildBranch`. */
  sealed trait RightNonEmptyChild[T <: Exec[T], P, H, A]
    extends RightChild   [T, P, H, A]
      with  NonEmptyChild[T, P, H, A] {

    private[DetSkipOctree] def updateParentRight(p: RightBranch[T, P, H, A])(implicit tx: T): Unit
  }

  sealed trait TopBranch[T <: Exec[T], P, H, A] extends Branch[T, P, H, A]

  sealed trait LeftTopBranch[T <: Exec[T], P, H, A]
    extends LeftBranch  [T, P, H, A]
      with  TopBranch   [T, P, H, A]

  sealed trait RightTopBranch[T <: Exec[T], P, H, A]
    extends RightBranch [T, P, H, A]
      with  TopBranch   [T, P, H, A]

  case object Empty
    extends LeftChild   [Nothing, Nothing, Nothing, Nothing]
      with  RightChild  [Nothing, Nothing, Nothing, Nothing]
      with  Next        [Nothing, Nothing, Nothing, Nothing]
      with  LeafOrEmpty [Nothing, Nothing, Nothing, Nothing] {

    def write(out: DataOutput): Unit = out.writeByte(0)
  }

  /** A left tree node implementation provides more specialized child nodes
   * of type `LeftChild`. It furthermore defines a resolution method
   * `findImmediateLeaf` which is typically called after arriving here
   * from a `findP0` call.
   */
  sealed trait LeftBranch[T <: Exec[T], P, H, A]
    extends Branch[T, P, H, A] /* Like */
      with LeftNonEmpty[T, P, H] {

    def prevOption: Option[Branch[T, P, H, A]]

    def child(idx: Int)(implicit tx: T): LeftChild[T, P, H, A]

    private[DetSkipOctree] def insert(point: P, value: A)(implicit tx: T): Leaf[T, P, H, A]

    private[DetSkipOctree] def updateChild(idx: Int, c: LeftChild[T, P, H, A])(implicit tx: T): Unit

    /** Instantiates an appropriate
     * leaf whose parent is this node, and which should be
     * ordered according to its position in this node.
     *
     * @param   qIdx  the orthant index of the new leaf in this node
     * @param   value the value associated with the new leaf
     * @return  the new leaf which has already assigned this node as
     *          parent and is already stored in this node's children
     *          at index `qIdx`
     */
    private[DetSkipOctree] def newLeaf(qIdx: Int, value: A)(implicit tx: T): Leaf[T, P, H, A]
  }


  /** A right tree node implementation provides more specialized child nodes
   * of type `RightChild`. It furthermore defines the node in Qi-1 via the
   * `prev` method.
   */
  sealed trait RightBranch[T <: Exec[T], P, H, A] extends Next[T, P, H, A] with Branch[T, P, H, A] {
    def prev: Branch[T, P, H, A]

    private[DetSkipOctree] def updateChild(idx: Int, c: RightChild[T, P, H, A])(implicit tx: T): Unit

    /** Promotes a leaf that exists in Qi-1 to this
     * tree, by inserting it into this node which
     * is its interesting node in Qi.
     *
     * If the result of insertion is a new child node
     * below this node, this intermediate node will
     * be connected to Qi by looking for the corresponding
     * hyper-cube in the given search path that led here
     * (i.e. that was constructed in `findPN`).
     *
     * This method also sets the parent of the leaf
     * accordingly.
     */
    private[DetSkipOctree] def insert(point: P, leaf: Leaf[T, P, H, A])(implicit tx: T): Unit
  }

  sealed trait LeftChildBranch[T <: Exec[T], P, H, A]
    extends LeftBranch        [T, P, H, A]
      with  ChildBranch       [T, P, H, A]
      with  LeftNonEmptyChild [T, P, H, A] {

    def parent(implicit tx: T): LeftBranch[T, P, H, A]

    private[DetSkipOctree] def parent_=(node: LeftBranch[T, P, H, A])(implicit tx: T): Unit
  }

  sealed trait RightChildBranch[T <: Exec[T], P, H, A]
    extends RightBranch       [T, P, H, A]
      with  ChildBranch       [T, P, H, A]
      with  RightNonEmptyChild[T, P, H, A] {

    def parent(implicit tx: T): RightBranch[T, P, H, A]

    private[DetSkipOctree] def parent_=(node: RightBranch[T, P, H, A])(implicit tx: T): Unit
  }

  /* Nodes are defined by a hyperCube area as well as a list of children,
   * as well as a pointer `next` to the corresponding node in the
   * next highest tree. A `Branch` also provides various search methods.
   */
  private sealed trait BranchImpl[T <: Exec[T], P, H, A] {
    thisBranch: Branch[T, P, H, A] =>

    // ---- abstract ----

    protected def nextRef: Var[T, Next[T, P, H, A]]

    /** Called when a leaf has been removed from the node.
     * The node may need to cleanup after this, e.g. promote
     * an under-full node upwards.
     */
    protected def leafRemoved()(implicit tx: T): Unit

    protected def nodeName: String

    // ---- impl ----

    final def next_=(node: Next[T, P, H, A])(implicit tx: T): Unit = nextRef() = node

    final def next(implicit tx: T): Next[T, P, H, A] = nextRef()

    final def nextOption(implicit tx: T): Option[Branch[T, P, H, A]] = thisBranch.next match {
      case Empty                      => None
      case b: Branch[T, P, H, A]  => Some(b)
    }

    final def union(mq: H, point2: P)(implicit tx: T): H = {  // scalac warning bug
      ???
    }

    final def orthantIndexIn(iq: H)(implicit tx: T): Int =  // scalac warning bug
      ???

    protected final def shortString = s"$nodeName($thisBranch.hyperCube)"
  }

  private trait LeftBranchImpl[T <: Exec[T], P, H , A]
    extends BranchImpl[T, P, H, A] {

    branch: LeftBranch[T, P, H, A] =>

    // ---- abstract ----
    
    protected val octree: Impl[T, P, H, A]

    /** For a `LeftBranch`, all its children are more specific
     * -- they are instances of `LeftChild` and thus support
     * order intervals.
     */
    protected def children: Array[Var[T, LeftChild[T, P, H, A]]]

    // ---- impl ----

    final def prevOption: Option[Branch[T, P, H, A]] = None

    final def child(idx: Int)(implicit tx: T): LeftChild[T, P, H, A] = children(idx)()

    final def updateChild(idx: Int, c: LeftChild[T, P, H, A])(implicit tx: T): Unit =
      children(idx)() = c

    final def demoteLeaf(point: P, leaf: Leaf[T, P, H, A])(implicit tx: T): Unit = {
      val qIdx  = ??? : Int
      val ok    = child(qIdx) == leaf
      if (ok) {
        updateChild(qIdx, Empty)
        leafRemoved()
        leaf.remove() // dispose()
      } else {
        ???
      }
    }

    final def insert(point: P, value: A)(implicit tx: T): Leaf[T, P, H, A] = {
      val qIdx = ??? : Int
      child(qIdx) match {
        case Empty =>
          newLeaf(qIdx, /* point, */ value) // (this adds it to the children!)

        case old: LeftNonEmptyChild[T, P, H, A] =>
          ???
      }
    }

    /** Instantiates an appropriate
     * leaf whose parent is this node, and which should be
     * ordered according to its position in this node.
     *
     * @param   qIdx  the orthant index of the new leaf in this node
     * @param   value the value associated with the new leaf
     * @return  the new leaf which has already assigned this node as
     *          parent and is already stored in this node's children
     *          at index `qIdx`
     */
    private[DetSkipOctree] def newLeaf(qIdx: Int, value: A)(implicit tx: T): Leaf[T, P, H, A] = ???
  }

  /* A leaf in the octree, carrying a map entry
   * in the form of a point and associated value.
   * Note that a single instance of a leaf is used
   * across the levels of the octree! That means
   * that multiple child pointers may go to the
   * same leaf, while the parent of a leaf always
   * points into the highest level octree that
   * the leaf resides in, according to the skiplist.
   */
  private final class LeafImpl[T <: Exec[T], P,
    H , A](octree: Impl[T, P, H, A], val id: Ident[T], val value: A,
                              parentRef: Var[T, Branch[T, P, H, A]])
    extends LeftNonEmptyChild [T, P, H, A] 
      with RightNonEmptyChild [T, P, H, A]
      with LeafOrEmpty        [T, P, H, A]
      with Leaf               [T, P, H, A] {

    def updateParentLeft (p: LeftBranch [T, P, H, A])(implicit tx: T): Unit = parent_=(p)
    def updateParentRight(p: RightBranch[T, P, H, A])(implicit tx: T): Unit = parent_=(p)

    def parent(implicit tx: T): Branch[T, P, H, A] = parentRef()

    def parent_=(p: Branch[T, P, H, A])(implicit tx: T): Unit = parentRef() = p

    def dispose()(implicit tx: T): Unit = {
      ???
    }

    def write(out: DataOutput): Unit = {
      ???
    }

    def union(mq: H, point2: P)(implicit tx: T): H =
      ???

    def orthantIndexIn(iq: H)(implicit tx: T): Int =
      ???

    def shortString = s"Leaf($value)"

    def remove()(implicit tx: T): Unit = dispose()
  }

  private final class LeftChildBranchImpl[T <: Exec[T], P,
    H , A](val octree: Impl[T, P, H, A], val id: Ident[T],
                              parentRef: Var[T, LeftBranch[T, P, H, A]], val hyperCube: H,
                              protected val children: Array[Var[T, LeftChild[T, P, H, A]]],
                              protected val nextRef: Var[T, Next[T, P, H, A]])
    extends LeftBranchImpl[T, P, H, A] 
      with LeftChildBranch[T, P, H, A] {

    thisBranch =>

    protected def nodeName = "LeftInner"

    def updateParentLeft(p: LeftBranch[T, P, H, A])(implicit tx: T): Unit = parent = p

    def parent(implicit tx: T): LeftBranch[T, P, H, A] = parentRef()

    def parent_=(node: LeftBranch[T, P, H, A])(implicit tx: T): Unit = parentRef() = node

    def dispose()(implicit tx: T): Unit = ???

    def write(out: DataOutput): Unit = ???

    private[this] def remove()(implicit tx: T): Unit = dispose()

    // make sure the node is not becoming uninteresting, in which case
    // we need to merge upwards
    protected def leafRemoved()(implicit tx: T): Unit = {
      val sz = children.length
      @tailrec def removeIfLonely(i: Int): Unit =
        if (i < sz) child(i) match {
          case lonely: LeftNonEmptyChild[T, P, H, A] =>
            @tailrec def isLonely(j: Int): Boolean = {
              j == sz || (child(j) match {
                case _: LeftNonEmptyChild[T, P, H, A] => false
                case _ => isLonely(j + 1)
              })
            }
            if (isLonely(i + 1)) {
              val p     = parent
              val myIdx = ??? : Int
              p.updateChild(myIdx, lonely)
              if (lonely.parent == this) lonely.updateParentLeft(p)
              remove() // dispose() // removeAndDispose()
            }

          case _ => removeIfLonely(i + 1)
        }

      removeIfLonely(0)
    }
  }


  private trait RightBranchImpl[T <: Exec[T], P, H , A]
    extends BranchImpl[T, P, H, A] {

    branch: RightBranch[T, P, H, A] =>

    // ---- abstract ----
    
    protected val octree: Impl[T, P, H, A]

    protected def children: Array[Var[T, RightChild[T, P, H, A]]]

    // ---- impl ----

    final def prevOption: Option[Branch[T, P, H, A]] = Some(prev: Branch[T, P, H, A])

    final def child(idx: Int)(implicit tx: T): RightChild[T, P, H, A] = children(idx)()

    final def updateChild(idx: Int, c: RightChild[T, P, H, A])(implicit tx: T): Unit =
      children(idx)() = c

    /** Promotes a leaf that exists in Qi-1 to this
     * tree, by inserting it into this node which
     * is its interesting node in Qi.
     *
     * If the result of insertion is a new child node
     * below this node, this intermediate node will
     * be connected to Qi by looking for the corresponding
     * hyper-cube in the given search path that led here
     * (i.e. that was constructed in `findPN`).
     *
     * This method also sets the parent of the leaf
     * accordingly.
     */
    final def insert(point: P, leaf: Leaf[T, P, H, A])(implicit tx: T): Unit = {
      val qIdx = ??? : Int
      child(qIdx) match {
        case Empty =>
          updateChild(qIdx, leaf)
          leaf.parent = this
        case old: RightNonEmptyChild[T, P, H, A] =>
          // determine the greatest interesting square for the new
          // intermediate node to create
          val qn2: H = ???
          // find the corresponding node in the lower tree
          @tailrec def findInPrev(b: Branch[T, P, H, A]): Branch[T, P, H, A] = {
            if (b.hyperCube == qn2) b
            else {
              val idx = ??? : Integer
              b.child(idx) match {
                case _: LeafOrEmpty[T, P, H, A] => sys.error("Internal error - cannot find sub-cube in prev")
                case cb: Branch[T, P, H, A]     => findInPrev(cb)
              }
            }
          }
          val pPrev = findInPrev(prev)
          val n2    = newNode(qIdx, pPrev, qn2)
          val oIdx  = old.orthantIndexIn(qn2)
          n2.updateChild(oIdx, old)
          // This is a tricky bit! And a reason
          // why should eventually try to do without
          // parent pointers at all. Since `old`
          // may be a leaf whose parent points
          // to a higher level tree, we need to
          // check first if the parent is `this`,
          // and if so, adjust the parent to point
          // to the new intermediate node `ne`!
          if (old.parent == this) old.updateParentRight(n2)
          val lIdx = ??? : Int
          n2.updateChild(lIdx, leaf)
          leaf.parent = n2
      }
    }

    /*
     * Instantiates an appropriate
     * sub-node whose parent is this node, and whose predecessor
     * in the lower octree is given.
     *
     * @param   qIdx  the orthant index in this node where the node belongs
     * @param   prev  the new node's prev field, i.e. its correspondent in
     *                Qi-1
     * @param   iq    the hyper-cube for the new node
     * @return  the new node which has already been inserted into this node's
     *          children at index `qIdx`.
     */
    @inline private[this] def newNode(qIdx: Int, prev: Branch[T, P, H, A],
                                      iq: H)(implicit tx: T): RightChildBranch[T, P, H, A] = ???

    final def demoteLeaf(point: P, leaf: Leaf[T, P, H, A])(implicit tx: T): Unit = {
      val qIdx = ??? : Int // branch.hyperCube.indexOfP(point)
      assert(child(qIdx) == leaf)
      updateChild(qIdx, Empty)

      @tailrec def findParent(b: Branch[T, P, H, A], idx: Int): Branch[T, P, H, A] = b.child(idx) match {
        case sl: Leaf  [T, P, H, A] => assert(sl == leaf); b
        case cb: Branch[T, P, H, A] => findParent(cb, ??? : Int)
        case Empty                      => throw new IllegalStateException  // should not happen by definition
      }

      val newParent = findParent(prev, qIdx)
      leafRemoved()
      leaf.parent = newParent
    }
  }

  private final class RightChildBranchImpl[T <: Exec[T], P,
    H , A](val octree: Impl[T, P, H, A], val id: Ident[T],
                              parentRef: Var[T, RightBranch[T, P, H, A]], val prev: Branch[T, P, H, A],
                              val hyperCube: H,
                              protected val children: Array[Var[T, RightChild[T, P, H, A]]],
                              protected val nextRef: Var[T, Next[T, P, H, A]])
    extends RightChildBranch[T, P, H, A] with RightBranchImpl[T, P, H, A] {

    thisBranch =>

    protected def nodeName = "RightInner"

    def updateParentRight(p: RightBranch[T, P, H, A])(implicit tx: T): Unit =
      parent = p

    private[this] def remove()(implicit tx: T): Unit = {
      // first unlink
      prev.next = Empty
      ???
    }

    def parent(implicit tx: T): RightBranch[T, P, H, A] = parentRef()

    def parent_=(node: RightBranch[T, P, H, A])(implicit tx: T): Unit = parentRef() = node

    // make sure the node is not becoming uninteresting, in which case
    // we need to merge upwards
    protected def leafRemoved()(implicit tx: T): Unit = {
      val sz = children.length
      @tailrec def removeIfLonely(i: Int): Unit =
        if (i < sz) child(i) match {
          case lonely: RightNonEmptyChild[T, P, H, A] =>
            @tailrec def isLonely(j: Int): Boolean = {
              j == sz || (child(j) match {
                case _: RightNonEmptyChild[T, P, H, A]  => false
                case _                                  => isLonely(j + 1)
              })
            }
            if (isLonely(i + 1)) {
              val p       = parent
              val myIdx   = ??? : Int
              p.updateChild(myIdx, lonely)
              if (lonely.parent == this) lonely.updateParentRight(p)
              remove()
            }

          case _ => removeIfLonely(i + 1)
        }

      removeIfLonely(0)
    }
  }

  private final class LeftTopBranchImpl[T <: Exec[T], P,
    H , A](val octree: Impl[T, P, H, A], val id: Ident[T],
                              protected val children: Array[Var[T, LeftChild[T, P, H, A]]],
                              protected val nextRef: Var[T, Next[T, P, H, A]])
    extends LeftTopBranch[T, P, H, A]
      with LeftBranchImpl[T, P, H, A] 
      with TopBranchImpl [T, P, H, A] 
       {
    
    // that's alright, we don't need to do anything special here
    protected def leafRemoved()(implicit tx: T): Unit = ()

    protected def nodeName = "LeftTop"
  }


  private final class RightTopBranchImpl[T <: Exec[T], P,
    H , A](val octree: Impl[T, P, H, A], val id: Ident[T], val prev: TopBranch[T, P, H, A],
                              protected val children: Array[Var[T, RightChild[T, P, H, A]]],
                              protected val nextRef: Var[T, Next[T, P, H, A]])
    extends RightTopBranch[T, P, H, A]
      with RightBranchImpl[T, P, H, A]
      with TopBranchImpl  [T, P, H, A] {

    protected def nodeName = "RightTop"

    private[this] def remove()(implicit tx: T): Unit = {
      // first unlink
      assert(octree.lastTree == this)
      octree.lastTree = prev
      prev.next       = Empty
      dispose()
    }

    def dispose()(implicit tx: T): Unit = ???

    def write(out: DataOutput): Unit = ???

    // remove this node if it empty now and right-node tree
    protected def leafRemoved()(implicit tx: T): Unit = {
      if (next != Empty) return

      val sz = children.length
      var i = 0
      while (i < sz) {
        val c = child(i)
        if (c != Empty) return // node not empty, abort the check
        i += 1
      }

      // ok, we are the right most tree and the node is empty...
      remove()
    }
  }

  private sealed trait TopBranchImpl[T <: Exec[T], P, H , A] {
    protected val octree: Impl[T, P, H, A]
    
    final def hyperCube: H = ???
  }

  // cf. https://github.com/lampepfl/dotty/issues/9844
  private abstract class Impl[T <: Exec[T], P, H , A]
    extends DetSkipOctree[T, P, H, A] {
    octree =>

    final type Child              = DetSkipOctree.Child             [T, P, H, A]
    final type Branch             = DetSkipOctree.Branch            [T, P, H, A]
    final type Leaf               = DetSkipOctree.Leaf              [T, P, H, A]
    final type LeftBranch         = DetSkipOctree.LeftBranch        [T, P, H, A]
    final type RightBranch        = DetSkipOctree.RightBranch       [T, P, H, A]
    final type LeftChild          = DetSkipOctree.LeftChild         [T, P, H, A]
    final type RightChild         = DetSkipOctree.RightChild        [T, P, H, A]
    final type Next               = DetSkipOctree.Next              [T, P, H, A]
    final type ChildBranch        = DetSkipOctree.ChildBranch       [T, P, H, A]
    final type LeftChildBranch    = DetSkipOctree.LeftChildBranch   [T, P, H, A]
    final type RightChildBranch   = DetSkipOctree.RightChildBranch  [T, P, H, A]
    final type NonEmptyChild      = DetSkipOctree.NonEmptyChild     [T, P, H, A]
    final type LeafOrEmpty        = DetSkipOctree.LeafOrEmpty       [T, P, H, A]
    final type LeftNonEmptyChild  = DetSkipOctree.LeftNonEmptyChild [T, P, H, A]
    final type RightNonEmptyChild = DetSkipOctree.RightNonEmptyChild[T, P, H, A]
    final type TopBranch          = DetSkipOctree.TopBranch         [T, P, H, A]
    final type LeftTopBranch      = DetSkipOctree.LeftTopBranch     [T, P, H, A]
    final type RightTopBranch     = DetSkipOctree.RightTopBranch    [T, P, H, A]

    // ---- abstract types and methods ----
    
    protected def skipList: Set[Leaf]
    protected def lastTreeRef: Var[T, TopBranch]

    final def numOrthants: Int = ???

    final def write(out: DataOutput): Unit = ???

    final def clear()(implicit tx: T): Unit = ???

    final def dispose()(implicit tx: T): Unit = ???

    final def lastTree(implicit tx: T): TopBranch = lastTreeRef()

    final def lastTree_=(node: TopBranch)(implicit tx: T): Unit = lastTreeRef() = node

    final def size(implicit tx: T): Int = skipList.size

    final def add(elem: A)(implicit tx: T): Boolean =
      insertLeaf(elem) match {
        case Empty          => true
        case oldLeaf: Leaf  => oldLeaf.value != elem
      }

    final def update(elem: A)(implicit tx: T): Option[A] =
      insertLeaf(elem) match {
        case Empty          => None
        case oldLeaf: Leaf  => Some(oldLeaf.value)
      }

    final def remove(elem: A)(implicit tx: T): Boolean = ???

    final def removeAt(point: P)(implicit tx: T): Option[A] =
      removeLeafAt(point) match {
        case Empty          => None
        case oldLeaf: Leaf  => Some(oldLeaf.value)
      }

    final def contains(elem: A)(implicit tx: T): Boolean = {
      val point = ??? //  pointView(elem, tx)
      if (???) return false
      findAt(point) match {
        case l: Leaf  => l.value == elem
        case _        => false
      }
    }

    final def isDefinedAt(point: P)(implicit tx: T): Boolean = ???

    final def get(point: P)(implicit tx: T): Option[A] = {
      if (???) return None
      findAt(point) match {
        case l: Leaf  => Some(l.value)
        case _        => None
      }
    }

    final def nearestNeighbor[M](point: P)(implicit tx: T): A = {
      val nn = new NN(point).find()
      nn match {
        case Empty    => throw new NoSuchElementException("nearestNeighbor on an empty tree")
        case l: Leaf  => l.value
      }
    }

    final def nearestNeighborOption[M](point: P)(implicit tx: T): Option[A] = {
      val nn = new NN(point).find()
      nn match {
        case Empty    => None
        case l: Leaf  => Some(l.value)
      }
    }

    final def isEmpty(implicit tx: T): Boolean = {
      val n = headTree
      val sz = numOrthants
      @tailrec def step(i: Int): Boolean = if (i == sz) true
      else n.child(i) match {
        case _: NonEmptyChild => false
        case _ => step(i + 1)
      }
      step(0)
    }

    final def numLevels(implicit tx: T): Int = {
      @tailrec def step(b: Branch, num: Int): Int = {
        b.next match {
          case Empty => num
          case n: Branch => step(n, num + 1)
        }
      }
      step(headTree, 1)
    }

    final def +=(elem: A)(implicit tx: T): this.type = {
      insertLeaf(elem)
      //      match {
      //         case oldLeaf: Leaf => oldLeaf.dispose()
      //         case _ =>
      //      }
      this
    }

    final def -=(elem: A)(implicit tx: T): this.type = ???

    final def rangeQuery[Area]()(implicit tx: T): Iterator[A] = {
      val q = new RangeQuery()
      q.findNextValue()
      q
    }

    final def toIndexedSeq(implicit tx: T): Vec[A] = iterator.toIndexedSeq

    final def toList(implicit tx: T): List[A] = iterator.toList

    // note that `iterator.toSeq` produces a `Stream` !!
    final def toSeq(implicit tx: T): Seq[A] = iterator.toIndexedSeq

    final def toSet(implicit tx: T): Set[A] = iterator.toSet

    private[this] def findAt(point: P)(implicit tx: T): LeafOrEmpty = {
      val p0 = findP0(point) // lastTreeImpl.findP0( point )
      findLeafInP0(p0, point) // p0.findImmediateLeaf( point )
    }

    // OBSOLETE: the caller _must not call dispose_
    //
    // (( WARNING: if the returned oldLeaf is defined, the caller is
    // responsible for disposing it (after extracting useful information such as its value) ))
    private[this] def insertLeaf(elem: A)(implicit tx: T): LeafOrEmpty = {
      val point: P = ???
      val p0  = findP0(point) // lastTreeImpl.findP0( point )
      val res = findLeafInP0(p0, point)

      res match {
        case Empty =>
          ???

        case oldLeaf: Leaf =>
          ???
      }

      res
    }

    // WARNING: if the returned oldLeaf is defined, the caller is
    // responsible for disposing it (after extracting useful information such as its value)
    private[this] def removeLeafAt(point: P)(implicit tx: T): LeafOrEmpty = {
      if (???) return Empty

      // "To insert or delete a point y into or from S, we first search the
      // quadtree structure to locate y in each Qi ..."
      val p0 = findP0(point) // lastTreeImpl.findP0( point )

      // "... Then we insert or delete y
      // in the binary Q0 and update our total order."

      val res = findLeafInP0(p0, point) // p0.findImmediateLeaf( point )

      res match {
        case l: Leaf  => removeLeaf(point, l)
        case _        =>
      }

      res
    }

    def transformAt(point: P)(fun: Option[A] => Option[A])(implicit tx: T): Option[A] = {
      val p0 = findP0(point)
      findLeafInP0(p0, point) match {
        case Empty =>
          val res = None
          fun(res).foreach { elem =>
            ???
          }
          res

        case oldLeaf: Leaf =>
          // it's not possible currently to update a leaf's value...
          // remove previous leaf
          val res = Some(oldLeaf.value)
          removeLeaf(point, oldLeaf)
          fun(res).foreach {
            elem =>
              ???
          }
          res
      }
    }

    /*
     * After arriving at this node from a `findP0` call, this resolves
     * the given point to an actual leaf.
     *
     * @return  the `Leaf` child in this node associated with the given
     *          `point`, or `empty` if no such leaf exists.
     */
    private[this] def findLeafInP0(b: LeftBranch, point: P)(implicit tx: T): LeafOrEmpty = {
      val qIdx = ??? : Int
      b.child(qIdx) match {
        case l: Leaf if (??? : Boolean) == point => l
        case _ => Empty
      }
    }

    /*
     * Finds to smallest interesting hyper-cube
     * in Q0, containing a given point. This method
     * traverses downwards into its children, or,
     * if the "bottom" has been reached, tries to
     * continue in Qi-1.
     *
     * @return  the node defined by the given search `point`, or `empty`
     *          if no such node exists.
     */
    private[this] def findP0(point: P)(implicit tx: T): LeftBranch = {
      def stepLeft(lb: LeftBranch): LeftBranch = {
        val qIdx = ??? : Int
        lb.child(qIdx) match {
          case _: LeafOrEmpty => lb
          case cb: LeftBranch =>
            ???
        }
      }

      @tailrec def step(b: Branch): LeftBranch = b match {
        case lb: LeftBranch => stepLeft(lb)
        case rb: RightBranch =>
          val qIdx = ??? : Int
          val n = rb.child(qIdx) match {
            case cb: Branch if (??? : Boolean) => cb
            case _ => rb.prev
          }
          step(n)
      }

      step(lastTree)
    }

    private[this] def removeLeaf(point: P, l: Leaf)(implicit tx: T): Unit = ???

    final def iterator(implicit tx: T): Iterator[A] = skipList.iterator.map(_.value)

    private[this] final class NNIteration[M](val bestLeaf: LeafOrEmpty, val bestDist: M, val rMax: M)

    private[this] final class NN[M](point: P)(implicit tx: T)
      extends scala.math.Ordering[VisitedNode[M]] {

      def find(): LeafOrEmpty = ???

      def compare(a: VisitedNode[M], b: VisitedNode[M]): Int = ???
    }

    private[this] final class VisitedNode[M](val n: Branch, val minDist: M /*, val maxDist: M */) {
      override def toString = s"($n, min = $minDist" // , max = $maxDist)"
    }

    // note: Iterator is not specialized, hence we can safe use the effort to specialize in A anyway
    private[this] final class RangeQuery[Area]()(implicit tx: T) extends Iterator[A] {

      val in: mutable.Queue[NonEmptyChild] = mutable.Queue.empty

      def hasNext: Boolean = ???

      val sz = ??? : Int

      // search downwards:
      // "At each square q ∈ Qi we either go to a child square in Qi
      // that covers the same area of R ∪ A as p does, if such a child
      // square exists, or jump to the next level q ∈ Qi−1."
      @tailrec private[this] def findEquidistantStabbingTail(node: Branch): LeftBranch = {
        var pi = node
        var i = 0
        while (i < sz) {
          pi.child(i) match {
            case pic: Branch =>
              ???
            case _ => i += 1
          }
        }
        // ... or jump to the next (previous) level
        pi match {
          case lb: LeftBranch => lb
          case rb: RightBranch => findEquidistantStabbingTail(rb.prev)
        }
      }

      // the movement from Q0 to Qj
      // "assuming that p is not critical in Q0, we promote to Qj where Qj is the highest
      // level in which p is not a critical square"
      //
      // definition of critical square:
      // "a stabbing node of Qi whose child nodes are either not stabbing, or still
      // stabbing but cover less volume of R than p does."
      // ; or rather inverted: an uncritical node is one, in which exists at least one stabbing node
      // with the same overlapping area!
      //
      // definition stabbing: 0 < overlap-area < area-of-p
      @tailrec def findHighestUncritical(p0: Branch, area: Area): Branch = {
        @tailrec def isCritical(b: Branch, i: Int): Boolean = {
          i < sz && (b.child(i) match {
            // if there is any child which has the same overlap area, it means the node is uncritical
            case ci: Branch if (??? : Boolean) == area => true
            case _ => isCritical(b, i + 1)
          })
        }

        p0.next match {
          case Empty => p0
          case pi: Branch => if (isCritical(pi, 0)) p0 else findHighestUncritical(pi, area)
        }
      }

      def next(): A = ???

      def findNextValue(): Unit = {
        while (true) {
          if (???) {
            if (???) {
              ???
              return
            }
            val nc  = findEquidistantStabbingTail (???) // now traverse towards Q0 to find the critical square

            var i = 0
            while (i < sz) {
              nc.child(i) match {
                case cl: Leaf =>
                  ??? // if (qs.containsP(pointView(cl.value, tx))) in += cl
                case cn: ChildBranch =>
                  ???
                case _ =>
              }
              i += 1
            }

          } else {
            in.dequeue() match {
              case l: Leaf =>
                ???
                return
              case n: Branch =>
                var i = 0
                while (i < sz) {
                  n.child(i) match {
                    case cc: NonEmptyChild => in += cc // `enqueue` would create intermediate Seq because of varargs
                    case _ =>
                  }
                  i += 1
                }
            }
          }
        }
      }
    }
  }
}

sealed trait DetSkipOctree[T <: Exec[T], P, H, A]
  /*extends SkipOctree[T, P, H, A]*/ {

  def verifyConsistency(reportOnly: Boolean)(implicit tx: T): Vec[String]

  def headTree: DetSkipOctree.LeftTopBranch[T, P, H, A]
  def lastTree(implicit tx: T): DetSkipOctree.TopBranch [T, P, H, A]
}