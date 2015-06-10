s = new Server( "test" );
Server.program = "/Applications/SuperCollider_f/scsynth";
s.boot();
x = new Synth( "default", null, null, s.defaultGroup );
new Timer().runAfter( 1000 ) { x.free(); }
s.quit();
