package de.sciss.transactional.plugin

import tools.nsc
import nsc.plugins.PluginComponent
import nsc.Global
import nsc.symtab.Flags
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
      private def cleanPlainClass( cd: ClassDef ) {
         // check parents! cd.impl.parents

//         val impl = cd.impl
//         val plainImpl = treeCopy.Template( impl, impl.parents, impl.self, impl.body.map( transformPlainBody ))
//         val cpy = treeCopy.ClassDef( cd, cd.mods, cd.name, cd.tparams, plainImpl )
//         cpy.symbol.removeAnnotation( annotationClass )
//         cpy
         cd.symbol.removeAnnotation( annotationClass )
         cd.impl.body.foreach( cleanPlainClassBody )
      }

      private def cleanPlainClassBody( t: Tree ) { t match {
         case dd: DefDef if( dd.symbol.hasAnnotation( annotationClass )) =>
            dd.symbol.removeAnnotation( annotationClass )
         case _ =>
      }}

//      private def transformPlainBody( t: Tree ) : Tree = t match {
//         case dd: DefDef if( dd.symbol.hasAnnotation( annotationClass )) =>
////            log( "defdef: " + name )
//            val cpy = treeCopy.DefDef( dd, dd.mods, dd.name, dd.tparams, dd.vparamss, dd.tpt, dd.rhs )
//            cpy.symbol.removeAnnotation( annotationClass )
//            cpy
//         case _ => t
//      }

      private def mkTxnClassX( cd: ClassDef ) : ClassDef = {
         val txnName = (("Txn" + cd.name) : Name).toTypeName
         val txnImpl = mkTxnClassImpl( cd.impl )
         val txnMods = cd.mods | Flags.SYNTHETIC
         val cpy = treeCopy.ClassDef( cd, txnMods, txnName, Nil /* cd.tparams */, txnImpl )
         val txnSym = cd.symbol.owner.newClass( txnName )
         txnSym.flags = cd.symbol.flags | Flags.SYNTHETIC
         log( "Txn -- ValDef " + cd.impl.self )
//         cpy.symbol.removeAnnotation( annotationClass )
//         cpy.symbol.name = txnName
         cpy.symbol = txnSym
         cpy
      }

      private def mkTxnClass( cd: ClassDef ) : ClassDef = {
         val txnName = (("Txn" + cd.name) : Name).toTypeName
//         val txnImpl = Template( parents, self, constrMods, vparamss, argss ) // mkTxnClassImpl( cd.impl )
//         val txnParents = List[ Symbol ]( definitions.getClass( "java.lang.Object" ), definitions.getClass( "ScalaObject" ))
         val txnParents = List.empty[ Tree ]// TypeTree( definitions.getClass( "java.lang.Object" ).getType )
         val txnSelf = ValDef( mods = NoMods, name = "", tpt = EmptyTree, rhs = EmptyTree ) // ???
         val txnConstrMods = NoMods
         val txnVParamss = List.empty[ List[ ValDef ]]
         val txnArgss = List.empty[ List[ Tree ]]
         val txnBody = List.empty[ Tree ]
         val txnSuperPos = NoPosition
         val txnImpl = Template( txnParents, txnSelf, txnConstrMods, txnVParamss, txnArgss, txnBody, txnSuperPos ) // mkTxnClassImpl( cd.impl )
         val txnMods = cd.mods | Flags.SYNTHETIC
         val txnTParams = List.empty[ TypeDef ]
         val cpy = ClassDef( txnMods, txnName, txnTParams, txnImpl )
         val txnSym = cd.symbol.owner.newClass( txnName )
         txnSym.flags = cd.symbol.flags | Flags.SYNTHETIC
         cpy.symbol = txnSym
         cpy
      }

      private def mkTxnClassImpl( orig: Template ) : Template = {
         val txnBody = List.empty[ Tree ]
//         val txnSelf = orig.self // treeCopy.ValDef( ... )
         val v = orig.self
//         treeCopy.ValDef(tree, mods, name, tpt, rhs )
         val txnSelf = treeCopy.ValDef( v, v.mods, v.name, v.tpt, v.rhs )
         treeCopy.Template( orig, orig.parents, txnSelf, txnBody )
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
               case Left( cd )   =>
                  val txn = mkTxnClass( cd )
                  cleanPlainClass( cd )
//                  List( cd )
                  List( cd, txn )
               case Right( t ) =>
                  List( t )
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