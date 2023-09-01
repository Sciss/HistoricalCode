name                := "WebcamCapture-Test"
version             := "0.1.0-SNAPSHOT"
scalaVersion        := "2.12.4"
fork in run         := true

lazy val deps = new {
  val main = new {
    val webcam  = "0.3.10"
    val scopt   = "3.7.0"
    val bridj   = "0.7.0"
    val imageio = "3.1.2"
  }
}

libraryDependencies ++= Seq(
  "com.github.sarxos"         %   "webcam-capture"                  % deps.main.webcam,
  "com.github.sarxos"         %   "webcam-capture-driver-openimaj"  % deps.main.webcam
    exclude ("net.billylieurance.azuresearch" , "azure-bing-search-java")
    exclude ("uk.ac.ed.ph.snuggletex"         , "snuggletex-core")
    exclude ("uk.ac.ed.ph.snuggletex"         , "snuggletex-upconversion")
    exclude ("uk.ac.ed.ph.snuggletex"         , "snuggletex-jeuclid")
    exclude ("com.aetrion.flickr"             , "flickrapi")
    exclude ("vigna.dsi.unimi.it"             , "jal")
    exclude ("com.nativelibs4java"            , "bridj"),
  "com.nativelibs4java"       %   "bridj"                           % deps.main.bridj,
  "com.github.scopt"          %%  "scopt"                           % deps.main.scopt,
  "com.twelvemonkeys.imageio" %   "imageio-core"                    % deps.main.imageio,
  "com.twelvemonkeys.common"  %   "common-lang"                     % deps.main.imageio
)
