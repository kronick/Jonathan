import processing.core.PApplet;

public class RoadSegment extends Layerable {
	RoadSegment[] connections;
	boolean grow;
	int age;

	boolean updated;

	int orientation;
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
		this.updated = false;
	}

	public void update() {
		this.updated = false;
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
				// Close a loop
				// ------------------
				else if(adjacentCell != null && adjacentCell.getAdjacent(dir) != null && !adjacentCell.hasRoad() &&
						!adjacentCell.hasHouse() &&	adjacentCell.getAdjacent(dir).hasRoad() &&
						adjacentCell.getAdjacent(dir).getRoad().numberOfConnections() < 3 &&
						canvas.random(1.5f) < BRANCH_CHANCE) {
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
			if(numberOfConnections() == 1 || true) {
				// Plant a subdivision around the cul de sac
				for(int i=0; i<6; i++) {
					if(canvas.random(1) < 0.1f && parent.getAdjacent(i) != null && ((SuperCell)parent.getAdjacent(i)).isDevelopable()) {
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
			canvas.rotate(this.orientation * canvas.PI/3f);
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
		int diff = -1;
		switch(numberOfConnections()) {
			case 1:
				// Cul-de-sac
				this.shape = canvas.lookupSprite("road-culdesac.png");
				for(int i=0; i<6; i++)
					if(connections[i] != null)
						this.orientation = i;

				break;
			case 2:
				int[] indices = new int[2];
				int a = 0;
				for(int i=0; i<6; i++)
					if(connections[i] != null)
						indices[a++] = i;

				diff = indices[1]-indices[0];
				if(diff == 2 || diff == 4) {
					// Big curve
					this.shape = canvas.lookupSprite("road-bigcurve.png");
					if(diff == 4) 	this.orientation = indices[0];
					else			this.orientation = indices[1];
				}
				else if(diff == 1 || diff == 5) {
					// Small curve
					this.shape = canvas.lookupSprite("road-smallcurve.png");
					if(diff == 1) 	this.orientation = indices[1];
					else 			this.orientation = indices[0];
				}
				else if(diff == 3)
					// Straight piece
					this.shape = canvas.lookupSprite("road-straight.png");

				break;
			case 3:
				indices = new int[3];
				a = 0;
				for(int i=0; i<6; i++)
					if(connections[i] != null)
						indices[a++] = i;

				if(indices[0] == 1 && indices[1] == 3 && indices[2] == 5) {
					/// Threeway
					this.shape = canvas.lookupSprite("road-threeway.png");
					this.orientation = 1;
				}
				else if(indices[0] == 0 && indices[1] == 2 && indices[2] == 4) {
					/// Threeway
					this.shape = canvas.lookupSprite("road-threeway.png");
					this.orientation = 0;
				}
				else {
					// Branch
					diff = (indices[2]-indices[1]) + (indices[1]-indices[0]);
					if(diff == 4 && indices[1] > 2) {
						this.orientation = indices[0];
						this.shape = canvas.lookupSprite("road-branch-a.png");
					}
					else if(diff == 4 && indices[1] <= 2) {
						this.orientation = indices[2];
						this.shape = canvas.lookupSprite("road-branch-b.png");
					}
					else if(diff == 3 && indices[1]-indices[0] == 1) {
						this.orientation = indices[2];
						this.shape = canvas.lookupSprite("road-branch-a.png");
					}
					else if(diff == 3 && indices[1]-indices[0] == 2) {
						this.orientation = indices[0];
						this.shape = canvas.lookupSprite("road-branch-b.png");
					}
					else if(diff == 5 && indices[1] == 2)  {
						this.shape = canvas.lookupSprite("road-branch-a.png");
						this.orientation = indices[1];
					}
					else if(diff == 5 && indices[1] == 3)  {
						this.shape = canvas.lookupSprite("road-branch-b.png");
						this.orientation = indices[1];
					}
					// Tee
					else if(diff == 5 && indices[1] == 4) {
						this.shape = canvas.lookupSprite("road-tee.png");
						this.orientation = 5;
					}
					else if(diff == 5 && indices[1] == 1) {
						this.shape = canvas.lookupSprite("road-tee.png");
						this.orientation = 0;
					}
					else if(diff == 2) {
						this.shape = canvas.lookupSprite("road-tee.png");
						this.orientation = indices[1];
					}
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
		this.updated = true;
	}

	int getDrivewayType(int dir) {
		int[] indices = new int[this.numberOfConnections()];
		int a = 0;
		for(int i=0; i<6; i++)
			if(this.connections[i] != null)
				indices[a++] = i;

		int diff, base;
		switch(this.numberOfConnections()) {
			case 1:
				return 0;	// Cul-de-sac
			case 2:
				diff = indices[1]-indices[0];
				if(diff == 3) {	// Straight
					if(indices[0]-dir == 1 || indices[0]-dir == -5 || indices[1]-dir == 1)
						 return 10;	// regular
					else return 11;	// mirrored
				}
				if(diff == 2 || diff == 4) {
					// Big curve
					if(diff == 4) 	base = indices[0];
					else			base = indices[1];
					if(base-dir == 1 || base-dir == -5) return 24;
					if(base-dir == -1 || base-dir == 5) return 23;
					if(base-dir == -2 || base-dir == 4) return 20;
					if(base-dir == -3 || base-dir == 3) return 22;
				}
		}
		return -1;
	}
}
