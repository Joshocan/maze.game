package Game;

import java.io.Serializable;

public class Position implements Serializable {

	private static final long serialVersionUID = 5853049659356545637L;
	private int x;
	private int y;

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public Position(int x, int y) {
		super();
		this.x = x;
		this.y = y;

	}

	@Override
	public String toString() {
		return "Position [x=" + x + ", y=" + y + "]";
	}
}
