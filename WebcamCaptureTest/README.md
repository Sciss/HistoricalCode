# WebcamCapture Test

A simple test project for https://github.com/sarxos/webcam-capture in Scala.

Uses the default driver, grabs one frame and saves it as png file (in `test.png` in the current directory).

To test: `sbt run`. Switches:

 - `--list` list available cameras
 - `--cam <name>` select camera by name (uses default camera if absent)

Public domain.
