import processing.core.*;
import java.util.*;

public class Zone {

	int resolution;
	SuperCell[][] cells;
	Grid parent;
	LawnmowerGame canvas;
	Zone[] neighbors;

	PVector center;

	boolean drawnThisFrame;
	boolean updatedThisFrame;

	float width, height;

	static final int NEIGHBOR_TOP    = 0;
	static final int NEIGHBOR_RIGHT  = 1;
	static final int NEIGHBOR_BOTTOM = 2;
	static final int NEIGHBOR_LEFT   = 3;

	public Zone(LawnmowerGame canvas, Grid parent, int resolution) {
		this(canvas, parent, resolution, null);
	}

	public Zone(LawnmowerGame canvas, Grid parent, int resolution, PVector center) {
		this.canvas = canvas;
		this.parent = parent;
		this.resolution = resolution;
		this.center = center;
		cells = new SuperCell[resolution][resolution];

		// Populate the cells
		for(int i=0; i<cells.length; i++) {
			for(int j=0; j<cells[i].length; j++) {
				cells[i][j] = new SuperCell(canvas, this);
			}
		}

		neighbors = new Zone[4];  // Top, right, bottom, left

		width = 1.5f * resolution * parent.scale;
		height = (float)Math.sqrt(3) * resolution * parent.scale;
	}

	void update() {
		drawnThisFrame = false;
		if(!updatedThisFrame && parent.zoneIsVisible(this)) {
			//canvas.println("Updated zone.");
			for(int i=0; i<cells.length; i++) {
				for(int j=0; j<cells[i].length; j++) {
					cells[i][j].update();
				}
			}

			// Update neighbors if they haven't been updated, too
			updatedThisFrame = true;
			for(int i=0; i<4; i++) {
				if(neighbors[i] != null) {
					neighbors[i].update();
				}
				else {
					if(parent.zoneIsVisible(this)) {
						Zone foundZone = null;
						switch(i) {
							case NEIGHBOR_TOP:
							case NEIGHBOR_BOTTOM:
								//if(neighbors[NEIGHBOR_RIGHT] != null && neighbors[NEIGHBOR_RIGHT].neighbors[i] != null) {
								try {
									foundZone = neighbors[NEIGHBOR_RIGHT].neighbors[i].neighbors[NEIGHBOR_LEFT];
								}
								catch (NullPointerException e) {}
								try {
									foundZone = neighbors[NEIGHBOR_LEFT].neighbors[i].neighbors[NEIGHBOR_RIGHT];
								}
								catch (NullPointerException e) {}
								break;
							case NEIGHBOR_LEFT:
							case NEIGHBOR_RIGHT:
								try {
									foundZone = neighbors[NEIGHBOR_TOP].neighbors[i].neighbors[NEIGHBOR_BOTTOM];
								}
								catch (NullPointerException e) {}
								try {
									foundZone = neighbors[NEIGHBOR_BOTTOM].neighbors[i].neighbors[NEIGHBOR_TOP];
								}
								catch (NullPointerException e) {}
								break;
						}

						if(foundZone == null) {
							// Create a new one
							Zone newZone = parent.addZone();
							this.makeNeighbor(i, newZone);
						}
						else {
							this.makeNeighbor(i, foundZone);
						}
						//canvas.println("Creating new zone..." + newZone.center + " This zone: " + this.center);
					}
				}
			}
		}
	}

	void draw() {
		if(!drawnThisFrame && this.center != null && parent.zoneIsVisible(this)) {
			canvas.pushMatrix();
			canvas.translate(center.x, center.y);
			//canvas.translate(-parent.scale * cells.length/2f, -parent.scale * cells.length/2f * (float)Math.sqrt(3)/2f);
			// Draw all the cells
			for(int i=0; i<cells.length; i++) {
				for(int j=0; j<cells[i].length; j++) {
					canvas.pushMatrix();
					canvas.translate(parent.scale * 1.5f * i, parent.scale * (float)Math.sqrt(3) * 0.5f * (i + 2 * j));
					cells[i][j].draw();
					canvas.popMatrix();
				}
			}
			canvas.popMatrix();

			// Fill in neighbors if they haven't been drawn, too
			drawnThisFrame = true;
			for(int i=0; i<4; i++) {
				if(neighbors[i] != null) {
					//canvas.stroke(i*64,255,255);
					//canvas.strokeWeight(3);
					//canvas.line(center.x + i * 3, center.y + i * 3, neighbors[i].center.x + i*3, neighbors[i].center.y + i*3);
					neighbors[i].draw();
				}
			}
		}
		updatedThisFrame = false;
	}

	SuperCell getAdjacent(SuperCell c, int dir) {
		GridCoordinate coord = getCoordinate(c);
		if(coord != null) {
			switch(dir) {
				case SuperCell.ADJACENT_TOP:
					if(coord.y > 0) {
						return cells[coord.x][coord.y-1];
					}
					else {
						// Look at the zone above
						if(neighbors[NEIGHBOR_TOP] != null) {
							return neighbors[NEIGHBOR_TOP].cells[coord.x][resolution-1];
						}
						else return null;
					}
				case SuperCell.ADJACENT_BOTTOM:
					if(coord.y < resolution-1) {
						return cells[coord.x][coord.y+1];
					}
					else {
						// Look at the zone below
						if(neighbors[NEIGHBOR_BOTTOM] != null) {
							return neighbors[NEIGHBOR_BOTTOM].cells[coord.x][0];
						}
						else return null;
					}
				case SuperCell.ADJACENT_TOP_LEFT:
					if(coord.x > 0) {
						return cells[coord.x-1][coord.y];
					}
					else {
						// Look at the zone to left
						if(neighbors[NEIGHBOR_LEFT] != null) {
							return neighbors[NEIGHBOR_LEFT].cells[resolution-1][coord.y];
						}
						else return null;
					}
				case SuperCell.ADJACENT_TOP_RIGHT:
					if(coord.y > 0 && coord.x < resolution-1) {
						return cells[coord.x+1][coord.y-1];
					}
					else {
						if(coord.y == 0) {
							if(coord.x < resolution-1) {
								// Look above
								if(neighbors[NEIGHBOR_TOP] != null) {
									return neighbors[NEIGHBOR_TOP].cells[coord.x+1][resolution-1];
								}
								else return null;
							}
							else {
								// Look to the top right
								if(neighbors[NEIGHBOR_TOP] != null && neighbors[NEIGHBOR_TOP].neighbors[NEIGHBOR_RIGHT] != null) {
									return neighbors[NEIGHBOR_TOP].neighbors[NEIGHBOR_RIGHT].cells[0][resolution-1];
								}
							}
						}
						else {
							// Look to the right
							if(neighbors[NEIGHBOR_RIGHT] != null) {
								return neighbors[NEIGHBOR_RIGHT].cells[0][coord.y-1];
							}
							else return null;
						}

					}
				case SuperCell.ADJACENT_BOTTOM_LEFT:
					if(coord.x > 0 && coord.y < resolution-1) {
						return cells[coord.x-1][coord.y+1];
					}
					else {
						if(coord.x > 0) {
							// Look at the zone below
							if(neighbors[NEIGHBOR_BOTTOM] != null) {
								return neighbors[NEIGHBOR_BOTTOM].cells[coord.x-1][0];
							}
							else return null;
						}
						else {
							if(coord.y < resolution-1) {
								// Look at zone to the left
								if(neighbors[NEIGHBOR_LEFT] != null) {
									return neighbors[NEIGHBOR_LEFT].cells[resolution-1][coord.y+1];
								}
								else return null;
							}
							else if(neighbors[NEIGHBOR_BOTTOM] != null && neighbors[NEIGHBOR_BOTTOM].neighbors[NEIGHBOR_LEFT] != null) {
								return neighbors[NEIGHBOR_BOTTOM].neighbors[NEIGHBOR_LEFT].cells[resolution-1][0];
							}
							else return null;
						}
					}
				case SuperCell.ADJACENT_BOTTOM_RIGHT:
					if(coord.x < resolution-1) {
						return cells[coord.x+1][coord.y];
					}
					else {
						// Look at the zone to the right
						if(neighbors[NEIGHBOR_RIGHT] != null) {
							return neighbors[NEIGHBOR_RIGHT].cells[0][coord.y];
						}
						else return null;
					}
				default:
					return null;
			}
		}
		else return null;
	}

	GridCoordinate getCoordinate(SuperCell c) {
		SuperCell foundCell;
		for(int i=0; i<cells.length; i++) {
			for(int j=0; j<cells[i].length; j++) {
				if(cells[i][j] == c) return new GridCoordinate(i,j);
			}
		}
		return null;
	}

	PVector getCellPosition(SuperCell c) {
		GridCoordinate coord = getCoordinate(c);
		PVector out = new PVector(parent.scale * 1.5f * coord.x, parent.scale * (float)Math.sqrt(3) * 0.5f * (coord.x + 2 * coord.y));
		out.add(this.center);
		out.mult(-1);
		return out;
	}

  void makeNeighbor(int direction, Zone newNeighbor) {
    // Default to building the reciprocal link
    this.makeNeighbor(direction, newNeighbor, true);
  }
  void makeNeighbor(int direction, Zone newNeighbor, boolean reciprocate) {
    this.neighbors[direction] = newNeighbor;
    if(reciprocate) {
      newNeighbor.makeNeighbor((direction + 2)%4, this, false);
      switch(direction) {
        case NEIGHBOR_TOP: {
          newNeighbor.center = PVector.add(this.center, new PVector(0, -height));
          break;
        }
        case NEIGHBOR_BOTTOM: {
          newNeighbor.center = PVector.add(this.center, new PVector(0, height));
          break;
        }
        case NEIGHBOR_LEFT: {
          newNeighbor.center = PVector.add(this.center, new PVector(-width, -height/2));
          break;
        }
        case NEIGHBOR_RIGHT: {
          newNeighbor.center = PVector.add(this.center, new PVector(width, height/2));
          break;
        }
      }
    }
  }

}
