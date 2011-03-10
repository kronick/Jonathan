import processing.core.*;

import java.util.*;

public class Tipi extends Layerable{
	Sprite sprite;
	ArrayList<Smoke> smoke;
	int direction;
	int age;
	float size;

	public Tipi(SubCell parent, LawnmowerGame canvas, int direction) {
		super(parent, canvas);
		this.superLayer = canvas.BUILDING_LAYER;

		this.direction = direction;
		this.sprite = canvas.lookupSprite("tipi-plan.png");
		this.smoke = new ArrayList<Smoke>();
		this.age = 0;
	}
	public void update () {
		super.update();
		if(this.age < 30)
			this.size = 1.2f * (float)Math.sin(this.age/22f * Math.PI/2) * parent.diameter()*2;
		else this.size = parent.diameter()*2;

		if(this.age > 50 && this.age < 800) {
			if(canvas.random(0,1) < 0.01)
				this.smoke.add(new Smoke((SubCell)this.parent, this.canvas));
		}

		for(int i=0; i<smoke.size(); i++) {
			smoke.get(i).update();
		}

		this.age++;
	}
	public void draw() {
		canvas.pushMatrix();
			canvas.translate(-position.x, -position.y);
			canvas.pushMatrix();
				canvas.rotate((direction-2) * (float)Math.PI/3);
				sprite.draw(this.size);
			canvas.popMatrix();
		canvas.popMatrix();
	}

	public class Smoke extends Layerable{
		Sprite sprite;
		PVector drift;
		int age;
		int resetAge;
		float size;

		public Smoke(SubCell parent, LawnmowerGame canvas) {
			super(parent, canvas);
			this.superLayer = canvas.SKY_LAYER;
			this.sprite = canvas.lookupSprite("smoke.png");
			this.age = 0;
			this.resetAge = 100;
			//this.position = new PVector(0,0);
			this.drift = new PVector(0,0);
		}
		public void update () {
			super.update();
			if(this.age/4f%resetAge == 0) {
				this.position = parent.position.get();
				this.drift = new PVector(0,0);
			}

			this.size = (age/4f%resetAge/2f)/(float)resetAge * 8;
			this.position.sub(PVector.mult(canvas.wind,0.002f));
			this.position.sub(this.drift);

			this.drift.add(new PVector(canvas.random(-0.0001f, 0.0001f), canvas.random(-0.0001f, 0.0001f)));

			this.age++;
		}
		public void draw() {
			canvas.pushMatrix();
				canvas.translate(-this.position.x, -this.position.y);
				sprite.draw(this.size);
			canvas.popMatrix();
		}

	}

}