import java.util.ArrayList;

import processing.core.*;
import java.io.File;
import processing.opengl.*;

public class LawnmowerGame extends PApplet {
	Grid grid;
	ArrayList<Sprite> sprites;

	Mower mowerMan;

	int lastRoadDirection;
	RoadSegment roadToFollow;
	boolean followRoad;

	PVector mouseDown;

	PVector wind;

	public void setup() {
		//size(480, 320);
		size(800,600, OPENGL);
		//smooth();
		grid  = new Grid(this, 10);
		colorMode(HSB);
		rectMode(CENTER);
		imageMode(CENTER);
		frameRate(30);
		this.sprites = new ArrayList<Sprite>();

		mowerMan = new Mower(this);
		mowerMan.closestCell = grid.zones.get(0).cells[0][0];
		mouseDown = new PVector(0,0);

		followRoad = false;

		this.wind = new PVector(2,-5);
		//ortho();
	}

	public void draw() {
		background(60, 160, 198);
		grid.update();
		grid.beginTransform();
		grid.draw();
		//println("drawing @" + frameRate);

		mowerMan.update();
		grid.centerTarget = mowerMan.position;
		//grid.rotationTarget = -mowerMan.angle;
		mowerMan.draw();

		grid.endTransform();

		if(roadToFollow != null) {
			if(frameCount % 10 == 0) {
				if(roadToFollow.numberOfConnections() == 1 && !roadToFollow.grow) {
					// Choose another road at random
					lastRoadDirection = (lastRoadDirection + 3)%6;
				}

				// Move along this segment
				int direction;
				ArrayList<Integer> availableDirections = new ArrayList<Integer>();
				for(int i=0; i<6; i++) {
					if(roadToFollow.connections[i] != null && i != (lastRoadDirection+3)%6)
						availableDirections.add(i);
				}
				if(availableDirections.size() > 0 && followRoad) {
					direction = availableDirections.get((int)random(availableDirections.size()));
					roadToFollow = roadToFollow.connections[direction];
					grid.centerTarget = roadToFollow.parent.getPosition();
					grid.rotationTarget = -direction * PI/3;
					lastRoadDirection = direction;
				}
			}
		}
		else {
			roadToFollow = grid.zones.get(0).cells[0][0].getRoad();
		}
	}

	public static void main(String args[]) {
		PApplet.main(new String[] {"LawnmowerGame" });
	}

	public void keyPressed() {
		if(key == CODED) {
			switch (keyCode) {
				case UP:
					grid.center.add(new PVector(0,10/grid.zoom));
					break;
				case DOWN:
					grid.center.add(new PVector(0,-10/grid.zoom));
					break;
				case LEFT:
					grid.center.add(new PVector(10/grid.zoom,0));
					break;
				case RIGHT:
					grid.center.add(new PVector(-10/grid.zoom,0));
					break;
			}
		}
		else {
			switch (key) {
			case 'r':
				followRoad = !followRoad;
				break;
			case 'w':
				mowerMan.setSpeed(1);
				break;
			case 's':
				mowerMan.setSpeed(-1);
				break;
			case 'a':
				mowerMan.turnLeft();
				break;
			case 'd':
				mowerMan.turnRight();
				break;
			case 't':
				println(mowerMan.direction%6);
				if(mowerMan.closestCell.getAdjacent(mowerMan.direction) != null) {
					Tipi t = new Tipi(mowerMan.closestCell.getAdjacent(mowerMan.direction), this, mowerMan.direction);
					t.parent.addContent(t);
				}
			}
		}
	}

	public void mousePressed() {
		println(grid.screenToMap(new PVector(mouseX, mouseY)));
		this.mouseDown = new PVector(mouseX, mouseY);
	}
	public void mouseReleased() {
		mowerMan.setSpeed(0);
	}
	public void mouseDragged() {
		if(mouseButton == LEFT) {
			PVector dP = PVector.sub(new PVector(mouseX, mouseY), this.mouseDown);
			mowerMan.setSpeed(-dP.y/100f);
			if(dP.x > 50) {
				if(mowerMan.turnRight())
					mouseDown.x = mouseX;
			}
			if(dP.x < -50) {
				if(mowerMan.turnLeft())
					mouseDown.x = mouseX;
			}
			//grid.centerTarget.add(new PVector((mouseX-pmouseX)/grid.zoom, (mouseY-pmouseY)/grid.zoom));
			//grid.centerTarget.add(dP);
		}
		else
			grid.setZoom(grid.zoom + (pmouseY - mouseY) * 0.05f);
			//grid.rotationTarget += (pmouseY - mouseY) * 0.05f;
	}

	Sprite lookupSprite(String filename) {
		// Find a Sprite object reference by its filename
		for(int i=0; i<sprites.size(); i++) {
			if(sprites.get(i).filename.equals(filename)) {
				return sprites.get(i);
			}
		}

		println("Couldn't find file... trying to load it now: " + filename);

		// If none exists, try loading it
		File f = new File("art/64/" + filename);
		if(f.exists() || true) {
			Sprite s = new Sprite(this, filename);
			sprites.add(s);
			if(s != null)
				return s;
			else {
				println("Can't make that sprite.");
				return null;
			}
		}
		else {
			println("can't find file: " + filename);
		}

		return null;
	}

}

