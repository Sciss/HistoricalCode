//package de.sciss.cupola.video
//
//import javax.swing.AbstractListModel
//import collection.immutable.{IndexedSeq => IIdxSeq}
//
//class IndexedSeqListModel[ A ]( seq: IIdxSeq[ A ]) extends AbstractListModel {
//   def getSize                   = seq.size
//   def getElementAt( idx: Int )  = seq( idx ).asInstanceOf[ AnyRef ]
//}