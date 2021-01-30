//Jorge Luis Manzo Zúniga	A01633991
//Luis Alberto Bodart Valdez	A01635000
//Michel Lujano Velázquez	A01636172

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

public class TileMap extends JPanel {

	// ID for each tile type
	private final static int EMPTY = -1, BUILDINGS = 0, MORE = 16, MONUMENTS = 32, TRAIN = 48, TEC = 49, HOUSES = 50,
			ORIENTATION = 92, TREES = 108, MEDIUM = 144, BIG = 148, STREET = 152;
	// Size of each tile
	private final static int TILE_WIDTH = 64, TILE_HEIGHT = 32;
	// Array of tile type to create
	private static int[][] tile_type;

	// Map
	private Nodes[][] map;

	// Images
	private BufferedImage concrete, grass;
	private BufferedImage streetSheet;
	private BufferedImage buildings1Sheet, buildings2Sheet, monumentsSheet, train, tec, housesSheet,
			housesOrientationSheet, treesSheet, mediumTreesSheet, bigTreesSheet;
	private BufferedImage carSheet;

	// Sprites
	private BufferedImage[] streets;
	private BufferedImage[] buildings1, buildings2, monuments, houses, housesOrientation, trees, mediumTrees, bigTrees;
	private BufferedImage[] car;

	private static int groundType = -1;

	private ActionListener action;
	private Timer looper;

	// Car animations
	private Car car_up, car_down, car_left, car_right;
	private Car currentAnimation;

	// Car position
	private int x, y, px, py;

	private String directionCar;

	// Smooth movement (interpolation)
	private boolean isMoving;

	private float speed;

	private int x_start, y_start, x_end, y_end;

	private ArrayList<Nodes> path;
	private double cost;

	private int mouse_row, mouse_col;

	private boolean mouseInRange;

	public TileMap() {
		this.setBackground(new Color(200, 200, 200));
		// Load ground types
		concrete = loadImage("/res/concrete-64x32.png").getSubimage(0, 0, 64, 32);
		grass = loadImage("/res/grass-64x32.png").getSubimage(0, 0, 64, 32);

		// Load sprites sheets
		streetSheet = loadImage("/res/street-64x32_16.png");
		buildings1Sheet = loadImage("/res/buildings1-128X128_16.png");
		buildings2Sheet = loadImage("/res/buildings2-128x128_16.png");
		monumentsSheet = loadImage("/res/monuments-128x128_16.png");
		train = loadImage("/res/train-256x160.png").getSubimage(0, 0, 256, 160);
		tec = loadImage("/res/tec-448x256.png").getSubimage(0, 0, 448, 256);
		housesSheet = loadImage("/res/houses-64x64_42.png");
		housesOrientationSheet = loadImage("/res/housesOrientation-64x64_16.png");
		treesSheet = loadImage("/res/trees-64x32_36.png");
		mediumTreesSheet = loadImage("/res/trees-128x32_4.png");
		bigTreesSheet = loadImage("/res/trees-128x64_4.png");

		// Load car sheet
		carSheet = loadImage("/res/redVehicles-32x32_8.png");

		// Create sprites
		streets = cropImage(streetSheet, 0, 0, 64, 32, 3, 16);
		buildings1 = cropImage(buildings1Sheet, 0, 0, 128, 128, 5, 16);
		buildings2 = cropImage(buildings2Sheet, 0, 0, 128, 128, 5, 16);
		monuments = cropImage(monumentsSheet, 0, 0, 128, 128, 5, 16);
		houses = cropImage(housesSheet, 0, 0, 64, 64, 11, 42);
		trees = cropImage(treesSheet, 0, 0, 64, 32, 11, 36);
		mediumTrees = cropImage(mediumTreesSheet, 0, 0, 128, 32, 3, 4);
		bigTrees = cropImage(bigTreesSheet, 0, 0, 128, 64, 3, 4);

		car = cropImage(carSheet, 2, 0, 32, 32, 7, 8);

		// Create animation
		car_up = new Car(car, 4);
		car_down = new Car(car, 0);
		car_left = new Car(car, 2);
		car_right = new Car(car, 6);

		currentAnimation = car_right;
		directionCar = "right";

		x = 7;
		y = 5;
		px = x * TILE_WIDTH / 2 - y * TILE_WIDTH / 2;
		py = y * TILE_HEIGHT / 2 + x * TILE_HEIGHT / 2;

		isMoving = false;
		speed = 0;

		loadCity();

		map = new Nodes[tile_type.length][tile_type[0].length];

		path = new ArrayList<>();

		mouseInRange = false;

		for (int row = 0; row < map.length; row++) {
			for (int col = 0; col < map[0].length; col++) {
				int tp = tile_type[row][col];
				if (tp >= STREET) {
					map[row][col] = new Nodes(true, row, col, tp);
				} else {
					map[row][col] = new Nodes(false, row, col, tp);
				}
			}
		}

		action = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent a) {
				update();
				repaint();
			}
		};

		looper = new Timer(1000 / 60, action);
		looper.start();
	}

	// Load Images
	private static BufferedImage loadImage(String path) {
		try {
			return ImageIO.read(TileMap.class.getResource(path));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return null;
	}

	// Crop Images
	private BufferedImage[] cropImage(BufferedImage sheet, int row, int col, int w, int h, int tCol, int size) {
		BufferedImage[] frames = new BufferedImage[size];
		for (int i = 0; i < size; i++) {
			frames[i] = sheet.getSubimage(col * w, row * h, w, h);
			if (col == tCol) {
				col = -1;
				row++;
			}
			col++;
		}
		return frames;
	}

	// Load city from file
	private static void loadCity() {
		try {
			JFileChooser fc = new JFileChooser("../");
			fc.setDialogTitle("Choose a txt file to load city");
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(new FileNameExtensionFilter(".txt", "txt"));
			fc.showOpenDialog(null);
			fc.setVisible(true);

			File f = fc.getSelectedFile();
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line = br.readLine();

			ArrayList<String> array = new ArrayList<>();
			int sizeR = 0, sizeC = 0;

			if (!line.startsWith("{")) {
				System.out.println(f.getName() + " is not a Map");
				throw new NullPointerException();
			}

			System.out.println("Loading" + f.getName());

			while (line != null) {
				String[] strA = line.split("[\\s\\{\\,\\}]");
				for (int i = 0; i < strA.length; i++) {
					if (!strA[i].isEmpty()) {
						array.add(strA[i]);
						sizeR++;
					}
				}
				sizeC++;
				line = br.readLine();
			}
			sizeR = (int) Math.sqrt(sizeR);
			br.close();

			String[][] ttMap = new String[sizeR][sizeC];
			for (int r = 0; r < ttMap.length; r++) {
				for (int c = 0; c < ttMap[0].length; c++) {
					ttMap[r][c] = array.remove(0);
				}
			}
			createTileTypeMap(ttMap, sizeR, sizeC);
			String[] gType = { "Concrete", "Grass" };
			groundType = JOptionPane.showOptionDialog(null, "Choose ground type", "Ground", JOptionPane.DEFAULT_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, gType, gType[0]);
			System.out.println("Loaded Completed");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (NullPointerException npe) {
			loadDefault();
		}
	}

	// Load default city
	private static void loadDefault() {
		try {
			File f = new File("default.txt");
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line = br.readLine();

			ArrayList<String> array = new ArrayList<>();
			int sizeR = 0, sizeC = 0;

			System.out.println("Loading " + f.getName());

			while (line != null) {
				String[] strA = line.split("[\\s\\{\\,\\}]");
				for (int i = 0; i < strA.length; i++) {
					if (!strA[i].isEmpty()) {
						array.add(strA[i]);
						sizeR++;
					}
				}
				sizeC++;
				line = br.readLine();
			}
			sizeR = (int) Math.sqrt(sizeR);
			br.close();

			String[][] ttMap = new String[sizeR][sizeC];
			for (int r = 0; r < ttMap.length; r++) {
				for (int c = 0; c < ttMap[0].length; c++) {
					ttMap[r][c] = array.remove(0);
				}
			}
			createTileTypeMap(ttMap, sizeR, sizeC);
			groundType = 1;
			System.out.println("Loaded Completed");
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	// Create Tile Type Map
	private static void createTileTypeMap(String[][] map, int row, int col) {
		tile_type = new int[row][col];
		for (int r = 0; r < map.length; r++) {
			for (int c = 0; c < map[0].length; c++) {
				String type = map[r][c].replaceAll("\\d+\\W+", "");
				String tile = map[r][c].replaceAll("\\D+", "");
				if (type.contains("EMPTY")) {
					tile_type[r][c] = EMPTY;
				} else if (type.contains("BUILDINGS")) {
					tile_type[r][c] = Integer.parseInt(tile) + BUILDINGS;
				} else if (type.contains("MORE")) {
					tile_type[r][c] = Integer.parseInt(tile) + MORE;
				} else if (type.contains("EXCLUSIVE")) {
					tile_type[r][c] = Integer.parseInt(tile) + MONUMENTS;
				} else if (type.contains("TRAIN")) {
					tile_type[r][c] = TRAIN;
				} else if (type.contains("TEC")) {
					tile_type[r][c] = TEC;
				} else if (type.contains("HOUSES")) {
					tile_type[r][c] = Integer.parseInt(tile) + HOUSES;
				} else if (type.contains("TREES")) {
					tile_type[r][c] = Integer.parseInt(tile) + TREES;
				} else if (type.contains("MEDIUM")) {
					tile_type[r][c] = Integer.parseInt(tile) + TREES;
				} else if (type.contains("BIG")) {
					tile_type[r][c] = Integer.parseInt(tile) + TREES;
				} else if (type.contains("STREET")) {
					tile_type[r][c] = Integer.parseInt(tile) + STREET;
				}
			}
		}
	}

	// Update map
	private void update() {
		if (isMoving && !path.isEmpty()) {
			// Interpolate car to the next node in the path
			x_start = (int) (x * TILE_WIDTH / 2 - y * TILE_WIDTH / 2 + 1.500 * x);
			y_start = (int) (y * TILE_HEIGHT / 2 + x * TILE_HEIGHT / 2 + 1.500 * y);

			int path_x = path.get(0).getCol();
			int path_y = path.get(0).getRow();

			x_end = (int) (path_x * TILE_WIDTH / 2 - path_y * TILE_WIDTH / 2 + 1.500 * x);
			y_end = (int) (path_y * TILE_HEIGHT / 2 + path_x * TILE_HEIGHT / 2 + 1.500 * y);

			px = linearInterpolation(speed, x_start, x_end);
			py = linearInterpolation(speed, y_start, y_end);

			speed += 0.06;

			if (speed > 1) {
				speed = 0;
				x = path_x;
				y = path_y;
				path.remove(0);
				setAnimation();
				if (path.isEmpty()) {
					isMoving = false;
					cost = 0;
				}
			}
		}

		float x = Control.mouse_X - getWidth() / 2;
		float y = Control.mouse_Y - getHeight() / 2 - 27 + map.length * TILE_HEIGHT / 2;

		// Convert mouse coordinates to tile coordinates
		mouse_row = (int) (y / TILE_HEIGHT - x / TILE_WIDTH);
		mouse_col = (int) (x / TILE_WIDTH + y / TILE_HEIGHT);

		if (mouse_row >= 0 && mouse_row < map.length && mouse_col >= 0 && mouse_col < map[0].length) {
			mouseInRange = true;
		} else {
			mouseInRange = false;
		}
		if (Control.leftClick && mouseInRange && map[mouse_row][mouse_col].getTile() >= STREET) {
			requestPath(map[mouse_row][mouse_col]);
		}
	}

	// Linear Interpolation function
	private int linearInterpolation(float t, int start, int end) {
		return (int) (start + ((end - start) * t));
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.translate(getWidth() / 2, getHeight() / 2 - map.length * TILE_HEIGHT / 2);

		for (int row = 0; row < map.length; row++) {
			for (int col = 0; col < map[0].length; col++) {
				// Draw Ground
				if (groundType == -1 || groundType == 0) {
					drawTile(g, concrete, col, row);
				} else if (groundType == 1) {
					drawTile(g, grass, col, row);
				}

				int tile = map[row][col].getTile();

				if (tile >= STREET) {
					// Draw streets
					drawTile(g, streets[tile - STREET], col, row);
				}
			}
		}

		// Draw car
		g.drawImage(currentAnimation.getCurrentFrame(), px - TILE_WIDTH / 2 + 2, py - 16, null);

		for (int row = 0; row < map.length; row++) {
			for (int col = 0; col < map[0].length; col++) {
				int tile = map[row][col].getTile();
				if (tile < STREET && tile > EMPTY) {
					if (tile >= BIG) {
						// Draw streets
						drawTile(g, trees[tile - BIG], col, row);
					} else if (tile >= MEDIUM) {
						// Draw streets
						drawTile(g, trees[tile - MEDIUM], col, row);
					} else if (tile >= TREES) {
						// Draw streets
						drawTile(g, trees[tile - TREES], col, row);
					} else if (tile >= HOUSES) {
						// Draw houses
						drawTile(g, houses[tile - HOUSES], col, row);
					} else if (tile == TEC) {
						// Draw train
						drawTile(g, tec, col, row);
					} else if (tile == TRAIN) {
						// Draw train
						drawTile(g, train, col, row);
					} else if (tile >= MONUMENTS) {
						// Draw exclusive buildings
						drawTile(g, monuments[tile - MONUMENTS], col, row);
					} else if (tile >= MORE) {
						// Draw buildings
						drawTile(g, buildings2[tile - MORE], col, row);
					} else if (tile >= BUILDINGS) {
						// Draw buildings
						drawTile(g, buildings1[tile - BUILDINGS], col, row);
					}
				}
			}
		}

		for (int i = 0; i < path.size(); i++) {
			Nodes n = path.get(i);
			// Draw path
			drawTile(g, Color.CYAN, n.getCol(), n.getRow());
		}

		// Draw path & cost
		g.setColor(Color.WHITE);
		if (!path.isEmpty()) {
			g.drawString("Path: " + path.toString(), -480, -80);
		}
		DecimalFormat df = new DecimalFormat("0.00");
		if (cost >= 0) {
			g.drawString("Cost: " + df.format(cost) + " km", -480, -60);
		}

		// Draw selectable tile if is street
		if (mouseInRange) {
			int type = map[mouse_row][mouse_col].getTile();
			if (type < STREET || mouse_row == y && mouse_col == x) {
				drawTile(g, Color.RED, mouse_col, mouse_row);
			} else {
				drawTile(g, Color.GREEN, mouse_col, mouse_row);
			}
		}
	}

	// Draw tile image
	private void drawTile(Graphics g, BufferedImage tile, int x, int y) {
		int iso_x = x * TILE_WIDTH / 2 - y * TILE_WIDTH / 2;
		int iso_y = y * TILE_HEIGHT / 2 + x * TILE_HEIGHT / 2;

		int diff_y = tile.getHeight() - TILE_HEIGHT;

		g.drawImage(tile, iso_x - TILE_WIDTH / 2, iso_y - diff_y, null);
	}

	// Draw mouse
	private void drawTile(Graphics g, Color color, int x, int y) {
		int px = x * TILE_WIDTH / 2 - y * TILE_WIDTH / 2;
		int py = y * TILE_HEIGHT / 2 + x * TILE_HEIGHT / 2;

		int[] xPoints = new int[] { px, px + TILE_WIDTH / 2, px, px - TILE_WIDTH / 2 };
		int[] yPoints = new int[] { py, py + TILE_HEIGHT / 2, py + TILE_HEIGHT, py + TILE_HEIGHT / 2 };

		Graphics2D g2d = (Graphics2D) g;

		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
		g2d.setColor(color);
		g2d.fillPolygon(xPoints, yPoints, 4);
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
	}

	private void requestPath(Nodes target) {
		path = findPath(map[y][x], target);
		try {
			if (path.isEmpty()) {
				return;
			}
			isMoving = true;
			setAnimation();
		} catch (NullPointerException npe) {
			path = new ArrayList<>();
			Control.leftClick = false;
			cost = 0;
			JOptionPane.showMessageDialog(null, "Path not found to target", "Path not found",
					JOptionPane.PLAIN_MESSAGE);
		}
	}

	private void setAnimation() {
		if (path.isEmpty()) {
			if (directionCar.equals("up")) {
				currentAnimation = car_up;
			} else if (directionCar.equals("down")) {
				currentAnimation = car_down;
			} else if (directionCar.equals("left")) {
				currentAnimation = car_left;
			} else if (directionCar.equals("right")) {
				currentAnimation = car_right;
			}
			return;
		}

		int posX = path.get(0).getCol();
		int posY = path.get(0).getRow();

		if (y - posY > 0) {
			currentAnimation = car_up;
			directionCar = "up";
		} else if (y - posY < 0) {
			currentAnimation = car_down;
			directionCar = "down";
		} else if (x - posX > 0) {
			currentAnimation = car_left;
			directionCar = "left";
		} else if (x - posX < 0) {
			currentAnimation = car_right;
			directionCar = "right";
		}
	}

	// BFS Breadth Search First Algorithm
	private ArrayList<Nodes> findPath(Nodes start, Nodes target) {
		for (int r = 0; r < map.length; r++) {
			for (int c = 0; c < map[0].length; c++) {
				map[r][c].setVisited(false);
			}
		}

		Queue<Nodes> q = new LinkedList<>();
		ArrayList<Nodes> prev = new ArrayList<>();
		q.add(start);
		cost = 0;
		start.setVisited(true);

		while (!q.isEmpty()) {
			Nodes temp = q.remove();
			if (temp.equals(target)) {
				prev.add(temp);
				for (Nodes na : prev) {
					na.setVisited(false);
				}
				return retracePath(prev);
			}
			ArrayList<Nodes> myNeighbours = getNeighbours(temp);
			for (Nodes newN : myNeighbours) {
				if (!newN.isVisited() && newN.isStreet() && newN.getTile() >= STREET) {
					q.add(newN);
					newN.setVisited(true);
					prev.add(temp);
				}
			}
		}
		// return null;
		return prev;
	}

	// Path that the car follow
	private ArrayList<Nodes> retracePath(ArrayList<Nodes> prev) {
		Nodes n = prev.get(prev.size() - 1);
		int counter = prev.size() - 2;
		while (n != prev.get(0)) {
			boolean same = false;
			if ((n.getRow() > 0 && n.getRow() < (map[0].length - 1))
					&& map[n.getRow() - 1][n.getCol()].equals(prev.get(counter))) {
				same = true;
			} else if ((n.getRow() > 0 && n.getRow() < (map[0].length - 1))
					&& map[n.getRow() + 1][n.getCol()].equals(prev.get(counter))) {
				same = true;
			} else if (map[n.getRow()][n.getCol() - 1].equals(prev.get(counter))) {
				same = true;
			} else if (map[n.getRow()][n.getCol() + 1].equals(prev.get(counter))) {
				same = true;
			}
			if (same) {
				n = prev.get(counter);
			} else {
				prev.remove(counter);
			}
			counter--;
		}
		prev.remove(0);
		cost = prev.size();
		return prev;
	}

	// Neighbors of node
	private ArrayList<Nodes> getNeighbours(Nodes n) {
		ArrayList<Nodes> neighbours = new ArrayList<>();
		if (n.getRow() > 0 && n.getRow() < (map[0].length - 1)) {
			if (map[n.getRow() - 1][n.getCol()] != null) {
				neighbours.add(map[n.getRow() - 1][n.getCol()]);
			}
			if (map[n.getRow() + 1][n.getCol()] != null) {
				neighbours.add(map[n.getRow() + 1][n.getCol()]);
			}
		}
		if (map[n.getRow()][n.getCol() - 1] != null) {
			neighbours.add(map[n.getRow()][n.getCol() - 1]);
		}
		if (map[n.getRow()][n.getCol() + 1] != null) {
			neighbours.add(map[n.getRow()][n.getCol() + 1]);
		}
		return neighbours;
	}
}
