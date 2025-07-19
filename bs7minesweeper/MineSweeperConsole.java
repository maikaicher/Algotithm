package bs7minesweeper;

import javax.swing.JOptionPane;

public class MineSweeperConsole {

	public static void main(String[] args) {
		doGame(10, 10, 10);
	}

	/**
	 * Complete game loop
	 * @param sizeX width of the game field
	 * @param sizeY height of the game field
	 * @param mines number of mines to be placed
	 */
	private static void doGame(int sizeX, int sizeY, int mines) {
		MineSweeperModel model = new MineSweeperModel(sizeX, sizeY, mines);

		do {
			System.out.println();
			printField(model.getField());

			String input = JOptionPane.showInputDialog("Eingabe Auswahl");
			if (input == null) { // in case the user quits
				model.showAllFields();
				printField(model.getField());
				return;
			}
			int[] pos = parseInput(input, model);
			switch(model.uncover(pos)) {
			case MineSweeperModel.GAME_USER_LOOSES:
				model.showAllFields();
				printField(model.getField());
				System.out.println("Verloren");
				break;
			case MineSweeperModel.GAME_USER_WINS:
				model.showAllFields();
				printField(model.getField());
				System.out.println("Gewonnen");
				break;
			}
		} while(model.gameRunning());
	}

	/**
	 * Parses the input string including simple error handling. Any non valid number
	 * will be replaced with 0. The expected string is a comma separated pair of X and Y.
	 * For example the position X=3 and Y=8 must be entered as "3,8".
	 * @param input String with the coordinates.
	 * @param model Reference to the game model in order to retrieve maximum width and height values.
	 * @return A new position vector.
	 */
	public static int[] parseInput(String input, MineSweeperModel model) {
		boolean error = false; // for controlling the user info on the console
		int[] pos = new int[2];  // default values will be 0,0
		String[] data = input.split(","); 
		
		if (data.length == 2) {
			try {
				int value = Integer.parseInt(data[0]);
				if (value < model.getWidth() && value >= 0) {
					pos[MineSweeperModel.X] = value;					
				} else {
					error = true;
				}
				value = Integer.parseInt(data[1]);
				if (value < model.getHeight() && value >= 0) {
					pos[MineSweeperModel.Y] = value;
				} else {
					error = true;
				}
			} catch (Exception e) {
				error = true;
			}
		} else {
			error = true;
		}
		if (error) {
			System.out.println("Falsche Eingabe - verwende " + pos[MineSweeperModel.X] + "," + pos[MineSweeperModel.Y] + " anstelle!");
		}
		return pos;
	}

	/**
	 * Prints the field with simplified row and col index values
	 * @param values game field
	 */
	public static void printField(int[][] values) {
		System.out.print("  ");
		for (int x = 0; x < values[0].length; x++) {
			System.out.print(x%10);
		}
		System.out.println();
		System.out.print(" +");
		for (int x = 0; x < values[0].length; x++) {
			System.out.print("-");
		}
		System.out.println("+");
		for (int y = 0; y < values.length; y++) {
			System.out.print((y%10) + "|");
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
			System.out.println("|" + (y%10));
		}
		System.out.print(" +");
		for (int x = 0; x < values[0].length; x++) {
			System.out.print("-");
		}
		System.out.println("+");
		System.out.print("  ");
		for (int x = 0; x < values[0].length; x++) {
			System.out.print(x%10);
		}
		System.out.println();
	}

}
