// Board.java
package tetris;

import java.util.Arrays;

/**
 CS108 Tetris Board.
 Represents a Tetris board -- essentially a 2-d grid
 of booleans. Supports tetris pieces and row clearing.
 Has an "undo" feature that allows clients to add and remove pieces efficiently.
 Does not do any drawing or have any idea of pixels. Instead,
 just represents the abstract 2-d board.
*/
public class Board	{
	// Some ivars are stubbed out for you:
	private int width;
	private int height;
	private boolean[][] grid;
	private int[] yArr;
	private int[] xArr;
	
	private boolean[][] oldGrid;
	private int[] oldXArr;
	private int[] oldYArr;
	
	private int currHeight;
	
	private boolean DEBUG = true;
	boolean committed;
	// Here a few trivial methods are provided:
	
	/**
	 Creates an empty board of the given width and height
	 measured in blocks.
	*/
	public Board(int width, int height) {
		this.width = width;
		this.height = height;
		committed = true;
		
		grid = new boolean[height][width];
		xArr = new int[width];
		yArr = new int[height];
		
		oldGrid = new boolean[height][width];
		oldXArr = new int[width];
		oldYArr = new int[height];
		
		currHeight = 0;
	}
	
	
	/**
	 Returns the width of the board in blocks.
	*/
	public int getWidth() {
		return width;
	}
	
	
	/**
	 Returns the height of the board in blocks.
	*/
	public int getHeight() {
		return height;
	}
	
	
	/**
	 Returns the max column height present in the board.
	 For an empty board this is 0.
	*/
	public int getMaxHeight() {	 
		return currHeight; // YOUR CODE HERE
	}
	
	
	/**
	 Checks the board for internal consistency -- used
	 for debugging.
	*/
	public void sanityCheck() {
		if (DEBUG) {
			// YOUR CODE HERE
		}
	}
	
	/**
	 Given a piece and an x, returns the y
	 value where the piece would come to rest
	 if it were dropped straight down at that x.
	 
	 <p>
	 Implementation: use the skirt and the col heights
	 to compute this fast -- O(skirt length).
	*/
	public int dropHeight(Piece piece, int x) {
		int[] skirt = piece.getSkirt();
		int result = Integer.MIN_VALUE;
		for(int i = 0; i<skirt.length; i++){
			result = (result < xArr[x + i] - skirt[i]) ? (xArr[x + i] - skirt[i]) : result;
		}
		return result;
	}
	
	
	/**
	 Returns the height of the given column --
	 i.e. the y value of the highest block + 1.
	 The height is 0 if the column contains no blocks.
	*/
	public int getColumnHeight(int x) {
		return xArr[x]; // YOUR CODE HERE
	}
	
	
	/**
	 Returns the number of filled blocks in
	 the given row.
	*/
	public int getRowWidth(int y) {
		 return yArr[y]; // YOUR CODE HERE
	}
	
	
	/**
	 Returns true if the given block is filled in the board.
	 Blocks outside of the valid width/height area
	 always return true.
	*/
	public boolean getGrid(int x, int y) {
		if(x<0||y<0||x>=width||y>=height) return true;
		return grid[y][x];
	}
	
	
	public static final int PLACE_OK = 0;
	public static final int PLACE_ROW_FILLED = 1;
	public static final int PLACE_OUT_BOUNDS = 2;
	public static final int PLACE_BAD = 3;
	
	/**
	 Attempts to add the body of a piece to the board.
	 Copies the piece blocks into the board grid.
	 Returns PLACE_OK for a regular placement, or PLACE_ROW_FILLED
	 for a regular placement that causes at least one row to be filled.
	 
	 <p>Error cases:
	 A placement may fail in two ways. First, if part of the piece may falls out
	 of bounds of the board, PLACE_OUT_BOUNDS is returned.
	 Or the placement may collide with existing blocks in the grid
	 in which case PLACE_BAD is returned.
	 In both error cases, the board may be left in an invalid
	 state. The client can use undo(), to recover the valid, pre-place state.
	*/
	public int place(Piece piece, int x, int y) {
		// flag !committed problem
		if (!committed) throw new RuntimeException("place commit problem");
		committed = false;
		
		int result = PLACE_OK;
		
		TPoint[] tp = piece.getBody();
		for(int i = 0; i<tp.length; i++){
			int atX = x + tp[i].x;
			int atY = y + tp[i].y;
			
			if(0 > atX || 0 > atY || width <= atX || height <= atY) {
				result = PLACE_OUT_BOUNDS;
				break;
			}
			
			if(grid[atY][atX] == true) {
				result = PLACE_BAD;
				break;
			}
			
			grid[atY][atX] = true;
			
			yArr[atY]++;
			if(yArr[atY] == width) result = PLACE_ROW_FILLED;
			
			if(xArr[atX]<(atY+1)) xArr[atX] = atY + 1;
			if(xArr[atX]>currHeight) currHeight = xArr[atX];
		}
		return result;
	}
	
	private void undoBuffer(){
		System.arraycopy(xArr, 0, oldXArr, 0, width);
		System.arraycopy(yArr, 0, oldYArr, 0, height);
		for(int i = 0; i<height; i++){
			System.arraycopy(grid[i], 0, oldGrid[i], 0, width);
		}
	}
	
	/**
	 Deletes rows that are filled all the way across, moving
	 things above down. Returns the number of rows cleared.
	*/
	public int clearRows() {
		int rowsCleared = 0;
		committed = false;
		for(int i = 0; i<height; i++){
			if(yArr[i] == width){
				for(int m = i; m<height; m++){
					for(int n = 0; n<width; n++){
						if(m!=height-1){
							grid[m][n] = grid[m+1][n];
						} else {
							grid[m][n] = false;
						}
					}
				}
				//ReArrange yAxis array 
				reArrange(i);
				i--;
				rowsCleared++;
			}
		}
		
		for(int i = 0; i<width; i++){
			int count = 0;
			for(int j = 0; j<height; j++){
				if(grid[j][i]) {
					count = j + 1;
				}
				xArr[i] = count;
			}
		}
		currHeight = findCurrHeight();
		
		sanityCheck();
		return rowsCleared;
	}
	
	private int findCurrHeight(){
		int[] tmpArr = Arrays.copyOf(xArr, xArr.length);
		Arrays.sort(tmpArr);
		return tmpArr[tmpArr.length-1];
	}

	private void reArrange(int from){
		for(;from<yArr.length-1; from++) {
			yArr[from]=yArr[from+1];
		}
		yArr[yArr.length-1] = 0;
	}

	/**
	 Reverts the board to its state before up to one place
	 and one clearRows();
	 If the conditions for undo() are not met, such as
	 calling undo() twice in a row, then the second undo() does nothing.
	 See the overview docs.
	*/
	public void undo() {
		if(!committed) {
			committed = true;
			System.arraycopy(oldXArr, 0, xArr, 0, width);
			System.arraycopy(oldYArr, 0, yArr, 0, height);
			for(int i = 0; i<height; i++){
				System.arraycopy(oldGrid[i], 0, grid[i], 0, width);
			}
			currHeight = findCurrHeight();
		}
	}
	
	
	/**
	 Puts the board in the committed state.
	*/
	public void commit() {
		undoBuffer();
		committed = true;
	}


	
	/*
	 Renders the board state as a big String, suitable for printing.
	 This is the sort of print-obj-state utility that can help see complex
	 state change over time.
	 (provided debugging utility) 
	 */
	public String toString() {
		StringBuilder buff = new StringBuilder();
		for (int y = height-1; y>=0; y--) {
			buff.append('|');
			for (int x=0; x<width; x++) {
				if (getGrid(x,y)) buff.append('+');
				else buff.append(' ');
			}
			buff.append("|\n");
		}
		for (int x=0; x<width+2; x++) buff.append('-');
		return(buff.toString());
	}
}