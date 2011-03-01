import processing.core.PApplet;


public class House extends Mappable{
	Sprite sprite;
	RoadSegment serviceRoad;
	int drivewayDirection;
	public House(Cell parent, LawnmowerGame canvas, RoadSegment serviceRoad, int drivewayDirection) {
		super(parent, canvas);
		this.serviceRoad = serviceRoad;
		this.drivewayDirection = drivewayDirection;
		this.sprite = canvas.lookupSprite("house.png");
	}
	public void update () {

	}
	public void draw() {

		canvas.pushStyle();
		canvas.pushMatrix();
		canvas.rotate(drivewayDirection * (float)Math.PI/3f);
		/*
		// Draw house
		canvas.noStroke();
		canvas.fill(23, 55, 160);
		canvas.rect(0,-5,10,6);

		// Draw driveway
		canvas.stroke(0,0,0);
		canvas.line(0, -5, 0, -20);
		*/

		this.sprite.draw(parent.diameter());
		canvas.popMatrix();
		canvas.popStyle();

	}

}
