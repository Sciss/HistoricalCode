~prob = 0.5;
~neighbors = [[ 1, 0 ], [ 0, 1 ], [ -1, 0 ], [ 0, -1 ]];
~numCols = 9;
~numRows = 9;
~numStates = 15; // 4;
~pixelSize = 24;

~createCell = { arg row, col;
	var map;
	map = AnyMap.new;
	map.value = 0;
	map.row = row;
	map.col = col;
};

~matrix = ~numRows.collect({ arg row;
	~numCols.collect({ arg col;
		~createCell.( row, col );
	});
});

~colors = ~numStates.collect({ arg i; Color.hsv( i / ~numStates, 1, 1 )});

//w.refresh;

~matrix.flatten.do({ arg cell; cell.value = ~numStates.rand });

~rule = { arg row, col;
   var n;
	if( ~prob.coin, {
	   n = ~neighbors.choose;
		~matrix[ row ][ col ].nextValue = ~matrix[ (row + n[ 1 ]).wrap( 0, ~numRows - 1 )][ (col + n[ 0 ]).wrap( 0, ~numCols - 1 )].value;
	}, {
		~matrix[ row ][ col ].nextValue = ~matrix[ row ][ col ].value;
	});
};

//~dur = 1.0;
~dur = 0.25;
//~dur = 0.01;
//~dur = 0.001;
//~prob = 1;

~step = {
	~numRows.do({ arg row; ~numCols.do({ arg col;
		~rule.( row, col );
	})});
	~numRows.do({ arg row; ~numCols.do({ arg col;
		~matrix[ row ][ col ].value = ~matrix[ row ][ col ].nextValue;
	})});
};

w = Window( "CA", Rect( 100, 100, ~numCols * ~pixelSize, ~numRows * ~pixelSize ), false ).front;
w.drawHook = {
	~numRows.do({ arg row;
		~numCols.do({ arg col;
			Pen.fillColor = ~colors[ ~matrix[ row ][ col ].value ];
			Pen.fillRect( Rect( col * ~pixelSize, row * ~pixelSize, ~pixelSize, ~pixelSize ))
		});
	});
};

~anim = Routine({ inf.do({
	~step.();
	w.refresh;
	~dur.wait;
})}).play( AppClock );

w.onClose = { ~anim.stop };

