package de.sciss.transactional.plugin

import tools.nsc
import nsc.ast.TreeDSL
import nsc.plugins.PluginComponent
import nsc.Global
import nsc.transform.{Transform, TypingTransformers}

final class TransformAnnotations( plugin: TransactionalPlugin, val global: Global )
extends PluginComponent with Transform with TypingTransformers {
   val runsAfter  = List( "typer" )  // "refchecks"
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

      override def transform( tree: Tree ) : Tree = super.transform( tree match {
         case cd @ ClassDef( _, _, _, _ ) if( cd.symbol.hasAnnotation( annotationClass )) =>
            log( "Has txn annotation : " + cd.name )
            // XXX
            cd
         case _ => tree
      })
   }
}