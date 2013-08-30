Small GraphViz (.dot) project for me to keep track of my libraries at [GitHub](https://github.com/Sciss). 

 - Unless noted, Scala projects use version 2.10
 - Artifact IDs for Scala libraries are constructed with prefix `"de.sciss" %%"`
 - Artifact IDs for Java libraries are constructed with prefix `"de.sciss" %"`
 - Scala projects have black outline, Sbt plugins have blue outline, Java projects have green outline
 - Red outline indicates projects still rely on older versions of dependencies
 - Arrows point from dependent to dependency
 - Versions refer to latest stable. If a newer snapshow exists, outline is dotted
 - Test-only dependencies have dotted arrows.

See also the [sbt-dependency-graph](https://github.com/jrudolph/sbt-dependency-graph) project for an alternative way of creating such diagrams automatically.
