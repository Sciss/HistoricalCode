## AudioSnake

### statement

AudioSnake is an experiment with using variable length Markov chains on a stream of audio samples. It is by no means complete or in any production quality. Eventually it could go as a module into FScape.

It is (C)opyright 2013 by Hanns Holger Rutz. All rights reserved. AudioSnake is released under the [GNU General Public License](https://raw.github.com/Sciss/AudioSnake/master/LICENSE) and comes with absolutely no warranties. To contact the author, send an email to `contact at sciss.de`

### requirements / installation

AudioSnake compiles against Scala 2.10 using sbt 0.12. To run:

    $ sbt
    > run

This will print the usage. Examples

    > run -l 60.0 audio_work/WindspielRec1Cut-M.aif -o audio_work/windspiel_snake2.aif -b 24 -q 16384
    > run -l 60.0 -g 1.0 -q 8192 -b 5 -o audio_work/tagesschau_snake.aif audio_work/tagesschau891109.aif

FScape FFT example. Assumes that the input file was forward transformed.

    > run audio_work/WindspielRec1Cut-MFT.aif  -o audio_work/wind_fft_re.aif -b 12 -q 16384 -l 95.10893424036281 --float --rand 0
    > run audio_work/WindspielRec1Cut-MFTi.aif -o audio_work/wind_fft_im.aif -b 12 -q 16384 -l 95.10893424036281 --float --rand 0

(and then invert the FFT)

Wavetlet example. This is actually quite amazing. Assumes that the input has been forward transformed, e.g. with Daubechies 16:

    > run audio_work/WindspielRec1Cut-MWT.aif -o audio_work/wind_wt_snake.aif -b 12 -q 16384 -l 95.10893424036281 --float --rand 0

(and then invert the WT)

### notes

- uses an in-memory data structure. give sbt `-Xmx2048m` heap memory at least.
- smaller `-b` values give you more noise, higher values give you larger grain sizes
- works with monophonic inputs only at the moment
- markov problem is small frequencies. therefore quantisation is used. this is a trade-off with fidelity / quantisation noise.
- to address the frequency problem, the input should be around 1 or 2 minutes long. longer is fine but you may run into out-of-memory errors.
- back tracking only happens at the head of the snake; in a future version this should include the tail to improve context richness.a

### ideas

- when adding a fuzzy selector to the context tree, perhaps we could define an increase in fuzziness towards the snake's head
- allow complex numbers (see FFT example above)
- run on feature vectors (e.g. MFCC)
