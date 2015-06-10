x = [ "helm.w64", "ko.w64", "leventhal.w64", "lindig.w64", "meyerkoenig.w64", "rutz.w64" ].collect({ arg name; OhrwaldPiece( name )});
x[ 4 ].order = \circ;
x.asCompileString;