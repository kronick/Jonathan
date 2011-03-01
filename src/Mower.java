import processing.core.*;

public class Mower {
	public PVector position;
	public float speed, angle;
	public int direction, nextDirection;
	LawnmowerGame canvas;
	Cell closestCell;
	private Sprite mowerSprite;
	private Sprite bladeSprite;
	private Sprite manSprite;
	PVector manPosition;
	private float scale = 10;
	private boolean mowing = true;

	public Mower(LawnmowerGame canvas) {
		this.canvas = canvas;
		this.position = new PVector(0,0);
		this.manPosition = new PVector(0,0);
		this.speed = 0;
		this.angle = 0;
		this.direction = 3;
		this.nextDirection = 3;
		this.mowerSprite = new Sprite(canvas, "mower-body.png", new PVector(0,19));
		this.bladeSprite = new Sprite(canvas, "mower-blade.png", new PVector(0,19));
		this.manSprite = new Sprite(canvas, "mower-man.png", new PVector(0,0));
	}

	public void update() {
		//canvas.println(getDistanceFromCellCenter() + " < " + (closestCell.diameter()/2));
		//canvas.println(this.closestCell.getPosition());
		if(getDistanceFromCellCenter() > closestCell.diameter() * 0.5) {
			transitionToNextCell();
		}
		if(isAlignedWithGrid() && nextDirection != this.direction) {
			snapToGrid();
			this.direction = this.nextDirection;
		}
		float targetAngle = this.direction * (float)Math.PI/3;
		float angleError = getAngleError();
		this.angle += angleError * 0.1f;
		if(this.speed != 0) {
			if(Math.abs(angleError) < 0.05f) {
				PVector dP = new PVector(-(float)Math.sin(targetAngle)*speed, (float)Math.cos(targetAngle)*speed);
				this.position.add(dP);
			}
		}
		else {
			PVector offset = PVector.sub(this.position, this.closestCell.getPosition());
			offset.mult(0.1f);
			this.position.sub(offset);
		}
	}

	public void changeDirection(int dir) {
		while(dir < 0) dir+=6;
		this.nextDirection = dir%6;
	}

	public boolean turnLeft() {
		if(Math.abs(getAngleError()) < 0.2f) {
			canvas.println("TURNING LEFT");
			changeDirection(this.direction - 1);
			return true;
		}
		else return false;
	}
	public boolean turnRight() {
		if(Math.abs(getAngleError()) < 0.2f) {
			canvas.println("TURNING RIGHT");
			changeDirection(this.direction + 1);
			return true;
		}
		else return false;
	}

	private float getAngleError() {
		float targetAngle = this.direction * (float)Math.PI/3;
		float angleError = (targetAngle - this.angle);
		while(angleError > Math.PI) angleError -= Math.PI*2;
		while(angleError < -Math.PI) angleError += Math.PI*2;
		return angleError;
	}

	public void setSpeed(float s) {
		this.speed = s;
	}

	public void draw() {
		this.manPosition = new PVector(manPosition.x+(-getAngleError()*4-manPosition.x)*.5f, manPosition.y+(this.speed-manPosition.y)*.5f);

		canvas.pushMatrix();
			canvas.translate(-this.position.x, -this.position.y);
			canvas.scale(0.5f);
			canvas.rotate(this.angle);
			this.mowerSprite.draw(scale);
			canvas.pushMatrix();
				canvas.rotate(canvas.frameCount/3f);
				this.bladeSprite.draw(scale);
			canvas.popMatrix();

			canvas.pushMatrix();
				canvas.translate(this.manPosition.x, 12-this.manPosition.y);
				this.manSprite.draw(scale);
			canvas.popMatrix();

			canvas.noStroke();
			canvas.fill(0);
			canvas.beginShape();
				canvas.vertex(manPosition.x-5, 12-manPosition.y);
				canvas.vertex(manPosition.x-3.5f, 12-manPosition.y);
				canvas.vertex(-3,5);
				canvas.vertex(-4,5);
			canvas.endShape(canvas.CLOSE);
			canvas.beginShape();
				canvas.vertex(manPosition.x+5, 12-manPosition.y);
				canvas.vertex(manPosition.x+3.5f, 12-manPosition.y);
				canvas.vertex(3,5);
				canvas.vertex(4,5);
			canvas.endShape(canvas.CLOSE);
		canvas.popMatrix();
	}

	public float getDistanceFromCellCenter() {
		PVector offset = PVector.sub(this.position, this.closestCell.getPosition());
		return offset.mag();
	}

	void transitionToNextCell() {
		PVector offset = PVector.sub(this.position, this.closestCell.getPosition());
		offset.normalize();
		float ang = (float)Math.PI * 2 - (float)Math.atan2(offset.x, offset.y);
		int dir = (int)((ang) / (Math.PI/3) + (Math.PI/6));
		canvas.println(dir + " from " + ang + " as " + ((ang) / (Math.PI/3) + (Math.PI/6)));
		if(this.closestCell == null)
			canvas.println("Current closest cell is null... wtf?");
		if(this.closestCell.getAdjacent(dir) != null) {
			this.closestCell = this.closestCell.getAdjacent(dir);
			canvas.println("moving to next cell");
		}
		else {
			canvas.println("Adjacent cell is null! wtf?");
			snapToGrid();
		}
	}

	public boolean isAlignedWithGrid() {
		return getDistanceFromCellCenter() < 1f;
	}

	void snapToGrid() {
		this.position = this.closestCell.getPosition();
	}
}
