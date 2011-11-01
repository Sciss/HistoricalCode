package de.sciss.transactional.annotation

import annotation.target.field
import concurrent.stm.Ref

/**
 *
 */
object txn {
   def transform[ A ]( ref: Ref[ A ])( fun: A => A ) {}
}
@field class txn extends annotation.StaticAnnotation