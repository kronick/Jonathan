import java.util.ArrayList;


public class TriangleCell {
	ArrayList<Mappable> contents;
	Cell parent;
	LawnmowerGame canvas;
	Sprite background;

	public TriangleCell(LawnmowerGame canvas, Cell parent) {
		this.canvas = canvas;
		this.parent = parent;
		this.contents = new ArrayList<Mappable>();
	}

}
