import processing.core.*;
import java.util.*;

public class Cell {
	LawnmowerGame canvas;
	ArrayList<Layerable> contents;
	PVector position;

	public Cell(LawnmowerGame canvas) {
		this.canvas = canvas;
		this.contents = new ArrayList<Layerable>();
	}

	public float diameter() {
		return 0;
	}

	Cell getAdjacent(int dir) {
		return null;
	}

	PVector getPosition() {
		return this.position.get();
	}
}
