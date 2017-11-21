# JNITablet

__Note:__ I don't use Mac any longer, and this library probably does not work with current versions of macOS. See [jpen](https://github.com/nicarran/jpen/) for a cross-platform tablet support library for the JVM.

## statement

A modified version of the JNI wrapper to access wacom tablet data in OS X. The original code is by Jerry Huxtable.

Unfortunately the included Xcode project doesn't build anymore in Snow Leopard. JH has [updated his version](http://www.jhlabs.com/java/tablet/) to include a fix called JRSwizzle... This project needs to be upgraded accordingly. Currently it has the function to store the .jar and .jnilib files for download access by sbt.
