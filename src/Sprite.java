import processing.core.*;
public class Sprite {
	PImage[] images;
	LawnmowerGame canvas;
	String filename;
	PVector center;

	public Sprite(LawnmowerGame canvas, String filename) {
		this(canvas, filename, new PVector(0,0));
	}
	public Sprite(LawnmowerGame canvas, String filename, PVector icenter) {
		this.filename = filename;
		this.canvas = canvas;
		images = new PImage[4];
		center = new PVector(icenter.x, icenter.y);
		// Load all by default
		images[0] = canvas.requestImage("art/32/" + filename);
		images[1] = canvas.requestImage("art/64/" + filename);
		images[2] = canvas.requestImage("art/128/" + filename);
		images[3] = canvas.requestImage("art/256/" + filename);
	}

	public void draw(float size) {
		this.draw(size, 3);
	}
	public void draw(float size, int level) {
		if(images[level] != null && images[level].width > 0 && images[level].height > 0) {
			canvas.image(images[level], (center.x*(size+0.5f)/256f),(center.y*(size+0.5f)/256f),
						 size + 0.5f, images[level].height*(float)size/images[level].width + 0.5f);
		}
	}
}
