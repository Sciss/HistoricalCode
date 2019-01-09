# Scala-Native-Test

## statement

A small test project evaluating [Scala Native](https://github.com/scala-native/scala-native).
The current test case is using the [Jack Audio](https://github.com/jackaudio/headers) library.
It is (C)opyright 2016 by Hanns Holger Rutz. All rights reserved. This project is released under 
the [GNU Lesser General Public License](https://raw.github.com/Sciss/scala-native-test/master/LICENSE) v2.1+ 
and comes with absolutely no warranties. To contact the author, send an email to `contact at sciss.de`.

## requirements / installation

This project compiles against the current 0.1-SNAPSHOT of Scala Native. More precisely, I have last
used it at [1756442](https://github.com/scala-native/scala-native/tree/1756442067d2d8353017afd26e3cd7e190db06a3).

I am working on Debian 8 (Jessie / stable). This requires that a newer Clang is installed. What I did
is download the binary 'Clang for x86_64 Debian 8' from [here](http://llvm.org/releases/download.html#3.8.0).
You'll have to add the `clang/bin` to your `PATH`. What I did is add these lines to `~/.profile`:

    if [ -d "$HOME/Documents/devel/clang/bin" ] ; then
      PATH="$PATH:$HOME/Documents/devel/clang/bin"
    fi

The project defines the bindings for Jack and assumes that the Jack development files are installed.
On my computer this is:

    sudo apt-get install libjack-jackd2-dev
    
Then the required library is at `/usr/lib/x86_64-linux-gnu/libjack.so`. If it is elsewhere, the
path in `build.sbt` must be adjusted accordingly.