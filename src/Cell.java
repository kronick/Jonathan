import processing.core.*;
import java.util.*;

public class Cell {
	ArrayList<Mappable> contents;
	Zone parent;
	LawnmowerGame canvas;
	Sprite background;

	static final int ADJACENT_TOP			= 0;
	static final int ADJACENT_TOP_RIGHT		= 1;
	static final int ADJACENT_BOTTOM_RIGHT	= 2;
	static final int ADJACENT_BOTTOM 		= 3;
	static final int ADJACENT_BOTTOM_LEFT 	= 4;
	static final int ADJACENT_TOP_LEFT 		= 5;


	public Cell(LawnmowerGame canvas, Zone parent) {
		this.canvas = canvas;
		this.parent = parent;
		this.contents = new ArrayList<Mappable>();
	}

	void update() {
		if(this.background == null) {
			this.background = canvas.lookupSprite("tile-bg.png");
		}
		/*
		if(isDevelopable() && canvas.random(1) < 0.001) {
			House newHouse = new House(this, canvas);
			this.addContent(newHouse);
		}
		*/
	}

	void draw() {
		if(this.background != null)
			this.background.draw(this.diameter());

		for(int i=0; i<contents.size(); i++) {
			contents.get(i).update();
		}
		for(int i=0; i<contents.size(); i++) {
			contents.get(i).draw();
		}
	}

	float diameter() {
		return parent.parent.scale * 2;
	}

	Cell getAdjacent(int dir) {
		return parent.getAdjacent(this, dir%6);
	}

	void addContent(Mappable m) {
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
}
