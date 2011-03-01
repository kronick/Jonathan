import processing.core.*;

public class Smoke extends Mappable{
	Sprite sprite;
	PVector position;
	PVector drift;
	int age;
	int resetAge;
	float size;
	public Smoke(Cell parent, LawnmowerGame canvas) {
		super(parent, canvas);
		this.sprite = canvas.lookupSprite("smoke.png");
		this.age = 0;
		this.resetAge = 100;
		this.position = new PVector(0,0);
		this.drift = new PVector(0,0);
	}
	public void update () {
		if(this.age/4f%resetAge == 0) {
			this.position = new PVector(0,0);
			this.drift = new PVector(0,0);
		}

		this.size = (age/4f%resetAge/2f)/(float)resetAge * 40;
		this.position.add(PVector.mult(canvas.wind,0.02f));
		this.position.add(this.drift);

		this.drift.add(new PVector(canvas.random(-0.001f, 0.001f), canvas.random(-0.001f, 0.001f)));

		this.age++;
	}
	public void draw() {
		canvas.pushMatrix();
			canvas.translate(this.position.x, this.position.y);
			sprite.draw(this.size);
		canvas.popMatrix();
	}

}