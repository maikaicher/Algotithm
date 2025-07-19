package bs7minesweeper;

import java.util.ArrayList;
import java.util.Random;

import javax.swing.JOptionPane;

/**
 * Class for developing the minesweeper game logic step by step and therefore in a procedural way.
 * @author Maik Aicher
 *
 */
public class MineSweeperProcedural {
	// Constants for accessing the position vector data
	public static final int X = 0;
	public static final int Y = 1;
	
	// movement directions towards the eight neighbor fields
	public static final int[] N  = { 0, -1};
	public static final int[] NE = { 1, -1};
	public static final int[] E  = { 1,  0};
	public static final int[] SE = { 1,  1};
	public static final int[] S  = { 0,  1};
	public static final int[] SW = {-1,  1};
	public static final int[] W  = {-1,  0};
	public static final int[] NW = {-1, -1};
	public static final int[] NONE = {0, 0};
	
	// all directions from a single point without own location.
	public static final int[][] DIR  = {N, NE, E, SE, S, SW, W, NW};
	
	// all directions from a single point including own location.
	public static final int[][] DIR2  = {N, NE, E, SE, S, SW, W, NW, NONE};
	
	public static final int EMPTY_HIDE = 0;  // field is empty, but hidden
	public static final int EMPTY_SHOW = 10;  // field is empty, but not hidden
	public static final int MINE_HIDE = -9;  // field has a mine, but is hidden
	public static final int MINE_SHOW = 9;  // field has a mine, but is not hidden
	
	// for controlling the game
	public static final int GAME_RUNNING = 0;
	public static final int GAME_USER_WINS = 1;
	public static final int GAME_USER_LOOSES = 2;
	

	public static void main(String[] args) {
		//testAufgabe3();
		//testAufgabe4();
		game();
	}

	/**
	 * Counts the values of all neighbors of a given position
	 */
	public static void testAufgabe3() {
		int[][] data = generateTestArray1();
		
		System.out.println(countNeighbors(data, new int[] {0, 0}));
		System.out.println(countNeighbors(data, new int[] {7, 7}));
		System.out.println(countNeighbors(data, new int[] {0, 3}));
		System.out.println(countNeighbors(data, new int[] {3, 0}));
		System.out.println(countNeighbors(data, new int[] {7, 3}));
		System.out.println(countNeighbors(data, new int[] {4, 5}));		
	}
	
	/**
	 * Calculates the number of neighbors of a field
	 */
	public static void testAufgabe4() {
		int[][] data = generateTestArray2();
		setMineNeighbors(data);
		showEmpty(data, new int[] {7, 7});
		printArray(data);
	}

	/**
	 * Uncovers all connected fields
	 */
	public static void testAufgabe6() {
		int[][] data = generateTestArray2();
		setMineNeighbors(data);
		System.out.println(uncover(data, new int[] {4, 0}));
		System.out.println(uncover(data, new int[] {1, 1}));
		System.out.println(uncover(data, new int[] {4, 2}));
		System.out.println(uncover(data, new int[] {0, 3}));
		System.out.println(uncover(data, new int[] {2, 4}));
		System.out.println(uncover(data, new int[] {4, 4}));
		System.out.println(uncover(data, new int[] {0, 7}));
		System.out.println(uncover(data, new int[] {3, 7}));
		System.out.println(uncover(data, new int[] {7, 7}));
		printArray(data);
		
	}
	
	/**
	 * Helper for "Aufgabe 3" 
	 * @return Array with numbers
	 */
	public static int[][] generateTestArray1() {
		int[][] data = new int[8][8];
		int val = 0;
		for (int y = 0; y < data.length; y++) {
			for (int x = 0; x < data[y].length; x++) {
				data[y][x] = val++;
			}
		}
		return data;
	}
	

	/**
	 * Helper for "Aufgabe 4" and "Aufgabe 6" 
	 * @return Array with hidden mines
	 */
	public static int[][] generateTestArray2() {
		int[][] data = new int[8][8];
		data[0][0] = MINE_HIDE;
		data[1][4] = MINE_HIDE;
		data[2][3] = MINE_HIDE;
		data[2][5] = MINE_HIDE;
		data[3][4] = MINE_HIDE;
		data[4][0] = MINE_HIDE;
		data[5][3] = MINE_HIDE;
		data[5][4] = MINE_HIDE;
		data[6][3] = MINE_HIDE;
		data[6][4] = MINE_HIDE;
		return data;
	}

	/**
	 * Helper for "Aufgabe 4" for using the direction array
	 * @param data Data field holding the game field with the numbers.
	 * @param pos Position from where the neighbors must be accessed for counting.
	 * @return Sum of the values of the neighbor fields.
	 */
	public static int countNeighbors(int[][] data, int[] pos) {
		int sum = 0;
		int[] search = new int[2]; // position vector where the actual search should be done
		int maxX = (data.length == 0 ? 0 : data[0].length); // determine the width of the field in a safe way
		                                                    // so that it will not crash, even if no row exists
		int maxY = data.length;
		for (int[] dir : DIR) { // go through all directions in the DIR array and use it for accessing the neighbors
			search[X] = pos[X];
			search[Y] = pos[Y];
			if (move(search, dir, maxX, maxY)) {  // if the move attempt exceeds the field, it will return false
				sum += data[search[Y]][search[X]];
			}
		}
		return sum;
	}

	/**
	 * Moves the position in the pos vector to the direction placed in the dir vector.
	 * @param pos Vector that points to a position in a X/Y field and therefore only positive numbers. The values will be changed according to d
	 * @param dir Direction vector. The values are added to the pos vector and therefore positive and negative values.
	 * @param maxX First x-value that exceeds the field.
	 * @param maxY First y-value that exceeds the field.
	 * @return true if pos was changed and false if the movement would have exceeded the field.
	 */
	public static boolean move(int[] pos, int[] dir, int maxX, int maxY) {
		if (pos[X] + dir[X] >= maxX) {
			return false;
		}

		if (pos[X] + dir[X] < 0) {
			return false;
		}

		if (pos[Y] + dir[Y] >= maxY) {
			return false;
		}

		if (pos[Y] + dir[Y] < 0) {
			return false;
		}
		pos[X] += dir[X];
		pos[Y] += dir[Y];

		return true;
	}	

	/**
	 * Because in the hidden status the number of the bordering mines is negative, the method decreases every 
	 * neighbor of the given position, assuming the method is only called if pos points to a mine.
	 * @param data game field
	 * @param pos Position of the current mine for adjusting the bordering fields.
	 */
	public static void decreaseNeighbors(int[][] data, int[] pos) {
		int[] search = new int[2];
		for (int[] dir : DIR) { // handle all directions in DIR
			search[X] = pos[X];
			search[Y] = pos[Y];
			if (move(search, dir, (data.length == 0 ? 0 : data[0].length), data.length)) {
				if (data[search[Y]][search[X]] <= 0 && data[search[Y]][search[X]] > MINE_HIDE) { // handle neighbors only, if they are hidden or empty
					data[search[Y]][search[X]]--;
				}
			}
		}
	}
	
	/**
	 * Method searches every field for hidden mines. If one is found, all neighbors are handled by decreaseNeighbors
	 * @param data
	 */
	public static void setMineNeighbors(int[][] data) {
		int[] pos = new int[2];
		for (int y = 0; y < data.length; y++) {
			pos[Y] = y;
			for (int x = 0; x < data[y].length; x++) {
				pos[X] = x;
				if (data[y][x] == MINE_HIDE) {
					decreaseNeighbors(data, pos);
				}
			}
		}
	}

	/**
	 * Recursive method to uncover all connected empty fields including mine neighbors.
	 * @param data game field
	 * @param pos Position from where the uncovering should start.
	 */
	public static void showEmpty(int[][] data, int[] pos) {
		int[] search = new int[2];
		for (int[] dir : DIR2) { // handle all fields around and on pos
			search[X] = pos[X];
			search[Y] = pos[Y];
			if (move(search, dir, (data.length == 0 ? 0 : data[0].length), data.length)) { // only access valid fields
				if (data[search[Y]][search[X]] == EMPTY_HIDE) { // if a field is empty and has no mine neighbor
					data[search[Y]][search[X]] = EMPTY_SHOW; // show it
					showEmpty(data, search); // handle its neighbors accordingly
				} else if (data[search[Y]][search[X]] < 0 && data[search[Y]][search[X]] != MINE_HIDE) { // if field is a mine neighbor
					data[search[Y]][search[X]] *= -1;  // uncover it with the positive number and handle no neighbors - might be a mine!
				}
			}		
		}
	}

	/**
	 * Game action - if a user wants to uncover a field, it might be a mine -> user looses or no mine.
	 * In this case user might have won (if the hidden fields are exclusively mines - so all empty fields have been found). 
	 * @param data game field
	 * @param pos Position to uncover
	 * @return Constant, indicating game status.
	 */
	public static int uncover(int[][] data, int[] pos) {
		if (data[pos[Y]][pos[X]] == MINE_HIDE) {
			showAllFields(data);
			return GAME_USER_LOOSES;
		}
		showEmpty(data, pos);
		if (onlyMines(data)) {
			return GAME_USER_WINS;
		}
		
		return GAME_RUNNING;
	}
	
	/**
	 * Returns false, if at least one hidden field is no mine.
	 * @param data game field
	 * @return True, if every hidden field is a mine.
	 */
	public static boolean onlyMines(int[][] data) {
		for (int y = 0; y < data.length; y++) {
			for (int x = 0; x < data[y].length; x++) {
				if (data[y][x] <= 0 && data[y][x] != MINE_HIDE) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Uncovers every mine and sets every other field to blank.
	 * @param data game field
	 */
	public static void showAllFields(int[][] data) {
		for (int y = 0; y < data.length; y++) {
			for (int x = 0; x < data[y].length; x++) {
				if (data[y][x] == MINE_HIDE ) {
					data[y][x] = MINE_SHOW;
				} else {
					data[y][x] = EMPTY_SHOW;
				}
			}
		}
	}

	/**
	 * Creates a field with the given dimensions and the given number of mines
	 * which are distributed randomly.
	 * @param xSize Width of game field
	 * @param ySize Height of game field
	 * @param mines Number of mines to be placed on the field.
	 * @return game field with mines
	 */
	public static int[][] generateField(int xSize, int ySize, int mines) {
		int[][] data = new int[ySize][xSize]; // game field that will be returned
		ArrayList<int[]> fields = new ArrayList<>(xSize * ySize); // dynamic field for preventing to place a mine 
		                                                          // twice on the same spot.

		// fill the fields ArrayList with every possible position on the game field
		for (int y = 0; y < data.length; y++) {
			for (int x = 0; x < data[y].length; x++) {
				fields.add(new int[] {x, y});
			}
		}
		Random myRnd = new Random();
		// Every mine will now be placed
		for (int i = 0; i < mines; i++) {
			int index = myRnd.nextInt(fields.size());  // get a random position in the ArrayList, which is equal to a random position
			                                           // in the game field
			int[] pos = fields.get(index);
			data[pos[Y]][pos[X]] = MINE_HIDE; // place a mine on the field
			fields.remove(index); // remove current position to prevent another mine to be placed there
			if (fields.size() == 0) { // if someone wants to place more mines than fields, stop if all fields are occupied by a mine
				return data;
			}
		}
		
		return data;
	}

	/**
	 * Expects an input of x and y value. If the user wants to point to the column 4 in the row 6
	 * the input string is expected to be 4,6. There is no error handling implemented.
	 * @param input String representation of the position
	 * @return 
	 */
	public static int[] parseInput(String input) {
		String[] data = input.split(","); // no Errorhandling
		return new int[] {Integer.parseInt(data[0]), Integer.parseInt(data[1])};
	}
	
	/**
	 * Prints the game field. Every hidden field is printed as an "X" empty non hidden fields with a space
	 * and non hidden mine neighbors with the number of mines as a neighbor.
	 * @param values
	 */
	public static void printArray(int[][] values) {
		for (int y = 0; y < values.length; y++) {
			for (int x = 0; x < values[y].length; x++) {
				if (values[y][x] > 0) {
					if (values[y][x] == 10) {
						System.out.print(" ");
					} else {
						System.out.print(values[y][x]);
					}
				} else {
					System.out.print("X");
				}
			}
			System.out.println();
		}
	}

	/**
	 * Game control logic. Note, that in this constellation the first uncovered field 
	 * can be a mine.
	 */
	public static void game() {
		int[][] field = generateField(10, 8, 5);
		setMineNeighbors(field);
		boolean gameRunning = true;
		do {
			System.out.println();
			printArray(field);
			int[] pos = parseInput(JOptionPane.showInputDialog("Eingabe Auswahl"));
			switch(uncover(field, pos)) {
			case GAME_USER_LOOSES:
				System.out.println("Verloren");
				gameRunning = false;
				break;
			case GAME_USER_WINS:
				System.out.println("Gewonnen");
				gameRunning = false;
				break;
			}
		} while(gameRunning);
	}

}
