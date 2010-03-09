package de.sciss.datanucleustest

import javax.jdo.{ JDOHelper }
import java.util.{ Collection => JCollection }
import scala.collection.JavaConversions._

object Main {
   var didOpenPM = false
   lazy val pm = {
      val pmf  = JDOHelper.getPersistenceManagerFactory( "datanucleus.properties" )
      didOpenPM = true
      pmf.getPersistenceManager
   }
   
   def main( args: Array[ String ]) {
      println( "--- JDO Test --- " )
      val success = try {
         args.headOption.getOrElse( "-help" ) match {
            case "-create" => {
               create( args( 1 ).toLong, args( 2 ).toLong )
               dump
            }
            case "-dump"   => dump
            case "-query"  => query
            case "-delete" => delete( args( 1 ).toLong, args( 2 ).toLong )
            case "-help" => {
               println( """
Options:
-create <start> <stop>
-query
-delete <start> <stop>
               """ )
               true
            }
            case x => {
               println( "Unknown option '" + x + "'. Use -help to see possible options." )
               false
            }
         }
      } finally {
         if( didOpenPM ) pm.close()
      }
      System.exit( if( success ) 0 else 1 )
   }

   def create( start: Long, stop: Long ) : Boolean = {
      val tx = pm.currentTransaction
      try {
         tx.begin()
         val span = Span( start, stop )
         pm.makePersistent( span )
         tx.commit()
         true
      } finally {
         if( tx.isActive ) tx.rollback()
         false
      }
   }

   def query : Boolean = {
      val tx = pm.currentTransaction
      try {
         tx.begin()
         val e = pm.getExtent( classOf[ Span ], true )
         val q = pm.newQuery( e, "start < 1000" )
         q.setOrdering( "start ascending" )
         val c = q.execute().asInstanceOf[ JCollection[ Span ]]
         for( s <- c ) println( s.toString )
         tx.commit()
         true
      } finally {
         if( tx.isActive ) tx.rollback()
         false
      }
   }

   def dump : Boolean = {
      val tx = pm.currentTransaction
      tx.begin()
      val e = pm.getExtent( classOf[ Span ], true )
      for( s <- e ) println( s.toString )
      tx.commit()
      true
   }

   def delete( start: Long, stop: Long ) : Boolean = {
      println( "DELETE NOT YET IMPLEMENTED" )
      false
   }
}