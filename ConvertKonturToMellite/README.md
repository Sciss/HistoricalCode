# ConvertKonturToMellite

Small utility that reads a Kontur session file and creates an equivalent timeline within a Mellite workspace.

Currently only works with the latest snapshot versions of either application.

Kontur and Mellite are released under GNU GPL v2+ and v3+ respectively. This utility accordingly is released under GNU GPL v3+.
(C)opyright 2014 by Hanns Holger Rutz. All rights reserved.

## Limitations

Kontur didn't care for the audio-file sampling rate and was always playing the files back at the nominal session rate. Mellite in contrast applies sample-rate-conversion if necessary. Therefore, to get exactly the same sound impression, audio-files whose sampling rate differs from the nominal timeline rate should be modified such that the header contains the timeline's sample rate. This can be done with Eisenkraut, for example.
