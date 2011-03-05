import processing.core.*;

import java.util.*;

public class SuperCell extends Cell {
	Zone parent;
	Sprite background;

	SubCell[][] subCells;

	static final int ADJACENT_TOP			= 0;
	static final int ADJACENT_TOP_RIGHT		= 1;
	static final int ADJACENT_BOTTOM_RIGHT	= 2;
	static final int ADJACENT_BOTTOM 		= 3;
	static final int ADJACENT_BOTTOM_LEFT 	= 4;
	static final int ADJACENT_TOP_LEFT 		= 5;


	public SuperCell(LawnmowerGame canvas, Zone parent) {
		super(canvas);
		this.parent = parent;

		// Set up the sub cell structure
		subCells = new SubCell[13][13];	// This is what I believe is called a "sparse matrix." Some of the entries will be blank because of the hex shape.
		for(int i=0; i<subCells.length; i++) {
			for(int j=0; j<subCells[i].length; j++) {
				if(i >= minColumnIndex(j) && i <= maxColumnIndex(j)) {
					subCells[i][j] = new SubCell(canvas, this);

				}
				else subCells[i][j] = null;
			}
		}


	}

	void update() {
		if(this.position == null)
			this.position = this.getPosition();

		if(this.background == null)
			this.background = canvas.lookupSprite("tile-bg.png");

		for(int i=0; i<subCells.length; i++) {
			for(int j=0; j<subCells.length; j++) {
				if(subCells[i][j] != null)
					subCells[i][j].update();
			}
		}
		for(int i=0; i<contents.size(); i++) {
			contents.get(i).update();
		}

		/*
		if(isDevelopable() && canvas.random(1) < 0.001) {
			House newHouse = new House(this, canvas);
			this.addContent(newHouse);
		}
		*/
	}

	void draw() {
		//if(this.background != null)
		//	this.background.draw(this.diameter()*((float)Math.sqrt(3)/2))

		for(int i=0; i<subCells.length; i++) {
			for(int j=0; j<subCells.length; j++) {
				if(subCells[i][j] != null) {
					//canvas.pushMatrix();
					//canvas.translate(-parent.parent.scale * 1.5f * i / 9f, -parent.parent.scale * (float)Math.sqrt(3) * 0.5f * (i + 2 * j)/9f);
					//canvas.translate(parent.parent.scale, (float)Math.sqrt(3) * parent.parent.scale);
					subCells[i][j].draw();
					//canvas.popMatrix();
				}
			}
		}

		for(int i=0; i<contents.size(); i++) {
		//	contents.get(i).draw();
		}
	}

	public float diameter() {
		return parent.parent.scale * 2;
	}

	SuperCell getAdjacent(int dir) {
		return parent.getAdjacent(this, dir%6);
	}

	void addContent(Layerable m) {
		this.contents.add(m);
	}

	RoadSegment getRoad() {
		for(int i=0; i<contents.size(); i++) {
			if(contents.get(i).getClass() == RoadSegment.class) {
				return (RoadSegment)contents.get(i);
			}
		}
		return null;
	}

	boolean hasRoad() {
		for(int i=0; i<contents.size(); i++) {
			if(contents.get(i).getClass() == RoadSegment.class) {
				return true;
			}
		}
		return false;
	}

	boolean hasHouse() {
		for(int i=0; i<contents.size(); i++) {
			if(contents.get(i).getClass() == House.class) {
				return true;
			}
		}
		return false;
	}


	boolean isDevelopable() {
		if(!hasRoad() && !hasHouse()) {
			// Must be serviced by a road
			boolean roadService = false;
			int numberOfNeighbors = 0;
			for(int i=0; i<6; i++) {
				if(getAdjacent(i) != null) {
					if(getAdjacent(i).hasRoad())
						roadService = true;
					if(getAdjacent(i).hasHouse())
						numberOfNeighbors++;
				}
			}
			return (roadService && numberOfNeighbors < 4);
		}
		else return false;
	}

	boolean isPavable(int exception) {
		return isPavable(exception, 0);
	}
	boolean isPavable(int exception, int recurse) {
		if(exception != -1)
			exception = exception % 6;	// For safety
		// Check if there's a road already here
		if(this.hasRoad()) return false;

		// Make sure neighbors are road-free (except the expansion requester)
		for(int i=0; i<6; i++) {
			if(i != exception && getAdjacent(i) != null && getAdjacent(i).hasRoad()) {
				return false;
			}
		}
		if(recurse > 0) {
			int direction = (exception+3)%6;
			int pavedTiles = 0;
			for(int i=-1; i<=1; i++) {
				if(exception != (direction+i)%6 && getAdjacent((direction+i)%6) != null && !getAdjacent((direction+i)%6).isPavable(-1, recurse-1))
					pavedTiles++;
			}
			if(pavedTiles > 1) return false;
		}


		return true;
	}

	PVector getPosition() {
		return this.parent.getCellPosition(this);
	}

	PVector getSubCellPosition(GridCoordinate coord) {
		if(coord == null) canvas.println("ruh-row");
		PVector out = new PVector(parent.parent.scale * 1.5f * coord.x / 9f, parent.parent.scale * (float)Math.sqrt(3) * 0.5f * (coord.x + 2 * coord.y) / 9f);
		out.add(this.getPosition());
		out.sub(new PVector(parent.parent.scale,parent.parent.scale * (float)Math.sqrt(3)));
		//out.y *= -1;
		//out.mult(-1);
		return out;
	}
	PVector getSubCellPosition(SubCell c) {
		GridCoordinate coord = getCoordinate(c);
		return getSubCellPosition(coord);
	}

	GridCoordinate getCoordinate(SubCell c) {
		for(int i=0; i<subCells.length; i++) {
			for(int j=0; j<subCells[i].length; j++) {
				if(subCells[i][j] != null && subCells[i][j] == c)
					return new GridCoordinate(i,j);
			}
		}
		return null;
	}

	SubCell getAdjacentSubCell(SubCell c, int dir) {
		GridCoordinate coord = getCoordinate(c);
		if(coord != null) {
			// Get what the neighboring coordinate would be, even if it doesn't really exist
			GridCoordinate neighborCoord = new GridCoordinate(-1,-1);
			switch(dir) {
				case ADJACENT_TOP:
					neighborCoord = new GridCoordinate(coord.x, coord.y+1); break;
				case ADJACENT_TOP_RIGHT:
					neighborCoord = new GridCoordinate(coord.x-1, coord.y+1); break;
				case ADJACENT_BOTTOM_RIGHT:
					neighborCoord = new GridCoordinate(coord.x-1, coord.y); break;
				case ADJACENT_BOTTOM:
					neighborCoord = new GridCoordinate(coord.x, coord.y-1); break;
				case ADJACENT_BOTTOM_LEFT:
					neighborCoord = new GridCoordinate(coord.x+1, coord.y-1); break;
				case ADJACENT_TOP_LEFT:
					neighborCoord = new GridCoordinate(coord.x+1, coord.y); break;
			}
			// The simple case-- the predicted neighbor has valid coordinate and isn't null
			if(neighborCoord.x >= 0 && neighborCoord.x < subCells.length &&
					neighborCoord.y >= 0 && neighborCoord.y < subCells[0].length &&
					subCells[neighborCoord.x][neighborCoord.y] != null)
				return subCells[neighborCoord.x][neighborCoord.y];
			else {
				// The case where the neighbor subcell is owned by another cell
				SuperCell neighborCell = null;
				switch(dir) {
					case ADJACENT_TOP:
						canvas.println("Transitioning up!");
						if(coord.x < 3) {
							// Top-right edge
							neighborCell = parent.getAdjacent(this, ADJACENT_TOP_RIGHT);
							neighborCoord.x = coord.y;
							neighborCoord.y = coord.x+1;
						}
						else if(coord.x < 10) {
							// Top edge
							neighborCell = parent.getAdjacent(this, ADJACENT_TOP);
							neighborCoord.x = coord.x;
							neighborCoord.y = minRowIndex(neighborCoord.x);
							//if(coord.x%2 == 1) neighborCoord.y--;	// Odd columns protrude
						}
						else {
							// Top-left edge
							neighborCell = parent.getAdjacent(this, ADJACENT_TOP_LEFT);
							neighborCoord.y = coord.y + 1;
							neighborCoord.x = minColumnIndex(neighborCoord.y);
						}
						break;
					case ADJACENT_BOTTOM:
						if(coord.x < 3) {
							// Bottom-right edge
							neighborCell = parent.getAdjacent(this, ADJACENT_BOTTOM_RIGHT);
							neighborCoord.y = coord.y-1;
							neighborCoord.x = maxColumnIndex(neighborCoord.y);
						}
						else if(coord.x < 10) {
							// Bottom edge
							neighborCell = parent.getAdjacent(this, ADJACENT_BOTTOM);
							neighborCoord.x = coord.x;
							neighborCoord.y = maxRowIndex(neighborCoord.x);
							if(coord.x%2 == 1) neighborCoord.y--;	// Odd columns protrude
						}
						else {
							// Bottom-left edge
							neighborCell = parent.getAdjacent(this, ADJACENT_BOTTOM_LEFT);
							neighborCoord.x = coord.y;
							neighborCoord.y = coord.x-1;
						}
						break;
					case ADJACENT_TOP_RIGHT:
						if(coord.x+coord.y >= 9 && coord.x+coord.y < 16) { // Top-right edge
							neighborCell = parent.getAdjacent(this, ADJACENT_TOP_RIGHT);
							neighborCoord.y = coord.x;
							neighborCoord.x = coord.y;
							if((coord.x + coord.y) % 2 == 1) {	// Odd sums protrude
								neighborCoord.x--;
								neighborCoord.y++;
							}
						}
						else if(coord.x+coord.y >= 16) { // Top edge
							neighborCell = parent.getAdjacent(this, ADJACENT_TOP);
							neighborCoord.x = coord.x-1;
							neighborCoord.y = minRowIndex(neighborCoord.x);
						}
						else {							// Bottom-right edge
							neighborCell = parent.getAdjacent(this, ADJACENT_BOTTOM_RIGHT);
							neighborCoord.y = coord.y+1;
							neighborCoord.x = maxColumnIndex(neighborCoord.y);
						}
						break;
					case ADJACENT_BOTTOM_LEFT:
						if(coord.x+coord.y >= 9 && coord.x+coord.y < 16) { // Bottom-left edge
							neighborCell = parent.getAdjacent(this, ADJACENT_BOTTOM_LEFT);
							neighborCoord.y = coord.x;
							neighborCoord.x = coord.y;
							if((coord.x + coord.y) % 2 == 1) {	// Odd sums protrude
								neighborCoord.x++;
								neighborCoord.y--;
							}
						}
						else if(coord.x+coord.y >= 16) { // Top-Left edge
							neighborCell = parent.getAdjacent(this, ADJACENT_TOP_LEFT);
							neighborCoord.y = coord.y-1;
							neighborCoord.x = minColumnIndex(neighborCoord.y);
						}
						else {							// Bottom edge
							neighborCell = parent.getAdjacent(this, ADJACENT_BOTTOM);
							neighborCoord.x = coord.x+1;
							neighborCoord.y = maxRowIndex(neighborCoord.x);
						}
						break;
					case ADJACENT_BOTTOM_RIGHT:
						if(coord.y < 3) {		// Bottom edge
							neighborCell = parent.getAdjacent(this, ADJACENT_BOTTOM);
							neighborCoord.x = coord.x - 1;
							neighborCoord.y = maxRowIndex(neighborCoord.x);
						}
						else if(coord.y  < 10) {	// Bottom-right edge
							neighborCell = parent.getAdjacent(this, ADJACENT_BOTTOM_RIGHT);
							neighborCoord.y = coord.y;
							neighborCoord.x = maxColumnIndex(neighborCoord.y);
							if(coord.y%2 == 1) neighborCoord.x--;	// Odd rows protrude
						}
						else {					// Top-Right edge
							neighborCell = parent.getAdjacent(this, ADJACENT_TOP_RIGHT);
							neighborCoord.x = coord.y-1;
							neighborCoord.y = coord.x;
						}
						break;
					case ADJACENT_TOP_LEFT:
						if(coord.y < 3) {		// Bottom-left edge
							neighborCell = parent.getAdjacent(this, ADJACENT_BOTTOM_LEFT);
							neighborCoord.y = coord.x;
							neighborCoord.x = coord.y+1;

						}
						else if(coord.y  < 10) {	// Top-left edge
							neighborCell = parent.getAdjacent(this, ADJACENT_TOP_LEFT);
							neighborCoord.y = coord.y;
							neighborCoord.x = minColumnIndex(neighborCoord.y);
							if(coord.y%2 == 1) neighborCoord.x++;	// Odd rows protrude
						}
						else {					// Top edge
							neighborCell = parent.getAdjacent(this, ADJACENT_TOP);
							neighborCoord.x = coord.x + 1;
							neighborCoord.y = minRowIndex(neighborCoord.x);
						}
						break;
				}
				if(neighborCell != null) {
					canvas.println("Transitioned to neighbor cell: " + neighborCoord.x + ", " + neighborCoord.y);
					return neighborCell.subCells[neighborCoord.x][neighborCoord.y];
				}
				else return null;
			}
		}
		else return null;
	}

	int minColumnIndex(int row) {
		// Fuck formulas. It's lookup table time. Subdividing hexagons is hard.
		switch(row) {
			case 0: return 9;
			case 1: return 7;
			case 2: return 5;
			case 3: return 3;
			case 4: return 3;
			case 5: return 2;
			case 6: return 2;
			case 7: return 1;
			case 8: return 1;
			case 9: return 0;
			case 10: return 1;
			case 11: return 2;
			case 12: return 3;
		}
		return -1;
	}
	int maxColumnIndex(int row) {
		switch(row) {
			case 0: return 9;
			case 1: return 10;
			case 2: return 11;
			case 3: return 12;
			case 4: return 11;
			case 5: return 11;
			case 6: return 10;
			case 7: return 10;
			case 8: return 9;
			case 9: return 9;
			case 10: return 7;
			case 11: return 5;
			case 12: return 3;
		}
		return -1;
	}

	int minRowIndex(int column) {
		// Fuck formulas. It's lookup table time. Subdividing hexagons is hard.
		switch(column) {
			case 0: return 9;
			case 1: return 7;
			case 2: return 5;
			case 3: return 3;
			case 4: return 3;
			case 5: return 2;
			case 6: return 2;
			case 7: return 1;
			case 8: return 1;
			case 9: return 0;
			case 10: return 1;
			case 11: return 2;
			case 12: return 3;
		}
		return -1;
	}
	int maxRowIndex(int column) {
		switch(column) {
			case 0: return 9;
			case 1: return 10;
			case 2: return 11;
			case 3: return 12;
			case 4: return 11;
			case 5: return 11;
			case 6: return 10;
			case 7: return 10;
			case 8: return 9;
			case 9: return 9;
			case 10: return 7;
			case 11: return 5;
			case 12: return 3;
		}
		return -1;
	}

}
