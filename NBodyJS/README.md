# NBodyJS

An HTML5/JavaScript n-body simulator... written in Scala.
This is a fork from [github.com/ccampo133/NBodyJS](https://github.com/ccampo133/NBodyJS),
departing from the original Scala code and converting the recent Kotlin version back to Scala.

The [sbt-extras](https://github.com/paulp/sbt-extras) script by Paul Phillips, made available
under a BSD-style license, is included, so you can simply build by running:

    ./sbt fastOptJS

Then to see the demo in the browser:

    xdg-open demo.html

I haven't published the Scala version to GH pages yet, but you can see the (basically identical,
although of course less elegant ;) Kotlin version as a
[demo on GitHub pages](http://ccampo133.github.com/NBodyJS).
