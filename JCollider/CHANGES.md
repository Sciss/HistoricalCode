### v0.37 (jan 2010 - SVN rev. 27)

- Compatibility for new scsynth /server.reply command

### v0.36 (oct 2009 - SVN rev. 25)

- Bus: adding set(n)(Msg), get(n)(Msg), fill(Msg)

### v0.35 (sep 2009 - SVN rev. 19)

- Updated ugen infos: DiskOut number of outputs

### v0.34 (jul 2009 - SVN rev. 16)

- Updated ugen infos from latest SuperCollider
- Control: possible to create multiinput control with just one name
- Server: default bufsize 64k, better boot thread quitting
- Synth: get(n)(Msg) added, grain added
- Fixed some threading issues
- Uses ScissLib now
- Uses ant doc target

### v0.32 (feb 2008 - SVN rev. 3)

- ServerOptions: added verbosity and rendezvous
- Node: added register, unregister
- UGenInfo: added binary file format (much faster)

### v0.31 (nov 2007)

- switched over to Eclipse IDE and Ant build tool
- Buffer: added getDuration, allocConsecutive, allocReadChannel, readChannel methods

### v0.30 (jul 2007)

- updated for new NetUtil version

### v0.29 (oct 2006)

- updated for new NetUtil version; allows to talk to server using TCP
- bug fixes and improvements in NodeWatcher
- slight modfications of OSCMultiResponder and OSCResponderNode, see readme_api_changes.txt for details
- v0.291 fixes missing server.start() calls in demos and boot thread, includes TCP fixes from NetUtil 0.31
- v0.292 fixes a bug in NodeWatcher.dispose()

### v0.28 (jul 2006)

- incorporates some sclang updates, mainly variable bus allocator classes in ServerOptions and Server.
- Server's alive thread more robust against occasional server irresponsiveness
- extended method documentation
- added a couple of missing methods
- added ControlSpec, Warp, EZSlider
- bugfixes (OSCMultiResponder et al.)

### v0.27 (feb 2006)

- bugfix in SynthDef: recognizes unnamed controls generated from arrays

### v0.26 (nov 2005)

- bugfix in SynthDef: recognizes LagControl, TrigControl

### v0.24 (oct 2005)

- adds write() methods to Buffer
- added /fail support for Server.sendMsgSync() ; added Server.sendBundleSync()

### v0.23 (oct 2005)

- new NodeWatcher class, new GUI classes NodeTreeManager and NodeTreePanel
- Node is abstract now, and implements the TreeNode interface which nicely interacts with the new GUI classes ; Nodes can be named for human readability
- removed setGroup, setRunning and setPlaying statements from the Node classes; this job must be explictely done now by a NodeWatcher or the like, which is a much cleaner solution
- after booting, server will call initTree before dispatching the server event

### v0.21 (sep 2005)

- fixed bug in Server.boot() (didn't reset the booting status when boot failed)

### v0.2 (sep 2005)

- first version separately released. alpha stadium, probably totally buggy
