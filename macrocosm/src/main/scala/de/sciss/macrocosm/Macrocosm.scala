package de.sciss.macrocosm

import scala.reflect.makro.Context
import language._
import experimental.macros

object Macrocosm {
   import com.github.retronym.macrocosm.Macrocosm.desugar

   sealed trait MaybeRate
   object UndefinedRate extends MaybeRate
   sealed trait Rate extends MaybeRate
   object audio extends Rate
   object control extends Rate
   trait GE { def rate: MaybeRate }

   object LPF {
      def ar( in: GE ) : LPF = macro arImpl

      /* private */ def arImpl(c: Context)(in: c.Expr[GE]) = {
         import c.universe._
//         apply( audio, in )
//
         reify {
            println( desugar( in.splice ))
            apply(audio, in.splice)
         }
//         val appl = reify( apply _ )
//         Apply( appl., in )

//         c.Expr(
//           Apply(This)
//           Literal(Constant(s))
//         )
      }
   }
   final case class LPF( rate: Rate, in: GE )
}