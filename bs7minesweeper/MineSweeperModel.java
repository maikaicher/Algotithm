package bs7minesweeper;

import java.util.ArrayList;
import java.util.Random;

/**
 * Minesweeper game logic.
 * @author Maik Aicher
 *
 */
public class MineSweeperModel {
	// Constants for accessing the position vector data
	public static final int X = 0;
	public static final int Y = 1;
	
	// movement directions towards the eight neighbor fields
	private static final int[] N  = { 0, -1};
	private static final int[] NE = { 1, -1};
	private static final int[] E  = { 1,  0};
	private static final int[] SE = { 1,  1};
	private static final int[] S  = { 0,  1};
	private static final int[] SW = {-1,  1};
	private static final int[] W  = {-1,  0};
	private static final int[] NW = {-1, -1};
	private static final int[] NONE = {0, 0};
	
	// all directions from a single point including own location.
	private static final int[][] DIR  = {N, NE, E, SE, S, SW, W, NW, NONE};

	public static final int EMPTY_HIDE = 0;  // field is empty, but hidden
	public static final int EMPTY_SHOW = 10;  // field is empty, but not hidden
	public static final int MINE_HIDE = -9;  // field has a mine, but is hidden
	public static final int MINE_SHOW = 9;  // field has a mine, but is not hidden
	
	// for controlling the game
	public static final int GAME_STOP = 0;
	public static final int GAME_RUNNING = 1;
	public static final int GAME_USER_WINS = 2;
	public static final int GAME_USER_LOOSES = 3;
	
	private int[][] field; // the game field
	
	private int sizeX; // game field width
	private int sizeY; // game field height
	private int mines; // over all number of mines in the field
	private int gameStatus = GAME_STOP; // for game status control
	
	public MineSweeperModel(int sizeX, int sizeY, int mines) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.mines = mines;
		
		
		// this functionality was moved to uncover() in order to prevent hitting a mine at the first move
		//generateField();
		//setMineNeighbors();
		//gameStatus = GAME_RUNNING;

		generateField(null); // temporary game field in case the user quits before first move
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
	 * @param pos Position of the current mine for adjusting the bordering fields.
	 */
	private void decreaseNeighbors(int[] pos) {
		int[] search = new int[2];
		for (int[] dir : DIR) { // handle all directions in DIR
			search[X] = pos[X];
			search[Y] = pos[Y];
			if (move(search, dir, sizeX, sizeY)) {
				if (field[search[Y]][search[X]] <= 0 && field[search[Y]][search[X]] > MINE_HIDE) { // handle neighbors only, if they are hidden or empty
					field[search[Y]][search[X]]--;
				}
			}
		}
	}
	
	/**
	 * Method searches every field for hidden mines. If one is found, all neighbors are handled by decreaseNeighbors
	 */
	private void setMineNeighbors() {
		int[] pos = new int[2];
		for (int y = 0; y < sizeY; y++) {
			pos[Y] = y;
			for (int x = 0; x < sizeX; x++) {
				pos[X] = x;
				if (field[y][x] == MINE_HIDE) {
					decreaseNeighbors(pos);
				}
			}
		}
	}

	/**
	 * Recursive method to uncover all connected empty fields including mine neighbors.
	 * @param pos Position from where the uncovering should start.
	 */
	private void showEmpty(int[] pos) {
		int[] search = new int[2];
		for (int[] dir : DIR) { // handle all fields around and on pos
			search[X] = pos[X];
			search[Y] = pos[Y];
			if (move(search, dir, sizeX, sizeY)) {
				if (field[search[Y]][search[X]] == EMPTY_HIDE) { // if a field is empty and has no mine neighbor
					field[search[Y]][search[X]] = EMPTY_SHOW; // show it
					showEmpty(search); // handle its neighbors accordingly
				} else if (field[search[Y]][search[X]] < 0 && field[search[Y]][search[X]] != MINE_HIDE) { // if field is a mine neighbor
					field[search[Y]][search[X]] *= -1;  // uncover it with the positive number and handle no neighbors - might be a mine!
				}
			}		
		}
	}

	/**
	 * Game action - if a user wants to uncover a field, it might be a mine -> user looses or no mine.
	 * In this case user might have won (if the hidden fields are exclusively mines - so all empty fields have been found). 
	 * @param pos Position to uncover
	 * @return Constant, indicating game status.
	 */
	public int uncover(int[] pos) {
		if (gameStatus == GAME_STOP) {
			generateField(pos);
			setMineNeighbors();
			gameStatus = GAME_RUNNING;
		}
		if (field[pos[Y]][pos[X]] == MINE_HIDE) {
			showAllFields();
			gameStatus = GAME_USER_LOOSES;
			return gameStatus;
		}
		showEmpty(pos);
		if (onlyMines()) {
			gameStatus = GAME_USER_WINS;
			return gameStatus;
		}
		
		return gameStatus;
	}
	
	/**
	 * Returns false, if at least one hidden field is no mine.
	 * @return True, if every hidden field is a mine.
	 */
	private boolean onlyMines() {
		for (int y = 0; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {
				if (field[y][x] <= 0 && field[y][x] != MINE_HIDE) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Uncovers every mine and sets every other field to blank.
	 */
	public void showAllFields() {
		for (int y = 0; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {
				if (field[y][x] == MINE_HIDE ) {
					field[y][x] = MINE_SHOW;
				} else {
					field[y][x] = EMPTY_SHOW;
				}
			}
		}
	}

	/**
	 * Creates a field with the given dimensions and the given number of mines
	 * which are distributed randomly, guaranteeing no mine is on the block position
	 * @param block position where no mine should be placed
	 */
	private void generateField(int[] block) {
		field = new int[sizeY][sizeX];
		ArrayList<int[]> fields = new ArrayList<>(sizeX * sizeY); // dynamic field for preventing to place a mine 
                                                                  // twice on the same spot.

		// fill the fields ArrayList with every possible position on the game field
		for (int y = 0; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {				
				if (block == null || x != block[X] && y != block[Y]) { // avoid ot add the blocking position
					fields.add(new int[] {x, y});
				}
			}
		}
		Random myRnd = new Random();
		// Every mine will now be placed
		for (int i = 0; i < mines; i++) {
			int index = myRnd.nextInt(fields.size());  // get a random position in the ArrayList, which is equal to a random position
                                                       // in the game field
			int[] pos = fields.get(index);
			field[pos[Y]][pos[X]] = MINE_HIDE; // place a mine on the field
			fields.remove(index); // remove current position to prevent another mine to be placed there
			if (fields.size() == 0) { // if someone wants to place more mines than fields, stop if all fields are occupied by a mine
				return;
			}
		}
	}
	
	/**
	 * Getter of the field
 	 * @return the game field
	 */
	public int[][] getField() {
		return field;
	}
	
	/**
	 * Getter of game field width
	 * @return
	 */
	public int getWidth() {
		return sizeX;
	}
	
	/**
	 * Getter of game field height
	 * @return
	 */
	public int getHeight() {
		return sizeY;
	}
	
	/**
	 * True, if the game is not over yet
	 * @return true in case of game is running, false if game over
	 */
	public boolean gameRunning() {
		return gameStatus == GAME_RUNNING;
	}
}
