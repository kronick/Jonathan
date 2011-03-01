import processing.core.PApplet;

public class RoadSegment extends Mappable{
	RoadSegment[] connections;
	boolean grow;
	int age;
	Sprite shape;

	private int lastConnection;

	static final float BRANCH_CHANCE = 0.01f;
	public RoadSegment(Cell parent, LawnmowerGame canvas) {
		super(parent, canvas);
		connections = new RoadSegment[6];
		grow = true;
		age = 0;
		shape = null;
		lastConnection = 0;
	}

	public void update() {
		if(grow && (numberOfConnections() < 2 || (numberOfConnections() < 3 && canvas.random(1) < BRANCH_CHANCE))) {
			// choose a random direction,
			int dir = this.lastConnection + (int)canvas.random(-2,2);
			if(dir < 0) dir += 6;
			if(connections[dir%6] == null) {
				Cell adjacentCell = parent.getAdjacent(dir);
				if(adjacentCell != null && adjacentCell.isPavable((dir+3)%6)) { // If the chosen direction exists as a cell and does not already contain a road segment
					RoadSegment newSegment = new RoadSegment(adjacentCell, canvas);
					this.makeConnection(dir, newSegment);
					newSegment.makeConnection(dir+3, this);
					adjacentCell.addContent(newSegment);
				}
			}

			if(canvas.random(1) < 0.3) {
				int i = (int)canvas.random(0,6);
				if(parent.getAdjacent(i) != null && parent.getAdjacent(i).isDevelopable()) {
					House newHouse = new House(parent.getAdjacent(i), canvas, this, (i+3)%6);
					parent.getAdjacent(i).addContent(newHouse);
				}
			}
		}
		if(age > 50 && grow) {
			grow = false;
			if(numberOfConnections() == 1 && false) {
				// Plant a subdivision around the cul de sac
				for(int i=0; i<6; i++) {
					if(parent.getAdjacent(i) != null && parent.getAdjacent(i).isDevelopable()) {
						House newHouse = new House(parent.getAdjacent(i), canvas, this, (i+3)%6);
						parent.getAdjacent(i).addContent(newHouse);
					}
				}
			}
		}
		age++;
	}

	public void draw() {
		canvas.pushStyle();
		canvas.fill(0,0,grow ? 255 : 0);
		canvas.stroke(0,0,0);

		if(this.shape == null) {
			if(numberOfConnections() == 1)
				canvas.ellipse(0,0,parent.diameter()/2, parent.diameter()/2);
			for(int i=0; i<connections.length; i++) {
				if(connections[i] != null) {
					canvas.pushMatrix();
					canvas.rotate(i * PApplet.PI/3f);
					canvas.strokeWeight(4);
					canvas.line(0,0, 0, -parent.diameter() * 0.5f);
					canvas.popMatrix();
				}
			}
		}
		else {
			this.shape.draw(parent.diameter());
		}
		canvas.popStyle();
	}

	int numberOfConnections() {
		int n = 0;
		for(int i=0; i<connections.length; i++) {
			if(connections[i] != null) n++;
		}
		return n;
	}

	void updateShape() {
		switch(numberOfConnections()) {
			case 1:
				// Cul-de-sac
				for(int i=0; i<6; i++) {
					if(connections[i] != null) {
						this.shape = canvas.lookupSprite("1-culdesac-" + i + ".png");
					}
				}
				break;
			case 2:
				int[] indices = new int[2];
				int a = 0;
				for(int i=0; i<6; i++) {
					if(connections[i] != null)
						indices[a++] = i;
				}
				if(indices[1]-indices[0] == 2)
					// Big curve
					this.shape = canvas.lookupSprite("2-bigcurve-" + indices[0] + "" + indices[1] + ".png");
				else if(indices[1]-indices[0] == 4)
					// Big curve
					this.shape = canvas.lookupSprite("2-bigcurve-" + indices[1] + "" + indices[0] + ".png");
				else if(indices[1]-indices[0] == 1) {
					// Small curve
					if(indices[1] != 5)
						this.shape = canvas.lookupSprite("2-smallcurve-" + indices[0] + "" + indices[1] + ".png");
					else
						this.shape = canvas.lookupSprite("2-smallcurve-" + indices[1] + "" + indices[0] + ".png");
				}
				else if(indices[1] - indices[0] == 3)
					// Straight piece
					this.shape = canvas.lookupSprite("2-straight-" + indices[0] + "" + indices[1] + ".png");
				break;
			case 3:
				indices = new int[3];
				a = 0;
				for(int i=0; i<6; i++) {
					if(connections[i] != null)
						indices[a++] = i;
				}
				if(indices[0] == 1 && indices[1] == 3 && indices[2] == 5)
					this.shape = canvas.lookupSprite("3-round-135.png");
				else if(indices[0] == 0 && indices[1] == 2 && indices[2] == 4)
					this.shape = canvas.lookupSprite("3-round-024.png");
				else {
					if((indices[2]-indices[1]) + (indices[1]-indices[0]) == 2 ||
						(indices[0] == 0 && indices[1] == 4 && indices[2] == 5) ||
						(indices[0] == 0 && indices[1] == 1 && indices[2] == 5))
						this.shape = canvas.lookupSprite("3-tee-" + indices[0] + "" + indices[1]  + "" + indices[2] + ".png");
					else
						this.shape = canvas.lookupSprite("3-branch-" + indices[0] + "" + indices[1]  + "" + indices[2] + ".png");
				}
				break;
			default:
				this.shape = null;
		}
	}

	void makeConnection(int dir, RoadSegment road) {
		this.connections[dir%6] = road;
		this.lastConnection = (dir+3) % 6;
		this.updateShape();
	}
}
