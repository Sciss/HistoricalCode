# play-json-sealed

## statement

This project is meant as an extension for the [play-json-alone](https://github.com/mandubian/play-json-alone) project, enabling the automatic creation of JSON formats for sealed traits.

It is released under the [GNU Lesser General Public License](https://raw.github.com/Sciss/play-json-sealed/master/LICENSE) v2.1+ and comes with absolutely no warranties. It will probably be submitted to Play at some point. To contact the author, send an email to `contact at sciss.de`.

## requirements / installation

This project currently compiles against Scala 2.10 using sbt 0.12.

To use the library in your project:

    "de.sciss" %% "play-json-sealed" % v

The current version `v` is `"0.0.+"`

In order to find the dependency (Play JSON 2.2 Snapshot), the following resolver must also be added:

    "Mandubian repository snapshots" at "https://github.com/mandubian/mandubian-mvn/raw/master/snapshots/"

## example

Examples are provided by way of test sources (`sbt test`).

## limitations

Recursive types are not yet possible (see `RecursiveTest`).