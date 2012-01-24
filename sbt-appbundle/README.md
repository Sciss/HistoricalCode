## sbt-appbundle

### statement

sbt-appbundle is a plugin for xsbt (sbt 0.11) that adds the `appbundle` task to create a standalone OS X application bundle.

sbt-appbundle is (C)opyright 2011&ndash;2012 by Hanns Holger Rutz. All rights reserved. It is released under the [GNU Lesser General Public License](http://github.com/Sciss/sbt-appbundle/blob/master/licenses/sbt-appbundle-License.txt) and comes with absolutely no warranties. To contact the author, send an email to `contact at sciss.de`.

### usage

To use the plugin in your sbt project, add the following entry to `project/plugins.sbt`:

    addSbtPlugin( "de.sciss" % "sbt-appbundle" % "0.11")

sbt-appbundle is now migrating to [sbt Community Plugins](http://www.scala-sbt.org/community-plugins.html).

You can find an example of its usage in `src/test-project`. Basically you add the following statement to the beginning of the main `build.sbt`:

    seq(appbundle.settings: _*)

And can then configure the `appbundle` task. Without any additional configuration, the task will create the app bundle in the base directory under the name `project-name.app`. The following keys are available:

|**name**          |**key-type**           |**description**             |**default**|
|------------------|-----------------------|----------------------------|-----------|
|`name`            |`String`               |Name for the bundle, without the .app extension | `name` in main scope |
|`normalizedName`  |`String`               |Lower case namem used as second part in the bundle identifier | `normalizedName` in main scope |
|`organization`    |`String`               |Your publishing domain (reverse website style), used as first part in the bundle identifier | `organization` in main scope |
|`version`         |`String`               |Version string which is shown in the Finder and About menu | `version` in main scope |
|`mainClass`       |`Option[String]`       |Main class entry when application is launched. Appbundle fails when this is not specified or inferred | `mainClass` in main scope |
|`stub`            |`File`                 |Path to the java application stub executable. | /System/ Library/ Frameworks/ JavaVM.framework/ Versions/ Current/ Resources/ MacOS/ JavaApplicationStub |
|`fullClasspath`   |`Classpath`            |Constructed from the `fullClasspath` entries in `Compile` and `Runtime` | |
|`javaVersion`     |`String`               |The minimum Java version required to launch the application | `1.6+` |
|`javaOptions`     |`Seq[String]`          |Options passed to the `java` command when launching the application | `javaOptions` in main scope |
|`systemProperties`|`Seq[(String, String)]`|A key-value map passed as Java `-D` arguments (system properties) | extracts `-D` entries from `javaOptions` and adds entries for `screenMenu` and `quartz` |
|`screenMenu`      |`Boolean`              |Whether to display the menu bar in the screen top | `true`
|`quartz`          |`Option[Boolean]`      |Whether to use the Apple Quartz renderer (`true`) or the default Java renderer | `None`. In this case Quartz is used for Java 1.5, but not for Java 1.6+ |
|`icon`            |`Option[File]`         |Image or icon file which is used as application icon. A native `.icns` file will be copied unmodified to the bundle, while an image (such as `.png`) will be converted through the OS X shell utility `sips`, scaling the image to the next supported size, which is either of 16, 32, 48, 128, 256, or 512 pixels width/height | `None` |

Example:

    appbundle.name := "CamelCase"
    appbundle.javaOptions += "-Xmx1024m"
    appbundle.javaOptions ++= Seq( "-ea" )
    appbundle.systemProperties += "SC_HOME" -> "../scsynth"
    appbundle.icon = Some( file( "myicon.png" ))

### creating an IntelliJ IDEA project

If you want to work on the plugin in IntelliJ IDEA, you can set up a project like this: Make sure you have the following contents in `~/.sbt/plugins/build.sbt`:

    resolvers += "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"
    
    addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.0.0")

Then to create the IDEA project, run the following two commands from the xsbt shell:

    > set ideaProjectName := "sbt-appbundle"
    > gen-idea


### credits

The test application icon is in the public domain and was obtained from the [Open Clip Art Library](http://openclipart.org/detail/20299/moon-in-comic-style-by-rg1024-20299)                     .
