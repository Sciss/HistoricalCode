package de.sciss.transactional.plugin

import tools.nsc.plugins.{Plugin, PluginComponent}
import tools.nsc.Global


final class TransactionalPlugin( val global: Global )
extends Plugin {
   val name          = "transactional"
   val description   = "processes the @txn annotation"

   val components    = List[ PluginComponent ]( new TransformAnnotations( this, global ))
}