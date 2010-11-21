package testinonetwo

trait Listener[ -C, -T ] {
   def updated( v: T )( implicit c: C ) : Unit
}

trait EVar[ C, T ] {
   def get( implicit c: C ) : T
   def set( v: T )( implicit c: C ) : Unit
   def addListener( l: Listener[ C, T ])( implicit c: ECtx ) : Unit
   def removeListener( l: Listener[ C, T ])( implicit c: ECtx ) : Unit
}

trait KVar[ C, T ] extends EVar[ C, T ] {
   def range( vStart: Int, vStop: Int )( implicit c: ECtx ) : Traversable[ T ]
}

trait ECtx {
   def v[ T ]( init: T ) : EVar[ ECtx, T ]
}

trait KCtx {
   def v[ T ]( init: T ) : KVar[ KCtx, T ]
   def eph : ECtx
}
