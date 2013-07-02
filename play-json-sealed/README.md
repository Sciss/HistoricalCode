# play-json-sealed

## statement

This project is meant as an extension for the [play-json-alone](https://github.com/mandubian/play-json-alone) project, enabling the automatic creation of JSON formats for sealed traits.

It is released under the [GNU Lesser General Public License](https://raw.github.com/Sciss/play-json-sealed/master/LICENSE) v2.1+ and comes with absolutely no warranties. It might be submitted to Play at some point. To contact the author, send an email to `contact at sciss.de`.

## requirements / installation

This project currently compiles against Scala 2.10 using sbt 0.12.

To use the library in your project:

    "de.sciss" %% "play-json-sealed" % v

The current version `v` is `"0.0.+"`

The following resolvers must also be added:

    "Mandubian repository snapshots" at "https://github.com/mandubian/mandubian-mvn/raw/master/snapshots/"
    "Sonatype OSS snapshots"         at "https://oss.sonatype.org/content/repositories/snapshots/"

## example

Examples are provided by way of test sources (`sbt test`). Basically you get a format via `SealedTraitFormat[A]`. If that trait requires formats for other traits, you need to put their respective formats implicitly in scope, first. There are also a few useful formats in `Formats`, such as `FileFormat` and `Tuple2Format`.

## limitations and issues

- Recursive types are not yet possible (see `RecursiveTest`).
- There is a scoping issue. If you have `object Foo { case class Bar(i: Int) }`, then you need to __import__ the `Bar` symbol for the macros to work. E.g. `Json.format[Foo.Bar]` fails, whereas `import Foo.Bar; Json.format[Bar]` works.
