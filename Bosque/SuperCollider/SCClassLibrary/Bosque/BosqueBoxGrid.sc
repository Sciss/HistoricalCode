// (c) 2006, Thor Magnusson - www.ixi-software.net
// GNU licence - google it.
// modified by Andre Bartetzki:
// GUI-Support

/**
 *	Changelog:
 *		- 21-Aug-07 fixing things
 *		- 14-Jun-09 fixing for relative origin = true
 *
 *	@author	Thor Magnusson
 *	@author	André Bartetzki
 *	@author	Hanns Holger Rutz
 */
BosqueBoxGrid {

	var <>gridNodes; 
	var tracknode, chosennode, mouseTracker;
// SCISS FIXED
//	var <bounds;
	var downAction, upAction, trackAction, keyDownAction, rightDownAction, backgrDrawFunc;
	var background;
	var columns, rows;
	var fillcolor, fillmode;
	var traildrag, bool;
	var <>mode;		// \all, \one, \onePerColumn, \onePerRow
	var font, fontColor;
	var <enabled = true;
	
	*new { arg parent, bounds, columns, rows; 
		^super.new.initBoxGrid(parent, bounds, columns, rows);
	}
	
	initBoxGrid { arg parent, argbounds, argcolumns, argrows;
		var p, rect, pen, pad, bounds;
		
		pad	= if( GUI.id === \cocoa, 0, 0.5 );
		
		bounds = argbounds ? Rect(20, 20, 400, 200);
		bounds = Rect(bounds.left + 0.5, bounds.top + 0.5, bounds.width, bounds.height);
		
		pen	= GUI.pen;

		/*
		win = w ? GUI.window.new("BoxGrid", 
			Rect(10, 250, bounds.left + bounds.width + 40, bounds.top + bounds.height+30));
		//win.front;
		*/
		tracknode = 0;
		columns = argcolumns ? 6;
		rows = argrows ? 8;
		background = Color.clear;
		fillcolor = Color.new255(103, 148, 103);
		fillmode = true;
		traildrag = false;
		bool = false;
		font = GUI.font.new("Arial", 8);
		fontColor = Color.black;

		mode = \all;		
		gridNodes = Array.newClear(rows) ! columns;
		
				
		mouseTracker = GUI.userView.new(parent, bounds);
		
		// jetzt bounds aktualisieren wg. Verschiebung durch decorator
//		bounds = mouseTracker.bounds;
		columns.do({arg c;
			rows.do({arg r;
// SCISS FIXED
//				rect = Rect((bounds.left+(c*(bounds.width/columns))).round(1)+0.5, 
//							(bounds.top+(r*(bounds.height/rows))).round(1)+0.5, 
//							(bounds.width/columns).round(1), 
//							(bounds.height/rows).round(1)
//						);
//				gridNodes[c][r] = BosqueBox.new(rect, c, r, fillcolor);
				gridNodes[c][r] = BosqueBox.new(Rect.new, c, r, fillcolor);
			});
		});
		
		mouseTracker.canFocus_(false)
			.enabled_(enabled)
// SCISS FIXED
//			.mouseBeginTrackFunc_({|me, x, y, mod| ... })
			.mouseDownAction_({|me, x, y, mod|
					chosennode = this.findNode(x, y);
					if(mod == 262401, { // right mouse down
						rightDownAction.value(chosennode.nodeloc);
					}, {
						if(chosennode !=nil, {  
							chosennode.state = not(chosennode.state);
							if (chosennode.state) { this.prModeCheck(chosennode) };
							tracknode = chosennode;
							downAction.value(chosennode.nodeloc);
							this.refresh;	
						});
					});
			})
// SCISS FIXED
//			.mouseTrackFunc_({|me, x, y, mod| ... })
			.mouseMoveAction_({|me, x, y, mod|
				chosennode = this.findNode(x, y);
				if(chosennode != nil, {  
					if(tracknode.rect != chosennode.rect, {
						if(traildrag == true, { // on dragging mouse
							if(bool == true, { // boolean switching
								chosennode.state = not(chosennode.state);
							}, {
								chosennode.state = true;
							});
						},{
							chosennode.state = true;
							tracknode.state = false;
						});
						if (chosennode.state) { this.prModeCheck(chosennode) };
						tracknode = chosennode;
						trackAction.value(chosennode.nodeloc);
						this.refresh;
					});
				});
			})
// SCISS FIXED
//			.mouseEndTrackFunc_({|me, x, y, mod| ... })
			.mouseUpAction_({|me, x, y, mod|
				chosennode = this.findNode(x, y);
				if(chosennode !=nil, {  
					tracknode = chosennode;
					upAction.value(chosennode.nodeloc);
					this.refresh;
				});
			})
// SCISS FIXED
//			.keyDownFunc_({ |me, key, modifiers, unicode | ... })
			.keyDownAction_({ |me, key, modifiers, unicode |				keyDownAction.value(key, modifiers, unicode);
				this.refresh;
			})
			.drawFunc_({arg view; var bounds = view.bounds.moveTo( 0, 0 ), fillrect;
				pen.width = 1;
				//background.set; // background color
				pen.color = background;
				pen.fillRect(bounds+0.5); // background fill
			pen.use({ pen.translate( pad, pad );
				backgrDrawFunc.value; // background draw function
			});	
				// Draw the boxes
				//Color.black.set;
				pen.strokeColor = Color.black;
				gridNodes.do({arg row;
					row.do({arg node; 
// SCISS FIXED
fillrect = Rect((bounds.left+(node.nodeloc[0]*(bounds.width/columns))).round(1)+0.5+node.border, 
							(bounds.top+(node.nodeloc[1]*(bounds.height/rows))).round(1)+0.5+node.border, 
							(bounds.width/columns).round(1)-(2*node.border), 
							(bounds.height/rows).round(1)-(2*node.border)
						);
						
						if(node.state == true, {
							if(fillmode, {
								//node.color.set;
								pen.fillColor = node.color;
//								pen.fillRect(node.fillrect);
// SCISS FIXED
pen.fillRect(fillrect);

								//Color.black.set;	
								//pen.strokeColor = Color.black;
								//pen.strokeRect(node.fillrect);
							},{
								//Color.black.set;	
								pen.fillColor = node.color;
//								pen.strokeRect(node.fillrect);
// SCISS FIXED
pen.strokeRect(fillrect);
							});
						});
// SCISS FIXED
						if( node.string.size > 0, {
							pen.fillColor = fontColor;
							pen.font = font;
							pen.stringCenteredIn( node.string, fillrect );
						});
//						node.string.drawCenteredIn( node.fillrect, font, fontColor);
	/*					node.string.drawInRect(Rect(node.fillrect.left+5,
					    					node.fillrect.top+(node.fillrect.height/2)-(font.size/1.5), 
					    					80, 16),   
					    					font, fontColor);
	*/
		
					});
				});
				//Color.black.set;
				pen.strokeColor = Color.black;
				(columns+1).do({arg i;
					pen.line(
						Point(bounds.left+(i*(bounds.width/columns)),
								bounds.top).round(1) + 0.5, 
						Point(bounds.left+(i*(bounds.width/columns)),
								bounds.height+bounds.top).round(1) + 0.5
					);
				});
				(rows+1).do({arg i;
					pen.line(
						Point(bounds.left, 
							bounds.top+(i*(bounds.height/rows))).round(1) + 0.5, 
						Point(bounds.width+bounds.left, 
							bounds.top+(i*(bounds.height/rows))).round(1) + 0.5
					);
				});
				pen.stroke;			
			});
			
	
	}
	
// SCISS ADDED
	asView { ^mouseTracker }
	bounds { ^mouseTracker.bounds }
	
	// GRID
	
	enabled_ { arg bool;
		enabled = bool;
		mouseTracker.enabled_(enabled);
		mouseTracker.refresh;
	}
	
	setBackgrColor_ {arg color;
		background = color;
		mouseTracker.refresh;
	}
		
	setFillMode_ {arg mode;
		fillmode = mode;
		mouseTracker.refresh;
	}
	
	setFillColor_ {arg color;
		gridNodes.do({arg row;
			row.do({arg node; 
				node.setColor_(color);
			});
		});
		mouseTracker.refresh;
	}
	
	setTrailDrag_{arg mode, argbool=false;
		traildrag = mode;
		bool = argbool;
	}

	refresh {
		mouseTracker.refresh;
	}
		
	// NODES	
	setNodeBorder_ {arg border;
		gridNodes.do({arg row;
			row.do({arg node; 
				node.setBorder_(border);
			});
		});
		mouseTracker.refresh;
	}
	
	
	// depricated
	setVisible_ {arg col, row, state;
		gridNodes[col][row].setVisible_(state);
		mouseTracker.refresh;
	}

	setState_ {arg col, row, state;
		if(state.isInteger, {state = state!=0});
		gridNodes[col][row].setState_(state);
		mouseTracker.refresh;
	}
	
	getState {arg col, row;
		var state;
		state = gridNodes[col][row].getState;
		^state;
	//	^state.binaryValue;
	}	
	
	setBoxColor_ {arg col, row, color;
		gridNodes[col][row].setColor_(color);
		mouseTracker.refresh;
	}
	
	getBoxColor {arg col, row;
		^gridNodes[col][row].getColor;	
	}
	
	getNodeStates {
		var array;
		array = Array.newClear(rows) ! columns;
		gridNodes.do({arg rows, c;
			rows.do({arg node, r; 
				//array[r][c] = node.state.binaryValue;
				array[c][r] = node.state;
			});
		});
		^array;
	}
	
	setNodeStates_ {arg array;
		gridNodes.do({arg rows, c;
			rows.do({arg node, r; 
				node.state = array[c][r]!=0;
			});
		});
		mouseTracker.refresh;
	}
	
	clearGrid {
		gridNodes.do({arg rows, c;
			rows.do({arg node, r; 
				node.state = false;
			});
		});
		mouseTracker.refresh;
	}	
	
	// PASSED FUNCTIONS OF MOUSE OR BACKGROUND
	nodeDownAction_ { arg func;
		downAction = func;
	}
	
	nodeUpAction_ { arg func;
		upAction = func;
	}
	
	nodeTrackAction_ { arg func;
		trackAction = func;
	}
	
	keyDownAction_ {arg func;
		mouseTracker.canFocus_(true); // in order to detect keys the view has to be focusable
		keyDownAction = func;
	}
	
	rightDownAction_ {arg func;
		rightDownAction = func;
	}
	
	setBackgrDrawFunc_ { arg func;
		backgrDrawFunc = func;
	}
		
	setFont_ {arg f;
		font = f;
		mouseTracker.refresh;	
	}
	
	setFontColor_ {arg fc;
		fontColor = fc;
		mouseTracker.refresh;	
	}
	
	setNodeString_ {arg col, row, string;
		gridNodes[col][row].string = string;
		mouseTracker.refresh;		
	}
	
	getNodeString {arg col, row;
		^gridNodes[col][row].string;
	}

	setStrings_ { arg array;
		array.do { arg strings, col;
			strings.do {arg string, row;
				this.setNodeString_(col, row, string)
			}
		}
	}

	// local function
	findNode {arg x, y; var rect, bounds = this.bounds.moveTo( 0, 0 );
		gridNodes.do({arg row;
			row.do({arg node;
// SCISS FIXED
rect = Rect((bounds.left+(node.nodeloc[0]*(bounds.width/columns))).round(1)+0.5, 
							(bounds.top+(node.nodeloc[1]*(bounds.height/rows))).round(1)+0.5, 
							(bounds.width/columns).round(1), 
							(bounds.height/rows).round(1)
						);

//				if(node.rect.containsPoint(Point.new(x,y)), { ... })
				if(rect.containsPoint(Point.new(x,y)), {
					^node;
				});
			});
		});
		^nil;
	}
	
	prModeCheck {arg node;
		var row, col;
		row = node.nodeloc[1];
		col = node.nodeloc[0];
		
		switch (mode)
		{ \one } 			{ this.clearGrid; this.setState_(col, row, true); }
		{ \onePerColumn } 	{ gridNodes.do({arg rows, c;
								rows.do({arg node, r; 
									if( (c == col) && (r != row)) { node.state = false; }
								})
							}) }		
		{ \onePerRow } 	{ gridNodes.do({arg rows, c;
								rows.do({arg node, r; 
									if( (r == row) && (c != col)) { node.state = false; }
								})
							}) }		
			
		//{ \all } 			{  }
		;
	}
	
	
}

BosqueBox {
	var <>fillrect, <>state, <>border, <>rect, <>nodeloc, <>color;
	var <>string;
	
	*new { arg rect, column, row, color ; 
		^super.new.initGridNode( rect, column, row, color);
	}
	
	initGridNode {arg argrect, argcolumn, argrow, argcolor;
		rect = argrect;
		nodeloc = [ argcolumn, argrow ];	
		color = argcolor;	
		border = 2;
		fillrect = Rect(rect.left+border, rect.top+border, 
					rect.width-(border*2), rect.height-(border*2));
		state = false;
		string = "";
	}
	
	setBorder_ {arg argborder;
		border = argborder;
		fillrect = Rect(rect.left+border, rect.top+border, 
					rect.width-(border*2), rect.height-(border*2));
	}
	
	setVisible_ {arg argstate;
		state = argstate;
	}
	
	setState_ {arg argstate;
		state = argstate;
	}
	
	getState {
		^state;
	}
	
	setColor_ {arg argcolor;
		color = argcolor;
	}
	
	getColor {
		^color;
	}
}
