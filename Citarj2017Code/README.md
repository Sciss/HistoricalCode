# Citarj2017Code

## statement

This project contains some code for an illustration in an upcoming journal article.

This project is (C)opyright 2017 by Hanns Holger Rutz. All rights reserved. It is released under 
the [GNU General Public License](https://raw.github.com/Sciss/Citarj2017Code/master/LICENSE) v3+ and comes 
with absolutely no warranties. To contact the author, send an email to `contact at sciss.de`

(GPL is required by inclusion of the PDF rendering library, it could probably be moved to a sub-project).

## requirements / installation

This project builds using sbt. To run an example:

    sbt 'run -o test.png src'

This creates a tableau of tree-maps from the source code in directory `src` and writes the result to file `test.png`.
