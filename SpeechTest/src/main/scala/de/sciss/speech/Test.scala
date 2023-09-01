package de.sciss.speech

import javax.sound.sampled.{AudioFormat, AudioSystem, AudioFileFormat}

import com.sun.speech.freetts.VoiceManager
import com.sun.speech.freetts.audio.{JavaStreamingAudioPlayer, SingleFileAudioPlayer}
import de.sciss.file._

object Test extends App {
  val vm = VoiceManager.getInstance()
  vm.getVoices.foreach(v => println(v.getName))
  val voice = vm.getVoice("kevin16")

    val dir = userHome / "Documents" / "misc"
    require(dir.isDirectory && dir.canWrite)
    val f = dir / "test-speech"
    val player = new SingleFileAudioPlayer(f.path, AudioFileFormat.Type.AIFF)

//  val player = new JavaStreamingAudioPlayer

  player.setAudioFormat(new AudioFormat(44100.0f, 16, 2, true, false))
//  player.setAudioFormat(new AudioFormat(48000.0f, 16, 2, true, false))

  voice.setRate(150f)
  voice.setPitchShift(0.65f)
  voice.setVolume(0.95f)
  voice.setAudioPlayer(player)
  voice.allocate()

//  voice.speak("How do I create a new voice?")
  voice.speak("Hey Jonathan, I'm very impressed with your work.")

  player.close()
  voice.deallocate()
}
