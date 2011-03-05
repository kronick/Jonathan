import processing.core.PApplet;

public class RoadSegment extends Layerable {
	RoadSegment[] connections;
	boolean grow;
	int age;
	Sprite shape;

	private int lastConnection;

	static final float BRANCH_CHANCE = 0.01f;
	public RoadSegment(SuperCell parent, LawnmowerGame canvas) {
		super(parent, canvas);
		this.superLayer = canvas.ROAD_LAYER;
		this.connections = new RoadSegment[6];
		this.grow = true;
		this.age = 0;
		this.shape = null;
		this.lastConnection = 0;
	}

	public void update() {
		super.update();
		if(grow && (numberOfConnections() < 2 || (numberOfConnections() < 3 && canvas.random(1) < BRANCH_CHANCE))) {
			// choose a random direction,
			int dir = this.lastConnection + (int)canvas.random(-2,2);
			if(dir < 0) dir += 6;
			if(connections[dir%6] == null) {
				SuperCell adjacentCell = (SuperCell)parent.getAdjacent(dir);
				if(adjacentCell != null && adjacentCell.isPavable((dir+3)%6)) { // If the chosen direction exists as a cell and does not already contain a road segment
					RoadSegment newSegment = new RoadSegment(adjacentCell, canvas);
					this.makeConnection(dir, newSegment);
					newSegment.makeConnection(dir+3, this);
					//adjacentCell.addContent(newSegment);
				}
				else if(adjacentCell != null && adjacentCell.getAdjacent(dir) != null && !adjacentCell.hasRoad() &&
						adjacentCell.getAdjacent(dir).hasRoad() &&
						adjacentCell.getAdjacent(dir).getRoad().numberOfConnections() < 3 &&
						canvas.random(0.5f) < BRANCH_CHANCE) {	// Close a loop
					RoadSegment newSegment = new RoadSegment(adjacentCell, canvas);
					this.makeConnection(dir, newSegment);
					newSegment.makeConnection(dir+3, this);
					//adjacentCell.addContent(newSegment);
					RoadSegment existingSegment = adjacentCell.getAdjacent(dir).getRoad();
					newSegment.makeConnection(dir, existingSegment);
					existingSegment.makeConnection(dir+3, newSegment);
				}
			}

			if(canvas.random(1) < 0.3) {
				int i = (int)canvas.random(0,6);
				if(parent.getAdjacent(i) != null && ((SuperCell)parent.getAdjacent(i)).isDevelopable()) {
					//House newHouse = new House(parent.getAdjacent(i), canvas, this, (i+3)%6);
					//parent.getAdjacent(i).addContent(newHouse);
				}
			}

			canvas.liveRoads++;
		}
		if(age > 50 && grow) {
			grow = false;
			if(numberOfConnections() == 1 && false) {
				// Plant a subdivision around the cul de sac
				for(int i=0; i<6; i++) {
					if(parent.getAdjacent(i) != null && ((SuperCell)parent.getAdjacent(i)).isDevelopable()) {
						House newHouse = new House(((SuperCell)parent.getAdjacent(i)), canvas, this, (i+3)%6);
						//parent.getAdjacent(i).addContent(newHouse);
					}
				}
			}
		}
		age++;
	}

	public void draw() {


		if(this.shape != null) {
			canvas.pushMatrix();
			canvas.translate(-position.x, -position.y);
			this.shape.draw(parent.diameter());
			canvas.popMatrix();
		}
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
