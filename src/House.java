import javax.media.opengl.GL;

import processing.core.PVector;


public class House extends Layerable {
	Sprite shape;
	Sprite drivewayShape;
	RoadSegment serviceRoad;
	Driveway driveway;

	int orientation;
	int houseType;
	int color;
	public House(SuperCell parent, LawnmowerGame canvas, RoadSegment serviceRoad, int drivewayDirection) {
		super(parent, canvas);
		this.serviceRoad = serviceRoad;
		this.orientation = drivewayDirection;
		this.houseType = (int)canvas.random(0,2);
		this.color = (int)canvas.random(0,8);
		this.shape = canvas.lookupSprite("house-" + this.houseType + "-" + this.color + ".png");
		this.superLayer = canvas.BUILDING_LAYER;
		this.driveway = new Driveway(parent, canvas, this, serviceRoad, drivewayDirection);
	}
	public void update () {
		super.update();
	}
	public void draw() {
		if(shape != null) {
			canvas.pushMatrix();
				canvas.translate(-position.x, -position.y);
				canvas.rotate((orientation+3) * canvas.PI/3f);
				if(this.driveway.flip) canvas.scale(-1,1);
				if(this.drivewayShape != null)
					drivewayShape.draw(parent.diameter());
				shape.draw(parent.diameter());
			canvas.popMatrix();
		}

	}

	public class Driveway extends Layerable {
		RoadSegment serviceRoad;
		Sprite shape;
		int type;
		int orientation;
		boolean flip;
		House house;

		public Driveway(SuperCell parent, LawnmowerGame canvas, House house, RoadSegment serviceRoad, int drivewayDirection) {
			super(parent, canvas);
			this.house = house;
			this.serviceRoad = serviceRoad;
			this.orientation = drivewayDirection;
			this.superLayer = canvas.DRIVEWAY_LAYER;
			this.flip = false;
			this.setShape();
		}
		public void update () {
			if(this.serviceRoad.updated) this.setShape();
			super.update();
		}
		public void draw() {
			if(shape != null) {
				canvas.pushMatrix();
					canvas.translate(-position.x, -position.y);
					canvas.rotate((orientation+3) * canvas.PI/3f);
					if(this.flip) canvas.scale(-1,1);
					shape.draw(parent.diameter());
				canvas.popMatrix();
			}

		}

		private void setShape() {
			switch(serviceRoad.getDrivewayType((orientation+3)%6)) {
				case 0:	// Cul-de-sac
					this.flip = false;
					this.shape = canvas.lookupSprite("driveway-" + house.houseType + "-culdesac.png"); break;
				case 11: // straight flipped
					this.flip = true;
				case 10: // straight
					this.shape = canvas.lookupSprite("driveway-" + house.houseType + "-straight.png"); break;
				case 20: // Big curve center
					this.shape = canvas.lookupSprite("driveway-" + house.houseType + "-bigcurve-0.png"); break;
				case 23: // Big curve end flipped
					this.flip = true;
				case 22: // Big curve end
					this.shape = canvas.lookupSprite("driveway-" + house.houseType + "-bigcurve-1.png"); break;
				case 24: // Big curve inside
					this.shape = canvas.lookupSprite("driveway-" + house.houseType + "-bigcurve-2.png"); break;
				case 31: // Small curve mid flipped
					this.flip = true;
				case 32: // Small curve mid
					this.shape = canvas.lookupSprite("driveway-" + house.houseType + "-smallcurve-0.png"); break;
				case 33: // Small curve end flipped
					this.flip = true;
				case 34: // Small curve end
					this.shape = canvas.lookupSprite("driveway-" + house.houseType + "-smallcurve-1.png"); break;
				case -1:
					this.shape = null; break;
			}
			if(this.shape != null) {
				this.shape.center = new PVector(0,146.12f);
			}
		}

	}
}
