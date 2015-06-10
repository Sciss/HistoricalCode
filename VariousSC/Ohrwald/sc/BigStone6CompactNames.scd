(
	var f, shift = 0;
	~cmds = nil;
	f = { arg i; OhrwaldUtil.workDir +/+ "BigStone6Cut%ConvLHPF.aif".format( (i + 100).asString.copyToEnd( 1 ))};
	(1..97).do({ arg i; var p;
		p = f.( i );
		if( File.exists( p ), {
			if( shift > 0, {
				~cmds = ~cmds.add( "mv % %".format( p.escapeChar($ ), f.( i - shift ).escapeChar($ )));
			});
		}, {
			shift = shift + 1;
		});
	});
)

~cmds.do(_.systemCmd);
