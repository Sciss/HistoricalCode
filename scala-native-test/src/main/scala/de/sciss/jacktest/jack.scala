package de.sciss.jacktest

import scala.scalanative.native.{CInt, CString, Ptr, extern}

@extern
object jack {
  type jack_options_t = CInt
  type jack_status_t  = CInt

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

/*
jack_client_t *jack_client_open (const char *client_name,
				 jack_options_t options,
				 jack_status_t *status, ...) JACK_OPTIONAL_WEAK_EXPORT;
 */

  def jack_client_open (client_name: CString, options: jack_options_t, status: Ptr[jack_status_t],
                        server_name: CString /* , ... */): Ptr[jack_client_t] = extern

  /**
    * Disconnects an external client from a JACK server.
    *
    * @return 0 on success, otherwise a non-zero error code
    */
  def jack_client_close (client: Ptr[jack_client_t]): CInt = extern
}

trait jack_client_t // ???