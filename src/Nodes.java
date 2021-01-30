//Jorge Luis Manzo Zúniga	A01633991
//Luis Alberto Bodart Valdez	A01635000
//Michel Lujano Velázquez	A01636172

public class Nodes {

	private boolean street, visited;
	private int row, col;
	private int tile;
	private Nodes parent;

	public Nodes(boolean street, int row, int col, int tile) {
		this.street = street;
		this.visited = false;
		this.row = row;
		this.col = col;
		this.tile = tile;
		this.parent = null;
	}

	public boolean isStreet() {
		return street;
	}

	public void setStreet(boolean street) {
		this.street = street;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public int getTile() {
		return tile;
	}

	public void setTile(int tile) {
		this.tile = tile;
	}

	public Nodes getParent() {
		return parent;
	}

	public void setParent(Nodes parent) {
		this.parent = parent;
	}

	public String toString() {
		return "" + this.getTile();
	}
}
