package de.sciss.transactional.plugin

import tools.nsc
import nsc.plugins.PluginComponent
import nsc.Global
import nsc.transform.{Transform, TypingTransformers}

final class TransformAnnotations( plugin: TransactionalPlugin, val global: Global )
extends PluginComponent with Transform with TypingTransformers {
   val runsAfter  = List( "typer" )  // "refchecks"  "namer"
   val phaseName  = "txn.annotations"

   /**
    * Implementation of abstract method in `Transform`.
    */
   protected def newTransformer( cu: global.CompilationUnit ) : global.Transformer = new TxnTransformer( cu )

   /**
    * `TypingTransformer` is an abstract class provided by the `TypingTransformer` trait
    */
   private final class TxnTransformer( cu: global.CompilationUnit )
   extends TypingTransformer( cu ) {
      import global._

//      def newPhase( prev: nsc.Phase ) : nsc.Phase = ...

      private val annotationClass: Symbol = definitions.getClass( "de.sciss.transactional.annotation.txn" )

      /*
       * This simply removes the txn annotation.
       */
      private def mkPlainClass( cd: ClassDef ) : ClassDef = {
//         cd.symbol.removeAnnotation( annotationClass )
         val cpy = cd // treeCopy.ClassDef( cd, cd.mods, cd.name, cd.tparams, cd.impl )
         cpy.symbol.removeAnnotation( annotationClass )
         cpy
      }

      private def mkTxnClass( cd: ClassDef ) : ClassDef = {
         val txnName = (("Txn" + cd.name) : Name).toTypeName
         val cpy = treeCopy.ClassDef( cd, cd.mods, txnName, cd.tparams, cd.impl )
         cpy.symbol.removeAnnotation( annotationClass )
         cpy.symbol.name = txnName
         cpy
      }

      private object AnnotatedClasses {
         def unapply( trees: List[ Tree ]) : Option[ AnnotatedClasses ] = {
            if( trees.exists {
               case cd: ClassDef if( cd.symbol.hasAnnotation( annotationClass )) => true
               case _ => false
            }) {

               val mapped = trees.map {
                  case cd: ClassDef if( cd.symbol.hasAnnotation( annotationClass )) =>
                     log( "Has txn annotation : " + cd.name )
                     Left( cd )
                  case t =>
                     Right( t )
               }

               Some( new AnnotatedClasses( mapped ))

            } else None
         }
      }
      private class AnnotatedClasses private( val cds: List[ Either[ ClassDef, Tree ]]) {
         def flatTransform : List[ Tree ] = {
            cds.flatMap {
               case Left( cd )   => List( mkPlainClass( cd ), mkTxnClass( cd ))
               case Right( t )   => List( t )
            }
         }
      }

//      private def transformAnnotatedClasses( cds: List[ ClassDef ]) : List[ ClassDef ] = cds.flatMap { cd =>
//         log( "(pd) Have txn annotation : " + cd.name )
//         val txn = treeCopy.ClassDef( cd, cd.mods, "Txn" + cd.name, cd.tparams, cd.impl )
//         Seq( cd, txn )
//      }

      override def transform( tree: Tree ) : Tree = super.transform( tree match {
//         case pd @ PackageDef( pid, AnnotatedClasses( anno )) =>
//            treeCopy.PackageDef( pd, pid, transformAnnotatedClasses( anno ))
//
//         case tmp @ Template( parents, self, AnnotatedClasses( anno )) =>
//            treeCopy.Template( tmp, tmp.parents, tmp.self, body )
//
//            anno.foreach { cd =>
//               log( "(tmp) Have txn annotation : " + cd.name )
//            }
//            tmp

         case pd @ PackageDef( pid, AnnotatedClasses( anno )) =>
            treeCopy.PackageDef( pd, pid, anno.flatTransform )
         case tmp @ Template( parents, self, AnnotatedClasses( anno )) =>
            treeCopy.Template( tmp, tmp.parents, tmp.self, anno.flatTransform )

//         case cd: ClassDef if( cd.symbol.hasAnnotation( annotationClass )) =>
//            log( "Has txn annotation : " + cd.name )
////            cd.symbol.removeAnnotation( annotationClass )
////            val txnName = ("Txn" + cd.name.decode).encode // .toTypeName
////            val cpy = cd.copy( name = txnName )
////            Block( mkPlainClass( cd ), mkTxnClass( cd ))
//            mkPlainClass( cd )
         case _ => tree
      })
   }
}