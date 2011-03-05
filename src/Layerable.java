import processing.core.*;

public class Layerable {
	PVector position;
	private int layer;
	public int superLayer;
	LawnmowerGame canvas;
	Cell parent;

	public Layerable(Cell parent, LawnmowerGame canvas) {
		this.parent = parent;
		this.canvas = canvas;
		this.layer = 0;

		// Layerables are automatically added to their parent cell's contents list upon creation
		parent.contents.add(this);

		// Default superLayer is the ground layer. Overwrite this if the subclassed object should go on a different layer.
		this.superLayer = canvas.GROUND_LAYER;
	}


	public void update() {
		if(this.position == null)
			this.position = parent.position.get();	// Assigned by value... so the layer can move around
		// This should be called every frame if the item is going to be drawn
		this.canvas.insertSubLayer(superLayer, this);
	}
	public void draw() {

	}

	public void setLayer(int i) {
		this.layer = i;
	}
	public int getLayer() {
		return this.layer;
	}
}
