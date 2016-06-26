package de.sciss

import scala.scalanative.native._

// ---- types.h ----
package object jack {

  type jack_options_t = CInt
  type jack_status_t  = CInt
  type jack_nframes_t = UInt
  type jack_uuid_t    = ULong
  type jack_port_id_t = UInt
  type jack_time_t    = ULong

  type jack_default_audio_sample_t  = CFloat

  //  type jack_native_thread_t = pthread_t

  type JackShutdownCallback   = FunctionPtr1[Ptr[_], Unit]
  type JackProcessCallback    = FunctionPtr2[jack_nframes_t, Ptr[_], CInt]
  type JackSampleRateCallback = FunctionPtr2[jack_nframes_t, Ptr[_], CInt]

  //  type JackLatencyCallback    = FunctionPtr2[jack_latency_callback_mode_t, Ptr[_], Unit]

  val JACK_DEFAULT_AUDIO_TYPE = c"32 bit float mono audio"

  object JackOptions {
    /** Null value to use when no option bits are needed. */
    val JackNullOption: CInt = 0x00

    /** Do not automatically start the JACK server when it is not
      * already running.  This option is always selected if
      * \$JACK_NO_START_SERVER is defined in the calling process
      * environment.
      */
    val JackNoStartServer: CInt = 0x01

    /** Use the exact client name requested.  Otherwise, JACK
      * automatically generates a unique one, if needed.
      */
    val JackUseExactName: CInt = 0x02

    /** Open with optional <em>(char *) server_name</em> parameter. */
    val JackServerName: CInt = 0x04

    /** Load internal client from optional <em>(char *)
      * load_name</em>.  Otherwise use the @a client_name.
      */
    val JackLoadName: CInt = 0x08

    /** Pass optional <em>(char *) load_init</em> string to the
      * jack_initialize() entry point of an internal client.
      */
    val JackLoadInit: CInt = 0x10

    /** Pass a SessionID Token this allows the sessionmanager to identify the client again. */
    val JackSessionID: CInt = 0x20
  }

  object JackStatus {
    /**
      * Overall operation failed.
      */
    val JackFailure: CInt = 0x01

    /**
      * The operation contained an invalid or unsupported option.
      */
    val JackInvalidOption: CInt = 0x02

    /**
      * The desired client name was not unique.  With the @ref
      * JackUseExactName option this situation is fatal.  Otherwise,
      * the name was modified by appending a dash and a two-digit
      * number in the range "-01" to "-99".  The
      * jack_get_client_name() function will return the exact string
      * that was used.  If the specified @a client_name plus these
      * extra characters would be too long, the open fails instead.
      */
    val JackNameNotUnique: CInt = 0x04

    /**
      * The JACK server was started as a result of this operation.
      * Otherwise, it was running already.  In either case the caller
      * is now connected to jackd, so there is no race condition.
      * When the server shuts down, the client will find out.
      */
    val JackServerStarted: CInt = 0x08

    /**
      * Unable to connect to the JACK server.
      */
    val JackServerFailed: CInt = 0x10

    /**
      * Communication error with the JACK server.
      */
    val JackServerError: CInt = 0x20

    /**
      * Requested client does not exist.
      */
    val JackNoSuchClient: CInt = 0x40

    /**
      * Unable to load internal client
      */
    val JackLoadFailure: CInt = 0x80

    /**
      * Unable to initialize client
      */
    val JackInitFailure: CInt = 0x100

    /**
      * Unable to access shared memory
      */
    val JackShmFailure: CInt = 0x200

    /**
      * Client's protocol version does not match
      */
    val JackVersionError: CInt = 0x400

    /*
     * BackendError
     */
    val JackBackendError: CInt = 0x800

    /*
     * Client is being shutdown against its will
     */
    val JackClientZombie: CInt = 0x1000
  }

  object JackPortFlags {

    /**
      * if JackPortIsInput is set, then the port can receive
      * data.
      */
    val JackPortIsInput: CInt = 0x1

    /**
      * if JackPortIsOutput is set, then data can be read from
      * the port.
      */
    val JackPortIsOutput: CInt = 0x2

    /**
      * if JackPortIsPhysical is set, then the port corresponds
      * to some kind of physical I/O connector.
      */
    val JackPortIsPhysical: CInt = 0x4

    /**
      * if JackPortCanMonitor is set, then a call to
      * jack_port_request_monitor() makes sense.
      *
      * Precisely what this means is dependent on the client. A typical
      * result of it being called with TRUE as the second argument is
      * that data that would be available from an output port (with
      * JackPortIsPhysical set) is sent to a physical output connector
      * as well, so that it can be heard/seen/whatever.
      *
      * Clients that do not control physical interfaces
      * should never create ports with this bit set.
      */
    val JackPortCanMonitor: CInt = 0x8

    /**
      * JackPortIsTerminal means:
      *
      *	for an input port: the data received by the port
      *                    will not be passed on or made
      *		           available at any other port
      *
      * for an output port: the data available at the port
      *                    does not originate from any other port
      *
      * Audio synthesizers, I/O hardware interface clients, HDR
      * systems are examples of clients that would set this flag for
      * their ports.
      */
    val JackPortIsTerminal: CInt = 0x10
  }
}
