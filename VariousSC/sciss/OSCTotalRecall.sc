/**
 *	(C)opyright 2006-2008 Hanns Holger Rutz. All rights reserved.
 *	Distributed under the GNU General Public License (GPL).
 *
 *	Changelog:
 *		04-Feb-08 - removed UpdateListener since Main:recvOSCmessage is not patched any more
 *
 *	@author	Hanns Holger Rutz
 *	@version	0.12, 04-Feb-08
 */
OSCTotalRecall : NetAddr {

	var /* upd, */ ev, rec = false;
	var <>notThese;
	var <netAddr;
	var funcOSC;

	*new { arg netAddr;
		^super.new( netAddr.hostname, netAddr.port ).prInit( netAddr );
	}
	
	prInit { arg argAddr;
		netAddr = argAddr;
		notThese = IdentitySet[ '/status', 'status.reply' ];
//		upd = UpdateListener({ arg upd, obj, what, time, replyAddr, msg; ... });
		funcOSC = { arg time, replyAddr, msg;
			var name;
			if( rec /* and: (what === \osc) */, {
				name = msg.first.asSymbol;
				if( (name !== '/done' or: { msg[ 1 ] === '/synced' }) and: { notThese.includes( name ).not }, {
					ev = ev.add(( when: Main.elapsedTime, what: \inc, data: msg ));
				});
			});
		};
//		({ ... });
	}
	
	startRecording {
		rec = true;
//		upd.addTo( Main );
		thisProcess.recvOSCfunc = thisProcess.recvOSCfunc.addFunc( funcOSC );
	}
	
	stopRecording {
		rec = false;
//		upd.remove;
		thisProcess.recvOSCfunc = thisProcess.recvOSCfunc.removeFunc( funcOSC );
	}
	
	clear {
		ev = nil;
	}
	
	write { arg path;
		var f;
		
		f = File( path, "w" );
		f.write( ev.asCompileString );
		f.close;
	}
	
	read { arg path;
		var f;
		
		f = File( path, "r" );
		ev = f.readAllString.interpret;
		f.close;
	}
	
	// a rate < 1.0 will "slow" down the replay,
	// possible to compensate for missing synchronization mechanism right now
	play { arg rate = 1.0, syncWithIncoming = true;
		var evCpy, rout, funcOSC2, /* upd, */ condInc, inMsg;
		evCpy = ev.copy;
		if( evCpy.size > 0, {
			inMsg = List.new;
			condInc = Condition.new;
//			upd = UpdateListener.newFor( Main, { arg upd, obj, what, time, replyAddr, msg; ... });
			funcOSC2 = { arg time, replyAddr, msg;
				var name;
//				if( what === \osc, {
					name = msg.first.asSymbol;
					if( (name !== '/done' or: { msg[ 1 ] === '/synced' }) and: { notThese.includes( name ).not }, {
						inMsg.add( msg );
						condInc.test = true;
						condInc.signal;
					});
//				});
			};
//			({ ... });
			thisProcess.recvOSCfunc = thisProcess.recvOSCfunc.addFunc( funcOSC2 );

			rout = Routine.run({
				var currentTime, lastTime, dt, sync;
			
				lastTime = evCpy.first[ \when ] / rate;
			
				evCpy.do({ arg event;
					currentTime = event[ \when ] / rate;
					dt = currentTime - lastTime;
					switch( event[ \what ],
						\msg, {
							if( dt > 0, {
								dt.wait;
							});
							netAddr.sendMsg( *event[ \data ]);
						},
						\bnd, {
							// XXX should send raw bytes here using proper bundle time
							// calculation from former logical time
							if( dt > 0, {
								dt.wait;
							});
							netAddr.sendBundle( if( event[ \bnd ].isNil, nil, { event[ \bnd ] / rate }), *event[ \data ]);
						},
						\inc, {
							if( syncWithIncoming, {
								sync = false;
								("awaiting " ++ event[ \data ] ++ " ... ").postln;
								while({ sync.not }, {
									block { arg break;
										inMsg.do({ arg msg;
											if( msg == event[ \data ], {
												"  ok".postln;
												sync = true;
												break.value;
											});
										});
										condInc.test = false;
										condInc.wait;
									};
								});
							}, {
								dt.wait;
							});
						}
					);
					lastTime = currentTime;
				});
//				upd.remove;
				thisProcess.recvOSCfunc = thisProcess.recvOSCfunc.removeFunc( funcOSC2 );
				("FileNetAddr2.play : done!").postln;
			}, clock: SystemClock );
		});
		^rout;
	}

//	sendRaw { arg rawArray;
//		this.prFlushMessages;
//		file.write( rawArray.size );
//		file.write( rawArray );
//	}
	
	sendMsg { arg ... args;
		if( rec, {
			ev = ev.add(( when: Main.elapsedTime, what: \msg, data: args ));
		});
		^netAddr.sendMsg( *args );
	}
	
	sendBundle { arg time ... args;
		if( rec, {
			ev = ev.add(( when: Main.elapsedTime, logical: thisThread.seconds, bundle: time, what: \bnd, data: args ));
		});
		^netAddr.sendBundle( time, *args );
	}

//	isConnected {
//		^netAddr.isConnected;
//	}
//
//	connect { arg disconnectHandler;
//		^netAddr.connect( disconnectHandler );
//	}
//
//	disconnect {
//		^netAddr.disconnect;
//	}
//
//	ip {
//		^netAddr.ip;
//	}
//	
//	recover {
//		^netAddr.recover;
//	}
//	
//	doesNotUnderstand { arg selector ... args;
//		^netAddr.perform( selector, args );
//	}
}