import processing.core.*;

import java.util.*;

public class SubCell extends Cell {

	SuperCell parent;
	PathSegment[] mowerPaths;
	public boolean currentCell;

	static final int ADJACENT_TOP			= 0;
	static final int ADJACENT_TOP_RIGHT		= 1;
	static final int ADJACENT_BOTTOM_RIGHT	= 2;
	static final int ADJACENT_BOTTOM 		= 3;
	static final int ADJACENT_BOTTOM_LEFT 	= 4;
	static final int ADJACENT_TOP_LEFT 		= 5;


	public SubCell(LawnmowerGame canvas, SuperCell parent) {
		super(canvas);
		this.parent = parent;
		this.currentCell = false;
		mowerPaths = new PathSegment[3];
	}

	public void update() {
		if(this.position == null)
			this.position = this.getPosition();

		for(int i=0; i<mowerPaths.length; i++) {
			if(mowerPaths[i] != null) {
				mowerPaths[i].update();
			}
		}
		for(int i=0; i<this.contents.size(); i++) {
			this.contents.get(i).update();
		}
	}

	public void draw() {

	}

	public float diameter() {
		return parent.diameter() / 9f;
	}

	SubCell getAdjacent(int dir) {
		return parent.getAdjacentSubCell(this, dir%6);
	}
	PVector getPosition() {
		return this.parent.getSubCellPosition(this);
	}
	GridCoordinate getCoordinate() {
		return this.parent.getCoordinate(this);
	}

	void pushPath(int entryDir, int exitDir) {
		// Shift the others back
		for(int i=0; i<mowerPaths.length-1; i++) {
			mowerPaths[i] = mowerPaths[i+1];
		}
		// Add the new one
		mowerPaths[mowerPaths.length-1] = new PathSegment(this, canvas);
		this.updatePath(entryDir, exitDir);
		//mowerPaths[mowerPaths.length-1].setShape(entryDir, exitDir);
		//mowerPaths[mowerPaths.length-1].setLayer(canvas.getNewLayer());
	}
	void updatePath(int entryDir, int exitDir) {
		if(mowerPaths[mowerPaths.length-1] != null) {
			//mowerPaths[mowerPaths.length-1] = new PathSegment(this, canvas);
			mowerPaths[mowerPaths.length-1].setShape(entryDir, exitDir);
			mowerPaths[mowerPaths.length-1].setLayer(canvas.getNewLayer());
		}
	}
}
