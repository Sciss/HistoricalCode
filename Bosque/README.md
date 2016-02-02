# Bosque

[![Flattr this](http://api.flattr.com/button/flattr-badge-large.png)](https://flattr.com/submit/auto?user_id=sciss&url=https%3A%2F%2Fgithub.com%2FSciss%2FBosque&title=Bosque&language=Scala&tags=github&category=software)

## statement

Bosque is (C)opyright by 2006&ndash;2016 Hanns Holger. All rights reserved. It is released under the [GNU General Public License](http://github.com/Sciss/Bosque/blob/master/licenses/Bosque-License.txt) and comes with absolutely no warranties.

Bosque is a multitrack timeline editor for SuperCollider. It consists of a class library for SuperCollider language, and a Java library which is embedded via SwingOSC. Bosque can be used to programmatically create timeline structures in SuperCollider, and to control algorithmic processes by a timeline. Timeline objects include audio file regions, markers, function regions (which execute arbitrary code) and envelope automations.

Although Bosque is not any more actively developed (there is a successor project "Kontur" which will eventually have the full feature set of Bosque), the software is currently in good shape and fully documented.

## requirements / installation

The SuperCollider classes and help files work out of the box. Just copy them to the appropriate folders for SuperCollider, or make symlinks. E.g. on OS X, link the `SCClassLibrary` folder into `~/Library/Application\ Support/SuperCollider/Extensions/SCClassLibrary/Bosque` and the `Help` folder into `~/Library/Application\ Support/SuperCollider/Extensions/Help/Bosque`.

The java library called "TimeBased" needs to be build initially. To do so, go into the "timebased" folder. You will need Java 1.5 and Apache ant, as well as a SwingOSC installation.

- make a symlink from your SwingOSC installation's `SwingOSC.jar` into `libraries/`.
- build using `ant` (it will run the default target)
- after a successful build you will find `TimeBased.jar`
- make a symlink from this `TimeBased.jar` into your SuperCollider installation's extensions folder, e.g. on OS X, link into `~/Library/Application\ Support/SuperCollider/Extensions/`

To run, SuperCollider version 3.3 and SwingOSC v0.65 or any newer versions should be fine.

As a starting point, open the main help file for "Bosque".

## contributing

Please see the file [CONTRIBUTING.md](CONTRIBUTING.md)

