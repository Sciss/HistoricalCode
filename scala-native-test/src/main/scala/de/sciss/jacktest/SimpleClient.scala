package de.sciss.jacktest

import scala.scalanative.native._
import scala.scalanative.native.stdio._
import scala.scalanative.native.stdlib._

object SimpleClient {
  import jack._
  import JackOptions._
  import JackStatus._
  import JackPortFlags._

  var input_port : Ptr[jack_port_t] = _
  var output_port: Ptr[jack_port_t] = _

  /** The process callback for this JACK application is called in a
    * special realtime thread once for each audio cycle.
    *
    * This client does nothing more than copy data from its input
    * port to its output port. It will exit when stopped by
    * the user (e.g. using Ctrl-C on a unix-ish operating system)
    */
  def process(nframes: jack_nframes_t, arg: Ptr[_]): CInt = {
    // jack_default_audio_sample_t *in, *out;
    val in  = jack_port_get_buffer(input_port , nframes).asInstanceOf[Ptr[jack_default_audio_sample_t]]
    val out = jack_port_get_buffer(output_port, nframes).asInstanceOf[Ptr[jack_default_audio_sample_t]]
    ??? // memcpy (out, in, sizeof[jack_default_audio_sample_t] * nframes)
    0
  }

  /**
    * JACK calls this shutdown_callback if the server ever shuts down or
    * decides to disconnect the client.
    */
  def jack_shutdown(arg: Ptr[_]): Unit = exit(1)

  def main(args: Array[String]): Unit = {
    val x = jack_client_name_size
    fprintf(stdout, c"Maximum client name size is %d\n", x)

    val client_name: CString = c"simple"
//    const char **ports;
    val server_name: CString = null
    val options: jack_options_t = JackNullOption
    val statusPtr: Ptr[jack_status_t] = malloc(sizeof[jack_status_t]).asInstanceOf[Ptr[jack_status_t]]

    /* open a client connection to the JACK server */

    val client = jack_client_open(client_name, options, statusPtr, server_name)
    val status = statusPtr(0)
//    if (client == null) {
//      fprintf (stderr, c"jack_client_open() failed, status = 0x%2.0x\n", status)
//      if ((status & JackServerFailed) != 0) {
//        fprintf (stderr, c"Unable to connect to JACK server\n")
//      }
//      exit (1)
//    }
    if ((status & JackServerStarted) != 0) {
      fprintf(stderr, c"JACK server started\n")
    }
    if ((status & JackNameNotUnique) != 0) {
      // client_name = jack_get_client_name(client)
      // fprintf (stderr, c"unique name `%s' assigned\n", client_name)
      fprintf(stderr, c"unique name assigned\n")
    }

    /* tell the JACK server to call `process()' whenever
       there is work to be done.
    */

    jack_set_process_callback(client, process _, null)

    /* tell the JACK server to call `jack_shutdown()' if
       it ever shuts down, either entirely, or if it
       just decides to stop calling us.
    */

    jack_on_shutdown(client, jack_shutdown _, null)

    /* display the current sample rate.
     */

    // " PRIu32 "
    printf(c"engine sample rate: %d\n", jack_get_sample_rate (client))

    /* create two ports */

    val input_port  = jack_port_register(client, c"input" , JACK_DEFAULT_AUDIO_TYPE, JackPortIsInput .toULong, 0.toULong)
    val output_port = jack_port_register(client, c"output", JACK_DEFAULT_AUDIO_TYPE, JackPortIsOutput.toULong, 0.toULong)

    if ((input_port == null) || (output_port == null)) {
      fprintf(stderr, c"no more JACK ports available\n")
      exit(1)
    }

    /* Tell the JACK server that we are ready to roll.  Our
     * process() callback will start running now. */

    if (jack_activate (client) != 0) {
      fprintf(stderr, c"cannot activate client")
      exit(1)
    }

    /* Connect the ports.  You can't do this before the client is
     * activated, because we can't make connections to clients
     * that aren't running.  Note the confusing (but necessary)
     * orientation of the driver backend ports: playback ports are
     * "input" to the backend, and capture ports are "output" from
     * it.
     */

    var ports = jack_get_ports(client, null, null, (JackPortIsPhysical | JackPortIsOutput).toULong)
    if (ports == null) {
      fprintf(stderr, c"no physical capture ports\n")
      exit(1)
    }

    if (jack_connect(client, ports(0), jack_port_name (input_port)) != 0)
      fprintf(stderr, c"cannot connect input ports\n")

    free(ports)

    ports = jack_get_ports(client, null, null, (JackPortIsPhysical | JackPortIsInput).toULong)
    if (ports == null) {
      fprintf(stderr, c"no physical playback ports\n")
      exit(1)
    }

    if (jack_connect(client, jack_port_name (output_port), ports(0)) != 0)
      fprintf(stderr, c"cannot connect output ports\n")

    free(ports)

    /* keep running until stopped by the user */

    // sleep (-1)
    while(true) ()

    /* this is never reached but if the program
       had some other way to exit besides being killed,
       they would be important to call.
    */

    jack_client_close(client)
    exit(0)
  }
}