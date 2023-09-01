# Scala-Native-Test

## statement

A small test project evaluating [Scala Native](https://github.com/scala-native/scala-native).
The current test case is using the [Jack Audio](https://github.com/jackaudio/headers) library.
It is (C)opyright 2016–2019 by Hanns Holger Rutz. All rights reserved. This project is released under 
the [GNU Lesser General Public License](https://raw.github.com/Sciss/scala-native-test/master/LICENSE) v2.1+ 
and comes with absolutely no warranties. To contact the author, send an email to `contact at sciss.de`.

## requirements / installation

This project compiles against version 0.3.8 of Scala Native (sbt plugin).

I am working on Debian 9 (stretch / stable). I prepared by following [the setup](http://www.scala-native.org/en/latest/user/setup.html)
and installing `sudo apt install clang libunwind-dev libgc-dev libre2-dev`.

The project defines the bindings for Jack and assumes that the Jack development files are installed.
On my computer this is:

    sudo apt install libjack-jackd2-dev
    
Then the required library is at `/usr/lib/x86_64-linux-gnu/libjack.so`. If it is elsewhere, the
path in `build.sbt` must be adjusted accordingly (?).

## running

The main test is in `SimpleClient`. It replicates [the same Jack example](https://github.com/jackaudio/example-clients/blob/master/simple_client.c)
by creating a jack client with one input and one output, piping the input simply to the output.
