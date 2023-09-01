//package de.sciss.synth.proc.view
//
//import de.sciss.synth.proc.{EphemeralSystem => Eph, _}
//import de.sciss.confluent.VersionPath
//import impl.{EphemeralModelVarImpl, ModelImpl}
//import javax.swing._
//import event.{AncestorListener, AncestorEvent}
//import java.awt.EventQueue
//import edu.stanford.ppl.ccstm.{Txn, TxnLocal, STM, Ref}
//
//object ContextNavigator {
//   def apply[ C ]()( implicit c: Ctx[ C ]) : ContextNavigator[ C ] = (c.repr match {
//      case ec: Ephemeral => new EphemeralNav
//
////      case KTemporalSystem =>
////      case PTemporalSystem =>
//
//      case bc: Bitemporal =>
////         val vRef    = Ref( bc.path )
////         val vModel  = new ModelImpl[ Ephemeral, VersionPath ] {}
//         implicit val c = bc
//         val vRef    = new EphemeralModelVarImpl[ Bitemporal, VersionPath ]( bc.path )
//         new BitemporalNav( bc.system, vRef )
//      case _ => error( "No context navigator available for " + c /*.system */ )
//   }).asInstanceOf[ ContextNavigator[ C ]] // XXX can we work around the cast?
//
//   private class EphemeralNav extends ContextNavigator[ Ephemeral ] {
////      def view = new JComponent with Disposable { def dispose {} }
//      def view = new JPanel()
//      def t[ T ]( fun: Ctx[ Ephemeral ] => T ) : T = Eph.t( fun )
//      def isApplicable( implicit c: Ctx[ Ephemeral ]) = true
//   }
//
//   private class BitemporalNav( system: BitemporalSystem, vRef: EphemeralModelVarImpl[ Bitemporal, VersionPath ])
//   extends ContextNavigator[ Bitemporal ] {
//      private val txnInitiator = new TxnLocal[ Boolean ] {
//         override protected def initialValue( txn: Txn ) = false
//      }
//
//      def isApplicable( implicit c: Ctx[ Bitemporal ]) = {
////         val cp = c.repr.path
//////         val ci = c.repr.interval
////         val np = vRef.get
////println( "checking cp = " + cp + ", np = " + np )
////         cp == np
//         txnInitiator.get( c.txn )
//      }
//
//      def view = {
////         val p = new JPanel with Disposable {
////            def dispose {
////
////            }
////         }
////         p.setLayout( new BoxLayout( p, BoxLayout.X_AXIS ))
//         val p = Box.createHorizontalBox()
//         p.add( new JLabel( "Path:" ))
//         p.add( Box.createHorizontalStrut( 4 ))
//         val ggVersion = new JTextField( 12 )
//         ggVersion.setBorder( null )
//         ggVersion.setEditable( false )
////         ggVersion.setText( vRef.get( c.txn ).toString )
//         p.add( ggVersion )
//
//         def setVersionPathString( vp: VersionPath ) {
//            ggVersion.setText( vp.toString )
//         }
//
//         val l = new Model.Listener[ Bitemporal, VersionPath ] {
//            def updated( newPath: VersionPath )( implicit c: Ctx[ Bitemporal ]) {
//               EventQueue.invokeLater( new Runnable { def run = setVersionPathString( newPath )})
//            }
//         }
//
//         ggVersion.addAncestorListener( new AncestorListener {
//            def ancestorAdded( e: AncestorEvent ) {
//               Eph.t { implicit c =>
//                  vRef.addListener( l )
//                  setVersionPathString( vRef.getTxn( c.txn ))
//               }
//            }
//            def ancestorRemoved( e: AncestorEvent ) {
//               Eph.t { implicit c => vRef.removeListener( l )}
//            }
//            def ancestorMoved( e: AncestorEvent ) {}
//         })
//
//         p
//      }
//
//      def t[ T ]( fun: Ctx[ Bitemporal ] => T ) : T = {
//         // XXX todo: should add t to BitemporalSystem and pass txn to in
//         // variant so we don't call atomic twice
//         // (although that is ok and the existing transaction is joined)
//         // ; like BitemporalSystem.inRef( vRef.getTxn( _ )) { ... } ?
//         STM.atomic { t =>
//            val oldPath = vRef.getTxn( t )
//            txnInitiator.set( true )( t )
//            system.in( oldPath ) { implicit c =>
//               val res     = fun( c )
//               val newPath = c.repr.path
//               if( newPath != oldPath ) {
//                  vRef.set( newPath )
//               }
//               txnInitiator.set( false )( t )
//               res
//            }
//         }
//      }
//   }
//}
//
//trait ContextNavigator[ C ] {
//   def view : JComponent /* with Disposable */
//   def t[ T ]( fun: Ctx[ C ] => T ) : T
//   def isApplicable( implicit c: Ctx[ C ]) : Boolean
//}