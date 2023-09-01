package formattest

import org.yaml.snakeyaml.Yaml

object YAMLTest extends App {
   val yaml = new Yaml()
   val text = io.Source.fromFile( "test.yaml", "UTF-8" ).mkString
//   println( "FOUND:\n" + text )
   val doc = yaml.load( text )
   doc match {
      case m: java.util.Map[_, _] => println( "a map" )
      case _ => println( "not: " + doc.getClass )
   }
}