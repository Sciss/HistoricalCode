package de.sciss.synth

import proc.{EVar, CtxLike}

package object proc {
   type Ct = CtxLike
   type Vr[ C, T ] = EVar[ C, T ]
}
