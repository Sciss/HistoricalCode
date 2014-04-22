# Project Dependencies

Small GraphViz (.dot) project for me to keep track of my libraries at [GitHub](https://github.com/Sciss). 

 - Unless noted, Scala projects use version 2.11
 - Artifact IDs for Scala libraries are constructed with prefix `"de.sciss" %%"`
 - Artifact IDs for Java libraries are constructed with prefix `"de.sciss" %"`
 - Scala projects have black outline, Sbt plugins have blue outline, Java projects have green outline
 - Red outline indicates projects still rely on older versions of dependencies
 - Arrows point from dependent to dependency
 - Versions refer to latest stable. If a newer snapshow exists, outline is dotted
 - Test-only dependencies have dotted arrows.

See also the [sbt-dependency-graph](https://github.com/jrudolph/sbt-dependency-graph) project for an alternative way of creating such diagrams automatically.

## License Check

In order to ensure the correct compatibility between licenses, I am now employing the following scheme:

 - in general, anything that can act as a library is licensed under LGPL v2.1+
 - a few things that are forked from others with only minor contributions remain at BSD style license
 - things that are generally standalone applications tend to use GPL v2+; however I am becoming more liberal and tend to relax them to LGPL in many cases
 - projects that link to third-party GPL v3 or AGPL v3 libraries are forced to be released with GPL v3+ or AGPL v3+

The projects are published as follows:

 - LGPL v2.1+: FingerTree, FileCache, ScissDSP, GUIFlitz, RaphaelIcons, KollFlitz, ScalaAudioFile, ScalaOSC, ScalaInterpreterPane, FileUtil, AudioWidgets, Span, Desktop, SwingPlus, Numbers, Processor, Model, Serial, SonogramOverview, FScapeJobs
 - GPL v2+: LucreSTM(x), LucreData(x), LucreEvent(x), LucreConfluent(x), LucreSwing(x), ScalaCollider-UGens(x) (spec: BSD), ScalaCollider(x), SoundProcesses, ContextSnake(x), Strugatzki(x), NuagesApp, Eisenkraut, FScape, Kontur
 - GPL v3+: PDFlitz, ScalaCollider-Swing, Mellite, Muta($), SysSon
 - AGPL v3+: Poirot
 - LGPL v3: TreeTable

(x) I am planning to move these to LGPL v2.1+.
($) I am planning to move these to LGPL v3+.
