package formattest

import com.codahale.jerkson.Json

case class Group(plugin: String, ugens: Seq[ Either[ RootUGenSpec, ChildUGenSpec ]])
case class RootUGenSpec(name: String, categories: Set[ String ], rates: Set[ String ], properties: Option[ Set[ String ]],
                        args: Seq[ Any ], summary: String, detail: Option[ String ], links: Option[ Seq[ String ]]) {
   def pretty: String = name + categories.mkString( "\n  categories = [" , ", ", "]" ) + "\n" +
      rates.mkString( "  rates: [", ", ", "]" ) + "\n  properties: " + properties.getOrElse( Set.empty ).mkString( "[", ", ", "]" ) +
      "\n\n  " + summary + detail.map( s => "\n\n  " + s ).getOrElse( "" ) + "\n\n  args: " + args
}

case class ChildUGenSpec(name: String, inherits: String, rates: Option[ Set[ String ]],
                         summary: Option[ String ], detail: Option[ String ], links: Option[ Seq[ String ]])

object JSONTest extends App {
   val text = io.Source.fromFile( "test.json", "UTF-8" ).mkString
   val res  = Json.parse[List[Group]]( text )

   res.foreach { group =>
      println( "---- PLUGIN: " + group.plugin + " ----" )
      group.ugens.foreach {
         case Left( root )    => println( root.pretty )
         case Right( child )  => println( "(inherited) " + child )
      }
   }
}