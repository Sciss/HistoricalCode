# Markdown Test

Figuring out how to render markdown documents within a Swing application.

There are several Scala based Markdown processors. [Laika](https://github.com/planet42/Laika) has a 2.9 MB binary; [ScalaMD](https://github.com/chirino/scalamd) has tiny 85 KB (Apache License). We try with the latter, it only converts an MD string into a HTML fragment string.

We render with standard HTML editor kit and [SwingBox](https://github.com/radkovo/SwingBox) (licensed under LGPL). It seems a much larger project than we need, biggest transitive dependency is Xerces (1.4 MB). Also spams the console with dozens of log messages upon startup, Java style. Have to figure out how to turn logging off... But rendering is nice, using GitHub default CSS (no language specific colourisation, tough).

## build + run

`sbt run`