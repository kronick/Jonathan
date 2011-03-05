
public class PathSegment extends Layerable  {
	Sprite shape;
	int orientation;
	boolean reflect;
	int age;
	int layer;

	public PathSegment(SubCell parent, LawnmowerGame canvas) {
		super(parent, canvas);
		this.superLayer = canvas.GROUND_LAYER;
		this.reflect = false;
	}

	public void update() {
		age++;
		super.update();
	}

	public void draw() {
		if(shape != null) {
			canvas.pushMatrix();
				canvas.translate(-position.x, -position.y);
				canvas.rotate(orientation * (float)Math.PI/3);
				if(reflect) canvas.scale(-1,1);
				shape.draw(parent.diameter()/(float)Math.sqrt(3));
			canvas.popMatrix();
		}
	}

	public void setShape(int entry, int exit) {
		entry = entry%6;
		exit = exit%6;

		switch(Math.abs(entry-exit)) {
			// Entry-and-exit or just a new entry makes a terminus
			case 0:
				this.shape = canvas.lookupSprite("mowed-terminus.png");
				this.orientation = entry+3;
				this.reflect = false;
				break;
			// Small curve happens when entry and exit are 1 (or 5) vertex apart
			case 1:
				this.shape = canvas.lookupSprite("mowed-smallcurve.png");
				this.orientation = entry + 3;
				if(exit > entry) reflect = true;
				else reflect = false;
				break;
			case 5:
				this.shape = canvas.lookupSprite("mowed-smallcurve.png");
				this.orientation = entry + 3;
				if(exit < entry) reflect = true;
				else reflect = false;
				break;
			// Straight line when difference is 3
			case 3:
				this.shape = canvas.lookupSprite("mowed-straight.png");
				this.orientation = entry;
				this.reflect = false;
				break;
			// Big curve happens when difference is 2 or 4
			case 2:
				this.shape = canvas.lookupSprite("mowed-bigcurve.png");
				this.orientation = entry + 3;
				if(exit > entry) this.reflect = true;
				else this.reflect = false;
				break;
			case 4:
				this.shape = canvas.lookupSprite("mowed-bigcurve.png");
				this.orientation = entry + 3;
				if(exit < entry) this.reflect = true;
				else this.reflect = false;
				break;
		}
	}
}
