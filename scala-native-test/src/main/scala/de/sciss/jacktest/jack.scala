package de.sciss.jacktest

import scala.scalanative.native._

/** Scala bindings for the Jack audio connection kit C library.
  * See: https://github.com/jackaudio/headers/blob/master/jack.h
  */
@extern
object jack {
  // ---- types.h ----

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

  // ---- jack.h ----

  // ---- ClientFunctions Creating & manipulating clients ----

  /** Opens an external client session with a JACK server.  This interface
    * is more complex but more powerful than `jack_client_new()`.  With it,
    * clients may choose which of several servers to connect, and control
    * whether and how to start the server automatically, if it was not
    * already running.  There is also an option for JACK to generate a
    * unique client name, when necessary.
    *
    * @param client_name of at most `jack_client_name_size()` characters.
    * The name scope is local to each server.  Unless forbidden by the
    * `JackUseExactName` option, the server will modify this name to
    * create a unique variant, if needed.
    *
    * @param options formed by OR-ing together `JackOptions` bits.
    * Only the `JackOpenOptions` bits are allowed.
    *
    * @param status (if non-null) an address for JACK to return
    * information from the open operation.  This status word is formed by
    * OR-ing together the relevant `JackStatus` bits.
    *
    * @param  args
    * <b>Optional parameters:</b> depending on corresponding [@a options
    * bits] additional parameters may follow @a status (in this order).
    * [@ref JackServerName] <em>(char *) server_name</em> selects
    * from among several possible concurrent server instances.  Server
    * names are unique to each user.  If unspecified, use "default"
    * unless \$JACK_DEFAULT_SERVER is defined in the process environment.
    *
    * @return Opaque client handle if successful.  If this is `null`, the
    * open operation failed, `status` includes `JackFailure` and the
    * caller is not a JACK client.
    */
  def jack_client_open(client_name: CString, options: jack_options_t, status: Ptr[jack_status_t],
                       server_name: CString, args: Vararg*): Ptr[jack_client_t] = extern

  /** Disconnects an external client from a JACK server.
    *
    * @return 0 on success, otherwise a non-zero error code
    */
  def jack_client_close(client: Ptr[jack_client_t]): CInt = extern

  /** Returns the maximum number of characters in a JACK client name
    * including the final NULL character. This value is a constant.
    */
  def jack_client_name_size: CInt = extern

  /** Returns pointer to actual client name. This is useful when
    * `JackUseExactName` is not specified on open and
    * `JackNameNotUnique` status was returned. In that case, the actual
    * name will differ from the `client_name` requested.
    */
  def jack_get_client_name(client: Ptr[jack_client_t]): CString = extern

  /** Returns pointer to a string representation of the UUID for
    * a client named `name`. If no such client exists, return NULL
    *
    * @param client the client making the request
    * @param name   the name of the client whose UUID is desired
    * @return NULL if no such client with the given name exists
    */
  def jack_get_uuid_for_client_name(client: Ptr[jack_client_t], name: CString): CString = extern

  /** Returns a pointer to the name of the client with the UUID
    * specified by uuid.
    *
    * @param client making the request
    * @param uuid   the uuid of the client whose name is desired
    * @return NULL if no such client with the given UUID exists
    */
  def jack_get_client_name_by_uuid(client: Ptr[jack_client_t], uuid: CString): CString = extern

  /** Tells the Jack server that the program is ready to start processing
    * audio.
    *
    * @return 0 on success, otherwise a non-zero error code
    */
  def jack_activate (client: Ptr[jack_client_t]): CInt = extern

  /** Tells the Jack server to remove this @a client from the process
    * graph.  Also, disconnect all ports belonging to it, since inactive
    * clients have no port connections.
    *
    * @return 0 on success, otherwise a non-zero error code
    */
  def jack_deactivate (client: Ptr[jack_client_t]): Int = extern

  //  /** Returns the pthread ID of the thread running the JACK client side
  //    * code.
  //    */
  //  def jack_client_thread_id (client: Ptr[jack_client_t]): jack_native_thread_t = extern

  // ---- ----

  /** Checks if the JACK subsystem is running with -R (--realtime).
    *
    * @param client pointer to JACK client structure.
    * @return 1 if JACK is running realtime, 0 otherwise
    */
  def jack_is_realtime (client: Ptr[jack_client_t]): CInt = extern

  // ---- NonCallbackAPI The non-callback API ----

  /** Waits until this JACK client should process data.
    *
    * @param client - pointer to a JACK client structure
    * @return the number of frames of data to process
    */
  def jack_cycle_wait (client: Ptr[jack_client_t]): jack_nframes_t = extern

  /** Signals next clients in the graph.
    *
    * @param client - pointer to a JACK client structure
    * @param status - if non-zero, calling thread should exit
    */
  def jack_cycle_signal (client: Ptr[jack_client_t], status: CInt): Unit = extern

  //  /** Tells the Jack server to call @a thread_callback in the RT thread.
  //    * Typical use are in conjunction with @a jack_cycle_wait and @ jack_cycle_signal functions.
  //    * The code in the supplied function must be suitable for real-time
  //    * execution. That means that it cannot call functions that might
  //    * block for a long time. This includes all I/O functions (disk, TTY, network),
  //    * malloc, free, printf, pthread_mutex_lock, sleep, wait, poll, select, pthread_join,
  //    * pthread_cond_wait, etc, etc.
  //    *
  //    * @return 0 on success, otherwise a non-zero error code.
  //    */
  //  def jack_set_process_thread(client: Ptr[jack_client_t], fun: JackThreadCallback, arg: Ptr[_]): CInt = extern

  // ---- ClientCallbacks Setting Client Callbacks ----

  //  /** Tells JACK to call @a thread_init_callback once just after
  //    * the creation of the thread in which all other callbacks
  //    * will be handled.
  //    *
  //    * The code in the supplied function does not need to be
  //    * suitable for real-time execution.
  //    *
  //    * @return 0 on success, otherwise a non-zero error code, causing JACK
  //    * to remove that client from the process() graph.
  //    */
  //  def jack_set_thread_init_callback (client: Ptr[jack_client_t], thread_init_callback: JackThreadInitCallback,
  //                                     arg: Ptr[_]): CInt = extern

  /** Registers a function (and argument) to be called if and when the
    * JACK server shuts down the client thread.  The function must
    * be written as if it were an asynchonrous POSIX signal
    * handler --- use only async-safe functions, and remember that it
    * is executed from another thread.  A typical function might
    * set a flag or write to a pipe so that the rest of the
    * application knows that the JACK client thread has shut
    * down.
    *
    * NOTE: clients do not need to call this.  It exists only
    * to help more complex clients understand what is going
    * on.  It should be called before jack_client_activate().
    *
    * NOTE: if a client calls this AND jack_on_info_shutdown(), then
    * the event of a client thread shutdown, the callback 
    * passed to this function will not be called, and the one passed to
    * jack_on_info_shutdown() will.
    *
    * @param client pointer to JACK client structure.
    * @param function The jack_shutdown function pointer.
    * @param arg The arguments for the jack_shutdown function.
    */
  def jack_on_shutdown(client: Ptr[jack_client_t], function: JackShutdownCallback, arg: Ptr[_]): Unit = extern

  //  /** Registers a function (and argument) to be called if and when the
  //    * JACK server shuts down the client thread.  The function must
  //    * be written as if it were an asynchronous POSIX signal
  //    * handler --- use only async-safe functions, and remember that it
  //    * is executed from another thread.  A typical function might
  //    * set a flag or write to a pipe so that the rest of the
  //    * application knows that the JACK client thread has shut
  //    * down.
  //    *
  //    * NOTE: clients do not need to call this.  It exists only
  //    * to help more complex clients understand what is going
  //    * on.  It should be called before jack_client_activate().
  //    *
  //    * NOTE: if a client calls this AND jack_on_shutdown(), then in the
  //    * event of a client thread shutdown, the callback   passed to
  //    * this function will be called, and the one passed to
  //    * jack_on_shutdown() will not.
  //    *
  //    * @param client pointer to JACK client structure.
  //    * @param function The jack_shutdown function pointer.
  //    * @param arg The arguments for the jack_shutdown function.
  //    */
  //  def jack_on_info_shutdown(client: Ptr[jack_client_t], function: JackInfoShutdownCallback,
  //                            arg: Ptr[_]): Unit = extern // JACK_WEAK_EXPORT;

  /** Tells the Jack server to call @a process_callback whenever there is
    * work be done, passing @a arg as the second argument.
    *
    * The code in the supplied function must be suitable for real-time
    * execution. That means that it cannot call functions that might
    * block for a long time. This includes all I/O functions (disk, TTY, network),
    * malloc, free, printf, pthread_mutex_lock, sleep, wait, poll, select, pthread_join,
    * pthread_cond_wait, etc, etc. 
    *
    * @return 0 on success, otherwise a non-zero error code, causing JACK
    * to remove that client from the process() graph.
    */
  def jack_set_process_callback(client: Ptr[jack_client_t], process_callback: JackProcessCallback,
                                arg: Ptr[_]): CInt = extern

  //  /** Tells the Jack server to call @a freewheel_callback
  //    * whenever we enter or leave "freewheel" mode, passing @a
  //    * arg as the second argument. The first argument to the
  //    * callback will be non-zero if JACK is entering freewheel
  //    * mode, and zero otherwise.
  //    *
  //    * @return 0 on success, otherwise a non-zero error code.
  //    */
  //  def jack_set_freewheel_callback (client: Ptr[jack_client_t], freewheel_callback: JackFreewheelCallback,
  //                                   arg: Ptr[_]): CInt = extern

  //  /** Tells JACK to call @a bufsize_callback whenever the size of the the
  //    * buffer that will be passed to the @a process_callback is about to
  //    * change.  Clients that depend on knowing the buffer size must supply
  //    * a @a bufsize_callback before activating themselves.
  //    *
  //    * @param client pointer to JACK client structure.
  //    * @param bufsize_callback function to call when the buffer size changes.
  //    * @param arg argument for @a bufsize_callback.
  //    *
  //    * @return 0 on success, otherwise a non-zero error code
  //    */
  //  def jack_set_buffer_size_callback(client: Ptr[jack_client_t], bufsize_callback: JackBufferSizeCallback,
  //                                    arg: Ptr[_]): CInt = extern

  /** Tells the Jack server to call @a srate_callback whenever the system
    * sample rate changes.
    *
    * @return 0 on success, otherwise a non-zero error code
    */
  def jack_set_sample_rate_callback(client: Ptr[jack_client_t], srate_callback: JackSampleRateCallback,
                                    arg: Ptr[_]): CInt = extern

  //  /** Tells the JACK server to call @a registration_callback whenever a
  //    * port is registered or unregistered, passing @a arg as a parameter.
  //    *
  //    * @return 0 on success, otherwise a nonzero error code
  //    */
  //  def jack_set_client_registration_callback(client: Ptr[jack_client_t],
  //                                            registration_callback: JackClientRegistrationCallback,
  //                                            arg: Ptr[_]): CInt = extern

  //  /** Tells the JACK server to call @a registration_callback whenever a
  //    * port is registered or unregistered, passing @a arg as a parameter.
  //    *
  //    * @return 0 on success, otherwise a non-zero error code
  //    */
  //  def jack_set_port_registration_callback(client: Ptr[jack_client_t],
  //                                          registration_callback: JackPortRegistrationCallback,
  //                                          arg: Ptr[_]): CInt = extern

  //  /** Tells the JACK server to call @a registration_callback whenever a
  //    * port is registered or unregistered, passing @a arg as a parameter.
  //    *
  //    * @return 0 on success, otherwise a non-zero error code
  //    */
  //  def jack_set_port_rename_callback(client: Ptr[jack_client_t],
  //                                    rename_callback: JackPortRenameCallback, arg: Ptr[_]): CInt = extern

  //  /** Tells the JACK server to call @a connect_callback whenever a
  //    * port is connected or disconnected, passing @a arg as a parameter.
  //    *
  //    * @return 0 on success, otherwise a non-zero error code
  //    */
  //  def jack_set_port_connect_callback(client: Ptr[jack_client_t], connect_callback: JackPortConnectCallback,
  //                                     arg: Ptr[_]): CInt = extern

  //  /** Tells the JACK server to call @a graph_callback whenever the
  //    * processing graph is reordered, passing @a arg as a parameter.
  //    *
  //    * @return 0 on success, otherwise a non-zero error code
  //    */
  //  def jack_set_graph_order_callback(client: Ptr[jack_client_t], graph_callback: JackGraphOrderCallback,
  //                                    arg: Ptr[_]): CInt = extern

  //  /** Tells the JACK server to call @a xrun_callback whenever there is a
  //    * xrun, passing @a arg as a parameter.
  //    *
  //    * @return 0 on success, otherwise a non-zero error code
  //    */
  //  def jack_set_xrun_callback(client: Ptr[jack_client_t], xrun_callback: JackXRunCallback,
  //                             arg: Ptr[_]): CInt = extern

  //  /** Tells the Jack server to call @a latency_callback whenever it
  //    * is necessary to recompute the latencies for some or all
  //    * Jack ports.
  //    *
  //    * @a latency_callback will be called twice each time it is
  //    * needed, once being passed JackCaptureLatency and once
  //    * JackPlaybackLatency. See @ref LatencyFunctions for
  //    * the definition of each type of latency and related functions.
  //    *
  //    * <b>IMPORTANT: Most JACK clients do NOT need to register a latency
  //    * callback.</b>
  //    *
  //    * Clients that meet any of the following conditions do NOT
  //    * need to register a latency callback:
  //    *
  //    *    - have only input ports
  //    *    - have only output ports
  //    *    - their output is totally unrelated to their input
  //    *    - their output is not delayed relative to their input
  //    *        (i.e. data that arrives in a given process()
  //    *         callback is processed and output again in the
  //    *         same callback)
  //    *
  //    * Clients NOT registering a latency callback MUST also
  //    * satisfy this condition:
  //    *
  //    *    - have no multiple distinct internal signal pathways
  //    *
  //    * This means that if your client has more than 1 input and
  //    * output port, and considers them always "correlated"
  //    * (e.g. as a stereo pair), then there is only 1 (e.g. stereo)
  //    * signal pathway through the client. This would be true,
  //    * for example, of a stereo FX rack client that has a
  //    * left/right input pair and a left/right output pair.
  //    *
  //    * However, this is somewhat a matter of perspective. The
  //    * same FX rack client could be connected so that its
  //    * two input ports were connected to entirely separate
  //    * sources. Under these conditions, the fact that the client
  //    * does not register a latency callback MAY result
  //    * in port latency values being incorrect.
  //    *
  //    * Clients that do not meet any of those conditions SHOULD
  //    * register a latency callback.
  //    *
  //    * See the documentation for  @ref jack_port_set_latency_range()
  //    * on how the callback should operate. Remember that the @a mode
  //    * argument given to the latency callback will need to be
  //    * passed into @ref jack_port_set_latency_range()
  //    *
  //    * @return 0 on success, otherwise a non-zero error code
  //    */
  //  def jack_set_latency_callback(client: Ptr[jack_client_t], latency_callback: JackLatencyCallback,
  //                                arg: Ptr[_]): CInt //  JACK_WEAK_EXPORT
  
  // ---- ServerControl Controlling & querying JACK server operation ----

  /** Starts/Stops JACK's "freewheel" mode.
    *
    * When in "freewheel" mode, JACK no longer waits for
    * any external event to begin the start of the next process
    * cycle. 
    *
    * As a result, freewheel mode causes "faster than realtime"
    * execution of a JACK graph. If possessed, real-time
    * scheduling is dropped when entering freewheel mode, and
    * if appropriate it is reacquired when stopping.
    *
    * IMPORTANT: on systems using capabilities to provide real-time
    * scheduling (i.e. Linux kernel 2.4), if onoff is zero, this function
    * must be called from the thread that originally called jack_activate(). 
    * This restriction does not apply to other systems (e.g. Linux kernel 2.6 
    * or OS X).
    *
    * @param client pointer to JACK client structure
    * @param onoff  if non-zero, freewheel mode starts. Otherwise
    *                  freewheel mode ends.
    *
    * @return 0 on success, otherwise a non-zero error code.
    */
  def jack_set_freewheel(client: Ptr[jack_client_t], onoff: CInt): CInt = extern

  /**
    * Change the buffer size passed to the @a process_callback.
    *
    * This operation stops the JACK engine process cycle, then calls all
    * registered @a bufsize_callback functions before restarting the
    * process cycle.  This will cause a gap in the audio flow, so it
    * should only be done at appropriate stopping points.
    *
    * @see jack_set_buffer_size_callback()
    *
    * @param client pointer to JACK client structure.
    * @param nframes new buffer size.  Must be a power of two.
    *
    * @return 0 on success, otherwise a non-zero error code
    */
  def jack_set_buffer_size (client: Ptr[jack_client_t], nframes: jack_nframes_t): CInt = extern

  /** Returns the sample rate of the jack system, as set by the user when
    * jackd was started.
    */
  def jack_get_sample_rate (client: Ptr[jack_client_t]): jack_nframes_t = extern

  /** Returns the current maximum size that will ever be passed to the @a
    * process_callback.  It should only be used *before* the client has
    * been activated.  This size may change, clients that depend on it
    * must register a @a bufsize_callback so they will be notified if it
    * does.
    *
    * @see jack_set_buffer_size_callback()
    */
  def jack_get_buffer_size (client: Ptr[jack_client_t]): jack_nframes_t = extern

  /** Returns the current CPU load estimated by JACK.  This is a running
    * average of the time it takes to execute a full process cycle for
    * all clients as a percentage of the real time available per cycle
    * determined by the buffer size and sample rate.
    */
  def jack_cpu_load (client: Ptr[jack_client_t]): CFloat = extern

  // ---- PortFunctions Creating & manipulating ports ----

  /** Creates a new port for the client. This is an object used for moving
    * data of any type in or out of the client.  Ports may be connected
    * in various ways.
    *
    * Each port has a short name.  The port's full name contains the name
    * of the client concatenated with a colon (:) followed by its short
    * name.  The jack_port_name_size() is the maximum length of this full
    * name.  Exceeding that will cause the port registration to fail and
    * return NULL.
    *
    * The @a port_name must be unique among all ports owned by this client. 
    * If the name is not unique, the registration will fail. 
    *
    * All ports have a type, which may be any non-NULL and non-zero
    * length string, passed as an argument.  Some port types are built
    * into the JACK API, like JACK_DEFAULT_AUDIO_TYPE or JACK_DEFAULT_MIDI_TYPE
    *
    * @param client pointer to JACK client structure.
    * @param port_name non-empty short name for the new port (not
    * including the leading @a "client_name:"). Must be unique.
    * @param port_type port type name.  If longer than
    * jack_port_type_size(), only that many characters are significant.
    * @param flags `JackPortFlags` bit mask.
    * @param buffer_size must be non-zero if this is not a built-in @a
    * port_type.  Otherwise, it is ignored.
    *
    * @return jack_port_t pointer on success, otherwise NULL.
    */
  def jack_port_register(client: Ptr[jack_client_t], port_name: CString, port_type: CString, flags: ULong,
                         buffer_size: ULong): Ptr[jack_port_t] = extern

  /** Removes the port from the client, disconnecting any existing
    * connections.
    *
    * @return 0 on success, otherwise a non-zero error code
    */
  def jack_port_unregister(client: Ptr[jack_client_t], port: Ptr[jack_port_t]): CInt = extern

  /** Returns a pointer to the memory area associated with the
    * specified port. For an output port, it will be a memory area
    * that can be written to; for an input port, it will be an area
    * containing the data from the port's connection(s), or
    * zero-filled. if there are multiple inbound connections, the data
    * will be mixed appropriately.  
    *
    * Do not cache the returned address across process() callbacks.
    * Port buffers have to be retrieved in each callback for proper functionning.
    */
  def jack_port_get_buffer(port: Ptr[jack_port_t], frames: jack_nframes_t): Ptr[_] = extern

  /** Returns the full name of the jack_port_t (including the @a
    * "client_name:" prefix).
    *
    * @see jack_port_name_size().
    */
  def jack_port_name (port: Ptr[jack_port_t]): CString = extern

  /** Returns the UUID of the jack_port_t
    *
    * @see jack_uuid_to_string() to convert into a string representation
    */
  def jack_port_uuid (port: Ptr[jack_port_t]): jack_uuid_t = extern

  /** Returns the short name of the jack_port_t (not including the @a
    * "client_name:" prefix).
    *
    * @see jack_port_name_size().
    */
  def jack_port_short_name (port: Ptr[jack_port_t]): CString = extern

  /**
    * @return the @ref JackPortFlags of the jack_port_t.
    */
  def jack_port_flags (port: Ptr[jack_port_t]): CInt = extern

  /** Returns the @a port type, at most jack_port_type_size() characters
    * including a final NULL.
    */
  def jack_port_type (port: Ptr[jack_port_t]): CString = extern

  /** Returns TRUE if the jack_port_t belongs to the jack_client_t.
    */
  def jack_port_is_mine (client: Ptr[jack_client_t], port: Ptr[jack_port_t]): CInt = extern

  /** Returns number of connections to or from @a port.
    *
    * Note: The calling client must own @a port.
    */
  def jack_port_connected (port: Ptr[jack_port_t]): CInt = extern

  /** Returns TRUE if the locally-owned @a port is @b directly connected
    * to the @a port_name.
    *
    * @see jack_port_name_size()
    */
  def jack_port_connected_to (port: Ptr[jack_port_t], port_name: CString): CInt = extern

  /**
    * @return a null-terminated array of full port names to which the @a
    * port is connected.  If none, returns NULL.
    *
    * The caller is responsible for calling jack_free(3) on any non-NULL
    * returned value.
    *
    * @param port locally owned jack_port_t pointer.
    *
    * @see jack_port_name_size(), jack_port_get_all_connections()
    */
  def jack_port_get_connections (port: Ptr[jack_port_t]): Ptr[CString] = extern

  /** Returns a null-terminated array of full port names to which the @a
    * port is connected.  If none, returns NULL.
    *
    * The caller is responsible for calling jack_free(3) on any non-NULL
    * returned value.
    *
    * This differs from jack_port_get_connections() in two important
    * respects:
    *
    *     1) You may not call this function from code that is
    *          executed in response to a JACK event. For example,
    *          you cannot use it in a GraphReordered handler.
    *
    *     2) You need not be the owner of the port to get information
    *          about its connections. 
    *
    * @see jack_port_name_size()
    */
  def jack_port_get_all_connections (client: Ptr[jack_client_t], port: Ptr[jack_port_t]): Ptr[CString] = extern

  /** Modifies a port's short name.  May NOT be called from a callback handling a server event.  
    * If the resulting full name (including the @a "client_name:" prefix) is
    * longer than jack_port_name_size(), it will be truncated.
    *
    * @return 0 on success, otherwise a non-zero error code.
    *
    * This differs from jack_port_set_name() by triggering PortRename notifications to 
    * clients that have registered a port rename handler.
    */
  def jack_port_rename (client: Ptr[jack_client_t], port: Ptr[jack_port_t], port_name: CString): CInt = extern

  /** Sets @a alias as an alias for @a port.  May be called at any time.
    * If the alias is longer than jack_port_name_size(), it will be truncated.
    *
    * After a successful call, and until JACK exits or
    * jack_port_unset_alias() is called, may be
    * used as a alternate name for the port.
    *
    * Ports can have up to two aliases - if both are already 
    * set, this function will return an error.
    *
    * @return 0 on success, otherwise a non-zero error code.
    */
  def jack_port_set_alias (port: Ptr[jack_port_t], alias: CString): CInt = extern

  /** Removes @a alias as an alias for @a port.  May be called at any time.
    *
    * After a successful call, @a alias can no longer be 
    * used as a alternate name for the port.
    *
    * @return 0 on success, otherwise a non-zero error code.
    */
  def jack_port_unset_alias (port: Ptr[jack_port_t], alias: CString): CInt = extern

//  /* Gets any aliases known for @port.
//   *
//   * @return the number of aliases discovered for the port
//   */
//  def jack_port_get_aliases (port: Ptr[jack_port_t], char* const aliases[2]): CInt = extern

  /** If @ref JackPortCanMonitor is set for this @a port, turns input
    * monitoring on or off. Otherwise, does nothing.
    */
  def jack_port_request_monitor (port: Ptr[jack_port_t], onoff: CInt): CInt = extern

  /** If @ref JackPortCanMonitor is set for this @a port_name, turns input
    * monitoring on or off. Otherwise, does nothing.
    *
    * @return 0 on success, otherwise a non-zero error code.
    *
    * @see jack_port_name_size()
    */
  def jack_port_request_monitor_by_name (client: Ptr[jack_client_t], port_name: CString, onoff: CInt): CInt = extern

  /** If @ref JackPortCanMonitor is set for a port, this function turns
    * on input monitoring if it was off, and turns it off if only one
    * request has been made to turn it on.  Otherwise it does nothing.
    *
    * @return 0 on success, otherwise a non-zero error code
    */
  def jack_port_ensure_monitor (port: Ptr[jack_port_t], onoff: CInt): CInt = extern

  /** Returns TRUE if input monitoring has been requested for @a port.
    */
  def jack_port_monitoring_input (port: Ptr[jack_port_t]): CInt = extern

  /** Establishes a connection between two ports.
    *
    * When a connection exists, data written to the source port will
    * be available to be read at the destination port.
    *
    * <ul>
    * <li> The port types must be identical.
    * <li> The @ref JackPortFlags of the @a source_port must include @ref
    * JackPortIsOutput.
    * <li> The @ref JackPortFlags of the @a destination_port must include
    * <li> JackPortIsInput.
    * </ul>
    *
    * @return 0 on success, EEXIST if the connection is already made,
    * otherwise a non-zero error code
    */
  def jack_connect (client: Ptr[jack_client_t], source_port: CString, destination_port: CString): CInt = extern

  /** Removes a connection between two ports.
    *
    * <ul>
    * <li> The port types must be identical.
    * <li> The @ref JackPortFlags of the @a source_port must include @ref
    * JackPortIsOutput.
    * <li> The @ref JackPortFlags of the @a destination_port must include
    * `JackPortIsInput`.
    * </ul>
    *
    * @return 0 on success, otherwise a non-zero error code
    */
  def jack_disconnect (client: Ptr[jack_client_t], source_port: CString, destination_port: CString): CInt = extern

  /** Performs the same function as jack_disconnect() using port handles
    * rather than names.  This aUnits the name lookup inherent in the
    * name-based version.
    *
    * Clients connecting their own ports are likely to use this function,
    * while generic connection clients (e.g. patchbays) would use
    * jack_disconnect().
    */
  def jack_port_disconnect (client: Ptr[jack_client_t], port: Ptr[jack_port_t]): CInt = extern

  /** Returns the maximum number of characters in a full JACK port name
    * including the final NULL character.  This value is a constant.
    *
    * A port's full name contains the owning client name concatenated
    * with a colon (:) followed by its short name and a NULL
    * character.
    */
  def jack_port_name_size: CInt = extern

  /** Returns the maximum number of characters in a JACK port type name
    * including the final NULL character.  This value is a constant.
    */
  def jack_port_type_size: CInt = extern

  /** Returns the buffersize of a port of type @arg port_type.
    *
    * this function may only be called in a buffer_size callback.
    */
  def jack_port_type_get_buffer_size (client: Ptr[jack_client_t], port_type: CString): CSize = extern // JACK_WEAK_EXPORT;
  
  // ---- LatencyFunctions Managing and determining latency ----
  
  // The purpose of JACK's latency API is to allow clients to
  // easily answer two questions:
  //
  // - How long has it been since the data read from a port arrived
  //   at the edge of the JACK graph (either via a physical port
  //   or being synthesized from scratch)?
  //
  // - How long will it be before the data written to a port arrives
  //   at the edge of a JACK graph?
  //
  // To help answering these two questions, all JACK ports have two
  // latency values associated with them, both measured in frames:
  //
  // <b>capture latency</b>: how long since the data read from
  //                  the buffer of a port arrived at at
  //                  a port marked with JackPortIsTerminal.
  //                  The data will have come from the "outside
  //                  world" if the terminal port is also
  //                  marked with JackPortIsPhysical, or
  //                  will have been synthesized by the client
  //                  that owns the terminal port.
  //
  // <b>playback latency</b>: how long until the data
  //                   written to the buffer of port will reach a port
  //                   marked with JackPortIsTerminal.
  //
  // Both latencies might potentially have more than one value
  // because there may be multiple pathways to/from a given port
  // and a terminal port. Latency is therefore generally
  // expressed a min/max pair.
  //
  // In most common setups, the minimum and maximum latency
  // are the same, but this design accomodates more complex
  // routing, and allows applications (and thus users) to
  // detect cases where routing is creating an anomalous
  // situation that may either need fixing or more
  // sophisticated handling by clients that care about
  // latency.
  //
  // See also @ref jack_set_latency_callback for details on how
  // clients that add latency to the signal path should interact
  // with JACK to ensure that the correct latency figures are
  // used.

  //  /** Returns the latency range defined by `mode` for `port`, in frames.
  //    *
  //    * See @ref LatencyFunctions for the definition of each latency value.
  //    *
  //    * This is normally used in the LatencyCallback.
  //    * and therefor safe to execute from callbacks.
  //    */
  //  def jack_port_get_latency_range(port: Ptr[jack_port_t], mode: jack_latency_callback_mode_t,
  //                                  range: Ptr[jack_latency_range_t]): Unit = extern // JACK_WEAK_EXPORT;

  //  /** Sets the minimum and maximum latencies defined by `mode` for `port`, in frames.
  //    *
  //    * See @ref LatencyFunctions for the definition of each latency value.
  //    *
  //    * This function should ONLY be used inside a latency
  //    * callback. The client should determine the current
  //    * value of the latency using @ref jack_port_get_latency_range()
  //    * (called using the same mode as @a mode)
  //    * and then add some number of frames to that reflects
  //    * latency added by the client.
  //    *
  //    * How much latency a client adds will vary
  //    * dramatically. For most clients, the answer is zero
  //    * and there is no reason for them to register a latency
  //    * callback and thus they should never call this
  //    * function.
  //    *
  //    * More complex clients that take an input signal,
  //    * transform it in some way and output the result but
  //    * not during the same process() callback will
  //    * generally know a single constant value to add
  //    * to the value returned by @ref jack_port_get_latency_range().
  //    *
  //    * Such clients would register a latency callback (see
  //    * @ref jack_set_latency_callback) and must know what input
  //    * ports feed which output ports as part of their
  //    * internal state. Their latency callback will update
  //    * the ports' latency values appropriately.
  //    *
  //    * A pseudo-code example will help. The @a mode argument to the latency
  //    * callback will determine whether playback or capture
  //    * latency is being set. The callback will use
  //    * @ref jack_port_set_latency_range() as follows:
  //    *
  //    * \code
  //    * jack_latency_range_t range;
  //    * if (mode == JackPlaybackLatency) {
  //    *  foreach input_port in (all self-registered port) {
  //    *   jack_port_get_latency_range (port_feeding_input_port, JackPlaybackLatency, &range);
  //    *   range.min += min_delay_added_as_signal_flows_from port_feeding to input_port;
  //    *   range.max += max_delay_added_as_signal_flows_from port_feeding to input_port;
  //    *   jack_port_set_latency_range (input_port, JackPlaybackLatency, &range);
  //    *  }
  //    * } else if (mode == JackCaptureLatency) {
  //    *  foreach output_port in (all self-registered port) {
  //    *   jack_port_get_latency_range (port_fed_by_output_port, JackCaptureLatency, &range);
  //    *   range.min += min_delay_added_as_signal_flows_from_output_port_to_fed_by_port;
  //    *   range.max += max_delay_added_as_signal_flows_from_output_port_to_fed_by_port;
  //    *   jack_port_set_latency_range (output_port, JackCaptureLatency, &range);
  //    *  }
  //    * }
  //    * \endcode
  //    *
  //    * In this relatively simple pseudo-code example, it is assumed that
  //    * each input port or output is connected to only 1 output or input
  //    * port respectively.
  //    *
  //    * If a port is connected to more than 1 other port, then the
  //    * range.min and range.max values passed to @ref
  //    * jack_port_set_latency_range() should reflect the minimum and
  //    * maximum values across all connected ports.
  //    *
  //    * See the description of @ref jack_set_latency_callback for more
  //    * information.
  //    */
  //  def jack_port_set_latency_range (port: Ptr[jack_port_t], mode: jack_latency_callback_mode_t,
  //                                   range: Ptr[jack_latency_range_t]): Unit = extern // JACK_WEAK_EXPORT;

  /** Requests a complete re-computation of all port latencies. This
    * can be called by a client that has just changed the internal
    * latency of its port using  jack_port_set_latency
    * and wants to ensure that all signal pathways in the graph
    * are updated with respect to the values that will be returned
    * by  jack_port_get_total_latency. It allows a client 
    * to change multiple port latencies without triggering a 
    * recompute for each change.
    *
    * @return zero for successful execution of the request. non-zero
    *         otherwise.
    */
  def jack_recompute_total_latencies (client: Ptr[jack_client_t]): CInt = extern

  // ---- PortSearching Looking up ports ----

  /**
    * @param port_name_pattern A regular expression used to select 
    * ports by name.  If NULL or of zero length, no selection based 
    * on name will be carried out.
    * @param type_name_pattern A regular expression used to select 
    * ports by type.  If NULL or of zero length, no selection based 
    * on type will be carried out.
    * @param flags A value used to select ports by their flags.  
    * If zero, no selection based on flags will be carried out.
    *
    * @return a NULL-terminated array of ports that match the specified
    * arguments.  The caller is responsible for calling jack_free(3) any
    * non-NULL returned value.
    *
    * @see jack_port_name_size(), jack_port_type_size()
    */
  def jack_get_ports (client: Ptr[jack_client_t], port_name_pattern: CString,
                      type_name_pattern: CString, flags: ULong): Ptr[CString] = extern

  /** Returns address of the jack_port_t named @a port_name.
    *
    * @see jack_port_name_size()
    */
  def jack_port_by_name (client: Ptr[jack_client_t], port_name: CString): Ptr[jack_port_t] = extern

  /** Returns address of the jack_port_t of a @a port_id. */
  def jack_port_by_id (client: Ptr[jack_client_t], port_id: jack_port_id_t): Ptr[jack_port_t] = extern

  // ---- TimeFunctions Handling time ----
  //
  //  JACK time is in units of 'frames', according to the current sample rate.
  //  The absolute value of frame times is meaningless, frame times have meaning
  //  only relative to each other.

  /** Returns the estimated time in frames that has passed since the JACK
    * server began the current process cycle.
    */
  def jack_frames_since_cycle_start (client: Ptr[jack_client_t]): jack_nframes_t = extern

  /** Returns the estimated current time in frames.
    * This function is intended for use in other threads (not the process
    * callback).  The return value can be compared with the value of
    * jack_last_frame_time to relate time in other threads to JACK time.
    */
  def jack_frame_time (client: Ptr[jack_client_t]): jack_nframes_t = extern

  /** Returns the precise time at the start of the current process cycle.
    * This function may only be used from the process callback, and can
    * be used to interpret timestamps generated by jack_frame_time() in
    * other threads with respect to the current process cycle.
    *
    * This is the only jack time function that returns exact time:
    * when used during the process callback it always returns the same
    * value (until the next process callback, where it will return
    * that value + nframes, etc).  The return value is guaranteed to be
    * monotonic and linear in this fashion unless an xrun occurs.
    * If an xrun occurs, clients must check this value again, as time
    * may have advanced in a non-linear way (e.g. cycles may have been skipped).
    */
  def jack_last_frame_time (client: Ptr[jack_client_t]): jack_nframes_t = extern

  /** This function may only be used from the process callback.
    * It provides the internal cycle timing information as used by
    * most of the other time related functions. This allows the
    * caller to map between frame counts and microseconds with full
    * precision (i.e. without rounding frame times to integers),
    * and also provides e.g. the microseconds time of the start of
    * the current cycle directly (it has to be computed otherwise).
    *
    * If the return value is zero, the following information is
    * provided in the variables pointed to by the arguments:
    *
    * current_frames: the frame time counter at the start of the
    *                 current cycle, same as jack_last_frame_time().
    * current_usecs:  the microseconds time at the start of the
    *                 current cycle.
    * next_usecs:     the microseconds time of the start of the next
    *                 next cycle as computed by the DLL.
    * period_usecs:   the current best estimate of the period time in
    *                  microseconds.
    *
    * NOTES:
    *
    * Because of the types used, all the returned values except period_usecs
    * are unsigned. In computations mapping between frames and microseconds
    * *signed* differences are required. The easiest way is to compute those
    * separately and assign them to the appropriate signed variables,
    * int32_t for frames and int64_t for usecs. See the implementation of
    * jack_frames_to_time() and Jack_time_to_frames() for an example.
    *
    * Unless there was an xrun, skipped cycles, or the current cycle is the
    * first after freewheeling or starting Jack, the value of current_usecs
    * will always be the value of next_usecs of the previous cycle.
    *
    * The value of period_usecs will in general NOT be exactly equal to
    * the difference of next_usecs and current_usecs. This is because to
    * ensure stability of the DLL and continuity of the mapping, a fraction
    * of the loop error must be included in next_usecs. For an accurate
    * mapping between frames and microseconds, the difference of next_usecs
    * and current_usecs should be used, and not period_usecs.
    *
    * @return zero if OK, non-zero otherwise.
    */
  def jack_get_cycle_times (client: Ptr[jack_client_t], current_frames: Ptr[jack_nframes_t],
                            current_usecs: Ptr[jack_time_t], next_usecs: Ptr[jack_time_t],
                            period_usecs: Ptr[CFloat]): CInt = extern

  /** Returns the estimated time in microseconds of the specified frame time. */
  def jack_frames_to_time(client: Ptr[jack_client_t], frames: jack_nframes_t): jack_time_t = extern

  /** Returns the estimated time in frames for the specified system time. */
  def jack_time_to_frames(client: Ptr[jack_client_t], time: jack_time_t): jack_nframes_t = extern

  /** Returns JACK's current system time in microseconds, using the JACK clock source.
    *
    * The value returned is guaranteed to be monotonic, but not linear.
    */
  def jack_get_time: jack_time_t = extern

  // ---- ErrorOutput Controlling error/information output ----

  //  /** Displays JACK error message.
  //    *
  //    * Set via jack_set_error_function(), otherwise a JACK-provided
  //    * default will print @a msg (plus a newline) to stderr.
  //    *
  //    * @param msg error message text (no newline at end).
  //    */
  //  extern Unit (*jack_error_callback)(const char *msg) = extern

  //  /** Sets the @ref jack_error_callback for error message display.
  //    *
  //    * The JACK library provides two built-in callbacks for this purpose:
  //    * default_jack_error_callback() and silent_jack_error_callback().
  //    */
  //  def jack_set_error_function (void (*func)(const char *)): Unit = extern

  //  /** Displays JACK info message.
  //    *
  //    * Set via jack_set_info_function(), otherwise a JACK-provided
  //    * default will print @a msg (plus a newline) to stdout.
  //    *
  //    * @param msg info message text (no newline at end).
  //    */
  //  extern Unit (*jack_info_callback)(const char *msg) = extern

  //  /** Sets the @ref jack_info_callback for info message display. */
  //  Unit jack_set_info_function (Unit (*func)(const char *)) = extern

  // ---- ----

  /** The free function to be used on memory returned by jack_port_get_connections,
    * jack_port_get_all_connections and jack_get_ports functions.
    * This is MANDATORY on Windows when otherwise all nasty runtime version related crashes can occur.
    * Developers are strongly encouraged to use this function instead of the standard "free" function in new code.
    */
  def jack_free(ptr: Ptr[_]): Unit = extern
}

sealed trait jack_client_t  // opaque
sealed trait jack_port_t    // opaque