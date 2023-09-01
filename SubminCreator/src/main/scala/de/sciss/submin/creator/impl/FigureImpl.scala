package de.sciss.submin.creator
package impl

import de.sciss.lucre.{data, DataOutput, stm, DataInput, geom}
import data.SkipOctree
import geom.{IntHyperRectangleN, IntPointN, IntHyperCubeN, IntSpace}
import data.SpaceSerializers
import collection.immutable.{IndexedSeq => IIdxSeq}
import IntSpace.NDim

object FigureImpl {
   private val SER_VERSION = 0

   private implicit val space : NDim = NDim( 4 )
   private final val hyperExt = 0x40000000
   private final val hyper    = IntHyperCubeN( IIdxSeq( 0, 0, 0, 0 ), hyperExt )

   private implicit def elementToPoint( elem: Element, tx: S#Tx ) : NDim#PointLike = {
      val r = elem.bounds
      val c = IIdxSeq( r.left, r.top, r.right, r.bottom )
      IntPointN( c )
   }

   import SpaceSerializers.IntHyperCubeNSerializer

   def empty( implicit tx: S#Tx ) : Figure = {
      val id      = tx.newID()
      val nameRef = Strings.newVar( Strings.newConst[ S ]( "Untitled" ))
      val oct     = SkipOctree.empty[ S, NDim, Element ]( hyper )
      new Impl( id, nameRef, oct )
   }

   def read( in: DataInput, access: S#Acc )( implicit tx: S#Tx ) : Figure = {
      serializer.read( in, access )
   }

   private object serializer extends stm.Serializer[ S#Tx, S#Acc, Impl ] {
      def write( v: Impl, out: DataOutput ) { v.write( out )}

      def read( in: DataInput, access: S#Acc )( implicit tx: S#Tx ) : Impl = {
         val serVer  = in.readUnsignedByte()
         require( serVer == SER_VERSION, "Wrong serialized version. Expected " + SER_VERSION + " but found " + serVer )
         val id      = tx.readID( in, access )
         val nameRef = Strings.readVar[ S ]( in, access )
         val oct     = SkipOctree.read[ S, NDim, Element ]( in, access )
         new Impl( id, nameRef, oct )
      }
   }

   private final class Impl( val id: S#ID, nameRef: ExVar[ String ], oct: SkipOctree[ S, NDim, Element ])
   extends Figure with stm.Mutable[ S#ID, S#Tx ] {
      def name( implicit tx: S#Tx ) : Ex[ String ] = nameRef.get
      def name_=( value: Ex[ String ])( implicit tx: S#Tx ) { nameRef.set( value )}

      def elements( overlapping: Rectangle )( implicit tx: S#Tx ) : IIdxSeq[ Element ] = {
         // - the four components stored per element are left, top, right, bottom
         // - we are asking for all elements overlapping the query rectangle qr
         // - thus we need to filter elements e where
         //   e.left <= qr.right && e.top <= qr.bottom && e.right >= qr.left && e.bottom >= qr.top
         // - in other words the query shape must implement the following intervals
         //   1st dimension : [-inf, qr.right)
         //   2nd dimension : [-inf, qr.bottom)
         //   3rd dimension : (qr.left, +inf]
         //   4th dimension : (qr.top, +inf]
         val c    = IIdxSeq( -hyperExt        -> overlapping.right,
                             -hyperExt        -> overlapping.bottom,
                             overlapping.left -> (hyperExt - 1),
                             overlapping.top  -> (hyperExt - 1) )
         val qr   = IntHyperRectangleN( c )
         oct.rangeQuery( qr ).toIndexedSeq
      }

      def dispose()( implicit tx: S#Tx ) {
         id.dispose()
         nameRef.dispose()
         oct.dispose()
      }

      def write( out: DataOutput ) {
         out.writeUnsignedByte( SER_VERSION )
         id.write( out )
         nameRef.write( out )
         oct.write( out )
      }
   }
}