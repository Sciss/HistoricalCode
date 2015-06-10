s.options.device = "Fireface 800 (EB1)"; // "MOTU Traveler";
s.options.numOutputBusChannels = 28;
s.reboot;

OhrwaldUtil.workDir = "/Volumes/KarminUser/Projekte/Ohrwald/Users/rutz/audio_work";
OhrwaldUtil.loadBallons;
OhrwaldUtil.loadResps;
OhrwaldUtil.loadResps2;
OhrwaldUtil.loadGehirne;
// OhrwaldUtil.gehirne.asCompileString;

OhrwaldUtil.ballonEditGUI( \gehirne, 0, 14 );
OhrwaldUtil.ballonEditGUI( \gehirne, 1, 14 );
OhrwaldUtil.ballonEditGUI( \gehirne, 2, 14 );
OhrwaldUtil.ballonEditGUI( \gehirne, 3, 14 );
OhrwaldUtil.ballonEditGUI( \gehirne, 4, 14 );
OhrwaldUtil.ballonEditGUI( \gehirne, 5, 14 );
OhrwaldUtil.ballonEditGUI( \gehirne, 6, 14 );
OhrwaldUtil.ballonEditGUI( \gehirne, 7, 14 ); // XXX
OhrwaldUtil.ballonEditGUI( \gehirne, 8, 14 ); // XXX
OhrwaldUtil.ballonEditGUI( \gehirne, 9, 14 );
OhrwaldUtil.ballonEditGUI( \gehirne, 10, 14 );
OhrwaldUtil.ballonEditGUI( \gehirne, 11, 14 );
OhrwaldUtil.ballonEditGUI( \gehirne, 12, 14 );
