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
			this.size = 1.2f * (float)Math.sin(this.age/22f * Math.PI/2) * parent.diameter();
		else this.size = parent.diameter();

		if(this.age > 50 && this.age < 400) {
			if(canvas.random(0,1) < 0.03)
				this.smoke.add(new Smoke(this.parent, this.canvas));
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

			for(int i=0; i<smoke.size(); i++) {
				smoke.get(i).update();
				smoke.get(i).draw();
			}
		canvas.popMatrix();
	}

}